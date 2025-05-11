package com.drunkncode.raw;

import com.drunkncode.raw.Expr.AnonymousFunction;
import com.drunkncode.raw.Expr.Call;
import com.drunkncode.raw.Expr.Get;
import com.drunkncode.raw.Expr.Set;
import com.drunkncode.raw.Expr.Super;
import com.drunkncode.raw.Expr.This;

class RpnPrinter implements Expr.Visitor<String> {
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return expr.left.accept(this) + " " + expr.right.accept(this) + " " + expr.operator.lexeme;
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return expr.left.accept(this) + " " + expr.right.accept(this) + " " + expr.operator.lexeme;
    }

    @Override
    public String visitCommaExpr(Expr.Comma expr) {
        return expr.left.accept(this) + " " + expr.right.accept(this) + " " + expr.comma.lexeme;
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return expr.value.accept(this) + " " + expr.name.lexeme + " =";
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return expr.right.accept(this) + " " + expr.operator.lexeme;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value == null ? "nil" : expr.value.toString();
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return expr.condition.accept(this) + " " + expr.trueExpr.accept(this) + " " + expr.falseExpr.accept(this)
                + " ?:";
    }

    @Override
    public String visitCallExpr(Call expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
    }

    @Override
    public String visitAnonymousFunctionExpr(AnonymousFunction expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAnonymousFunctionExpr'");
    }

    @Override
    public String visitGetExpr(Get expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitGetExpr'");
    }

    @Override
    public String visitSetExpr(Set expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSetExpr'");
    }

    @Override
    public String visitThisExpr(This expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitThisExpr'");
    }

    @Override
    public String visitSuperExpr(Super expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSuperExpr'");
    }
}
