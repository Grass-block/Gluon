package me.gb2022.gluon;

import me.gb2022.commons.compatibility.APIIncompatibleException;

@FunctionalInterface
public interface CompatibilityProvider {
    void checkCompatibility() throws APIIncompatibleException;
}
