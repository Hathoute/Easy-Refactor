package refactoring.postprocessing;

import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class SimplifyWrappingModifier extends ModifierVisitor<Void> {

    @Override
    public Visitable visit(EnclosedExpr n, Void arg) {

        if(canSimplify(n.getInner())) {
            Expression e = n.getInner().clone();
            n.replace(e);
            return e;
        }


        return super.visit(n, arg);
    }

    private boolean canSimplify(Expression expression) {
        return !(expression.isBinaryExpr()
            || expression.isVariableDeclarationExpr()
            || expression.isCastExpr()
            || expression.isConditionalExpr()
            || expression.isInstanceOfExpr());
    }
}
