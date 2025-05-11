package com.drunkncode.raw;

/**
 * Return
 * This java class is only used for control flow not for any real use of
 * throwing exceptions.
 * That's why we use super (null, null, false, false) to remove some features of
 * RuntimeException class.
 * We want this to unwiend all the way bact to where the function is called.
 */

class Return extends RuntimeException {
  final Object value;

  Return(Object value) {
    super(null, null, false, false);
    this.value = value;
  }

}
