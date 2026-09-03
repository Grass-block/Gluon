package me.gb2022.gluon;

import me.gb2022.gluon.module.ModuleManager;
import me.gb2022.gluon.pack.ApplicationPackage;
import me.gb2022.gluon.pack.ApplicationPackageProvider;
import me.gb2022.gluon.pack.PackageManager;
import me.gb2022.gluon.service.ServiceLayer;
import me.gb2022.gluon.service.ServiceManager;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class ModularApplicationContext {
    private final String applicationName;
    private final Object holder;
    private final LogProvider logProvider;
    private final PackageManager packageManager;
    private final ServiceManager serviceManager;
    private final ModuleManager moduleManager;

    private ModularApplicationContext(Object holder, Builder builder) {
        this.holder = holder;
        this.applicationName = builder.applicationName;
        this.logProvider = builder.logProviderFunction.apply(this);
        this.packageManager = builder.packageManagerProvider.apply(this);
        this.serviceManager = builder.serviceManagerProvider.apply(this);
        this.moduleManager = builder.moduleManagerProvider.apply(this);
    }

    public static Builder builder(Object holder) {
        return new Builder(holder);
    }

    public Set<ApplicationPackage> registerPackage(Object holder, Class<?> target) {
        var packages = new HashSet<ApplicationPackage>();

        for (var m : target.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(ApplicationPackageProvider.class)) {
                continue;
            }

            packages.add(this.getPackageManager().buildPackage(holder, m));
        }

        for (var p : packages) {
            this.packageManager.addPackage(p);
        }

        return packages;
    }

    public void initialize() {
        try {
            this.packageManager.enable();
            this.moduleManager.enable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        try {
            this.moduleManager.disable();
            this.serviceManager.removeAll(ServiceLayer.USER);
            this.serviceManager.removeAll(ServiceLayer.FRAMEWORK);
            this.serviceManager.removeAll(ServiceLayer.FOUNDATION);
            this.packageManager.disable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public PackageManager getPackageManager() {
        return packageManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public LogProvider getLogProvider() {
        return logProvider;
    }

    public <H> H holder(Class<H> type) {
        return type.cast(holder);
    }

    public static final class Builder {
        private final Object holder;

        private String applicationName = "Gluon";
        private Function<ModularApplicationContext, LogProvider> logProviderFunction = (s) -> new LogProvider.DefaultLogProvider(s.applicationName);
        private Function<ModularApplicationContext, ServiceManager> serviceManagerProvider = ServiceManager::new;
        private Function<ModularApplicationContext, PackageManager> packageManagerProvider = PackageManager::new;
        private Function<ModularApplicationContext, ModuleManager> moduleManagerProvider = ModuleManager::new;

        public Builder(Object holder) {
            this.holder = holder;
        }

        public Builder serviceManager(Function<ModularApplicationContext, ServiceManager> provider) {
            this.serviceManagerProvider = provider;
            return this;
        }

        public Builder packageManager(Function<ModularApplicationContext, PackageManager> provider) {
            this.packageManagerProvider = provider;
            return this;
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder logProvider(Function<ModularApplicationContext, LogProvider> provider) {
            this.logProviderFunction = provider;
            return this;
        }

        public Builder moduleManager(Function<ModularApplicationContext, ModuleManager> provider) {
            this.moduleManagerProvider = provider;
            return this;
        }

        public ModularApplicationContext build() {
            return new ModularApplicationContext(this.holder, this);
        }
    }
}
