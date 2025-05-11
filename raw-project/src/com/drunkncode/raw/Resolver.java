// Handles the scope and resolution

// Interpreter needs to figure out which variable name refers to maybe one defined inside function, or globally or in a class.

// What it does ? ==> Walks through the code before it runs figures out which variable comes from (scope resolution) helps catch bugs like using a variable before it's declared
// Why it's useful? ==> makes the interpreter faster and it helps handling local variable, closures, and class field correctly.

// Working on semantic analysis phase of an interperter, specificially resolving variable scope for a programming language interpreter. 
// The resolver class is analysis the abstract syntax tree (AST) of the user's program before it's executed to figure out what each variable refers to. This is important because it avoid looking up varaible during runtime each single time. (SLOW)

// declare(name) --> Mark as declared
// resolve(initializer) --> Resolve the expression if it exists
// define(name) --> Mark as fully defined

// resolveLocal(expr, expr.name) --> tells the interpreter how many deep the variable really is;

// Abort_1 ==> A stack is like pile of papers. Each item in the stack is a map of variables that are available in the scope (Like local variable inside a function block.); USEFULL ?? --> helps resolver to track the scope of variables are in which scope . Knows the x variable is alocal variable, global or any outer function.
// Each hashmap represents a single scope (like a block {}) keys is variable name and values = true means it is declared and used. if false means it is declared but not yet defined.

// Abort_2 
// Declare ==> like hey variable exists in the scope now
// Define ==> Now it's safe to use this variable example: var a = a + 1 the a is declared but not defined in here.

// Abort_3 
// resolveLocal ==> This method figures out "Where is the variable declared in the stack of scopes?" it then tells the interpreter "To access the variable u have to go X scopes deep." USEFULL ==> Avoid searching for the variable again and again during runtime. Improves performance.

