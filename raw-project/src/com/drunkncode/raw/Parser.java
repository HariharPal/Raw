//program        → declaration* EOF ;
//declaration    → varDecl | statement | funDecl | classDecl;
//classDecl      → "class" IDENTIFIER ( "<" IDENTIFIER ) ? "{" function* "}";
//funDecl        → "fun" function;
//function       → IDENTIFIER "(" parameters? ")" block;
//parameters     → IDENTIFIER ("," IDENTIFIER);
//varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
//statement      → returnStmt | breakStmt | continueStmt | exprStmt | printStmt | forStmt | ifStmt | whileStmt | block;
//returnStmt     → "return " expression? ";";
//breakStmt      → break ";";
//continueStmt   → continue ";";
//whileStmt      → "while" "(" expression ")" statement;
//forStmt        → "for" "(" (varDecl | exprStmt | ";") expression? ";" expression? ")" statement;
//ifStmt         → "if" "(" expression ")" statement ("else" statement) ? ;
//block          → "{"declaration "]";
//exprStmt       → expression ";" ;
//printStmt      → "print" expression ";" ;
//printlnStmt    → "println" expression ";";
//expression     → anonymousFunction | assignment;
//anonymousFunction → "fun" "(" parameters? ")" block;
//assignment     → (call ".") ? IDENTIFIER "=" assignment | ternary;
//logic_or       → login_and ( "or" logic_and )*;
//logic_and      → ternary ( "and" ternary)*;
//ternary        → bitwiseOr;
//bitwiseOr      → bitwiseXor;
//bitwiseXor     → bitwiseAnd;
//bitwiseAnd     → comma;
//comma          → equality();
//equality       → comparison ( ( "!=" | "==" ) comparison )* ;
//comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
//term           → factor ( ( "-" | "+" ) factor )* ;
//factor         → unary ( ( "/" | "*" | "%") unary )* ;
//unary          → ( "!" | "-" ) unary | call ;
//call           → primary ( "(" arguments? ")" | "." IDENTIFIER)* ;
//argument       → expression("," expression)*;
//primary        → NUMBER | STRING | "true" | "false" | "this" | IDENTIFIER | "super" "." IDENTIFIER | "nil" | "(" expression ")" ;

// Abort_1
// Error recovery : The way a parser responds to the error and keeps going to look for later errors is called Error Recovery
// Solution : Panic mode error recovery
// As soon as the parser detects the error it enters panic mode. So before it get back to parsing it needs to get its state and sequence of forthcoming token aligned such that next token just matches the rule being parsed. The process is called Synchronization
// To do that we mark the Synchronization point. The parser fix its parsing state by jumping out of any nested production until it gets back to that rule. Then it synchronize the token stream by discarding tokens until reaches one that can appear at that point in the rule.

// Abort_2
// When we want to synchronize we throw the ParseError object. Higher up method for the grammer rule we are synchronizing to, and we'll catch it. After the exception is caught, the parser is in the right state. ALl that left is to synchronize the tokens.
// Till the statement boundry we discard the tokens after catching ParseError we'll call this and then we hope that we back to sync. Now we parse the rest of the file starting at the next Statement.

// Abort_3
// A cover grammer a looser grammer that accept all the valid expression (ternary() in our case) and assignment target syntaxes. When u hit an "=" repor tan error if the left-hand side isn't within the valid assignment target grammar. Conversly if you don't hit an =, report an error if the left hand side isn't a valid expression. 
// first we parse the left side like a normal expression (could be an variable or more) then we look for the equals if it matches then we recursively iterate the left hand side using assignment() operator  and error occur when something like (a + 1) = 3 like condition occur by it means one is expression not variable but we still try to assign the value to it;

// Abort_4
// We create an empty list and parse the statements and add them to the list until we reach to the end of the block, marking by the closing }. Not the loop is explicitly checking for function isAtEnd(). We have to be careful to avoid infinite loops, even when parsing invalid code

