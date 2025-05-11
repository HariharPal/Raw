package com.drunkncode.raw;

class RuntimeError extends RuntimeException {
  final Token token;

  RuntimeError(Token token, String message) {
    super(message);
    this.token = token;
  }
}

class BreakError extends RuntimeException {
}

class ContinueError extends RuntimeException {

}
