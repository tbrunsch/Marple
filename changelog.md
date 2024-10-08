# Changelog

## v0.5.1

Bug fixes:

  - Additional settings had not been applied if no preference file existed.

## v0.5.0

  - switched to Zenodot 0.4 (support for parser extensions, code completions for inner classes)
  - applying code completions for non-imported classes imports these classes temporarily instead of inserting the fully qualified class name
  - added possibility to switch between the plain object view and `List`, `Iterable`, and `Map` views (if available)
  - added possibility to specify custom `List`, `Iterable`, and `Map` views for certain objects via `VisualSettingsBuilder.listView()`, `VisualSettingsBuilder.iterableView()`, and `VisualSettingsBuilder.mapView()`
  - Marple can now be extended by additional evaluation settings via `EvaluationSettingsBuilder.setAdditionalSettings()`
  - The expression history is not cleared anymore when the evaluation dialog is closed.
  - Marple now distinguished between the minimum access modifier for fields and the minimum access modifier for methods (and constructors).
  - added Java 11 and Java 17 support

Behavioral changes:
  - Marple does not inherently provide `List` views for Apache primitive lists anymore because they can now be specified by the API user if required.
  - The "Inspect Object" and "Inspect Component" actions are not always the default action (the action that is performed when clicking onto a navigable element) anymore, but only in the "Inspection Dialog". In the "Evaluation Dialog", the "Evaluate Expression" action is now the default action.
  - When choosing the "Evaluate Expression" action, then the evaluation dialog is not only opened with the expression as context, but the expression is now also evaluated automatically.
  - Marple does not store its preferences in the specified preferences file anymore, but in a similar file. The reason is that different Marple versions that use different preferences file formats must write into different preferences files.

API changes:

  - `PreferenceUtils.writeSettings()` now expects an `InspectionContext` parameter instead of an `InspectionSettings` parameter

## v0.4.0

  - switched to Zenodot 0.3 (lambda support, new evaluation mode "mixed typing" that is similar to dynamic typing, but without side effects, accessing the field "length" of arrays)
  - made new evaluation mode "mixed typing" the default evaluation mode
  - removed Marple workaround for specifying lambdas
  - improved display of lists
  - added possibility to provide an initial expression and to suggest related objects when evaluating certain objects
  - added expression evaluation history
  - added possibility to change variable values via expressions
  - added method `ObjectInspectionFramework.preloadClasses()` that allows loading classes before they are needed
  - added "Custom Action" panel that allows defining actions for certain classes and an optional keyboard shortcut; removed breakpoint trigger facility from "Debug Support" panel as this can now be realized via a custom action
  - added possibility to specify a preferences file via `InspectionSettingsBuilder.preferencesFile()`; if specified, then mutable parts of the `InspectionSettings` will be stored there and loaded when next session is started 

API changes:

  - several changes resulting from API changes of Zenodot, most notably replacement of wrapper class `ObjectInfo` by `Object`
  - Variables are not part of the Zenodot configuration stored in `ParserSettings` anymore, but part of Marple's `ExpressionEvaluator`. You can now specify their type and whether they are `final`. If they are not final, then you can change their value within expressions.
  - `ObjectView`s are now `Disposable`, so you have to implement `dispose()` when you provide such a view. This method is called when the inspection dialog is closed and gives you a chance to release certain data.
  - There are no `DebugSettings` for specifying a breakpoint trigger command anymore. Instead, you can specify arbitrary `CustomAction`s as part of the `CustomActionSettings`.
  - `KeySettingsBuilder` not only contains a single parameterized setter `key` instead of one key setter for each function.

## v0.3.1

  - operate on runtime types instead of declared types whenever possible
  - fixed bug that prevented treating custom implementations of Apache's primitive lists as lists  

## v0.3.0

  - iterables tab now provides additional operations "count" and "group"
  - inspection history now also stores the view settings, which will be restored when navigating the history
  - matching nodes in the search dialog are now highlighted

API changes:
  - `ObjectView.applyViewSettings()` now gets an additional argument that describes whether the view settings have been taken for the current object or for a different object.

## v0.2.1

  - changed to Zenodot 0.2.1
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