// Abort_5
// We're doing for loop consider as while loop we convert it to the while loop by checking for conditions and statements 
// Here in for loop it exists like this for(...;...;...) where first one can be a variable using VAR, or expression or may be nothing. So we check for it what we get 
// Second must be expression consists of condition of loop ending which we're getting by the previous boiler plate code expressionStatement;
// Third loop consists of the incremental condition similar as condition we get it from expressionStatement;
//
// So in this code we're getting 1. Initializer 2. Condition 3. Increment which we use it to create a while loop
// first we got statement for the block we're in after that condition 
// So for the body of the block we're adding the increment condition which contains the list of body of the statements then we go further and find condition if there's condition we're appling while loop to it by covering it by while statement where while statement covers the list of all statement to execute with different increment. Then for the while loop we're getting it and covering it by the initial statement so for each while loop runs with initial statement and each statement have conditions and those condition has list of increment body of statement of it;

package com.drunkncode.raw;

import static com.drunkncode.raw.TokenType.BANG;
import static com.drunkncode.raw.TokenType.BANG_EQUAL;
import static com.drunkncode.raw.TokenType.CLASS;
import static com.drunkncode.raw.TokenType.COLON;
import static com.drunkncode.raw.TokenType.COMMA;
import static com.drunkncode.raw.TokenType.EOF;
import static com.drunkncode.raw.TokenType.EQUAL_EQUAL;
import static com.drunkncode.raw.TokenType.FALSE;
import static com.drunkncode.raw.TokenType.GREATER;
import static com.drunkncode.raw.TokenType.GREATER_EQUAL;
import static com.drunkncode.raw.TokenType.IDENTIFIER;
import static com.drunkncode.raw.TokenType.LEFT_PAREN;
import static com.drunkncode.raw.TokenType.LESS;
import static com.drunkncode.raw.TokenType.LESS_EQUAL;
import static com.drunkncode.raw.TokenType.MINUS;
import static com.drunkncode.raw.TokenType.NIL;
import static com.drunkncode.raw.TokenType.NUMBER;
import static com.drunkncode.raw.TokenType.PLUS;
import static com.drunkncode.raw.TokenType.QUESTION;
import static com.drunkncode.raw.TokenType.RIGHT_PAREN;
import static com.drunkncode.raw.TokenType.SEMICOLON;
import static com.drunkncode.raw.TokenType.SLASH;
import static com.drunkncode.raw.TokenType.STAR;
import static com.drunkncode.raw.TokenType.STRING;
import static com.drunkncode.raw.TokenType.THIS;
import static com.drunkncode.raw.TokenType.TRUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.drunkncode.raw.Expr.Literal;

