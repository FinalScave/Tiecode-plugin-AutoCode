package com.tiecode.plugin.autocode.processor;

import com.tiecode.platform.compiler.api.descriptor.Name;
import com.tiecode.platform.compiler.toolchain.env.CompilationEnv;
import com.tiecode.platform.compiler.toolchain.env.Context;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationLevel;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationProcessor;
import com.tiecode.platform.compiler.toolchain.processor.Levels;
import com.tiecode.platform.compiler.toolchain.tree.TCTree;
import com.tiecode.platform.compiler.toolchain.tree.type.Type;
import com.tiecode.platform.compiler.toolchain.tree.type.Types;
import com.tiecode.platform.compiler.toolchain.util.Names;

/**
 * 输出到主包 注解处理器
 * @author Scave
 */
public class ToMainPkgProcessor implements AnnotationProcessor {
    public final static Name TO_MAIN_PKG = Names.of("输出到主包");

    @Override
    public boolean supportSuffix() {
        return false;
    }

    @Override
    public Name[] getSuffixes() {
        return null;
    }

    @Override
    public String getDescription() {
        return "该属性只能应用于类上，被标注的类在打包时将会被强制输出到主包，而非当前所处包";
    }

    @Override
    public Levels getLevels() {
        return new Levels(AnnotationLevel.CLASS);
    }

    @Override
    public int getPeriod() {
        return Period.ENTER;
    }

    @Override
    public Type getArgType(Types types) {
        return types.VOID;
    }

    @Override
    public void process(TCTree.TCAnnotation annotation, TCTree.TCAnnotated annotated, Context context) {
        TCTree.TCClass clazz = (TCTree.TCClass) annotated;
        CompilationEnv env = context.get(CompilationEnv.key);
        TCTree.TCClass startupClass = env.getFirstClass(Names.of("启动窗口"));
        clazz.symbol.owner = startupClass.symbol.owner;
    }
}
