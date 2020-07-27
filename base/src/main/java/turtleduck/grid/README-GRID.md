# A new (improved?) version of the grid ADT

## Changes from the Cellular Automaton version

* If you look at `MyGrid.java`, you will notice that it no longer uses `IList<T>`/`MyList<T>` to hold the grid cells, and both `IList.java` and `MyList.java` have been removed. Instead we use standard built-in Java lists, with the `List<T>` interface (and `ArrayList<T>` as the class for new list objects). 
   * This saves us the work of maintaining and improving our own list, and makes it immediately compatible with useful standard Java library operations on lists. 
   * In general, you should always use built-in APIs (such as the Java Collections API which provides lists and similar) when available. Not only does it save time, but they're likely to be much better tested (and probably better designed) than you can do yourself, so they're less likely to have, e.g., stupid bugs that only show up in corner cases. You may also get better performance.
   * You might want to do things yourself if you want to learn how stuff works; also, in some cases, there may not be a suitable API available – or the API doesn't really fully fit with what we want to do. While Java has one-dimensional lists and arrays, it doesn't really have built-in classes for grids, so we're making our own.
   * An alternative to IGrid/MyGrid would be to use two-dimensional arrays – but these aren't *actually* two-dimensional arrays, but rather arrays-of-arrays, which makes them fit less well with the concept of a “grid” (the same goes for making a list of lists). In addition to being a bit inconvenient, they're also much less efficient in use – and they won't let us do useful things like “please give me the cell to the north of this cell” or “please give me all cells neighbouring this cell”.
   