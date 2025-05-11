// Abort_1 Here the code part consists after we call the funciton by funciton.call(this.arguments); 
// means they'r providing the argumest of the fucntion and which creates the new Scope and it's a new Environement that inherits from the global scope. All the function local variable will live here.
// Now we're assigning the params of declaration will get provided into the arguments of the function (assigning)

// So main curks of this code is we're adding the function name to our current environement which might be in some block maybe but we're adding our function name scope to it. And when we run that function we're creating an new environement which currently have scope of the arguments we have and it took those argument and work on it which works when we call visitCallExpr which has been called when we found a function of same name and with same paramenters and it calls the call fucntion which resides in RawFunction which assign that environement when we execute the function it goest to the call method and then call method assign the variables to it after assigning the variable to the environemnt it send to executeBlock function which present in the interpreter used to call the inner block ;
package com.drunkncode.raw;

import java.util.List;

class RawFunction implements RawCallable {
  private final boolean isInitializer;
  private final Stmt.Function declaration;
  private final Environment closure;
  private final Expr.AnonymousFunction anonymous;

  RawFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
    this.closure = closure;
    this.anonymous = null;
    this.declaration = declaration;
    this.isInitializer = isInitializer;
  }

  RawFunction(Expr.AnonymousFunction anonymous, Environment closure, boolean isInitializer) {
    this.isInitializer = isInitializer;
    this.closure = closure;
    this.anonymous = anonymous;
    this.declaration = null;
  }

  RawFunction bind(RawInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new RawFunction(declaration, environment, isInitializer);
  }

  public boolean isGetter() {
    return declaration.params == null && anonymous.params == null;
  }

  // Abort_1
  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    // checking for closure environment or the function higher block instead of
    // straight looking for global variables
    Environment environment = new Environment(closure);
    List<Token> params = declaration != null ? declaration.params : anonymous.params;
    List<Stmt> body = declaration != null ? declaration.body : anonymous.body;

    // bind the paramenter to arguments
    if (params != null) {
      for (int i = 0; i < params.size(); i++) {
        environment.define(params.get(i).lexeme, arguments.get(i));
      }

    }
    // Runs the body of the function we wrap with Return exception because if error
    // occur we return the object body to say yo man the function here return some
    // exception oh btw it's not an exception it's what u need. Because we don't
    // have any way to go back to where the function is called ever;
    try {
      interpreter.executeBlock(body, environment);
    } catch (Return returnValue) {
      if (isInitializer)
        return closure.getAt(0, "this");
      return returnValue.value;
    }

    if (isInitializer) {
      return closure.getAt(0, "this");
    }
    return null;
  }

  @Override
  public int arity() {
    return (declaration != null ? declaration.params : anonymous.params).size();
  }

  @Override
  public String toString() {
    String name = declaration != null ? declaration.name.lexeme
        : (anonymous.name != null ? anonymous.name.lexeme : "anonymous");
    return "<fn " + name + ">";
  }
}
