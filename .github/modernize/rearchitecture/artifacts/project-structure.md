# Project Structure

## Classification

This is a single-module native Android application, classified as a frontend/mobile UI project. It is not a backend, library, CLI, or monorepo.

## Modules and layers

- `app/`: the only Gradle application module.
- Presentation layer: `MainActivity`, `activity_main.xml`, and `MazeView`.
- Algorithm/domain logic: maze generation, bounded brute-force DFS, and greedy best-first search are implemented directly in `MainActivity`.
- Resource/configuration layer: Android manifest, XML layouts, themes, colors, strings, launcher assets, and backup rules.
- Test layer: one JVM unit-test class and one Android instrumentation-test class.

## Entry points

- `app/src/main/AndroidManifest.xml:15` declares `MainActivity`.
- `app/src/main/AndroidManifest.xml:18-22` marks it as the exported `MAIN`/`LAUNCHER` activity.
- `MainActivity.onCreate` inflates `activity_main`, wires button listeners, and generates the initial maze.
- `activity_main.xml:44` embeds `MazeView` as the custom rendering surface.

## Functional areas

- Maze generation and level selection.
- Brute-force DFS path enumeration with a five-path and 5,000-node cap.
- Greedy best-first search using Manhattan distance to the fixed goal.
- Maze visualization with start, goal, wall, explored, and selected-path states.
- Path navigation and comparative algorithm statistics.

## File inventory

Source files under `app/src/main/java`: 2 Java files, both in `com.example.myapplication`.

- `MainActivity.java`: screen orchestration, state, maze generation, both search algorithms, and statistics.
- `MazeView.java`: custom Canvas renderer and overlay state.

Main resources include 1 layout, 2 values XML sets (`values` and `values-night`), 2 XML backup/configuration files, and launcher assets in drawable/mipmap directories.

## Key configuration files

- `settings.gradle`: root name, repositories, Foojay toolchain resolver, and `:app` inclusion.
- `build.gradle`: shared Android application plugin alias.
- `app/build.gradle`: Android namespace/SDK levels, Java 11 compatibility, and dependencies.
- `gradle/libs.versions.toml`: version catalog for AGP, AndroidX, Material, ConstraintLayout, JUnit, and Espresso.
