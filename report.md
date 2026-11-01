# Maze Runner: Algorithm Comparison Report

## 1. Application Overview

**Maze Runner** is an Android educational game that demonstrates how different path-finding strategies solve the same maze. The player can:

- Generate and reset a maze.
- Run a brute-force search.
- Run a heuristic search.
- Compare path length, explored nodes, execution time, and path optimality.
- Browse several paths discovered by the brute-force search.
- Play the maze manually by moving the runner one adjacent tile at a time.
- Change between Easy, Medium, and Hard levels.

The application uses a 10 x 10 maze. The start position is the upper-left cell, `(0, 0)`, and the goal is the lower-right cell, `(9, 9)`. Walls, open cells, explored cells, algorithm paths, and the player's route are displayed with different colors.

The maze generator creates several guaranteed routes and then adds additional openings depending on the selected level. This ensures that the application normally has a solvable maze while still producing different route choices for the algorithms.

## 2. What Is Brute Force?

Brute force solves a problem by systematically trying all possible candidates, or as many candidates as the problem permits, without relying on a strong prediction about which choice is best.

In this application, brute force is implemented as a depth-first search (DFS):

1. Start at `(0, 0)`.
2. Visit an available neighboring cell.
3. Continue recursively until reaching the goal or reaching a dead end.
4. Backtrack when a route cannot continue.
5. Record routes that reach the goal.
6. Keep the shortest route found as the brute-force best path.

The implementation prevents cycles by marking cells as visited during the current route. It also prunes routes that are already at least as long as the best route found so far. The app displays up to five discovered routes for browsing, while still tracking the shortest route found.

### Why brute force can prove the best path

When the search explores every relevant simple route and compares their lengths, it can establish that no shorter route was missed. That is why brute force is treated as the optimal-path reference in the comparison table.

This guarantee comes at a cost: the number of possible routes can grow very quickly as the search space becomes larger or more open.

## 3. What Is a Heuristic?

A heuristic is an estimate used to guide a search toward a likely solution. Instead of treating all unexplored cells equally, the algorithm assigns priority to cells that appear closer to the goal.

Maze Runner uses **greedy best-first search** as its heuristic method. For a cell `(r, c)`, it calculates the Manhattan-distance estimate:

```text
h(r, c) = |9 - r| + |9 - c|
```

This estimate counts the horizontal and vertical steps that would be needed to reach `(9, 9)` if there were no walls. A priority queue always selects the available cell with the smallest estimate.

The heuristic search:

1. Starts at `(0, 0)`.
2. Places reachable neighboring cells into a priority queue.
3. Chooses the cell with the smallest Manhattan-distance estimate.
4. Stores each cell's parent so the final route can be reconstructed.
5. Stops when it reaches the goal.

The app animates the explored cells first and then draws the selected heuristic path. It also records an initial trace of the cells and heuristic values considered by the search.

> This implementation is greedy best-first search, not full A* search. Greedy best-first search uses only `h(n)`, while A* normally uses `f(n) = g(n) + h(n)` and accounts for both the distance already traveled and the estimated distance remaining.

## 4. Why the Heuristic Is Usually Better for Speed

The heuristic has useful directional information. It tends to focus on cells that are closer to the goal instead of exploring every possible branch.

This often results in:

- Fewer explored nodes.
- Less computation.
- Faster visible results.
- A clearer route toward the goal.
- Better scalability when the maze or search space becomes larger.

In the app's comparison table, this behavior is visible through the **Nodes Explored** and **Time (ms)** values. The verdict card also reports which method was faster and how many nodes each method explored.

## 5. Why Brute Force Can Sometimes Be Better

The heuristic's estimate is only an approximation. Manhattan distance assumes that movement can proceed directly toward the goal, but walls and detours can make that assumption misleading.

The heuristic can choose a route that looks promising because it is geographically close to the goal, even though that route later becomes blocked or requires a long detour. Since this implementation is greedy, it does not fully reconsider the cost already spent on a route.

Brute force can therefore be better when:

- The maze contains misleading branches.
- A direct-looking route is blocked farther ahead.
- The shortest route initially moves away from the goal.
- Several routes have similar heuristic values.
- The maze is small enough that exhaustive exploration is affordable.

In these cases, brute force may:

- Find a shorter path.
- Prove that the path is optimal.
- Explore more nodes.
- Take more time than the heuristic.

The app explicitly reports this situation. A typical result can be:

```text
Heuristic faster, but brute force found optimal
```

This is an important distinction: **fastest does not always mean shortest**, and **shortest does not always mean cheapest to discover**.

## 6. Advantages and Disadvantages

