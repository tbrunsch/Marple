# Marple
Marple is a Swing-based Java library for analyzing Swing applications. Notable features are:

  - Analysis of hierarchical structure of Swing components including field search
  - Component, image, and color previews when analyzing fields
  - Highlighting of components referenced by fields
  - Analysis support for Iterables and arrays
  - Expression evaluation in the context of selected components
  - An instance-based search
  - Support for data exchange with debuggers

# Target
Marple has been developed for two reasons:

  1. To analyze the UI structure of Swing applications. One aspect is the analysis of the component hierarchy described by the parent-child relationship between Swing components. The second aspect is analysing how the hierarchy is represented by the code. For this, Marple scans fields values of components in the hierarchy and matches them against other components. By doing so, anonymous object references in the JVM are mapped to named fields in the source code. This can help developers understanding better how the UI code matches the resulting visualization. 

  1. For some applications, the UI is a good entry point for debugging. This is especially the case if there is a connection between the Swing components and core classes via object references. Marple provides code evaluation facilities via the *Zenodot* library, which allows developers to evaluate code in the context of a selected Swing component.

# Default Shortcuts

All shortcuts can be configured. Unless changed, the shortcuts are as follows:

|Shortcut|Action|
|---|---|
|`Ctrl + F1`|Quick Help dialog showing the actual shortcuts|
|`Ctrl + Shift + I`|Inspection dialog|
|`Alt + F8`|Expression Evaluation dialog|
|`Ctrl + Shift + F`|Find Instance dialog|
|`Ctrl + Shift + D`|Debug Support dialog|
|`Ctrl + Space`|Suggest code completions for expressions|
|`Ctrl + P`|Show method argument types|

# Features

## Hierarchical Structure Analysis

To start the hierarchical structure analysis, hover over the component you want to analyze and then press the inspection shortcut. The default shortcut is `Ctrl + Shift + I`. A dialog pops up that shows a linear tree from the Swing root component to the component under the mouse.

Note that for some components even subcomponents which are no regular Swing components (e.g., TreeNodes) are supported. Marple can also be configured to detect custom subcomponents.

The node names in the tree contain the class names of the components. Sometimes, there will be additional information embraced in parentheses. These are fields of classes that currently point to the component the node represents. That way, you do not only obtain information about the hierarchical Swing component structure, but also about the code that generates that structure.

There are at least two more tabs for analyzing the components: the "Fields" tab and the "Methods" tab. Clearly, these tabs list the available fields and methods of the selected component.

Starting from a component, you explore related instances by clicking on the component hierarchy tree nodes, the fields or methods without parameters. When you do so, the tabs are updated showing the information for the instance you requested. You can use the "Back" and "Forward" to navigate in the history of requested instances. The history will be cleared when the inspection dialog gets closed.

When you select an Iterable instance or an array, there will be an additional "Iterables" tab described in another section. 

## Component, Image, and Color Previews

To create a preview of a component, an image, or a color, right-click on the item in the inspection dialog and select the action "Snapshot". A new dialog will open with a rendered image of the selected instance. You can save that image or copy it to the clipboard.

Another handy feature is the "live preview". When enabled, you do not have to select the "Snapshot" action explicitly. The snapshot dialog will be updated automatically when hovering over an appropriate item in the inspection dialog.  

## Highlighting of Components

To highlight a component in the UI, right-click on the item in the inspection dialog and select "Highlight". The component, if currently visible, will flash multiple times. This feature complements the hierarchical structure analysis: In the structure analysis you find a field for a selected component. Here, you find the component for a selected field.

Note, however, that you should not highlight a component that is already being highlighted. Otherwise, the original state of the component will not be restored correctly after highlighting it. 

## Analysis Support for Iterables and Arrays

The "Iterables" tab in the inspection dialog offers some operations for Iterables and arrays (in the remainder, we use the term "Iterables" for both): Filter, Map, and ForEach.

### Lambdas

For all three operations you have to specify a lambda. However, you do not use Java lambda notation for this, but write an expression or a statement expressed in `this`. In this context, the keyword `this` refers to each element in the Iterable, one after the other.

Examples:

