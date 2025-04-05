package com.drunkncode.raw;
class RpnPrinter implements  Expr.Visitor<String>{
    @Override 
    public String visitBinaryExpr(Expr.Binary expr){
        return expr.left.accept(this) + " " + expr.right.accept(this) + " " + expr.operator.lexeme;

    }
    @Override 
    public String visitUnaryExpr(Expr.Unary expr){
        return expr.right.accept(this) + " " + expr.operator.lexeme;
    }
    @Override 
    public String visitGroupingExpr(Expr.Grouping expr){
        return expr.expression.accept(this);  
    }
    @Override 
    public String visitLiteralExpr(Expr.Literal expr){
        return expr.value == null ? "nil" : expr.value.toString();
    }
}
