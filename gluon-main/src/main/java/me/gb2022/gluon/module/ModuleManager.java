package me.gb2022.gluon.module;

import me.gb2022.commons.TriState;
import me.gb2022.gluon.FunctionalComponentStatus;
import me.gb2022.gluon.ModularApplicationContext;
import me.gb2022.gluon.ObjectOperationResult;
import me.gb2022.gluon.module.component.SubComponent;
import me.gb2022.gluon.module.component.SubComponentHolder;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ModuleManager {
    protected final Logger logger;
    protected final Map<String, ModuleContainer> modules = new HashMap<>();
    protected final Properties statusMap = new Properties();
    private final ModularApplicationContext context;

    public ModuleManager(ModularApplicationContext context) {
        this.context = context;
        this.logger = createLogger();
    }

    public Logger createLogger() {
        return this.context.getLogProvider().createLogger("ModuleManager");
    }

    public void enable() {
    }

    public void disable() {
        this.saveStatus();
        for (var id : new ArrayList<>(this.getModules().keySet())) {
            this.unregister(id);
        }
    }

    public ModularApplicationContext getContext() {
        return context;
    }

    public void register(ModuleContainer handle) {
        this.modules.put(handle.getMetadata().key().fullId(), handle);
        handle.register(this);

        if (handle.getStatus() == FunctionalComponentStatus.REGISTER_FAILED) {
            return;
        }

        handle.initContext(this.context);
        handle.construct();

        if (handle.getStatus() == FunctionalComponentStatus.CONSTRUCT_FAILED) {
            return;
        }

        var meta = handle.getMetadata();
        var id = meta.key().fullId();

        if (getStatus(id) == TriState.UNKNOWN) {
            var status = false;

            if (meta.defaultEnabled()) {
                status = getDefaultModuleStatus();
            }

            this.statusMap.put(id, status ? "enabled" : "disabled");
        }
        if (meta.internal()) {
            this.statusMap.put(meta.fullId(), "enabled");
        }

        this.saveStatus();
        if (getStatus(id) == TriState.TRUE && !meta.beta()) {
            this.handlePreEnable(handle);
            handle.init(this);

            if (handle.getStatus() == FunctionalComponentStatus.ENABLE_FAILED) {
                this.logger.error("Module {} reported exception. Continued registration with error.", handle.getMetadata().key());
                return;
            }

            this.handlePostEnable(handle, ObjectOperationResult.SUCCESS);
        }
    }

    public void unregister(String id) {
        if (!this.modules.containsKey(id)) {
            this.logger.warn("Module with id {} not found", id);
            return;
        }

        if (this.getStatus(id) == TriState.TRUE) {
            var container = this.get(id).orElseThrow();

            if(container.getStatus() == FunctionalComponentStatus.ENABLED) {
                _disable(container);
            }
        }

        this.modules.remove(id);
    }


    private ObjectOperationResult _enable(ModuleContainer container) {
        var result = ObjectOperationResult.INTERNAL_ERROR;

        try {
            this.handlePreEnable(container);
        }catch (Throwable ex) {
            this.logger.error("Failed to ENABLE '{}' on PRE_ENABLE:", ex);
            this.handleException(ex);
            return ObjectOperationResult.INTERNAL_ERROR;
        }

        try {
            container.enable();
            result = ObjectOperationResult.SUCCESS;
        }catch (Throwable ex) {
            this.logger.error("Failed to ENABLE '{}' on ENABLE:", ex);
            this.handleException(ex);
            return ObjectOperationResult.INTERNAL_ERROR;
        }

        try {
            this.handlePostEnable(container, result);
        }catch (Throwable ex) {
            this.logger.error("Failed to ENABLE '{}' on ENABLE:", ex);
            this.handleException(ex);
            return ObjectOperationResult.INTERNAL_ERROR;
        }

        return result;
    }

    private ObjectOperationResult _disable(ModuleContainer container) {
        var result = ObjectOperationResult.INTERNAL_ERROR;
        var id = container.getMetadata().key().toString();

        try {
            this.handlePreDisable(container);
        } catch (Throwable ex) {
            this.logger.error("Failed to UNREGISTER '{}' on PRE_DISABLE:", id);
            this.handleException(ex);
            return ObjectOperationResult.INTERNAL_ERROR;
        }

        try {
            container.disable();
            result = ObjectOperationResult.SUCCESS;
        } catch (NoClassDefFoundError e) {
            this.logger.warn("Module '{}' reported dep exception. Continued unload with error.", id);
        } catch (Throwable ex) {
            this.logger.error("Failed to UNREGISTER '{}' on DISABLE:", id);
            this.handleException(ex);
            return ObjectOperationResult.INTERNAL_ERROR;
        }

        try {
            this.handlePostDisable(container, result);
        } catch (Throwable ex) {
            this.logger.error("Failed to UNREGISTER '{}' on POST_DISABLE:", id);
            this.handleException(ex);
            return ObjectOperationResult.INTERNAL_ERROR;
        }

        return result;
    }

    public ObjectOperationResult enable(String id) {
        var o = get(id);
        var container = o.orElse(null);

        if(o.isEmpty()){
            return ObjectOperationResult.NOT_FOUND;
        }

        if (container.getMetadata().internal()) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }

        var result = checkState0(container, FunctionalComponentStatus.ENABLED);

        if (result != ObjectOperationResult.INTERNAL_ERROR) {
            return result;
        }

        result = _enable(container);

        if (result == ObjectOperationResult.SUCCESS) {
            this.statusMap.put(id, "enabled");
            this.logger.info("enabled module {}.", id);
            this.saveStatus();
        }

        return result;
    }

    public ObjectOperationResult disable(String id) {
        var o = this.get(id);
        var container = o.orElse(null);

        if (o.isEmpty()) {
            return ObjectOperationResult.NOT_FOUND;
        }

        if (container.getMetadata().internal()) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }

        var result = checkState0(container, FunctionalComponentStatus.DISABLED);

        if (result != ObjectOperationResult.INTERNAL_ERROR) {
            return result;
        }

        result = this._disable(container);

        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("disabled module {}.", id);
            this.statusMap.put(id, "disabled");
            this.saveStatus();
        }

        return result;
    }

    public final ObjectOperationResult reload(String id) {
        ObjectOperationResult result = this.disable(id);
        if (result != ObjectOperationResult.SUCCESS) {
            return result;
        }
        return enable(id);
    }



    public final Properties getStatusMap() {
        return statusMap;
    }

    public final Map<String, ModuleContainer> getModules() {
        return modules;
    }

    public final Optional<ModuleContainer> get(String id) {
        return Optional.ofNullable(this.modules.get(id));
    }

    public final TriState getStatus(String id) {
        if (!this.statusMap.containsKey(id)) {
            return TriState.UNKNOWN;
        }
        return Objects.equals(this.statusMap.get(id), "enabled") ? TriState.TRUE : TriState.FALSE;
    }

    public final Set<String> getIdsByStatus(TriState status) {
        var result = new HashSet<String>();
        for (var id : this.modules.keySet()) {
            if (getStatus(id) != status) {
                continue;
            }
            result.add(id);
        }
        return result;
    }



    private ObjectOperationResult checkState0(ModuleContainer handle, FunctionalComponentStatus state) {
        if (handle == null || handle.getStatus() == FunctionalComponentStatus.UNKNOWN) {
            return ObjectOperationResult.NOT_FOUND;
        }

        if (handle.getStatus() == state) {
            return ObjectOperationResult.ALREADY_OPERATED;
        }

        return ObjectOperationResult.INTERNAL_ERROR;
    }


    private void saveStatus() {
        this.saveStatus(this.statusMap);
    }


    public boolean validRegister(ModuleContainer handle) {
        return true;
    }

    public boolean getDefaultModuleStatus() {
        return true;
    }

    public void saveStatus(Properties meta) {
    }

    public void handleException(Throwable ex) {
        this.logger.catching(ex);
    }

    public void handlePreEnable(ModuleContainer handle) {
        var container = handle.getComponentContainer();

        container.clear();
        for (var component : SubComponentHolder.createComponents(handle.getHandle(AppModule.class))) {
            container.getComponents().put((Class<? extends SubComponent<?>>) component.getClass(), (component));
        }
    }

    public void handlePostEnable(ModuleContainer handle, ObjectOperationResult result) {
    }

    public void handlePreDisable(ModuleContainer handle) {
    }

    public void handlePostDisable(ModuleContainer handle, ObjectOperationResult result) {
        handle.getComponentContainer().getComponents().clear();
    }

    public void initializeModuleContainer(ModuleContainer handle) {
    }


    public final void enableAll() {
        for (String id : this.getModules().keySet()) {
            this.enable(id);
        }
    }

    public final void disableAll() {
        for (String id : this.getModules().keySet()) {
            this.disable(id);
        }
    }

    public final void reloadAll() {
        var list = new ArrayList<String>();
        for (String id : this.getModules().keySet()) {
            if (this.disable(id) != ObjectOperationResult.SUCCESS) {
                continue;
            }
            list.add(id);
        }
        for (var s : list) {
            this.enable(s);
        }
    }
}