1. A predicate that checks whether an element is at least 0.5 is written as `this >= 0.5` (instead of `x -> x >= 0.5`).
1. A function that maps an element to its String representation is written as `this.toString()` (instead of `x -> x.toString()` or `Object::toString`). Note that you can omit `this` in this case an simply write `toString()`.
1. A consumer that prints an element to the console is written as `System.out.println(this)` (instead of `x -> System.out.println(x)` or `System.out::println`).

### Filter

You can specify an arbitrary predicate that is applicable to the elements of the Iterable. The Iterable is then filtered with this predicate and the result is displayed at the bottom.

The output (if any) can either be a list or an index map. If you are just interested in the filtered elements, then you should choose a list. If you are interested in the original indices of the filtered elements, then you have to select the index map.  

Note that we decided to keep the number of operations at a minimum. Many other common operations can be solved via a filter:

1. `contains`, `anyMatch`, `noneMatch`: Filter with the predicate and check whether the output is empty or not.
1. `allMatch`: Filter with the negated predicate and check whether the output is empty or not.
1. `find`/`indexOf`: Select as output type an index map and filter with the predicate.    

### Map

You can specify an arbitrary (unary) function that is applicable to the elements of the Iterable. All elements will be mapped by this function and collected into a list.

### ForEach

You can specify an arbitrary consumer that is applicable to the elements of the Iterable. The consumer will subsequently be called for all elements.

### Stream-like Analysis

Streams are not supported by the expression parser. However, it is possible to simulate subsequent streaming operations. The result of an operation is displayed at the bottom of the dialog. If it is a list (you will not use an index map if you want to do streaming) and you click on that list, then you are inspecting that list now, meaning that you are still in the "Iterables" tab, but now in the context of that new list. Hence, you can apply the next streaming operation.

Note that with that workflow you benefit from the inspection history, which you can navigate in using the "Back" and "Forward" buttons.

## Expression Evaluation

Expressions are always evaluated in a certain context, given by the keyword `this`. There are two ways to specify what `this` refers to:

1. You can hover over a Swing component and press the evaluation shortcut. The default shortcut is `Alt + F8`. The evaluation dialog becomes visible with the component as context, i.e., `this` refers to that component.
1. You can navigate from instance to instance in the inspection dialog and then right-click on an instance you want to evaluate an expression in. When you then select "Evaluate" in the context menu, the evaluation dialog opens with that instance as context.  

In the evaluation dialog you can change some settings for the expression evaluation by clicking on the "..." button. The most notable are:

1. Dynamic Typing
1. Imports
1. Variables
1. Custom Hierarchy

### Dynamic Typing

Without dynamic typing expressions are evaluated based on declared types. You can only call methods and access fields that the declared types provide. When you need to analyze the runtime type, then you have to cast the instance to that type.

With dynamic typing, expressions are evaluated based on runtime types. That saves you unnecessary casts, but forces the evaluation framework to evaluate subexpressions to determine the runtime type. In particular, methods will be executed during code completion. This can be critical if the method has side effects or its evaluation is expensive. This is why dynamic typing is not activated by default. 

### Imports

When you specify imports, then you do not have to fully qualify classes in the specified package or specified classes. Imports can be preconfigured via the API and can be changed by the user at runtime. 

### Variables

You can introduce variables that can be access from within expressions. Variables can be preconfigured via the API and changed at runtime by the user. There are several ways to manage variables:

1. When right-clicking on an item in the inspection dialog, you can select the item "Add to variables" to add the selected instance to the pool of variables. You can also delete or rename variables in the variables dialog that pops up.
1. You can manage variables in the settings dialog for expressions. Just click the "..." button and go to the "Variables" tab.
1. In the "Variables" tab you can also load variables from the debug support by clicking on the "Import from 'Debug Support'" button. This will load all named slots. For more information see Section "Data Exchange With Debuggers".

Note that you have to specify for each variable if the framework should reference it via a hard or a weak reference. Weak references are the default because in most cases the application that you are debugging will hold a reference to the instances you are analyzing until the end of your analysis, but you do not want to unnecessarily prolong the lifetime of instances until you remove the variable from the pool again. 

### Custom Hierarchy

