package com.tiecode.plugin.autocode;

import com.tiecode.platform.compiler.api.descriptor.Name;
import com.tiecode.platform.compiler.toolchain.env.Context;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationLevel;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationProcessor;
import com.tiecode.platform.compiler.toolchain.processor.Levels;
import com.tiecode.platform.compiler.toolchain.tree.TCTree;
import com.tiecode.platform.compiler.toolchain.tree.TreeMaker;
import com.tiecode.platform.compiler.toolchain.tree.code.PositionImpl;
import com.tiecode.platform.compiler.toolchain.tree.symbol.ExtraData;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Modifier;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Modifiers;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Symbol;
import com.tiecode.platform.compiler.toolchain.tree.type.Type;
import com.tiecode.platform.compiler.toolchain.tree.type.Types;
import com.tiecode.platform.compiler.toolchain.util.Names;
import com.tiecode.platform.compiler.util.PinYinUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 序列化类 注解处理器
 *
 * @author Scave
 */
public class SerializeClassProcessor implements AnnotationProcessor {
    public final static Name SERIALIZE_CLASS = Names.of("序列化类");

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
        return "序列化类，只能在目标平台为JVM平台的工程中使用，应用该属性后，会将类的基础类自动设置为序列化类"
                + "并根据类名生成序列化ID";
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
        if (clazz.members == null) {
            return;
        }
        clazz.symbol.outputName = PinYinUtils.getHumpPinYin(String.valueOf(clazz.name));
        makeSuffix(clazz);
        if (clazz.codes == null) {
            clazz.codes = new ArrayList<>();
        }
        clazz.codes.add(makeSerializeID(clazz));
    }

    private void makeSuffix(TCTree.TCClass clazz) {
        Symbol.ClassSymbol symbol = clazz.symbol;
        symbol.addExtraData(new ExtraData(Names.of("后缀代码"), "implements java.io.Serializable"));
    }

    private TCTree.TCCode makeSerializeID(TCTree.TCClass clazz) {
        TreeMaker maker = new TreeMaker();
        List<TCTree> trees = new ArrayList<>();
        TCTree.TCText text = maker.text("public final static long serialVersionUID = " + clazz.name.hashCode() + "L;");
        trees.add(text);
        TCTree.TCCode code = maker.code(trees);
        int line = clazz.position.getStartLine() + 1;
        int column = clazz.position.getStartColumn();
        code.position = PositionImpl.of(line, column, line, column + text.text.length() + 4);
        return code;
    }
}
