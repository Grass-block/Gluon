package me.gb2022.gluon.module.attachment;

import me.gb2022.gluon.FunctionalComponent;
import me.gb2022.gluon.ModularApplicationContext;
import me.gb2022.gluon.module.ModuleContainer;

public interface ModuleAttachment extends FunctionalComponent {
    void initContext(ModularApplicationContext ctx, ModuleContainer container);
}
