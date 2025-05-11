// REPL stands for Read Eval Print Loop
package com.drunkncode.raw;

import java.util.HashMap;
import java.util.Map;

class Environment {
  private static final Object UNINITIALIZED = new Object();
  final Environment enclosing;

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  // keep track of object creation while REPL
  public final Map<String, Object> values = new HashMap<>();

  void define(String name, Object value) {
    values.put(name, value != null ? value : UNINITIALIZED);
  }

  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }

  // getAt returns the value of the variable in that environments map.
  Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }

  Environment ancestor(int distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }
    return environment;
  }

  // getting the object if exists in the Environment
  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return checkUninitialized(values.get(name.lexeme), name);
    }
    if (enclosing != null) {
      return enclosing.get(name);
    }
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  public Object checkUninitialized(Object value, Token name) {
    if (value == UNINITIALIZED) {
      throw new RuntimeError(name, "Variable '" + name.lexeme + "' is not initialized.");
    } else {
      return value;
    }
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }
}
