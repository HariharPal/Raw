package com.drunkncode.raw;

import java.util.List;

interface RawCallable {
  int arity();

  Object call(Interpreter interpreter, List<Object> arguments);
}
