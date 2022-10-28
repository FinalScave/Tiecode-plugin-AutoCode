package com.tiecode.plugin.autocode;

import com.tiecode.platform.compiler.source.tree.CodeReferenceTree;
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
 * 等值对象类 注解处理器
 * @author Scave
 */
public class HashObjectProcessor implements AnnotationProcessor {
    public final static Name HASH_OBJECT = Names.of("等值对象类");

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
        return "等值对象类，在进行对象比较的时候，只要所有变量值都相等，就判定两个对象相等";
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
        if (context.get(Options.key).target != Options.Target.ANDROID) {
            return;
        }
        if (clazz.codes == null) {
            clazz.codes = new ArrayList<>();
        }
        clazz.codes.add(makeHashCode(clazz));
        clazz.codes.add(makeEquals(clazz));
    }

    private TCTree.TCCode makeHashCode(TCTree.TCClass clazz) {
        TreeMaker maker = new TreeMaker();
        //嵌入式代码
        List<TCTree> trees = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append("@Override\n\tpublic int hashCode() {\n\t\t");
        builder.append("return java.util.Objects.hash(");
        List<TCTree.TCAnnotated> members = clazz.members;
        int count = 0;
        for (int i = 0, size = members.size(); i < size; i++) {
            TCTree.TCAnnotated member = members.get(i);
            if (member.getKind() != Tree.Kind.VARIABLE) {
                continue;
            }
            count++;
            TCTree.TCVariableDeclare variable = (TCTree.TCVariableDeclare) member;
            if (count > 1) {
                builder.append(", ");
            }
            TCTree.TCText text = maker.text(builder.toString());
            trees.add(text);
            builder.setLength(0);
            TCTree.TCIdentifier identifier = maker.identifier(variable.name);
            TCTree.TCCodeReference reference = maker.reference(CodeReferenceTree.Category.MEMBER, identifier);
            trees.add(reference);
        }
        builder.append(");\n\t}");
        TCTree.TCText text = maker.text(builder.toString());
        trees.add(text);
        TCTree.TCCode code = maker.code(trees);
        code.position = clazz.position;
        return code;
    }

    private TCTree.TCCode makeEquals(TCTree.TCClass clazz) {
        TreeMaker maker = new TreeMaker();
        //嵌入式代码
        List<TCTree> trees = new ArrayList<>();
        String prefix = "@Override\n\t"
                + "public boolean equals(Object o) {\n\t\t"
                + "if (this == o) return true;\n\t\t"
                + "if (o == null || getClass() != o.getClass()) return false;\n\t\t";
        TCTree.TCText text = maker.text(prefix);
        trees.add(text);

        TCTree.TCIdentifier className = maker.identifier(clazz.name);
        TCTree.TCCodeReference classRef = maker.reference(CodeReferenceTree.Category.TARGET_CLASS, className);
        trees.add(classRef);
        TCTree.TCText that = maker.text(" that = (");
        trees.add(that);
        trees.add(classRef);
        TCTree.TCText cast = maker.text(") o;\n\t\treturn ");
        trees.add(cast);

        List<TCTree.TCAnnotated> members = clazz.members;
        int count = 0;
        for (int i = 0, size = members.size(); i < size; i++) {
            TCTree.TCAnnotated member = members.get(i);
            if (member.getKind() != Tree.Kind.VARIABLE) {
                continue;
            }
            count++;
            TCTree.TCVariableDeclare variable = (TCTree.TCVariableDeclare) member;
            if (count > 1) {
                TCTree.TCText and = maker.text(" && ");
                trees.add(and);
            }
            TCTree.TCText equals = maker.text("java.util.Objects.equals(");
            trees.add(equals);

            TCTree.TCIdentifier thisVar = maker.identifier(variable.name);
            TCTree.TCCodeReference memberRef = maker.reference(CodeReferenceTree.Category.MEMBER, thisVar);
            trees.add(memberRef);

            TCTree.TCText thatPart = maker.text(", that."
                    + PinYinUtils.getHumpPinYin(String.valueOf(variable.name)) + ")");
            trees.add(thatPart);
        }
        TCTree.TCText last = maker.text(";\n\t}");
        trees.add(last);
        TCTree.TCCode code = maker.code(trees);
        code.position = clazz.position;
        return code;
    }
}
