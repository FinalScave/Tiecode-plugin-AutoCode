package com.tiecode.plugin.autocode;

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
 * 自动清理 注解处理器
 * @author Scave
 */
public class CleanUpProcessor implements AnnotationProcessor {
    public final static Name CLEAN_UP = Names.of("自动清理");

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
        return "自动清理，该属性可标注在输入输出流变量上，在变量退出其作用域时，会自动关闭输入输出流，无需自己编写代码去进行关闭";
    }

    @Override
    public Levels getLevels() {
        return new Levels(AnnotationLevel.VAR);
    }

    @Override
    public int getPeriod() {
        return Period.TAG;
    }

    @Override
    public Type getArgType(Types types) {
        return types.VOID;
    }

    @Override
    public void process(TCTree.TCAnnotation annotation, TCTree.TCAnnotated annotated, Context context) {
        TCTree.TCVariableDeclare variable = (TCTree.TCVariableDeclare) annotated;
        addToScopeEnd(variable);
    }

    private void addToScopeEnd(TCTree.TCVariableDeclare variable) {
        TreeMaker maker = new TreeMaker();
        int line = variable.position.getStartLine();
        int column = variable.position.getEndColumn() + 1;

        TCTree.TCIdentifier varName = maker.identifier(variable.name);
        TCTree.TCMethodInvocation invocation = maker.methodInvocation(Names.of("关闭"), varName, null);
        TCTree.TCExpressionStatement statement = maker.expressionStatement(invocation);
        statement.position = PositionImpl.of(line, column, line, column);
        variable.scope.enclBlock.statements.add(statement);
    }
}
