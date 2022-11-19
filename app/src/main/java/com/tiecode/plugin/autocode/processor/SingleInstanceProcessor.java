package com.tiecode.plugin.autocode.processor;

import com.tiecode.platform.compiler.api.descriptor.Name;
import com.tiecode.platform.compiler.source.tree.MethodTree;
import com.tiecode.platform.compiler.toolchain.env.Context;
import com.tiecode.platform.compiler.toolchain.parser.TiecodeToken;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationLevel;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationProcessor;
import com.tiecode.platform.compiler.toolchain.processor.Levels;
import com.tiecode.platform.compiler.toolchain.tree.TCTree;
import com.tiecode.platform.compiler.toolchain.tree.TreeMaker;
import com.tiecode.platform.compiler.toolchain.tree.code.PositionImpl;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Modifier;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Modifiers;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Symbol;
import com.tiecode.platform.compiler.toolchain.tree.type.Type;
import com.tiecode.platform.compiler.toolchain.tree.type.Types;
import com.tiecode.platform.compiler.toolchain.util.Names;

import java.util.ArrayList;
import java.util.List;

/**
 * 单例类 注解处理器
 * @author Scave
 */
public class SingleInstanceProcessor implements AnnotationProcessor {
    public final static Name SINGLE = Names.of("单例类");

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
        return "单例类，该属性只能应用于类上，被标注的类将会增加一个静态的获取实例方法，方法名称为\"取实例\"";
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
            clazz.members = new ArrayList<>();
        }
        clazz.members.add(makeStaticVar(clazz));
        clazz.members.add(makeStaticGet(clazz));
    }

    private TCTree.TCVariableDeclare makeStaticVar(TCTree.TCClass clazz) {
        TreeMaker maker = new TreeMaker();
        int line = clazz.position.getStartLine() + 1;
        int column = clazz.position.getStartColumn();
        //静态变量
        TCTree.TCVariableDeclare staticInstance = maker.variable(Names.of("实例"),
                null, maker.identifier(clazz.name), null);
        staticInstance.position = PositionImpl.of(line, column, line, column + 10);
        Modifiers modifiers = new Modifiers(Modifier.STATIC);
        modifiers.add(Modifier.PRIVATE);
        modifiers.add(Modifier.REFER_VAR);
        staticInstance.modifiers = modifiers;
        //变量的Symbol
        Symbol.VarSymbol varSym = new Symbol.VarSymbol(staticInstance.name);
        varSym.modifiers = staticInstance.modifiers;
        varSym.owner = clazz.symbol;
        staticInstance.symbol = varSym;
        return staticInstance;
    }

    private TCTree.TCMethodDeclare makeStaticGet(TCTree.TCClass clazz) {
        TreeMaker maker = new TreeMaker();
        int line = clazz.position.getStartLine() + 1;
        int column = clazz.position.getStartColumn();
        //方法体
        List<TCTree.TCStatement> statements = new ArrayList<>();
        //如果语句
        List<TCTree.TCStatement> ifStatements = new ArrayList<>();
        TCTree.TCIdentifier varName = maker.identifier(Names.of("实例"));
        TCTree.TCIdentifier className = maker.identifier(clazz.name);
        TCTree.TCNewClass newClass = maker.newClass(className);
        TCTree.TCAssignment assignment = maker.assign(varName, newClass);
        TCTree.TCExpressionStatement assignSt = maker.expressionStatement(assignment);
        assignSt.position = PositionImpl.of(line, column, line, column + 10);
        ifStatements.add(assignSt);
        TCTree.TCBlock block = maker.block(ifStatements);
        //条件
        TCTree.TCIdentifier condVarName = maker.identifier(varName.name);
        TCTree.TCLiteral nullptr = maker.literal(null, TiecodeToken.NULL);
        TCTree.TCBinary binary = maker.binary(condVarName, TiecodeToken.EQEQ, nullptr);
        TCTree.TCIf anIf = maker.ifSt(binary, block, null);
        anIf.position = assignSt.position;
        statements.add(anIf);
        //返回 实例
        TCTree.TCIdentifier resultVar = maker.identifier(varName.name);
        TCTree.TCReturn returnExpr = maker.returnExpr(resultVar);
        TCTree.TCExpressionStatement returnSt = maker.expressionStatement(returnExpr);
        returnSt.position = assignSt.position;
        statements.add(returnSt);
        //方法定义
        TCTree.TCBlock methBlock = maker.block(statements);
        TCTree.TCMethodDeclare method = maker.method(Names.of("取实例"),
                null, null, maker.identifier(clazz.name), methBlock);
        method.category = MethodTree.Category.NORMAL;
        method.modifiers = Modifiers.defaultModifiers();
        method.modifiers.add(Modifier.STATIC);
        method.position = anIf.position;
        method.symbol.outputName = "getInstance";
        return method;
    }
}
