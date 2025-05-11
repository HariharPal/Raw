// You're creating the objects form the class. Represents the individual objects.
// The RawClass it creates from 
// Its field or properties (name, age, etc..)
// It can loop up methods from the class 
//
// Allows the interpreter to keep track of each objects properties and how it behaves when method is called.

package com.drunkncode.raw;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

class RawInstance {
  private final Map<String, Object> fields = new HashMap<>();
  private RawClass cls;

  RawInstance(RawClass cls) {
    this.cls = cls;
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  // Checking if such named field or method contained in the rawInstance if yes
  // then return the fields of it otherwise raise the error.
  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }
    RawFunction method = cls.findMethod(name.lexeme);
    if (method != null)
      return method.bind(this);
    throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
  }

  @Override
  public String toString() {
    return cls.name + " instance.";
  }
}
