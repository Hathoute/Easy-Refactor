package refactoring.preprocessing;

import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import utility.StaticUtilities;

public class WrapStatementsModifier extends ModifierVisitor<Void> {

    @Override
    public Visitable visit(IfStmt n, Void arg) {
        if (!n.getThenStmt().isBlockStmt())
            n.setThenStmt(StaticUtilities.wrapStatement(n.getThenStmt()));

        if(n.getElseStmt().isPresent() && !n.getElseStmt().get().isBlockStmt())
            n.setElseStmt(StaticUtilities.wrapStatement(n.getElseStmt().get()));

        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(ForStmt n, Void arg) {
        if (!n.getBody().isBlockStmt())
            n.setBody(StaticUtilities.wrapStatement(n.getBody()));

        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(WhileStmt n, Void arg) {
        if (!n.getBody().isBlockStmt())
            n.setBody(StaticUtilities.wrapStatement(n.getBody()));

        return super.visit(n, arg);
    }
}