| Method | Advantages | Disadvantages |
|---|---|---|
| Brute force DFS | Can establish the shortest route; does not depend on a quality heuristic; useful as a correctness reference | Can explore many routes; may use more time and memory; scales poorly as the search space grows |
| Greedy best-first heuristic | Usually explores fewer nodes; often produces a result quickly; visually demonstrates goal-directed search; scales better on many practical searches | Does not guarantee the shortest route; can be misled by walls and detours; quality depends on the heuristic; may need to revisit the strategy in more advanced implementations |

### Brute force is preferable when:

- The search space is small.
- Correctness and an optimal answer are more important than speed.
- A trusted reference result is needed.
- The problem has no useful heuristic.

### A heuristic is preferable when:

- The search space is large.
- A good estimate of progress is available.
- A quick acceptable result is more valuable than a guaranteed optimum.
- The application needs to reduce unnecessary exploration.

## 7. How the Game Demonstrates the Concepts

Maze Runner turns an abstract algorithm comparison into an observable experiment.

### Visual exploration

The maze colors show what each method is doing:

- **Blue path:** a selected brute-force route or final algorithm path.
- **Yellow/orange explored cells:** cells examined during the heuristic search.
- **Purple route:** the player's manual path.
- **Green cell:** the start.
- **Red cell:** the goal.
- **Dark cells:** walls.

The animations make the difference in search behavior visible. Brute force appears to investigate routes through backtracking and can reveal multiple candidate paths. The heuristic focuses its exploration toward cells that look closer to the goal.

### Quantitative comparison

The comparison table records:

- **Path Length:** number of moves in the resulting route.
- **Nodes Explored:** how much of the maze the method examined.
- **Time (ms):** measured algorithm execution time.
- **Paths Found:** the number of brute-force paths retained for display.
- **Optimal?:** whether the route matches the brute-force shortest-path reference.

The verdict section combines these measurements and explains the trade-off in plain language.

### Interactive learning

The Play mode lets a user attempt the maze manually. The player must tap only adjacent open cells. At the end, the application compares the player's path with the brute-force optimal path and reports how many extra moves were used, if any.

This creates three levels of understanding:

1. **Algorithm behavior:** watch brute force and heuristic search.
2. **Measured performance:** compare time, nodes, and path length.
3. **Human decision-making:** attempt to solve the maze and compare the result with the algorithms.

The Previous and Next controls also allow users to inspect multiple brute-force routes rather than seeing only one final answer.

## 8. Application Design Details

- **Platform:** Android.
- **Application name:** Maze Runner.
- **Main screen:** maze, status message, level indicator, algorithm controls, and comparison table.
- **Maze size:** 10 rows by 10 columns.
- **Levels:** Easy, Medium, and Hard.
- **Search start:** `(0, 0)`.
- **Search goal:** `(9, 9)`.
- **Brute-force implementation:** recursive DFS with visited-cell tracking, pruning, shortest-path tracking, and up to five displayable paths.
- **Heuristic implementation:** greedy best-first search using Manhattan distance and a priority queue.
- **Player mode:** adjacent-cell movement with blocked-cell and invalid-move feedback.
- **Feedback:** animated exploration, status messages, path visualization, result colors, and a final verdict.

## 9. Important Limitations

The application is designed for learning rather than benchmarking production-grade path-finding systems.

1. The maze is small, so brute force is practical even though exhaustive search can become expensive on larger graphs.
2. The recorded execution time is affected by the Android device, runtime scheduling, and measurement overhead.
3. The heuristic is greedy best-first search, so it is not guaranteed to be optimal.
4. The brute-force display shows only a limited number of routes even though the search uses the routes it encounters to identify the best route.
5. The generated levels use fixed level-based random seeds, so each level follows a repeatable pattern rather than being fully random on every reset.
6. A stronger algorithmic comparison could add breadth-first search, Dijkstra's algorithm, or A* search.

## 10. Learning Outcomes

After using Maze Runner, a learner should understand that:

- Search algorithms can solve the same problem in very different ways.
- Exhaustive search is reliable but can be expensive.
- Heuristics improve focus and often reduce work.
- A heuristic is an estimate, not a guarantee.
- The shortest path, the fastest search, and the path with the fewest explored nodes are different measures.
- Algorithm selection depends on the problem, available information, and the required balance between optimality and performance.

## Conclusion

Maze Runner demonstrates the central trade-off between certainty and efficiency. Brute force provides a trustworthy optimal-path reference by exploring alternatives, while the heuristic method uses a goal-distance estimate to reach an answer with less exploration. The heuristic is often faster, but the maze can contain situations where its estimate is misleading and brute force finds a shorter route.

By combining animations, measurements, path browsing, and manual play, the application makes these trade-offs visible and interactive. It shows not only which algorithm wins on one maze, but also why the result changes when the structure of the problem changes.