class Parser {
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }
    return statements;
  }

  private Stmt declaration() {
    try {
      if (match(TokenType.VAR)) {
        return varDeclaration();
      }
      if (match(TokenType.CLASS)) {
        return classDeclaration();
      }
      if (match(TokenType.FUN)) {
        if (check(LEFT_PAREN)) {
          error(previous(), "Did you mean to write an anonymous function? Wrap it in parentheses.");
        }
        return function("function");
      }

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt classDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect class name.");
    Expr.Variable superClass = null;
    if (match(LESS)) {
      consume(IDENTIFIER, "Expected SuperClass Name");
      superClass = new Expr.Variable(previous());
    }
    consume(TokenType.LEFT_BRACE, "Expect '{' before class Body.");
    List<Stmt.Function> methods = new ArrayList<>();
    List<Stmt.Function> classMethods = new ArrayList<>();
    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      boolean isClassMethod = match(CLASS);
      (isClassMethod ? classMethods : methods).add(function("method"));
    }
    consume(TokenType.RIGHT_BRACE, "Expect '}' in the end of class body.");
    return new Stmt.Class(name, superClass, methods, classMethods);
  }

  private Stmt.Function function(String kind) {
    Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name");
    consume(TokenType.LEFT_PAREN, "Expected '(' after " + kind + " name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 254 paramenters.");
        }
        parameters.add(consume(IDENTIFIER, "Expect parameter name."));
      } while (match(TokenType.COMMA));
    }
    consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters");

    consume(TokenType.LEFT_BRACE, "Expected '{' before " + kind + " body.");
    List<Stmt> body = block();
    return new Stmt.Function(name, parameters, body);
  }

  private Expr anonymousFunction() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'fun'.");
    List<Token> parameters = new ArrayList<>();
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 254 paramenters.");
        }
        parameters.add(consume(IDENTIFIER, "Expect parameter name."));
      } while (match(TokenType.COMMA));
    }
    consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters");

    consume(TokenType.LEFT_BRACE, "Expected '{' before body.");
    List<Stmt> body = block();
    return new Expr.AnonymousFunction(null, parameters, body);

  }

  private Stmt varDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
    Expr initializer = null;
    if (match(TokenType.EQUAL)) {
      initializer = expression();
    }
    consume(SEMICOLON, "Expect ';' after variable declaration");
    return new Stmt.Var(name, initializer);
  }

  private Expr expression() {
    if (match(PLUS, STAR, SLASH, EQUAL_EQUAL, BANG_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      error(operator, "Missing left hand operand for binary operator.");
      expression();
      return null;
    }
    return assignment();
  }

  private Stmt statement() {
    if (match(TokenType.BREAK)) {
      return breakStatement();
    }
    if (match(TokenType.CONTINUE)) {
      return continueStatement();
    }
    if (match(TokenType.PRINT)) {
      return printStatement();
    }
    if (match(TokenType.RETURN)) {
      return returnStatement();
    }
    if (match(TokenType.PRINTLN)) {
      return printlnStatement();
    }
    if (match(TokenType.IF)) {
      return ifStatement();
    }
    if (match(TokenType.WHILE)) {
      return whileStatement();
    }
    if (match(TokenType.FOR)) {
      return forStatement();
    }
    if (match(TokenType.LEFT_BRACE)) {
      return new Stmt.Block(block());
    }
    return expressionStatement();
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;
    if (!check(TokenType.SEMICOLON)) {
      value = expression();
    }
    consume(TokenType.SEMICOLON, "Expected ';' after return value.");
    return new Stmt.Return(keyword, value);
  }

  private Stmt continueStatement() {
    Token keyword = previous();
    consume(TokenType.SEMICOLON, "Expect ';' after continue.");
    return new Stmt.Continue(keyword);
  }

  private Stmt breakStatement() {
    Token keyword = previous();
    consume(TokenType.SEMICOLON, "Expect ';' after 'break'.");
    return new Stmt.Break(keyword);
  }

  // Abort_5
  private Stmt forStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'for'.");

    // Parse the initializer but DO NOT let it eat the loop’s ';'
    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(TokenType.VAR)) {
      // Inline varDecl:
      Token name = consume(IDENTIFIER, "Expect variable name.");
      Expr init = null;
      if (match(TokenType.EQUAL)) {
        init = expression();
      }
      // Now consume exactly the for‑initializer semicolon:
      consume(TokenType.SEMICOLON, "Expect ';' after loop initializer.");
      initializer = new Stmt.Var(name, init);
    } else {
      initializer = expressionStatement();
    }

    // Parse the condition
    Expr condition = null;
    if (!check(TokenType.SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "Expect ';' after loop condition.");

    // Parse the increment
    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "Expect ')' after for clauses.");

    // Parse the loop body
    Stmt body = statement();

    // Desugar into a while‑loop with increment at the end
    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(
          body,
          new Stmt.Expression(increment)));
    }
    if (condition == null)
      condition = new Expr.Literal(true);
    body = new Stmt.While(condition, body, null);

    // If there was an initializer, wrap both in a block
    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt whileStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");
    Stmt body = statement();
    return new Stmt.While(condition, body, null);
  }

  private Stmt ifStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expect '(' after if condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(TokenType.ELSE)) {
      elseBranch = statement();
    }
    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt printlnStatement() {
    Expr value = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after value.");
    return new Stmt.Println(value);
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(value);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }

  // Abort_4
  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();
    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }
    consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
    return statements;
  }

  // Abort_3
  private Expr assignment() {
    Expr expr = or();
    if (match(TokenType.EQUAL)) {
      Token equals = previous();
      Expr value = assignment();
      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        return new Expr.Assign(name, value);
      } else if (expr instanceof Expr.Get) {// if there's a getter in assignment then we create a new getter and assign
                                            // it's value to the value given through assignemtn
        Expr.Get get = (Expr.Get) expr;
        return new Expr.Set(get.object, get.name, value);
      }
      error(equals, "Invalid Assginment Target.");
    }
    return expr;
  }

  private Expr or() {
    Expr expr = and();

    while (match(TokenType.OR)) {
      Token operator = previous();
      Expr right = and();

      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = ternary();

    while (match(TokenType.AND)) {
      Token operator = previous();
      Expr right = ternary();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  // If you want to add just add bitwiseOr and bitwiseAnd and if you have time
  // just add xor too
  private Expr ternary() {
    Expr expr = bitwiseOr();
    while (match(QUESTION)) {
      Expr thenBranch = expression();// parsing middle expression
      consume(COLON, "Expect ':' after then branch of conditional expression.");
      Expr elseBranch = ternary();// parse right associative else branch after expression branch it goes for
                                  // another conditions which then after the ternary
      expr = new Expr.Ternary(expr, thenBranch, elseBranch);
    }

    return expr;
  }

  private Expr bitwiseOr() {
    Expr expr = bitwiseXor();
    while (match(TokenType.BIT_OR)) {
      Token operator = previous();
      Expr right = bitwiseXor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr bitwiseXor() {
    Expr expr = bitwiseAnd();
    while (match(TokenType.BIT_XOR)) {
      Token operator = previous();
      Expr right = bitwiseAnd();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr bitwiseAnd() {
    Expr expr = comma();
    while (match(TokenType.BIT_AND)) {
      Token operator = previous();
      Expr right = comma();
      expr = new Expr.Comma(expr, operator, right);
    }
    return expr;
  }

  private Expr comma() {
    Expr expr = equality();
    while (match(COMMA)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr equality() {
    // Equality -> comparison(("!=" | "==") comparison)
    Expr expr = comparison();
    // The result of comparison which is high level then equality which will be
    // solved then the resutlant goes to the top of the buiding (tree -> Bottom to
    // top approach)

    // loop matches all the consecutive != and == operator and creating a sequence
    // of equality expressions that creates a left associative nested tree of binary
    // operator nodes.
    // Note if parser never matches equality operator, then it never enters the
    // loop. In this calls the eqality() method effectively calls and returns
    // comparison(). So, this method matches an eqality operator or anything of
    // higher precedence.
    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(SLASH, STAR, TokenType.MOD)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    // We're parsing unary expression again and again until we found primary
    // expression.
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return call();
  }

  private Expr call() {
    Expr expr = primary();

    while (true) {
      if (match(TokenType.LEFT_PAREN)) {
        expr = finishCall(expr);
      } else if (match(TokenType.DOT)) {
        Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
        expr = new Expr.Get(expr, name);
      } else {
        break;
      }
    }
    return expr;
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 argument.");
        }
        arguments.add(expression());
      } while (match(TokenType.COMMA));
    }
    Token paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments.");
    return new Expr.Call(callee, paren, arguments);
  }

  private Expr primary() {
    if (match(TokenType.SUPER)) {
      Token keyword = previous();
      consume(TokenType.DOT, "Expect '.' after 'super'.");
      Token method = consume(IDENTIFIER, "Expect superclass method name.");
      return new Expr.Super(keyword, method);
    }
    if (match(TokenType.FUN)) {
      return anonymousFunction();
    }
    if (match(THIS)) {
      return new Expr.This(previous());
    }
    if (match(FALSE))
      return new Expr.Literal(false);
    if (match(TRUE))
      return new Expr.Literal(true);
    if (match(NIL))
      return new Literal(null);

    if (match(NUMBER, STRING))
      return new Expr.Literal(previous().literal);

    if (match(TokenType.IDENTIFIER)) {
      return new Expr.Variable(previous());
    }
    // if found the left parenthesis we must find the right parentheses too.
    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      // Error -- Abort_1
      consume(RIGHT_PAREN, "Expect ')' after expression");
      return new Expr.Grouping(expr);
    }
    throw error(peek(), "Expect expression.");
  }

  private Token consume(TokenType type, String message) {
    if (check(type))
      return advance();
    throw error(peek(), message);
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private ParseError error(Token token, String message) {
    // Abort_2
    Raw.error(token, message);
    return new ParseError();
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) {
      return false;
    }
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd())
      current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  // Abort_2
  private void synchronize() {
    advance();
    while (!isAtEnd()) {
      if (previous().type == SEMICOLON)
        return;
      switch (peek().type) {
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
  private static class ParseError extends RuntimeException {

  }
}
