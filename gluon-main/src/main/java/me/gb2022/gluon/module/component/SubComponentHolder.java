package me.gb2022.gluon.module.component;

import me.gb2022.gluon.APIIncompatibleException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface SubComponentHolder {
    Logger LOGGER = LogManager.getLogger("ComponentHolder");

    static <E> Set<SubComponent<E>> createComponents(E holder) {
        var components = new HashSet<SubComponent<E>>();

        if (!holder.getClass().isAnnotationPresent(ComponentProvider.class)) {
            return components;
        }

        var a = holder.getClass().getAnnotation(ComponentProvider.class);

        for (var clazz : a.value()) {
            SubComponent<E> component;

            try {
                component = (SubComponent<E>) clazz.getDeclaredConstructor().newInstance();
            } catch (NoClassDefFoundError ignored) {
                continue;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            try {
                component.checkCompatibility();
            } catch (APIIncompatibleException e) {
                LOGGER.warn("failed to create attachment of {}: {}", holder.getClass(), e);
                continue;
            }

            component.ctx(holder);
            components.add(component);
        }

        return components;
    }

    Map<Class<? extends SubComponent<?>>, ? extends SubComponent<?>> getComponents();

    <I extends SubComponent<?>> void getComponent(Class<I> clazz, Consumer<I> consumer);

    <I extends SubComponent<?>> I getComponent(Class<I> clazz);
}
