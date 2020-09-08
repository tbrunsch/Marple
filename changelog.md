# Changelog

## v0.2.0

  - changed to Zenodot 0.2.0
  - components in hierarchy tree can now be selected without losing the subtree
  - iterables tab now supports converting iterables to other collections and arrays and creating maps
  - added maps tab with filtering and mapping operations for keys and values
  - fixed focus problems
  - fixed problem with infinitely waiting thread in instance search
  - order code completions intuitively
  - added changelog
  
API changes:
  - API changes of Zenodot 0.2.0
  - replaced unnamed slots `DebugSupport.SLOT_0`, `DebugSupport.SLOT_1`, etc. by array `DebugSupport.SLOTS`  

## v0.1.1

  - consider runtime type of evaluation context when evaluating expressions
  - consider all top-level classes when typing an unqualified class name 
  - display parse exceptions in separate panel
  - display input errors on instance search panel
  - store window locations instead of trying to find a suitable location
  - several performance improvements

## v0.1.0

First Marple release