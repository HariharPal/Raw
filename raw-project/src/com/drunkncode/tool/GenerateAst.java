// For everyone knowledge AST (Abstract Syntax Tree) is a tree implementation of abstract syntanic
// function of a program or code snippets where each node represents a construct occuring in the
// source code, remove the useless parantheses or semicolons.

// AST generate the classes for interprester which is in tree form ;
// Instead of manually writing expr.java we generate it dynamically

// Expr is an abstract class It represents a generic expression in the interpreter
// We generate the Expr class automaticallly because we don't want to write the code manually

// When writing an interpreter we need a way to represent expression in a language Example: 1 + 2 is
// a binary expressionn etc
package com.drunkncode.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(
                outputDir,
                "Expr",
                Arrays.asList(

                        "Assign : Token name, Expr value",
                        "Comma : Expr left, Token comma, Expr right",
                        "Call : Expr callee, Token paren, List<Expr> arguments",
                        "Get : Expr object, Token name",
                        "Set : Expr object, Token name, Expr value",
                        "Super : Token keyword, Token method",
                        "This : Token keyword",
                        "Logical : Expr left, Token operator, Expr right",
                        "Binary : Expr left, Token operator, Expr right",
                        "Grouping : Expr expression",
                        "Literal : Object value",
                        "Unary : Token operator, Expr right",
                        "AnonymousFunction : Token name, List<Token> params, List<Stmt> body",
                        "Variable : Token name",
                        "Ternary: Expr condition, Expr trueExpr, Expr falseExpr"));
        defineAst(outputDir, "Stmt", Arrays.asList("Block : List<Stmt> statements",
                "Class : Token name, Expr.Variable superclass, List<Stmt.Function> methods, List<Stmt.Function> classMethods",
                "While : Expr condition, Stmt body, Expr increment",
                "Break : Token keyword",
                "Continue : Token keyword",
                "Function : Token name, List<Token> params, List<Stmt> body",
                "If : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Return : Token keyword, Expr value",
                "Expression : Expr expression", "Print : Expr expression", "Println : Expr expression",
                "Var : Token name, Expr initializer"));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types)
            throws IOException {
        System.out.println(outputDir + "/" + baseName + ".java");
        System.out.println("Code is running in defineAst");
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.drunkncode.raw;");

        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        // The visitor pattern which used to overcome both object (classes) and method
        // (function) based
        // programming language problem;
        defineVisitor(writer, baseName, types);
        // The AST Classes
        for (String type : types) {
            String[] parts = type.split(":");
            String className = parts[0].trim();
            String fieldList = parts[1].trim();
            defineType(writer, baseName, className, fieldList);

        }

        // the base accept() method
        writer.println();
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.println();
        System.out.println("Writer is closed.");
        writer.close();
    }

    // for each subclass we're declaring a visit method for each one.
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        System.out.println("Code is running in defineVisitor");
        writer.println("    interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(
                    "    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("    }");
    }

    private static void defineType(
            PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println(" static class " + className + " extends " + baseName + " {");

        // constructor
        writer.println("    " + className + "(" + fieldList + ") {");
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("         this." + name + " = " + name + " ;");
        }
        writer.println("    }");

        // Visitor Pattern
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor){");
        writer.println("    return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        // fields
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + "; ");
        }
        writer.println("    }");
    }
}
