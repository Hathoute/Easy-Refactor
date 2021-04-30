package refactoring.tactics.ifstmt;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import utility.StaticUtilities;

public class ReverseIfStmtModifier extends ModifierVisitor<Void> {

    @Override
    public Visitable visit(IfStmt n, Void arg) {
        Visitable superValue = super.visit(n, arg);

        if (StaticUtilities.isRemoved(n) || StaticUtilities.isFinalStatement(n))
            return superValue;

        reverseIfStmt(n);

        return superValue;
    }

    private void reverseIfStmt(IfStmt n) {
        if (!n.getElseStmt().isPresent())
            return;

        Statement thenStmt = n.getThenStmt().clone();
        Statement elseStmt = n.getElseStmt().get().clone();
        n.setThenStmt(elseStmt);
        n.setElseStmt(thenStmt);

        reverseConditions(n.getCondition());
    }

    private void reverseConditions(Expression condition) {
        if (condition.isEnclosedExpr()) {
            reverseConditions(condition.asEnclosedExpr().getInner());
            return;
        }

        if(condition.isBinaryExpr()) {
            if (condition.asBinaryExpr().getOperator().name().equals("AND")
                || condition.asBinaryExpr().getOperator().name().equals("OR")) {
                reverseConditions(condition.asBinaryExpr().getLeft());
                reverseConditions(condition.asBinaryExpr().getRight());
            }

            BinaryExpr be = condition.asBinaryExpr();
            switch (be.getOperator()) {
                case EQUALS:
                    be.setOperator(BinaryExpr.Operator.NOT_EQUALS);
                    break;
                case NOT_EQUALS:
                    be.setOperator(BinaryExpr.Operator.EQUALS);
                    break;
                case GREATER:
                    be.setOperator(BinaryExpr.Operator.LESS_EQUALS);
                    break;
                case GREATER_EQUALS:
                    be.setOperator(BinaryExpr.Operator.LESS);
                    break;
                case LESS:
                    be.setOperator(BinaryExpr.Operator.GREATER_EQUALS);
                    break;
                case LESS_EQUALS:
                    be.setOperator(BinaryExpr.Operator.GREATER);
                    break;
                case AND:
                    be.setOperator(BinaryExpr.Operator.OR);
                    break;
                case OR:
                    be.setOperator(BinaryExpr.Operator.AND);
                    break;
                default:
                    throw new IllegalArgumentException("Operator " + be.getOperator() + " is not implemented.");
            }
            return;
        }

        Expression newExpression;
        if (condition.isUnaryExpr()
                && condition.asUnaryExpr().getOperator().equals(UnaryExpr.Operator.LOGICAL_COMPLEMENT)) {
            newExpression = condition.asUnaryExpr().getExpression().clone();
        }
        else {
            newExpression = new UnaryExpr();
            newExpression.asUnaryExpr().setExpression(condition.clone());
            newExpression.asUnaryExpr().setOperator(UnaryExpr.Operator.LOGICAL_COMPLEMENT);
        }

        condition.replace(newExpression);
    }
}
