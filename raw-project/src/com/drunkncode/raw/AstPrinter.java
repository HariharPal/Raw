package com.drunkncode.raw;

import java.util.List;

import com.drunkncode.raw.Expr.AnonymousFunction;
import com.drunkncode.raw.Expr.Call;
import com.drunkncode.raw.Expr.Get;
import com.drunkncode.raw.Expr.Set;
import com.drunkncode.raw.Expr.Super;
import com.drunkncode.raw.Expr.This;
import com.drunkncode.raw.Stmt.Block;
import com.drunkncode.raw.Stmt.Break;
import com.drunkncode.raw.Stmt.Class;
import com.drunkncode.raw.Stmt.Continue;
import com.drunkncode.raw.Stmt.Expression;
import com.drunkncode.raw.Stmt.Function;
import com.drunkncode.raw.Stmt.If;
import com.drunkncode.raw.Stmt.Print;
import com.drunkncode.raw.Stmt.Println;
import com.drunkncode.raw.Stmt.Return;
import com.drunkncode.raw.Stmt.Var;
import com.drunkncode.raw.Stmt.While;

class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
  String print(Expr expr) {
    return expr.accept(this);
  }

  String print(Stmt stmt) {
    return stmt.accept(this);
  }

  @Override
  public String visitCommaExpr(Expr.Comma expr) {
    return parenthesize(expr.comma.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitLogicalExpr(Expr.Logical expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitVariableExpr(Expr.Variable expr) {
    return expr.name.lexeme;
  }

  @Override
  public String visitAssignExpr(Expr.Assign expr) {
    return parenthesize("assign " + expr.name.lexeme, expr.value);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null) {
      return "nil";
    }
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  @Override
  public String visitTernaryExpr(Expr.Ternary expr) {
    return parenthesize("ternary", expr.condition, expr.trueExpr, expr.falseExpr);
  }

  @Override
  public String visitCallExpr(Call expr) {
    // Call expression: print (someExpr)
    StringBuilder sb = new StringBuilder();
    sb.append("(").append("call ").append(expr.callee.accept(this));
    for (Expr argument : expr.arguments) {
      sb.append(" ").append(argument.accept(this));
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public String visitAnonymousFunctionExpr(AnonymousFunction expr) {
    // Anonymous function expression: (fn (param1, param2) { body })
    StringBuilder sb = new StringBuilder();
    sb.append("(fn ");
    for (Token param : expr.params) {
      sb.append(param).append(" ");
    }
    sb.append("{ ").append(expr).append(" })");
    return sb.toString();
  }

  @Override
  public String visitGetExpr(Get expr) {
    // Get expression (e.g., object.property): object.property
    return parenthesize("get", expr.object, expr.object);
  }

  @Override
  public String visitSetExpr(Set expr) {
    // Set expression: object.property = value
    return parenthesize("set", expr.object, expr.value, expr.value);
  }

  @Override
  public String visitThisExpr(This expr) {
    // This expression (refers to the current instance)
    return "this";
  }

  @Override
  public String visitBlockStmt(Block stmt) {
    StringBuilder sb = new StringBuilder();
    sb.append("(block ");
    for (Stmt s : stmt.statements) {
      sb.append(s.accept(this)).append(" ");
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public String visitClassStmt(Class stmt) {
    return parenthesize("class", stmt.name, stmt.methods);
  }

  @Override
  public String visitWhileStmt(While stmt) {
    return parenthesize("while", stmt.condition, stmt.body);
  }

  @Override
  public String visitBreakStmt(Break stmt) {
    return "(break)";
  }

  @Override
  public String visitContinueStmt(Continue stmt) {
    return "(continue)";
  }

  @Override
  public String visitFunctionStmt(Function stmt) {
    return parenthesize("function " + stmt.name.lexeme, stmt.body);
  }

  @Override
  public String visitIfStmt(If stmt) {
    return parenthesize("if", stmt.condition, stmt.thenBranch, stmt.elseBranch);
  }

  @Override
  public String visitReturnStmt(Return stmt) {
    return parenthesize("return", stmt.value);
  }

  @Override
  public String visitExpressionStmt(Expression stmt) {
    return parenthesize("expression", stmt.expression);
  }

  @Override
  public String visitPrintStmt(Print stmt) {
    return parenthesize("print", stmt.expression);
  }

  @Override
  public String visitPrintlnStmt(Println stmt) {
    return parenthesize("println", stmt.expression);
  }

  @Override
  public String visitVarStmt(Var stmt) {
    return parenthesize("var " + stmt.name.lexeme, stmt.initializer);
  }

  private String parenthesize(String name, Object... parts) {
    StringBuilder sb = new StringBuilder();
    sb.append("(").append(name);
    for (Object part : parts) {
      sb.append(" ");
      if (part instanceof Expr) {
        sb.append(((Expr) part).accept(this));
      } else if (part instanceof Stmt) {
        sb.append(((Stmt) part).accept(this));
      } else if (part instanceof Token) {
        sb.append(((Token) part).lexeme);
      } else if (part instanceof List<?>) {
        for (Object item : (List<?>) part) {
          sb.append(" ");
          sb.append(parenthesize("list-item", item));
        }
      } else if (part != null) {
        sb.append(part.toString());
      } else {
        sb.append("nil");
      }
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public String visitSuperExpr(Super expr) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitSuperExpr'");
  }

}