Some applications hold some of their data in a tree. Since the classic Java syntax is class-based because it has to, it does not distinguish between different instances of the same class. In a typical API, accessing the 10th child of a node is done via a cryptic call like node.getChild(9) or something similar. From Java's point of view, the 10th node is nothing special but only one of many nodes. For a user, this more or less anonymous access is unsatisfying because in most cases the nodes have individual names that are displayed to him. This can be, e.g., file names in a directory. Accessing the files only via index is as intuitive as labeling the files in a directory just "file 0", "file 1", etc.

The concept of a custom hierarchy overcomes this anonymity of nodes by preconfiguring Marple with a custom hierarchy. The custom hierarchy specifies which node has which children by specifying the available names. Additionally, each node may carries an object it represents. The objects are the data that is organized in a tree-structure. The structure is only there for classification and navigation.

Note that the custom hierarchy configuration cannot be changed at runtime, but only at compile time. However, nodes are queried on demand, so the configured hierarchy can be dynamic and change over time, but according to a fixed logic. 

To access the data object of a certain node in the tree, we had to extend the Java syntax. Let us consider again the example above where the custom hierarchy represents the file structure. The node names are the file or directory names and the data behind a node is the `File` instance that points to that file. The syntax for accessing the the `File` instance corresponding the the file "Marple/doc/README.md" is as follows:

    {Marple#doc#README.md}

Hence, with the custom hierarchy configured as in this example, you could reference arbitrary files within an expression using that syntax plus you get code completion, i.e., suggestions for all available files in a directory.

This example is, of course, not very interesting. It is much easier to use your favorite file browser to navigate to the file you are interested in, copy its file path and then call the constructor `new File("Marple/doc/README.md")`. But this alternative does not exist for application-specific data that is organized in a custom tree structure. 

## Instance-based Search

Marple allows to find paths from one instance to another instance or all instances of a certain class satisfying a specified search criterion. If the field of an instance `x` references and instance `y`, then `(x, y)` is considered a (directed) edge in the search graph from node `x` to node `y`. The search can be used to analyze memory leaks or to track where certain parameters given to a method are internally stored.

There are two ways to open the search dialog:

1. You can hover over a Swing component and press the instance search shortcut. The default shortcut is `Ctrl + Shift + F`. The search dialog becomes visible with the component as root of the search.
1. You can navigate from instance to instance in the inspection dialog and then right-click on an instance you want to start your search or to search for. When you then select "Search instances from here" in the context menu, the search dialog opens with that instance as root of the search. When you select "Search this instance" instead, then the dialog also opens, but with that instance as target of the search.

## Data Exchange With Debuggers

The in-application debugging capabilities are a nice feature, but they are negligible compared to what real debuggers are capable of. Having that said, Marple is still valuable for your debugging process because it gives you good entry points via the GUI components of your application. Data exchange between Marple and the debugger is done via the Debug Support dialog, which can be opened in one of the following ways:

1. You can hover over a Swing component and press the Debug Support shortcut. The default shortcut is `Ctrl + Shift + D`. The Debug Support dialog becomes visible with the component as the context, i.e., when entering expressions, the keyword `this` refers to that component.
1. You can navigate from instance to instance in the inspection dialog and then right-click on an instance you want to have as context for the Debug Support dialog. When you then select "Debug Support" in the context menu, then the Debug Support dialog opens with that instance as context.

In the remainder of this section we refer to the class `DebugSupport` in the package `dd.kms.marple`.

### Unnamed Slots

The class `DebugSupport` contains 5 static fields `SLOT_0`, ..., `SLOT_4`, which we call "unnamed slots".

Unnamed slots are easy to access from a debugger (just type, e.g., `DebugSupport.SLOT_0`) and useful if you want to transfer a certain instance to the debugger. Of course you cannot transfer more than 5 instances at a time and it can become confusing if you are using multiple unnamed slots because of their generic names.

### Named Slots

As the term suggests, named slots allow you to assign instances a name under which they may be looked up. To access a slot with the name `"myCustomSlot"`, call `DebugSupport.getSlotValue("myCustomSlot")` in the debugger. You can also set the value of a named slot from within the debugger, which allows transferring data from the debugger to Marple. The available methods are listed in the following table:

|Method|Description|
|---|---|
|`Collection<String> getSlotNames()`|Returns the names of all named slots currently available.|
|`Object getSlotValue(String slotName)`|Returns the value of the specified named slot.|
|`boolean setSlotValue(String slotName, Object value)`|Sets the specified value to the specified named slot (possibly creating it if it does not exist) if the slot name is valid. Returns true iff the slot name was valid.|
|`boolean renameSlot(String oldSlotName, String newSlotName)`|Renames the specified named slot and returns true if the renaming was successful.|
|`void clearNamedSlots()`|Clears all named slots.|
|`void deleteSlot(String slotName)`|Deletes the named slot with the specified name.|

Named slots are less convenient to use than unnamed slots, but you can use expressive names, which can be helpful if you have to transfer multiple instances between Marple and the debugger.

### Importing and Exporting Variables

Variables and named slots are very similar, but they have a different scope: Variables are a concept of Zenodot, the library used by Marple to evaluate expressions, whereas named slots are a concept introduced in Marple to exchange data with a debugger.

Nevertheless, they share enough similarity that Marple provides a way to import/export variables to/from named slots. You can do so in the Variables panel, which you can get to from the Debug Support dialog by clicking the "Open variable dialog" button or as described in the section "Variables". The export feature is an easy way to share all variables you introduced in Marple with the debugger.

### Breakpoint Trigger

In order to start the debugging process, you need the debugger to jump in. You can pause the whole application to stop in the middle of nowhere. This works perfectly fine, but for some reason I do not like the unpredictability of that approach. Another approach would be to set a breakpoint somewhere and try to trigger it. Unfortunately, for the second part you need detailed information about the programs workflow in the current context. This is the motivation for the breakpoint trigger feature.

In the Debug Support dialog, you can specify a method that is called when clicking the "Trigger" button. You can choose between a (at compile tme) preconfigured method and a custom method. Having decided for a trigger method, you just have to set a breakpoint there, click the "Trigger" button and the debugger becomes active.

# Generic Type Tracking with ObjectInfo

The expression evaluation library Zenodot operates on a utility class called `ObjectInfo` instead of `Object`. This class allows, to some extend, keeping track of generic types, which improves code completions (if all type variables are resolved). Essentially, an instance of `ObjectInfo` does not only contain the object itself, but also its declared type, i.e., `ObjectInfo` = object + declared type. Since Marple is based on Zenodot, is also uses this class. This technicality should be transparent to the user. However, when you configure Marple, you must be aware of this. If you just don't care about the declared type, you can work with pure `Object` instances and perform one of the following transformations when interacting with the Marple API:

* `ObjectInfo.getObject()` returns the `Object` instance wrapped by the `ObjectInfo` instance.
* `InfoProvider.createObjectInfo(Object)` wraps an `Object` instance in an `ObjectInfo` instance (without declared type information).

# Setup and Configuration

To setup Marple, you just have to configure an `InspectionSettings` instance and call

    ObjectInspectionFramework.register(inspectionSettings)

Inspection settings can be build by an `InspectionSettingsBuilder`. You can get a preconfigured builder by calling

    ObjectInspectionFramework.createInspectionSettingsBuilder()
    
Of course you can also configure unnamed and named slots of the `DebugSupport`, but in most cases it will be easier to preconfigure variables available for expression evaluation.

In the remainder of this section we discuss some of the available settings.

## Component Hierarchy Model

Marple lets you analyse, among others, the hierarchy of Swing components. However, not every graphic element is a full-blown Swing component. We call such elements "subcomponents". It makes sense to see subcomponents in the hierarchy as well. By default, Marple supports the following subcomponents:

* nodes in a `JTree`
* items in a `JList`
* cells in a `JTable`

Subcomponents might also be structured hierarchically. These structures are described by the interface `ComponentHierarchyModel`. Instances of `ComponentHierarchyModel` are created via the `ComponentHierarchyModelBuilder`. There are different ways to create a builder instance:

1. The method `ComponentHierarchyModels.createBuilder()` creates an empty builder.
1. The method `ObjectInspectionFramework.createComponentHierarchyModelBuilder()` creates a builder with the default settings used by Marple.

The builder allows you to add `SubcomponentHierarchyStrategy` instances that return a list of subcomponents for a given component and a given point inside the component. Since in many cases there will be at most one subcomponent and no hierarchy of subcomponents, there is a utility method `ComponentHierarchyModels.createSingleSubcomponentStrategy` that simplifies the creation of such strategies a bit.

## Visual Settings

The interface `VisualSettings` describes how to display objects as text and which tabs are available in the inspection dialog for which object. You can create an instance of `VisualSettings` via the `VisualSettingsBuilder`. There are different ways to create a `VisualSettingsBuilder`:

1. The method `VisualSettingsUtils.createBuilder()` creates an empty builder.
1. The method `ObjectInspectionFramework.createVisualSettingsBuilder()` creates a builder with the default settings used by Marple.
1. You can create an empty builder and call `VisualSettingsUtils.addDefaultDisplayTextFunctions(builder)` or `VisualSettingsUtils.addDefaultViews(builder)` to just add the Marple default of only one of the two aspects of `VisualSettings`. 

The `VisualSettingsBuilder` provides 3 methods:

1. The method `nullDisplayText` lets you specify a String that is used to display the value `null`.

1. The method `displayText` lets you specify how to display objects of a certain class. Marple currently defines special logic to display instances of the following classes:  `String`, `char`, `Character`, `Object`, `Frame`, `AbstractButton`, `JLabel`, and `JTextComponent`. A sample usage of the method is `builder.displayText(char.class, c -> "'" + c.getObject() + "'")`. 

1. The method `objectView` lets you specify constructors/factory methods for `ObjectView` instances for a certain type of object. Each `ObjectView` describes one tab in the inspection dialog, so the available tabs depend on the inspected object. Note that the class only serves as a first filter for deciding which tabs to create and which not. The factory method you provide may return `null` if it decides that there should be no tab for that object although the class is as expected. A sample usage of the method is `builder.objectView(Object.class, VisualSettingsUtils::createIterableView)`. Note that the factory method `VisualSettingsUtils::createIterableView` is registered for `Object`, but the Iterables tab is not available for objects. As explained before, this is due to the fact that the factory method checks the concrete instance whether it is an `Iterable` or something semantically similar.

## Security Settings

If you decide to ship Marple with your application, then you might want to prevent your customers from analyzing your application with Marple. To do so, you can subclass the interface `SecuritySettings` to protect the Marple access with a password prompt. Marple does neither restrict the way the password prompt is implemented nor does it provide utilities for implementing it. 

## Debug Settings

The interface `DebugSettings` currently only contains information about the configurable breakpoint trigger. You have to subclass it in order to specify a custom breakpoint trigger.  

## Key Settings

The interface `KeySettings` contains all information about configurable shortcuts. You can create an instance of that interface by a `KeySettingsBuilder`. The method `KeySettingsBuilders.create()` creates such a builder, preconfigured with the default shortcuts.

## Parser Settings

There are some settings that control how expressions are evaluated. These settings can be changed by the user at runtime, but in most cases it is helpful to preconfigure them appropriately. You can set the parser settings at an instance of `InspectionSettings` as follows:

    inspectionSettings.getEvaluator().setParserSettings(parserSettings)

A `ParserSettings` instance is created via a `ParserSettingsBuilder` which you can obtain by the call `ParserSettingsUtils.createBuilder()`. The builder allows you, among others,

* to specify the imports (classes and packages),
* to specify the minimum access level (`private`, `package private`, `protected`, or `public`) that can be accessed from within expressions
* to enable/disable dynamic typing
* to predefine variables, which you can create via `ParserSettingsUtils.createVariable`
* to specify a custom hierarchy   

See Section "Expression Evaluation" for more details on these features.

# Open Source License Acknowledgement

Marple utilizes the following open source projects:
 
## AutoComplete
https://github.com/bobbylight/autocomplete

Copyright (c) 2012, Robert Futrell. 

AutoComplete is licensed under the [Modified BSD license](https://opensource.org/licenses/BSD-3-Clause).
 
## Guava: Google Core Libraries for Java
https://github.com/google/guava

Guava is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Zenodot
https://github.com/tbrunsch/Zenodot

Zenodot is licensed under the [MIT License](https://opensource.org/licenses/MIT).
