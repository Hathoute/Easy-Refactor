package utility;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import jdk.nashorn.internal.ir.BlockStatement;

import java.util.ArrayList;
import java.util.List;

public class StaticUtilities {

    /** If statement is a BlockStatement with one statement, returns the
     * statement inside the block, else returns the statement given as a parameter.
     * @param statement the statement.
     * @return child statement of BlockStatement or parameter itself.
     */
    public static Statement extractStatement(Statement statement) {
        if (statement.isBlockStmt() && statement.asBlockStmt().getStatements().size() == 1)
            return statement.asBlockStmt().getStatement(0);

        return statement;
    }

    /** Grabs the statement that follows the statement given as parameter.
     * @param statement the statement which precedes the statement we are looking for.
     * @return next statement if it exists, or null if not.
     */
    public static Statement getNextStatement(Statement statement) {
        if (!statement.getParentNode().isPresent())
            return null;

        if (!(statement.getParentNode().get() instanceof BlockStmt))
            return null;

        BlockStmt parentStatement = (BlockStmt)statement.getParentNode().get();
        List<Node> children = parentStatement.getChildNodes();
        int index = children.indexOf(statement);
        if (++index == children.size())
            return null;

        return parentStatement.getStatement(index);
    }

    /** Applies the logical complement (!) to the given expression.
     * @param expression The expression.
     * @return the complement of the given expression.
     */
    public static Expression applyLogicalComplement(Expression expression) {
        if (expression.isUnaryExpr()
                && expression.asUnaryExpr().getOperator() == UnaryExpr.Operator.LOGICAL_COMPLEMENT)
            return expression.asUnaryExpr().getExpression();

        UnaryExpr expr = new UnaryExpr();
        expr.setExpression(wrapExpression(expression));
        expr.setOperator(UnaryExpr.Operator.LOGICAL_COMPLEMENT);
        return expr;
    }

    /** Removes all the statements following the given statement
     * in the parent BlockStatement.
     * @param statement the statement
     */
    public static void removeFollowingStatements(Statement statement) {
        rec_removeFollowingStatements(getNextStatement(statement));
    }

    private static void rec_removeFollowingStatements(Statement statement) {
        if (statement == null)
            return;

        rec_removeFollowingStatements(getNextStatement(statement));
        statement.remove();
    }

    /** Checks if the statement has CompilationUnit
     * as the root node, if so the method returns true.
     * @param statement the statement
     * @return whether the statement was removed
     */
    public static boolean isRemoved(Node statement) {
        return !(statement.findRootNode() instanceof CompilationUnit);
    }

    /** Checks if the statement is in its final form
     * @param node the node
     * @return whether the node should not be edited anymore
     */
    public static boolean isFinalStatement(Node node) {
        return node.getParsed() == Node.Parsedness.UNPARSABLE;
    }

    public static List<NameExpr> getChildrenNameExpressions(Node node, String name) {
        ArrayList<NameExpr> nameExprs = new ArrayList<>();
        rec_getChildrenNameExpressions(node, name, nameExprs);
        return nameExprs;
    }
    private static void rec_getChildrenNameExpressions(Node curNode, String name, List<NameExpr> names) {
        for(Node node : curNode.getChildNodes()) {
            if(node instanceof NameExpr) {
                NameExpr nameExpr = (NameExpr)node;
                if (nameExpr.toString().equals(name))
                    names.add(nameExpr);

                continue;
            }

            rec_getChildrenNameExpressions(node, name, names);
        }
    }

    public static Node getFirstParentOfInstance(Node node, Class<?> parentClass) {
        if (node.getParentNode().isPresent())
            return rec_getFirstParentOfInstance(node.getParentNode().get(), parentClass);

        return null;
    }
    private static Node rec_getFirstParentOfInstance(Node node, Class<?> parentClass) {
        if (node == null || parentClass.isInstance(node))
            return node;

        return rec_getFirstParentOfInstance(
                node.getParentNode().isPresent() ? node.getParentNode().get() : null, parentClass);
    }

    public static BlockStmt wrapStatement(Statement statement) {
        if (statement.isBlockStmt())
            return statement.asBlockStmt();

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(statement);
        return blockStmt;
    }

    public static EnclosedExpr wrapExpression(Expression expression) {
        if (expression.isEnclosedExpr())
            return expression.asEnclosedExpr();

        EnclosedExpr enclosedExpr = new EnclosedExpr();
        enclosedExpr.setInner(expression);
        return enclosedExpr;
    }
}
