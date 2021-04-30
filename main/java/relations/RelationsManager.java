package relations;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RelationsManager {

    private static RelationsManager instance;

    private HashMap<Node, List<Class<?>>> nonAllowedRefactorings;

    private RelationsManager() {
        nonAllowedRefactorings = new HashMap<>();
    }

    public static RelationsManager getInstance() {
        if (instance == null)
            instance = new RelationsManager();

        return instance;
    }

    public void addConstraint(Node node, Class<?> refactoringClass) {
        if(!nonAllowedRefactorings.containsKey(node))
            nonAllowedRefactorings.put(node, new ArrayList<>());

        nonAllowedRefactorings.get(node).add(refactoringClass);
    }

    public boolean isRefactoringNotAllowed(Node node, Class<?> refactoringClass) {
        return nonAllowedRefactorings.containsKey(node)
                && nonAllowedRefactorings.get(node).contains(refactoringClass);
    }
}
