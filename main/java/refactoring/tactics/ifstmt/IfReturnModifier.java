package refactoring.tactics.ifstmt;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import utility.StaticUtilities;

public class IfReturnModifier extends ModifierVisitor<Void> {

    private ReturnStmt firstReturnStmt;
    private ReturnStmt secondReturnStmt;

    @Override
    public Visitable visit(IfStmt n, Void arg) {
        Visitable superValue = super.visit(n, arg);

        if (StaticUtilities.isRemoved(n) || StaticUtilities.isFinalStatement(n))
            return superValue;

        if (shouldRefactor1(n))
            refactorIfStmt1(n);

        resetFields();
        return superValue;
    }

    private boolean shouldRefactor1(IfStmt ifStmt) {
        Statement thenStmt = StaticUtilities.extractStatement(ifStmt.getThenStmt());
        if (!isValidReturnStatement(thenStmt))
            return false;

        Statement nextStatement;
        if (ifStmt.getElseStmt().isPresent())
            nextStatement = ifStmt.getElseStmt().get();
        else {
            nextStatement = StaticUtilities.getNextStatement(ifStmt);
        }

        if (nextStatement == null)
            return false;

        nextStatement = StaticUtilities.extractStatement(nextStatement);
        if(!isValidReturnStatement(nextStatement))
            return false;

        this.firstReturnStmt = thenStmt.asReturnStmt();
        this.secondReturnStmt = nextStatement.asReturnStmt();
        return true;
    }

    private void refactorIfStmt1(IfStmt ifStmt) {
        boolean firstReturnValue = firstReturnStmt.getExpression().get().asBooleanLiteralExpr().getValue();
        boolean secondReturnValue = secondReturnStmt.getExpression().get().asBooleanLiteralExpr().getValue();
        ReturnStmt returnStmt = new ReturnStmt();
        Expression expression;

        if (firstReturnValue != secondReturnValue) {
            expression = ifStmt.getCondition();
            if(!firstReturnValue)
                expression = StaticUtilities.applyLogicalComplement(expression);
        }
        else {
            expression = firstReturnStmt.getExpression().get();
        }

        returnStmt.setExpression(expression);
        StaticUtilities.removeFollowingStatements(ifStmt);
        ifStmt.replace(returnStmt);
    }

    private boolean isValidReturnStatement(Statement statement) {
        if (!(statement.isReturnStmt() && statement.asReturnStmt().getExpression().isPresent()))
            return false;

        return statement.asReturnStmt().getExpression().get().isBooleanLiteralExpr();
    }

    private void resetFields() {
        this.firstReturnStmt = null;
        this.secondReturnStmt = null;
    }
}
