//expression     → equality ;
//equality       → comparison ( ( "!=" | "==" ) comparison )* ;
//comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
//term           → factor ( ( "-" | "+" ) factor )* ;
//factor         → unary ( ( "/" | "*" ) unary )* ;
//unary          → ( "!" | "-" ) unary
//               | primary ;
//primary        → NUMBER | STRING | "true" | "false" | "nil"
//               | "(" expression ")" ;


// Abort_1
// Error recovery : The way a parser responds to the error and keeps going to look for later errors is called Error Recovery
// Solution : Panic mode error recovery
// As soon as the parser detects the error it enters panic mode. So before it get back to parsing it needs to get its state and sequence of forthcoming token aligned such that next token just matches the rule being parsed. The process is called Synchronization
// To do that we mark the Synchronization point. The parser fix its parsing state by jumping out of any nested production until it gets back to that rule. Then it synchronize the token stream by discarding tokens until reaches one that can appear at that point in the rule.

// Abort_2
// When we want to synchronize we throw the ParseError object. Higher up method for the grammer rule we are synchronizing to, and we'll catch it. After the exception is caught, the parser is in the right state. ALl that left is to synchronize the tokens.
// Till the statement boundry we discard the tokens after catching ParseError we'll call this and then we hope that we back to sync. Now we parse the rest of the file starting at the next Statement.

package com.drunkncode.raw;

import static com.drunkncode.raw.TokenType.BANG;
import static com.drunkncode.raw.TokenType.BANG_EQUAL;
import static com.drunkncode.raw.TokenType.EOF;
import static com.drunkncode.raw.TokenType.EQUAL_EQUAL;
import static com.drunkncode.raw.TokenType.FALSE;
import static com.drunkncode.raw.TokenType.GREATER;
import static com.drunkncode.raw.TokenType.GREATER_EQUAL;
import static com.drunkncode.raw.TokenType.LEFT_PAREN;
import static com.drunkncode.raw.TokenType.LESS;
import static com.drunkncode.raw.TokenType.LESS_EQUAL;
import static com.drunkncode.raw.TokenType.MINUS;
import static com.drunkncode.raw.TokenType.NIL;
import static com.drunkncode.raw.TokenType.NUMBER;
import static com.drunkncode.raw.TokenType.PLUS;
import static com.drunkncode.raw.TokenType.RIGHT_PAREN;
import static com.drunkncode.raw.TokenType.SEMICOLON;
import static com.drunkncode.raw.TokenType.SLASH;
import static com.drunkncode.raw.TokenType.STAR;
import static com.drunkncode.raw.TokenType.STRING;
import static com.drunkncode.raw.TokenType.TRUE;

import java.util.List;

import com.drunkncode.raw.Expr.Literal;

class Parser{
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens){
    this.tokens = tokens;
  }

  private Expr expression(){
    return equality();
  }

  Expr parse(){
    try{
      return expression();
    }catch(ParseError error){
      return null;
    }
  }

  private Expr equality(){
    // Equality -> comparison(("!=" | "==") comparison)
    Expr expr = comparison();
    // The result of comparison which is high level then equality which will be solved then the resutlant goes to the top of the buiding (tree -> Bottom to top approach)

    // loop matches all the consecutive != and == operator and creating a sequence of equality expressions that creates a left associative nested tree of binary operator nodes.
    // Note if parser never matches equality operator, then it never enters the loop. In this calls the eqality() method effectively calls and returns comparison(). So, this method matches an eqality operator or anything of higher precedence.
    while(match(BANG_EQUAL, EQUAL_EQUAL)){
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison(){
    Expr expr = term();

    while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term(){
    Expr expr = factor();

    while(match(MINUS, PLUS)){
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  } 

  private Expr factor(){
    Expr expr = unary();

    while(match(SLASH, STAR)){
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary(){
    // We're parsing unary expression again and again until we found primary expression.
    if(match(BANG, MINUS)){
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  private Expr primary(){
    if(match(FALSE)) return new Expr.Literal(false);
    if(match(TRUE)) return new Expr.Literal(true);
    if(match(NIL)) return new Literal(null);

    if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);
    // if found the left parenthesis we must find the right parentheses too.
    if(match(LEFT_PAREN)) {
      Expr expr = expression();
      // Error -- Abort_1
      consume(RIGHT_PAREN, "Expect ')' after expression");
      return new Expr.Grouping(expr);
    }
    throw error(peek(), "Expect expression.");
  }
  
  private Token consume(TokenType type, String message){
    if(check(type)) return advance();
    throw error(peek(), message);
  }

  private boolean match(TokenType... types){
    for(TokenType type: types){
      if(check(type)){
        advance();
        return true;
      }
    }
    return false;
  }

  private ParseError error(Token token, String message){
    // Abort_2
    Raw.error(token, message);
    return new ParseError();
  }

  private boolean check(TokenType type){
    if(isAtEnd()){
      return false;
    }
    return peek().type == type;
  }

  private Token advance(){
    if(!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd(){
    return peek().type == EOF;
  }

  private Token peek(){
    return tokens.get(current);
  }

  private Token previous(){
    return tokens.get(current-1);
  }

  // Abort_2
  private void synchronize(){
    advance();
    while(!isAtEnd()){
      if(previous().type == SEMICOLON) return;
      switch(peek().type){
        case CLASS:
        case FUN:
        case FOR:
        case RETURN:
        case PRINT:
        case WHILE:
        case IF:
        case VAR:
          return;
      }
      advance();
    }
  }

  // Abort_2
  private static class ParseError extends RuntimeException{
    

  }
}
