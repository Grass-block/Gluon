package me.gb2022.gluon.pack;

import me.gb2022.gluon.attachment.SimpleAttachmentContainer;
import me.gb2022.gluon.service.Service;
import me.gb2022.gluon.module.AppModule;

import java.util.HashSet;
import java.util.Set;

public final class ContentBuilder extends SimpleAttachmentContainer<PackageAttachment> {
    private final Set<Class<? extends Service>> services = new HashSet<>();
    private final Set<Class<? extends AppModule>> modules = new HashSet<>();

    public void module(Class<? extends AppModule> clazz) {
        this.modules.add(clazz);
    }

    public void service(Class<? extends Service> clazz) {
        this.services.add(clazz);
    }

    public Set<Class<? extends AppModule>> getModules() {
        return modules;
    }

    public Set<Class<? extends Service>> getServices() {
        return services;
    }
}
