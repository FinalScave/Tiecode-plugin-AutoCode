package com.tiecode.plugin.autocode;

import com.tiecode.platform.compiler.source.tree.CodeReferenceTree;
import com.tiecode.platform.compiler.source.tree.MemberReferenceTree;
import com.tiecode.platform.compiler.source.tree.MethodTree;
import com.tiecode.platform.compiler.source.tree.Tree;
import com.tiecode.platform.compiler.source.util.Name;
import com.tiecode.platform.compiler.toolchain.env.Context;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationLevel;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationProcessor;
import com.tiecode.platform.compiler.toolchain.processor.Levels;
import com.tiecode.platform.compiler.toolchain.tree.TCTree;
import com.tiecode.platform.compiler.toolchain.tree.TreeMaker;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Modifier;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Modifiers;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Symbol;
import com.tiecode.platform.compiler.toolchain.tree.type.Type;
import com.tiecode.platform.compiler.toolchain.tree.type.Types;
import com.tiecode.platform.compiler.toolchain.util.Names;
import com.tiecode.platform.compiler.util.PinYinUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据模型类 注解处理器
 *
 * @author Scave
 */
public class DataClassProcessor implements AnnotationProcessor {
    public final static Name DATA_CLASS = Names.of("数据模型类");

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
        return "数据模型类，会在类中自动生成所有变量的属性读写方法以及覆盖对象类的到文本方法"
                + "并将输出名更改为getXXX/setXXX/toString";
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
        List<TCTree.TCMethodDeclare> methods = new ArrayList<>();
        for (TCTree.TCAnnotated member : clazz.members) {
            if (member.getKind() != Tree.Kind.VARIABLE) {
                continue;
            }
            TCTree.TCVariableDeclare variable = (TCTree.TCVariableDeclare) member;
            if (variable.modifiers.isStatic()) {
                continue;
            }
            variable.symbol.outputName = PinYinUtils.getHumpPinYin(String.valueOf(variable.name));
            variable.modifiers.remove(Modifier.PUBLIC);
            variable.modifiers.add(Modifier.PRIVATE);
            TCTree.TCMethodDeclare getter = makeGetter(clazz, variable);
            TCTree.TCMethodDeclare setter = makeSetter(clazz, variable);
            methods.add(getter);
            methods.add(setter);
        }
        clazz.members.addAll(methods);
        if (clazz.codes == null) {
            clazz.codes = new ArrayList<>();
        }
        clazz.codes.add(makeToString(clazz));
    }

    private TCTree.TCMethodDeclare makeGetter(TCTree.TCClass clazz, TCTree.TCVariableDeclare variable) {
        TreeMaker maker = new TreeMaker();
        //方法体
        List<TCTree.TCStatement> statements = new ArrayList<>();
        //返回 变量名
        TCTree.TCReturn returnExpr = maker.returnExpr(maker.identifier(variable.name));
        TCTree.TCExpressionStatement statement = maker.expressionStatement(returnExpr);
        statement.position = variable.position;
        statements.add(statement);
        TCTree.TCBlock block = maker.block(statements);
        block.position = variable.position;
        //方法定义
        TCTree.TCMethodDeclare method = maker.method(variable.name,
                null, null, variable.type, block);
        method.position = variable.position;
        method.category = MethodTree.Category.GETTER;
        method.modifiers = Modifiers.defaultModifiers();
        //方法 symbol
        Symbol.MethodSymbol symbol = new Symbol.MethodSymbol(method.name);
        symbol.modifiers = method.modifiers;
        symbol.category = method.category;
        symbol.outputName = "get" + PinYinUtils.getHumpPinYin(String.valueOf(method.name));
        symbol.owner = clazz.symbol;
        method.symbol = symbol;
        return method;
    }

    private TCTree.TCMethodDeclare makeSetter(TCTree.TCClass clazz, TCTree.TCVariableDeclare variable) {
        TreeMaker maker = new TreeMaker();
        //参数
        List<TCTree.TCVariableDeclare> params = new ArrayList<>();
        TCTree.TCVariableDeclare param = maker.variable(variable.name, null, variable.type, null);
        param.isParameter = true;
        param.position = variable.position;
        param.modifiers = Modifiers.defaultModifiers();
        //参数Symbol
        Symbol.VarSymbol varSym = new Symbol.VarSymbol(variable.name);
        varSym.modifiers = param.modifiers;
        varSym.isParameter = param.isParameter;
        param.symbol = varSym;
        params.add(param);
        //方法体
        List<TCTree.TCStatement> statements = new ArrayList<>();
        TCTree.TCMemberReference reference = maker.memberReference(variable.name,
                MemberReferenceTree.Category.THIS, MemberReferenceTree.ReferenceMode.ACCESS, null);
        TCTree.TCIdentifier identifier = maker.identifier(variable.name);
        identifier.position = param.position;
        TCTree.TCAssignment assign = maker.assign(reference, identifier);
        TCTree.TCExpressionStatement statement = maker.expressionStatement(assign);
        statement.position = param.position;
        statements.add(statement);
        TCTree.TCBlock block = maker.block(statements);
        //方法定义
        TCTree.TCMethodDeclare method = maker.method(variable.name,
                null, params, null, block);
        method.category = MethodTree.Category.SETTER;
        method.modifiers = Modifiers.defaultModifiers();
        method.position = variable.position;
        //方法 symbol
        Symbol.MethodSymbol methSym = new Symbol.MethodSymbol(method.name);
        methSym.modifiers = method.modifiers;
        methSym.category = method.category;
        methSym.outputName = "set" + PinYinUtils.getHumpPinYin(String.valueOf(method.name));
        methSym.owner = clazz.symbol;
        method.symbol = methSym;
        return method;
    }

    private TCTree.TCCode makeToString(TCTree.TCClass clazz) {
        TreeMaker maker = new TreeMaker();
        //嵌入式代码
        List<TCTree> trees = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append("@Override\n\tpublic String toString() {\n\t\t");
        builder.append("return \"").append(clazz.name).append("{\\n\\t\"");
        List<TCTree.TCAnnotated> members = clazz.members;
        int count = 0;
        for (int i = 0, size = members.size(); i < size; i++) {
            TCTree.TCAnnotated member = members.get(i);
            if (member.getKind() != Tree.Kind.VARIABLE) {
                continue;
            }
            TCTree.TCVariableDeclare variable = (TCTree.TCVariableDeclare) member;
            if (variable.modifiers.isStatic()) {
                continue;
            }
            count++;
            builder.append(" + ").append("\"");
            if (count > 1) {
                builder.append(",\\n\\t");
            }
            builder.append(variable.name).append("=\" + ");
            TCTree.TCText text = maker.text(builder.toString());
            trees.add(text);
            builder.setLength(0);
            TCTree.TCIdentifier identifier = maker.identifier(variable.name);
            TCTree.TCCodeReference reference = maker.reference(CodeReferenceTree.Category.MEMBER, identifier);
            trees.add(reference);
        }
        builder.append("+ \"\\n}\";\n\t}");
        TCTree.TCText text = maker.text(builder.toString());
        trees.add(text);
        TCTree.TCCode code = maker.code(trees);
        code.position = clazz.position;
        return code;
    }
}
