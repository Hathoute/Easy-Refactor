package refactoring.tactics.expressionstmt;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import utility.StaticUtilities;

import java.util.ArrayList;
import java.util.List;

public class ApplyInitializationValueModifier extends ModifierVisitor<Void> {

    private List<NameExpr> nameExprList;

    public ApplyInitializationValueModifier() {
        nameExprList = new ArrayList<>();
    }

    @Override
    public Visitable visit(VariableDeclarator n, Void arg) {
        Visitable superValue = super.visit(n, arg);

        if (StaticUtilities.isRemoved(n) || StaticUtilities.isFinalStatement(n))
            return superValue;

        if (shouldRefactor(n))
            refactorStatements(n);

        resetFields();

        return superValue;
    }

    private boolean shouldRefactor(VariableDeclarator varDeclarator) {
        if (!(varDeclarator.getType() instanceof PrimitiveType
                || (varDeclarator.getType().isClassOrInterfaceType()
                    && varDeclarator.getType().asClassOrInterfaceType().getNameAsString().equals("String"))))
            return false;

        if (!varDeclarator.getInitializer().isPresent())
            return false;

        BlockStmt blockStatement = (BlockStmt)StaticUtilities.getFirstParentOfInstance(varDeclarator, BlockStmt.class);
        if(blockStatement == null)
            return false;

        nameExprList = StaticUtilities.getChildrenNameExpressions(blockStatement, varDeclarator.getNameAsString());

        for(NameExpr nameExpr : nameExprList) {
            if (!nameExpr.getParentNode().isPresent())
                continue;

            Node parentExpression = nameExpr.getParentNode().get();
            if((parentExpression instanceof AssignExpr && ((AssignExpr) parentExpression).getTarget().equals(nameExpr))
                || parentExpression instanceof UnaryExpr)
                return false;
        }
        return true;
    }

    private void refactorStatements(VariableDeclarator varDeclarator) {
        for(NameExpr nameExpr : nameExprList) {
            if (!nameExpr.getParentNode().isPresent())
                continue;

            nameExpr.replace(varDeclarator.getInitializer().get().clone());
        }

        varDeclarator.remove();
    }

    private void resetFields() {
        nameExprList.clear();
    }

}
