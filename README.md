# Zenodot
Zenodot is a Java library for parsing Java expressions. Notable features are:

  - Name and type based code completion
  - Optional dynamically typed expression evaluation
  - Parsing of custom variables
  - Parsing of individual hierarchies that are not reflected by regular Java syntax

# Target
Zenodot has been developed to complement the traditional IDE-based debugging. The traditional debugging steps are as follows:

  1. Set a break point at an appropriate position in the source code
  1. Define a condition for that break point to reduce the number of irrelevant stops
  1. Trigger an event in the application to make the debugger stop at that break point
  1. Evaluate an expression in the desired context

While this kind of debugging is very powerful with a modern IDE, it can be a bit frustrating to perform all these steps just to recover the object in the debugging process one has already found in the application.

When an application integrates the Zenodot library, then an alternative workflow will look as follows:

  1. Find the object in the application
  1. Open the UI that is linked with the library
  1. Enter the expression you want to evaluate

Note that you do not have to switch to your IDE at all. In particular, you can (to some extend) debug your application without an IDE.

# When not to use Zenodot

You should not use Zenodot if at least one of the following applies to you:

  - You need a full-blown Java parser that can parse whole code fragments.
  - You want expressions to be performed as fast as possible.
  - You are worried about security in general and code injection in particular.
  - You do not trust a parser library written by someone inexperienced in compiler construction.

# Features and Short Comings

Instead of listing all things that work as expected we will highlight positive and negative points that deviate from the expectations of a regular Java parser.

## Code Completion

If you only want to execute Java code (or at least something similar), then you can use [Groovy](http://www.groovy-lang.org/). It is much more powerful and probably also much more reliable. However, if you need code completion when writing a single expression, then Zenodot might be the right choice for you. Zenodot provides suggestions for packages, classes, methods, and fields. The suggestions are sorted according to a rating function that takes names and types into account.

## Dynamic Typing

When inspecting the internal state of an object returned by a method, you sometimes have to cast it to its implementation class to be able to access its methods because they are not published via the regular interface. To avoid such casts, you can activate dynamic typing. If this option is selected, then both, code completion and expression evaluation, ignore the declared type and use the runtime time of an object instead. Although this can be handy in some cases, you should be aware of the risks. If you call a method with side effects, this side effect will also be triggered when requesting a code completion or when evalutating an expression with a syntax error. Furthermore, method overloads can be resolved differently with static and dynamic typing.

## Custom Variables

Zenodot allows you to declare variables that can be set and accessed in expressions. This can save you some typing when repeatedly evalutating expressions in a certain context.

## Custom Hierarchies

One of the most interesting features of Zenodot is its support of custom hierarchies. If an application holds a dynamically created tree, then this tree can, of course, be traversed with an arbitrary Java parser. However, in many cases the traversed nodes are only represented by generic node classes. Only in rare cases there will be one node class for each node. Consequently, when traversing such a tree, you will have to call generic methods like `getChild(childIndex)` or something similar. This is different than what you see in your application where every node is displayed with its individual name. When restricted to Java syntax, you can not hope for meaningful code completion here. You have to deal with child indices instead.

Zenodot extends the Java syntax to allow for parsing a custom hierarchy. The only thing the application developer has to do is to specify his tree in a form Zenodot understands by implementing a certain interface. A user can then traverse that tree using the node names.

**Example:** Assume that you have a document viewer application that has loaded this document. It might store the content in a hierarchy with sections on the first level, subsections on the second level, and so one. Let us assume that we want to evaluate the object behind this section, i.e., the section "Features and Short Comings" -> "Custom Hierarchies". A classic approach would be to call `getSection(3).getSubsection(3)`. As you can imagine, handling the indices, which is only a technical detail, will be come troublesome in large trees. However, if you have configured Zenodot correctly, you can also write `{Features and Short Comings#Custom Hierarchies}` to reference the same node in your tree. This is much more readable and less prone to errors. (Also note the spaces inside the node names.) Furthermore, you can, e.g., request code completion after typing `{Features and Short Comings#Custom`. The result will be `Custom Variables` and `Custom Hierarchies`.

## Generics

The generic handling is based on the [Reflection API](https://github.com/google/guava/wiki/ReflectionExplained) of [Google Guava](https://github.com/google/guava). Due to type erasure, it is not possible to determine the parameters of parameterized types at runtime in general. However, it is possible to determine parameters when the declared type is known. If, e.g., it is known that an object is returned by a method that returns a `Collection<Integer>`, then the parameter `Integer` will be preserved. If the runtime type of the object is `ArrayList`, then Zenodot can even conclude that the actual type is `ArrayList<Integer>`.

However, there are some things that Zenodot can currently not do:

  1. You can only cast objects to raw types, not parameterized types. A cast `(List)` is perfectly fine, but a cast `(List<Integer)` will not work.
  1. You cannot call a method with a raw type if it expects a parameterized type. This is particularly unpleasent since you cannot cast to a parameterized type.
  1. Zenodot cannot deduce types, so you cannot call parameterized constructors or methods whose types partially depend on the types of the arguments. For instance, you cannot call `Arrays.asList(1, 2, 3)` as Zenodot cannot deduce that it should substitute the parameter `T` of that method for `Integer`.

## Operators

Zenodot implements most but not all unary and binary operators. The following operators are currently not supported:

  - Postfix increment (`++`) and decrement (`--`)
  - `instanceof`
  - ternary operator `? :`
  - the operators `+=`, `-=`, `*=`, `/=`, `%=`, `<<=`, `>>=`, `>>>=`, `&=`, `^=`, and `|=`