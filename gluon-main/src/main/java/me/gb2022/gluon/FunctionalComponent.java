package me.gb2022.gluon;

import me.gb2022.commons.compatibility.APIIncompatibleException;

public interface FunctionalComponent extends CompatibilityProvider {
    default void initialize() throws Exception {
    }

    default void enable() throws Exception {
    }

    default void disable() throws Exception {
    }

    @Override
    default void checkCompatibility() throws APIIncompatibleException {
    }
}
