import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;
import refactoring.postprocessing.SimplifyWrappingModifier;
import refactoring.tactics.expressionstmt.ApplyInitializationValueModifier;
import refactoring.tactics.expressionstmt.ConditionalToIfModifier;
import refactoring.tactics.forstmt.ForToWhileModifier;
import refactoring.tactics.ifstmt.IfReturnModifier;
import refactoring.tactics.ifstmt.IfToConditionalModifier;
import refactoring.tactics.ifstmt.ReverseIfStmtModifier;
import refactoring.preprocessing.WrapStatementsModifier;

import java.nio.file.Paths;

public class Entry {

    public static void main(String[] args) {
        // JavaParser has a minimal logging class that normally logs nothing.
        // Let's ask it to write to standard out:
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        // Set up a minimal type solver that only looks at the classes used to run this sample.
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        // SourceRoot is a tool that read and writes Java files from packages on a certain root directory.
        // In this case the root directory is found by taking the root from the current Maven module,
        // with src/main/resources appended.
        SourceRoot sourceRoot = new SourceRoot(CodeGenerationUtils.mavenModuleRoot(Entry.class).resolve("src/main/resources"));

        // Our sample is in the root of this directory, so no package name.
        CompilationUnit cu = sourceRoot.parse("", "Blabla.java");

        // Preprocessing
        cu.accept(new WrapStatementsModifier(), null);

        // Processing
        cu.accept(new ConditionalToIfModifier(), null);
        cu.accept(new ApplyInitializationValueModifier(), null);
        cu.accept(new IfReturnModifier(), null);
        cu.accept(new IfToConditionalModifier(), null);
        cu.accept(new ReverseIfStmtModifier(), null);
        cu.accept(new ForToWhileModifier(), null);

        // Postprocessing
        cu.accept(new SimplifyWrappingModifier(), null);

        // This saves all the files we just read to an output directory.
        sourceRoot.saveAll(
                // The path of the Maven module/project which contains the LogicPositivizer class.
                CodeGenerationUtils.mavenModuleRoot(Entry.class)
                        // appended with a path to "output"
                        .resolve(Paths.get("output")));
    }

}
