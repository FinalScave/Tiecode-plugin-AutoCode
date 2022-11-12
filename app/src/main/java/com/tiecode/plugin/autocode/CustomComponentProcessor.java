package com.tiecode.plugin.autocode;

import com.tiecode.platform.compiler.source.tree.CodeReferenceTree;
import com.tiecode.platform.compiler.source.tree.CodeTree;
import com.tiecode.platform.compiler.source.tree.Tree;
import com.tiecode.platform.compiler.source.util.Name;
import com.tiecode.platform.compiler.toolchain.env.Context;
import com.tiecode.platform.compiler.toolchain.env.Options;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationLevel;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationProcessor;
import com.tiecode.platform.compiler.toolchain.processor.Levels;
import com.tiecode.platform.compiler.toolchain.tree.TCTree;
import com.tiecode.platform.compiler.toolchain.tree.TreeMaker;
import com.tiecode.platform.compiler.toolchain.tree.type.Type;
import com.tiecode.platform.compiler.toolchain.tree.type.Types;
import com.tiecode.platform.compiler.toolchain.util.Names;
import com.tiecode.platform.compiler.util.PinYinUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义组件 注解处理器
 * @author Scave
 */
public class CustomComponentProcessor implements AnnotationProcessor {
    public final static Name CUSTOM_COMPONENT = Names.of("自定义组件");

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
        return "自定义组件，只能在目标平台为安卓平台的工程中使用，应用该属性后，" +
                "将会在类中自动生成相关的初始嵌入式代码，";
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
        return types.STRING;
    }

    @Override
    public void process(TCTree.TCAnnotation annotation, TCTree.TCAnnotated annotated, Context context) {
        TCTree.TCClass clazz = (TCTree.TCClass) annotated;
        if (context.get(Options.key).target != Options.Target.ANDROID) {
            return;
        }
        if (clazz.codes == null) {
            clazz.codes = new ArrayList<>();
        }
        clazz.codes.add(makeInitCode(clazz, (String) annotation.getArgConstValue()));
    }

    private TCTree.TCCode makeInitCode(TCTree.TCClass clazz, String className) {
        TreeMaker maker = new TreeMaker();
        //嵌入式代码
        List<TCTree> trees = new ArrayList<>();
        //构造方法
        String text = "public ";
        TCTree.TCText tCons1 = maker.text(text);
        trees.add(tCons1);
        TCTree.TCIdentifier name = maker.identifier(clazz.name);
        TCTree.TCCodeReference nameRef = maker.reference(CodeReferenceTree.Category.TARGET_CLASS, name);
        trees.add(nameRef);
        text = "(android.content.Context context) {\n\t\tsuper(context);\n\t}\n\n";
        text += "\t@Override\n\tpublic " + className + " onCreateView(android.content.Context context) {\n";
        text += "\t\t" + className + " view = new " + className + "(context);\n";
        text += "\t\treturn view;\n";
        text += "\t}\n\n";
        text += "\t@Override\n\tpublic " + className + " getView() {\n";
        text += "\t\treturn (" + className + ") view;\n";
        text += "\t}";
        TCTree.TCText tCons2 = maker.text(text);
        trees.add(tCons2);
        TCTree.TCCode code = maker.code(trees);
        code.category = CodeTree.Category.LONG;
        code.position = clazz.position;
        return code;
    }
}
