package com.drunkncode.raw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Raw {

  // Raw is a scripting language it executes directly from source. Our command
  // line contains two way
  // of running code
  // from command line giving path to a file which then it reads the file and
  // executes it;
  // and from line by line doing code in interactive way u can prompt and enter
  // code and execute
  // code one line at a time;

  private static final Interpreter interpreter = new Interpreter();

  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jRaw [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    // getting each bytes from file from the path and running those by run function

    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    if (hadError) {
      System.exit(65);
    }
    if (hadRuntimeError) {
      System.exit(70);
    }
  }

  // read line from user from the command line and return result accordingly
  // to kill interactive commandlline u just type Ctrl+D
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);
    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null || line.trim().equalsIgnoreCase("exit")) {
        break;
      }
      run(line);
      hadError = false;
    }
  }

  private static void run(String source) {
    Scanner sc = new Scanner(source);
    List<Token> tkns = sc.scanTokens();
    Parser parser = new Parser(tkns);
    List<Stmt> statements = parser.parse();
    Resolver resolver = new Resolver(interpreter);
    resolver.resolve(statements);
    if (hadError) {
      return;
    }
    if (statements.size() == 1 && statements.get(0) instanceof Stmt.Expression) {
      Expr expr = ((Stmt.Expression) statements.get(0)).expression;
      Object result = interpreter.evaluate(expr);
      if (result != null) {
        System.out.println(interpreter.stringify(result));
      }
    } else {
      interpreter.interpret(statements);
    }

  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  public String getGreeting() {
    return "HElloWOrld";
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error " + where + " : " + message);
    // reporting main fuunction that it's had an error
    hadError = true;
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end ", message);
    } else {
      report(token.line, " at " + token.lexeme + "'", message);
    }
  }

  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }

  public static void warning(Token token, String message) {
    System.out.println("[Line " + token.line + " ] Warning at '" + token.lexeme + "': " + message);
  }
}