// Abort_4
// beginScope() ==> Pushes a new Scope entering a block {}
// endScope() ==> Pops a new Scope exiting a block {}
// Mimics how blocks and function creates new scopes in the program Also checks unused variable at the end of the scope and shows warnings
package com.drunkncode.raw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private enum FunctionType {
    NONE, FUNCTION, METHOD, INITIALIZER
  }

  private enum ClassType {
    NONE, CLASS, INITIALIZER, SUBCLASS
  }

  // Abort_1
  private final Interpreter interpreter;
  private final Stack<HashMap<String, Boolean>> scopes = new Stack<>();
  private FunctionType currFunc = FunctionType.NONE;
  private int loopDepth = 0;
  private ClassType currClass = ClassType.NONE;

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  @Override
  public Void visitSuperExpr(Expr.Super expr) {
    if (currClass == ClassType.NONE) {
      Raw.error(expr.keyword, "Can't use 'super' outside the class.");
    } else if (currClass != ClassType.SUBCLASS) {
      Raw.error(expr.keyword, "Can't use 'super' in a class with no superclass.");
    }
    resolveLocal(expr, expr.keyword);
    return null;
  }

  @Override
  public Void visitThisExpr(Expr.This expr) {
    if (currClass == ClassType.NONE) {
      Raw.error(expr.keyword, "Can't use 'this' keyword outside the class.");
      return null;
    }
    resolveLocal(expr, expr.keyword);

    return null;
  }

  @Override
  public Void visitSetExpr(Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitGetExpr(Expr.Get expr) {
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    ClassType enclosingClass = currClass;
    currClass = ClassType.CLASS;

    declare(stmt.name);
    define(stmt.name);
    if (stmt.superclass != null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
      Raw.error(stmt.superclass.name, "A class cannot inherits from itself.");
    }
    if (stmt.superclass != null) {
      currClass = ClassType.SUBCLASS;
      beginScope();
      scopes.peek().put("super", true);
    }
    beginScope();

    // Binding this in the instance scope
    scopes.peek().put("this", true);

    // Handling instance methods
    for (Stmt.Function method : stmt.methods) {
      FunctionType declaration = FunctionType.METHOD;
      if (method.name.lexeme.equals("init")) {
        declaration = FunctionType.INITIALIZER;
      }
      resolveFunction(method, declaration);
    }

    // Handling class Methods: Don't have access to this
    for (Stmt.Function method : stmt.classMethods) {
      beginScope();
      scopes.peek().put("this", true);
      resolveFunction(method, FunctionType.METHOD);
      endScope();
    }

    endScope();
    if (stmt.superclass != null)
      endScope();

    currClass = enclosingClass;

    return null;
  }

  @Override
  public Void visitTernaryExpr(Expr.Ternary expr) {
    resolve(expr.condition);
    resolve(expr.trueExpr);
    resolve(expr.falseExpr);
    return null;
  }

  @Override
  public Void visitCommaExpr(Expr.Comma expr) {
    resolve(expr.right);
    resolve(expr.left);
    return null;
  }

  @Override
  public Void visitBreakStmt(Stmt.Break stmt) {
    if (loopDepth == 0) {
      Raw.error(stmt.keyword, "Can't use 'break' statement outside loops.");
    }
    return null;
  }

  @Override
  public Void visitContinueStmt(Stmt.Continue stmt) {
    if (loopDepth == 0) {
      Raw.error(stmt.keyword, "Can't use 'continue' statement outside loops.");
    }
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    resolve(expr.callee);
    for (Expr argument : expr.arguments) {
      resolve(argument);
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.condition);
    loopDepth++;
    resolve(stmt.body);
    loopDepth--;
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    if (currFunc == FunctionType.NONE) {
      Raw.error(stmt.keyword, "Can't return from top level code.");
    }
    if (stmt.value != null) {
      if (currFunc == FunctionType.INITIALIZER) {
        Raw.error(stmt.keyword, "Can't return value from an initializer.");
      }
      resolve(stmt.value);
    }
    return null;
  }

  @Override
  public Void visitPrintlnStmt(Stmt.Println stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null)
      resolve(stmt.elseBranch);
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitAnonymousFunctionExpr(Expr.AnonymousFunction expr) {
    if (expr.name != null) {
      declare(expr.name);
    }
    define(expr.name);
    resolveAnonymousFunction(expr, FunctionType.FUNCTION);
    return null;
  }

  public void resolveAnonymousFunction(Expr.AnonymousFunction anonymous, FunctionType type) {
    FunctionType enclosingFunction = currFunc;
    currFunc = type;
    beginScope();
    for (Token param : anonymous.params) {
      declare(param);
      define(param);
    }
    resolve(anonymous.body);
    endScope();
    currFunc = enclosingFunction;
  }

  // Function both bind the name as well as introduce the scope. The name of the
  // function bounds with the surrounding scope where's it declared. When we step
  // into the function body becomes itself the inner function scope.
  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    declare(stmt.name);
    define(stmt.name);
    resolveFunction(stmt, FunctionType.FUNCTION);
    return null;
  }

  // it seperates the method and create, s a new Scope for the body then bind the
  // variables for each of the function parameters. Just see the scopes it creates
  // a HashMap and for declare it put the map value to it the define and define
  // the function state.
  public void resolveFunction(Stmt.Function function, FunctionType type) {
    FunctionType enclosingFunction = currFunc;
    currFunc = type;
    beginScope();
    for (Token param : function.params) {
      declare(param);
      define(param);
    }
    resolve(function.body);
    endScope();
    currFunc = enclosingFunction;
  }

  // If variable is being accused inside its own initializer. This is where the
  // values in the scope map comes to play. If value exists in the current scope
  // but it's value is false that means we've declared but not yet defined that
  // variable.

  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    resolveLocal(expr, expr.name);

    // marking variable as used
    for (int i = scopes.size() - 1; i >= 0; i--) {
      HashMap<String, Boolean> scope = scopes.get(i);
      if (scope.containsKey(expr.name.lexeme)) {
        scope.put(expr.name.lexeme, true);
        break;
      }
    }
    return null;
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.value);
    resolveLocal(expr, expr.name);
    return null;
  }

  // Checking for the variable in each scope hashmap and if found we resove it,
  // passing in the number of scopes between the current innermost scope and the
  // scope where the variable is found;
  // Abort_3
  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    declare(stmt.name);
    if (stmt.initializer != null) {
      resolve(stmt.initializer);
    }
    define(stmt.name);
    return null;
  }

  // Abort_2
  private void declare(Token name) {
    if (scopes.isEmpty())
      return;
    Map<String, Boolean> scope = scopes.peek();
    if (scopes.peek().containsKey(name.lexeme)) {
      Raw.error(name, "Varible of same name already exists.");
    }
    scope.put(name.lexeme, false);
  }

  // Abort_2
  private void define(Token name) {
    if (scopes.isEmpty()) {
      return;
    }
    scopes.peek().put(name.lexeme, false);
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }

  void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }

  private void endScope() {
    HashMap<String, Boolean> scope = scopes.pop();
    for (Map.Entry<String, Boolean> entry : scope.entrySet()) {
      if (!entry.getValue()) {
        Raw.warning(new Token(TokenType.IDENTIFIER, entry.getKey(), null, -1), " Variable declared but never used ");
      }
    }
  }

  // Abort_4
  private void beginScope() {
    scopes.push(new HashMap<String, Boolean>());
  }

  // Abort_4
  private void resolve(Stmt stmt) {
    stmt.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }

}
