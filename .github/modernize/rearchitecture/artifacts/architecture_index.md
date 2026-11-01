# Architecture Index

This index is not the full contract. Do not implement from this file alone; follow the artifact paths below.

## Global artifacts

- `unit_graph.yaml` — all workers: entrypoint, source anchors, dynamic Android wiring, and dependencies.
- `migration_boundary.yaml` — scope guard: this analysis has no rewrite scope; source anchors are not rewrite targets.
- `wire_contracts.yaml` — no outward REST, messaging, SDK, SQL, or file contracts were found.
- `shared_modules.yaml` — no module is referenced by two externally triggerable units.
- `cross_unit_state.yaml` — no cross-unit implicit state flow exists because one unit was identified.
- `project-structure.md` — project classification, layers, entrypoints, and functional areas.
- `tech-stack.md` — existing language, Android/Gradle stack, versions, and dependencies.
- `data-model.md` — in-memory maze/search state; no persisted entities were found.

## Implementation Guide

### Unit: `maze_master_screen`

- external trigger: Android `MAIN`/`LAUNCHER` activity (`MainActivity`)
- must read:
  - `units/maze_master_screen/behavior.yaml` — side effects, UI branches, bounded search behavior, and error outcomes.
  - `units/maze_master_screen/bindings.yaml` — manifest, lifecycle, custom-view wiring, and SDK configuration.
  - `units/maze_master_screen/unit_decomposition.yaml` — advisory split candidates only; `commit: false` means no target structure is decided.
- relevant global rows:
  - `unit_graph.yaml` — filter `name: maze_master_screen`; inspect dynamic entrypoints and source anchors.
  - `migration_boundary.yaml` — use `must_rewrite` for implementation scope; it is empty for this analysis.
  - `wire_contracts.yaml` — no unit rows.
  - `shared_modules.yaml` — no unit rows.
  - `cross_unit_state.yaml` — no flows.
- before DONE report: artifact paths read, every `must_preserve` behavior implemented, unresolved/deferred contracts, and build/test/runtime evidence.
