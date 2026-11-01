# Data Model

## Persistence

No database, ORM, entity classes, migrations, or persisted domain model were found. The application keeps its maze and search state in activity/view memory only.

## In-memory model

- `maze`: `int[10][10]`; `1` represents a wall and `0` an open cell.
- `visited`: `boolean[10][10]`; temporary DFS recursion state.
- `explored`: `ArrayList<int[]>`; cells visited by greedy search and rendered yellow.
- `bestPath`: `ArrayList<int[]>`; reconstructed greedy route.
- `bruteBestPath`: `ArrayList<int[]>`; shortest path selected from collected DFS paths.
- `allPaths`: `ArrayList<ArrayList<int[]>>`; up to five complete DFS routes.
- `parentR`/`parentC`: `int[10][10]`; greedy-search parent coordinates used for route reconstruction.

## Relationships

There are no persisted entity relationships. `MainActivity` owns algorithm state and passes maze/overlay data to `MazeView`; the custom view owns render-only copies of maze, explored, and path cells.
