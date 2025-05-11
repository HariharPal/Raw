// Store the name of the class and the method defined in it
// It let us call the method of the class, check for inheritence and create instance of it.

/*
 * class Dog{
 *    bark(){
 *      print "Bark!!";
 *    }
 * }
 * */
package com.drunkncode.raw;

import java.util.List;
import java.util.Map;

class RawClass extends RawInstance implements RawCallable {
  final String name;
  private final Map<String, RawFunction> methods;
  final RawClass superClass;
  // For class storing the name of the class. and methods involved in it;

  RawClass(RawClass metaClass, RawClass superClass, String name, Map<String, RawFunction> methods) {
    super(metaClass); // inherit from metaclass
    this.name = name;
    this.methods = methods;
    this.superClass = superClass;
  }

  RawFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }
    if (superClass != null) {
      return superClass.findMethod(name);
    }
    return null;
  }

  // whenever
  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    RawInstance instance = new RawInstance(this);
    RawFunction initializer = findMethod("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }
    return instance;
  }

  @Override
  public int arity() {
    RawFunction initializer = findMethod("init");
    if (initializer != null) {
      return initializer.arity();
    }
    return 0;
  }

  @Override
  public String toString() {
    return name;
  }
}
