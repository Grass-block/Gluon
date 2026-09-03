package me.gb2022.gluon.service;

import me.gb2022.commons.compatibility.APIIncompatibleException;
import me.gb2022.gluon.Context;
import me.gb2022.gluon.Debug;
import me.gb2022.gluon.FunctionalComponent;
import me.gb2022.gluon.ModularApplicationContext;
import me.gb2022.gluon.pack.ApplicationPackage;

import java.lang.reflect.InvocationTargetException;

public final class ServiceContainer implements Context, FunctionalComponent {
    private final ApplicationPackage owner;
    private final ServiceMetadata metadata;
    private final Class<Service> handle;
    private ModularApplicationContext context;
    private Service defaultInstance;

    public ServiceContainer(ApplicationPackage owner, Class<? extends Service> handle) {
        this.owner = owner;
        this.handle = (Class<Service>) handle;
        this.metadata = ServiceMetadata.parse(owner.meta().id(), handle);
    }

    public Class<Service> getHandle() {
        return handle;
    }

    public void initContext(ModularApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() {
        this.defaultInstance = context.getServiceManager().createImplementation(this, this.handle);
        Debug.log().info("Created instance for service{}: {}", this.handle, this.defaultInstance);
    }

    @Override
    public void enable() throws Exception {
        if (this.defaultInstance != null) {
            Debug.log().info("Enabling default instance: {}", this.meta().id());
            this.defaultInstance.enable();
        }

        Debug.log().info("Injecting to handle: {}", this.meta().id());
        Service.inject(this.handle, this.defaultInstance);

        if (this.defaultInstance != null) {
            if (this.metadata.export()) {
                Debug.log().info("Exporting default instance: {}", this.meta().id());
                this.context.getServiceManager().exportService(this.defaultInstance, this.handle);
            }
        }
    }

    @Override
    public void disable() throws Exception {
        if (this.defaultInstance != null) {
            if (this.metadata.export()) {
                Debug.log().info("Cancel-Exporting default instance: {}", this.meta().id());
                this.context.getServiceManager().unregisterExportedService(this.handle);
            }
        }

        Debug.log().info("Removing injection to handle: {}", this.meta().id());
        Service.uninject(this.handle);

        if (this.defaultInstance != null) {
            Debug.log().info("Disabling default instance: {}", this.meta().id());
            this.defaultInstance.disable();
        }
    }

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        if (this.defaultInstance != null) {
            this.defaultInstance.checkCompatibility();
        }

        try {
            var sm = this.handle.getMethod("checkServiceCompatibility");

            if (sm.isAnnotationPresent(ServiceInject.class)) {
                return;
            }

            sm.invoke(null);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError e){
            throw new APIIncompatibleException(e.getMessage());
        }

        Debug.log().info("Service {} passed compat check.", this.meta().id());
    }

    public ServiceMetadata meta() {
        return this.metadata;
    }

    public ApplicationPackage owner() {
        return this.owner;
    }
}
