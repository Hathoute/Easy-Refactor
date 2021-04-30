package refactoring.tactics.ifstmt;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import utility.StaticUtilities;

public class IfToConditionalModifier extends ModifierVisitor<Void> {

    @Override
    public Visitable visit(IfStmt n, Void arg) {
        Visitable superValue = super.visit(n, arg);

        if (StaticUtilities.isRemoved(n) || StaticUtilities.isFinalStatement(n))
            return superValue;

        if(shouldRefactor(n))
            return refactorIfStmt(n);

        return superValue;
    }

    private boolean shouldRefactor(IfStmt ifStmt) {
        if (!ifStmt.getElseStmt().isPresent())
            return false;

        Statement thenStmt = ifStmt.getThenStmt();
        Statement elseStmt = ifStmt.getElseStmt().get();
        if (thenStmt.isBlockStmt()) {
            if (thenStmt.asBlockStmt().getStatements().size() != 1)
                return false;

            thenStmt = thenStmt.asBlockStmt().getStatement(0);
        }
        if (elseStmt.isBlockStmt()) {
            if (elseStmt.asBlockStmt().getStatements().size() != 1)
                return false;

            elseStmt = elseStmt.asBlockStmt().getStatement(0);
        }


        if (!thenStmt.isExpressionStmt()
                || !elseStmt.isExpressionStmt())
            return false;

        Expression thenExpr = thenStmt.asExpressionStmt().getExpression();
        Expression elseExpr = elseStmt.asExpressionStmt().getExpression();

        if (!(thenExpr.isAssignExpr() && elseExpr.isAssignExpr()))
            return false;

        return thenExpr.asAssignExpr().getTarget().equals(elseExpr.asAssignExpr().getTarget());
    }

    private Statement refactorIfStmt(IfStmt ifStmt) {
        Statement thenStmt = ifStmt.getThenStmt();
        Statement elseStmt = ifStmt.getElseStmt().get();
        if (thenStmt.isBlockStmt())
            thenStmt = thenStmt.asBlockStmt().getStatement(0);
        if (elseStmt.isBlockStmt())
            elseStmt = elseStmt.asBlockStmt().getStatement(0);

        AssignExpr thenAssignment = thenStmt.asExpressionStmt().getExpression().asAssignExpr();
        AssignExpr elseAssignment = elseStmt.asExpressionStmt().getExpression().asAssignExpr();

        ExpressionStmt conditionalExpr = new ExpressionStmt();
        AssignExpr assignExpr = new AssignExpr();
        ConditionalExpr value = new ConditionalExpr();

        value.setCondition(ifStmt.getCondition());
        value.setThenExpr(thenAssignment.getValue());
        value.setElseExpr(elseAssignment.getValue());

        assignExpr.setTarget(thenAssignment.getTarget());
        assignExpr.setValue(value);

        conditionalExpr.setExpression(assignExpr);

        ifStmt.replace(conditionalExpr);

        return conditionalExpr;
    }
}
