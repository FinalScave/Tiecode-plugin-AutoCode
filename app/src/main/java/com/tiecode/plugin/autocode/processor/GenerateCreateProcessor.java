package com.tiecode.plugin.autocode.processor;

import com.tiecode.platform.compiler.api.descriptor.Name;
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
import com.tiecode.platform.compiler.toolchain.tree.symbol.Modifier;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Modifiers;
import com.tiecode.platform.compiler.toolchain.tree.symbol.Symbol;
import com.tiecode.platform.compiler.toolchain.tree.type.Type;
import com.tiecode.platform.compiler.toolchain.tree.type.Types;
import com.tiecode.platform.compiler.toolchain.util.Names;

import java.util.ArrayList;
import java.util.List;

/**
 * 生成创建 注解处理器
 * @author Scave
 */
public class GenerateCreateProcessor implements AnnotationProcessor {
    public final static Name GENERATE_CREATE = Names.of("生成创建");

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
        return "生成创建，该属性只能应用于类上，用于为窗口组件类增加一个静态的创建方法，方法名称为\"新建\"";
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
        clazz.members.add(makeStaticCreate(clazz));
    }

    private TCTree.TCMethodDeclare makeStaticCreate(TCTree.TCClass clazz) {
        TreeMaker maker = new TreeMaker();
        //参数
        List<TCTree.TCVariableDeclare> params = new ArrayList<>();
        TCTree.TCVariableDeclare param = maker.variable(Names.of("环境"),
                null, maker.identifier(Names.of("安卓环境")), null);
        param.isParameter = true;
        int line = clazz.position.getStartLine() + 1;
        int column = clazz.position.getStartColumn();
        param.position = PositionImpl.of(line, column, line, column + 10);
        param.modifiers = Modifiers.defaultModifiers();
        //参数Symbol
        Symbol.VarSymbol varSym = new Symbol.VarSymbol(param.name);
        varSym.modifiers = param.modifiers;
        varSym.isParameter = param.isParameter;
        param.symbol = varSym;
        params.add(param);
        //方法体
        List<TCTree.TCStatement> statements = new ArrayList<>();
        //嵌入式代码
        List<TCTree> trees = new ArrayList<>();
        //return new
        TCTree.TCText start = maker.text("return new ");
        trees.add(start);
        //#<类名>
        TCTree.TCCodeReference classRef = maker.reference(CodeReferenceTree.Category.TARGET_CLASS,
                maker.identifier(clazz.name));
        trees.add(classRef);
        //(
        TCTree.TCText lParen = maker.text("(");
        trees.add(lParen);
        //#环境
        TCTree.TCIdentifier context = maker.identifier(Names.of("环境"));
        context.position = PositionImpl.of(line, column, line, column + 2);
        TCTree.TCCodeReference paramRef = maker.reference(CodeReferenceTree.Category.MEMBER, context);
        trees.add(paramRef);
        //);
        TCTree.TCText end = maker.text(");");
        trees.add(end);
        TCTree.TCCode code = new TCTree.TCCode(trees);
        code.position = PositionImpl.of(line, column, line, column + 10);
        code.category = CodeTree.Category.LINE;
        statements.add(code);
        TCTree.TCBlock block = maker.block(statements);
        //方法定义
        TCTree.TCMethodDeclare method = maker.method(Names.of("新建"),
                null, params, maker.identifier(clazz.name), block);
        method.category = MethodTree.Category.NORMAL;
        method.modifiers = Modifiers.defaultModifiers();
        method.modifiers.add(Modifier.STATIC);
        method.position = code.position;
        return method;
    }
}
