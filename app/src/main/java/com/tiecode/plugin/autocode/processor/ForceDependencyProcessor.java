package com.tiecode.plugin.autocode.processor;

import com.tiecode.platform.compiler.api.descriptor.Name;
import com.tiecode.platform.compiler.api.log.Diagnostic;
import com.tiecode.platform.compiler.api.log.Messager;
import com.tiecode.platform.compiler.api.process.Filter;
import com.tiecode.platform.compiler.api.process.Trees;
import com.tiecode.platform.compiler.source.tree.ClassTree;
import com.tiecode.platform.compiler.source.tree.CodeReferenceTree;
import com.tiecode.platform.compiler.source.tree.CodeTree;
import com.tiecode.platform.compiler.source.tree.MethodTree;
import com.tiecode.platform.compiler.toolchain.env.Context;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationLevel;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationProcessor;
import com.tiecode.platform.compiler.toolchain.processor.Levels;
import com.tiecode.platform.compiler.toolchain.tree.TCTree;
import com.tiecode.platform.compiler.toolchain.tree.TreeMaker;
import com.tiecode.platform.compiler.toolchain.tree.code.PositionImpl;
import com.tiecode.platform.compiler.toolchain.tree.code.TiecodeTrees;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Modifier;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Modifiers;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Symbol;
import com.tiecode.platform.compiler.toolchain.tree.type.Type;
import com.tiecode.platform.compiler.toolchain.tree.type.Types;
import com.tiecode.platform.compiler.toolchain.util.Names;

import java.util.ArrayList;
import java.util.List;

/**
 * 强制依赖 注解处理器
 * @author Scave
 */
public class ForceDependencyProcessor implements AnnotationProcessor {
    public final static Name FORCE_DEPENDENCY = Names.of("强制依赖");

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
        return "强制依赖，该属性只能应用于类上，参数为强制依赖的类名，只要当前类被输出，强制依赖的类也会被输出";
    }

    @Override
    public Levels getLevels() {
        return new Levels(AnnotationLevel.CLASS);
    }

    @Override
    public int getPeriod() {
        return Period.TAG;
    }

    @Override
    public Type getArgType(Types types) {
        return types.STRING;
    }

    @Override
    public void process(TCTree.TCAnnotation annotation, TCTree.TCAnnotated annotated, Context context) {
        Symbol.ClassSymbol target = ((TCTree.TCClass) annotated).symbol;
        String arg = (String) annotation.getArgConstValue();
        String[] names = arg.split(",");
        Trees trees = TiecodeTrees.instance(context);
        for (String name : names) {
            TCTree.TCClass clazz = (TCTree.TCClass) trees.getClass(trees.getName(name), (Filter<Symbol.ClassSymbol>) null);
            if (clazz == null) {
                trees.printMessage(Diagnostic.Kind.ERROR, "找不到类: " + name,
                        annotation.argument, trees.getPath(target).getRoot());
            } else {
                target.addCodeBody(clazz.symbol);
            }
        }
    }
}
