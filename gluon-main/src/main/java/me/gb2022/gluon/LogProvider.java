package me.gb2022.gluon;

import me.gb2022.gluon.module.ModuleContainer;
import me.gb2022.gluon.service.ServiceContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface LogProvider {
    Logger createLogger(String id);

    Logger createLogger(ModuleContainer container);

    Logger createLogger(ServiceContainer container);

    class DefaultLogProvider implements LogProvider {
        private final String name;

        public DefaultLogProvider(String name) {
            this.name = name;
        }

        @Override
        public Logger createLogger(String id) {
            return LogManager.getLogger(this.name + ":" + id);
        }

        @Override
        public Logger createLogger(ModuleContainer container) {
            return createLogger(container.getMetadata().fullId());
        }

        @Override
        public Logger createLogger(ServiceContainer container) {
            return createLogger(container.getHandle().getSimpleName());
        }
    }
}
