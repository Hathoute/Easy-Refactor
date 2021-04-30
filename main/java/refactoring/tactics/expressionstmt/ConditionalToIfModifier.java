package refactoring.tactics.expressionstmt;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import utility.StaticUtilities;

public class ConditionalToIfModifier extends ModifierVisitor<Void> {

    @Override
    public Visitable visit(ConditionalExpr n, Void arg) {
        Visitable superValue = super.visit(n, arg);

        if (StaticUtilities.isRemoved(n) || StaticUtilities.isFinalStatement(n))
            return superValue;

        if (shouldRefactor(n))
            refactorStatement(n);

        return superValue;
    }

    private boolean shouldRefactor(ConditionalExpr condExpr) {
        return true;
    }

    private void refactorStatement(ConditionalExpr condExpr) {
        Statement parentStmt = (Statement)StaticUtilities.getFirstParentOfInstance(condExpr, Statement.class);
        IfStmt ifStmt = new IfStmt();
        ifStmt.setCondition(condExpr.getCondition());
        Expression thenExpr = condExpr.getThenExpr();
        condExpr.replace(thenExpr);
        Statement thenStmt = parentStmt.clone();
        thenExpr.replace(condExpr.getElseExpr());
        Statement elseStmt = parentStmt.clone();

        BlockStmt thenBlock = new BlockStmt();
        thenBlock.addStatement(thenStmt);
        BlockStmt elseBlock = new BlockStmt();
        elseBlock.addStatement(elseStmt);

        ifStmt.setThenStmt(thenBlock);
        ifStmt.setElseStmt(elseBlock);

        parentStmt.replace(ifStmt);
        ifStmt.accept(new ConditionalToIfModifier(), null);
        ifStmt.setParsed(Node.Parsedness.UNPARSABLE);
    }
}
