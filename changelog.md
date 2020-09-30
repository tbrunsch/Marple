# Changelog

## v0.2.1

- instance search now ignores iterables and lists static fields as part of classes instead of instances
- added a "more ..." node to the field tree to support displaying huge or infinite iterables
- display a tool tip with the full text of a tree node if its text is only displayed partially

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
  - changed package structure: all API classes are now in the package `dd.kms.marple.api`
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