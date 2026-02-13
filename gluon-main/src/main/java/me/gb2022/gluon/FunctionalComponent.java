package me.gb2022.gluon;

import me.gb2022.commons.compatibility.APIIncompatibleException;

public interface FunctionalComponent {
    default void initialize() throws Exception {
    }

    default void enable() throws Exception {
    }

    default void disable() throws Exception {
    }

    default void checkCompatibility() throws APIIncompatibleException {
    }
}
