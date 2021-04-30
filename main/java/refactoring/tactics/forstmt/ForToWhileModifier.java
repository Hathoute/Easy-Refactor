package refactoring.tactics.forstmt;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import utility.StaticUtilities;

public class ForToWhileModifier extends ModifierVisitor<Void> {

    @Override
    public Visitable visit(ForStmt n, Void arg) {
        Visitable superValue = super.visit(n, arg);

        if (StaticUtilities.isRemoved(n))
            return superValue;

        if(shouldRefactor(n))
            refactorForLoop(n);

        return superValue;
    }

    private boolean shouldRefactor(ForStmt forStmt) {
        //TODO: this is only a random constraint...
        return forStmt.getInitialization().size() > 0 && forStmt.getUpdate().size() > 0;
    }

    private void refactorForLoop(ForStmt forStmt) {
        WhileStmt whileStmt = new WhileStmt();
        setWhileCondition(whileStmt, forStmt);
        setWhileBody(whileStmt, forStmt);
        forStmt.replace(whileStmt);

        BlockStmt parentNode = (BlockStmt)whileStmt.getParentNode().get();
        int index = parentNode.getStatements().indexOf(whileStmt);
        parentNode.addStatement(index, forStmt.getInitialization().get(0));
    }

    private void setWhileCondition(WhileStmt whileStmt, ForStmt forStmt) {
        if (forStmt.getCompare().isPresent())
            whileStmt.setCondition(forStmt.getCompare().get());
        else
            whileStmt.setCondition(new BooleanLiteralExpr(true));
    }

    private void setWhileBody(WhileStmt whileStmt, ForStmt forStmt) {
        if (!forStmt.getBody().isBlockStmt()) {
            BlockStmt block = new BlockStmt();
            block.addStatement(forStmt.getBody());
            forStmt.setBody(block);
        }

        whileStmt.setBody(forStmt.getBody());
        whileStmt.getBody().asBlockStmt().addStatement(forStmt.getUpdate().get(0));

    }
}
