package me.gb2022.gluon.pack;

import me.gb2022.gluon.FunctionalComponent;
import me.gb2022.gluon.ModularApplicationContext;

public interface PackageAttachment extends FunctionalComponent {
    void initContext(ModularApplicationContext ctx, ApplicationPackage pkg);
}
