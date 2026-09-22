package me.gb2022.gluon;

import java.util.*;
import java.util.function.Consumer;

public abstract class ComponentHookHolder<T extends FunctionalComponent> {
    private final Map<Class<? extends T>, Set<Consumer<? extends T>>> regHooks = new HashMap<>();
    private final Map<Class<? extends T>,Set<Consumer<? extends T>>> unregHooks = new HashMap<>();

    public <I extends T> void hookRegister(Class<I> type, Consumer<I> listener) {
        find(type).ifPresentOrElse(listener, () -> this.regHooks.computeIfAbsent(type, k -> new HashSet<>()).add(listener));
    }

    public <I extends T> void unhookRegister(Class<I> type, Consumer<I> listener) {
        find(type).ifPresentOrElse(listener, () -> this.unregHooks.get(type).add(listener));
    }

    public abstract <I extends T> Optional<I> find(Class<I> type);

    @SuppressWarnings("unchecked")
    public <I extends T> void handleReg(I component) {
        var hooks = this.regHooks.remove(component.getClass());

        if (hooks != null) {
            for (var hook : hooks) {

                ((Consumer<I>)hook).accept(component);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <I extends T> void handleUnreg(I component) {
        var hooks = this.unregHooks.remove(component.getClass());

        if (hooks != null) {
            for (var hook : hooks) {
                ((Consumer<I>)hook).accept(component);
            }
        }
    }
}
