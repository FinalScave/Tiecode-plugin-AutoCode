package com.tiecode.plugin.autocode;

import com.tiecode.platform.compiler.source.util.Name;
import com.tiecode.platform.compiler.source.util.PositionImpl;
import com.tiecode.platform.compiler.toolchain.completer.Checker;
import com.tiecode.platform.compiler.toolchain.env.Context;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationLevel;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationProcessor;
import com.tiecode.platform.compiler.toolchain.processor.Levels;
import com.tiecode.platform.compiler.toolchain.tree.TCTree;
import com.tiecode.platform.compiler.toolchain.tree.TreeMaker;
import com.tiecode.platform.compiler.toolchain.tree.symbol.ExtraData;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Symbol;
import com.tiecode.platform.compiler.toolchain.tree.type.Type;
import com.tiecode.platform.compiler.toolchain.tree.type.Types;
import com.tiecode.platform.compiler.toolchain.util.Names;
import com.tiecode.platform.compiler.util.PinYinUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 序列化类 注解处理器
 *
 * @author Scave
 */
public class ParameterNameProcessor implements AnnotationProcessor {
    public final static Name PARAM_NAME = Names.of("参数输出名");

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
        return "参数输出名，用于指定方法参数的输出名称，各参数输出名使用','分隔";
    }

    @Override
    public Levels getLevels() {
        return new Levels(AnnotationLevel.METHOD);
    }

    @Override
    public int getPeriod() {
        return Period.ENTER;
    }

    @Override
    public Type getArgType(Types types) {
        return types.STRING;
    }

    @Override
    public void process(TCTree.TCAnnotation annotation, TCTree.TCAnnotated annotated, Context context) {
        TCTree.TCMethodDeclare method = (TCTree.TCMethodDeclare) annotated;
        if (method.parameters == null) {
            return;
        }
        String value = (String) annotation.getArgConstValue();
        String[] names = value.split(",");
        if (names.length != method.parameters.size()) {
            Checker checker = context.get(Checker.key);
            checker.reportError(annotation, "parameter.size.not.match");
            return;
        }
        for (int i = 0; i < method.parameters.size(); i++) {
            TCTree.TCVariableDeclare param = method.parameters.get(i);
            param.symbol.outputName = names[i];
        }
    }
}
