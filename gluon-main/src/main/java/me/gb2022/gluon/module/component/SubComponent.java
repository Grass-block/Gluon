package me.gb2022.gluon.module.component;

import me.gb2022.gluon.FunctionalComponent;

public abstract class SubComponent<E> implements FunctionalComponent {
    protected E parent;

    public SubComponent() {
    }

    public SubComponent(final E parent) {
        ctx(parent);
    }

    public void ctx(E parent) {
        this.parent = parent;
    }

}
