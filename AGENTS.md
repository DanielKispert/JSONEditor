# AGENTS.md – JSONEditor Codebase Guide

## Project Overview
AGPL-3.0 desktop JSON editor written in **Java 21 + JavaFX 21**. Edits JSON files validated against a JSON Schema. Build tool: **Gradle**.

## Code Style
- 140 chars per line; curly brackets on the **next line**
- `final` for all non-reassigned variables
- Javadoc only on public/complex methods consumed by other components; skip obvious private methods
- English for all comments and docs

## Architecture: MVC + Observer

```
Main → Controller (ControllerImpl)
             ↓ calls
       WritableModel (ModelImpl)   ← holds all state (JsonNode tree, schema, settings)
             ↓ fires Event via EventSender (notifyObservers)
       View (ViewImpl / UIHandlerImpl / SceneHandlerImpl)
```

- **`ReadableModel`** – read-only interface consumed by the View
- **`WritableModel`** – extends `ReadableModel`, used by the Controller
- **`WritableModelInternal`** – used by `Command` implementations only (direct model mutation)
- View never writes to the model directly; all mutations go through `Controller` → `Command`

Key interfaces: `src/main/java/com/daniel/jsoneditor/model/{ReadableModel,WritableModel,WritableModelInternal}.java`

## Command Pattern (Undo/Redo)
Every user mutation is a `Command` (`model/commands/Command.java`):
1. Instantiate via `model.getCommandFactory().<method>()` – never use `new` directly from the View/Controller
2. Execute via `controller.getCommandManager().executeCommand(cmd)` – this handles the undo stack
3. `execute()` returns `List<ModelChange>` – semantic diffs used for UI refresh and undo stack
4. `ModelChange` has static factories: `ModelChange.add(path, node)`, `.remove(path, old)`, `.replace(path, old, new)`, `.move(...)`, `.sort(path, oldSnapshot, newSnapshot)`
5. `isUndoable()` defaults `true`; override to `false` for non-reversible ops
6. Extend `BaseCommand` for any command that directly mutates `WritableModelInternal`
7. Commands have a `CommandCategory` (`STRUCTURE`, `VALUE`, `NODE`, `OTHER`) for classification
8. `ReferenceableObjectCommand` – marker interface for commands that create referenceable objects; exposes `getCreatedObjectPath()`

All concrete commands live in `model/commands/impl/`. `CommandFactory` lists all available commands.

## Event / State Flow
After any model change the controller calls `model.sendEvent(new Event(EventEnum.X))`.  
The `View` observes the model via `Observer.update()` and reads `model.getLatestEvent()` to decide what to refresh.  
`EventEnum` (in `model/statemachine/impl/`) lists all state transitions – check it first when adding new UI reactions.  
Notable event: `COMMAND_APPLIED` fires after any command execute/undo/redo with metadata attached.

## Key Data Type
**`JsonNodeWithPath`** (`model/json/JsonNodeWithPath.java`) – a Jackson `JsonNode` + its JSON Pointer path string (`/foo/bar/0`). Used everywhere as the primary node reference. Paths use `/`-separated JSON Pointer syntax.

## Custom Schema Keywords
Schema extensions live in `model/json/schema/keywords/` and `model/json/schema/reference/`.  
- `referencesToObjects` / `referenceableObjects` – cross-node linking (see README)
- `uniquekeys` vocab supported via `networknt` validator

## View Structure
```
UIHandlerImpl → SceneHandlerImpl
                  ├── JSONSelectionScene   (file/schema picker)
                  └── EditorScene
                        ├── menubar/
                        ├── toolbar/        (configurable via settings JSON)
                        ├── navbar/         (tree navigation)
                        └── editorwindow/
                              ├── tableview/   (array nodes → table)
                              └── graph/       (reference graph view)
              tooltips/        (TooltipHelper)
```
UI components are in `view/impl/jfx/impl/scenes/impl/editor/components/`.  
`EditorWindowManager` decides which sub-editor to render based on the selected node type.

## Logging
SLF4J + Logback (`src/main/resources/logback.xml`). Pattern per class:
```java
private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
```

## Utilities
- **`JsonDiffer`** (`model/diff/JsonDiffer.java`) – static `calculateDiff(savedJson, currentJson, model)` returns `List<DiffEntry>` of what changed vs. disk. Used for the unsaved-changes indicator.
- **Git blame** (`model/git/`) – `GitBlameService` loads blame info per path; triggers `EventEnum.GIT_BLAME_LOADED`. `JsonPathToLineMapper` maps JSON Pointer paths to source line numbers.

## Validation
`model/validation/` contains `ReferenceValidator` for cross-node reference validation. `ValidationResult` and `ValidationError` carry structured error info.

## MCP Server
The MCP server exposes JSON editor operations to external AI agents via HTTP JSON-RPC.

### Multi-File Sessions
`FileSessionManager` (`model/sessions/`) manages multiple open file sessions. Each session has a unique `file_id`. Two session types:
- **GUI sessions** – registered when the GUI opens a file, protected from MCP close
- **Headless sessions** – opened via `open_file` tool, closeable via `close_file`

`EditorSession` (`model/sessions/`) is a record holding `id`, `ReadableModel`, file paths, and `guiOwned` flag.

### Architecture
```
GUI Mode:   ControllerImpl → FileSessionManager → McpController → JsonEditorMcpServer
Headless:   JFXLauncher --headless → FileSessionManager → JsonEditorMcpServer
```

`McpController` wraps `JsonEditorMcpServer`. Port set via `SettingsController.getMcpServerPort()`.

### Tools
Tools are registered in `McpToolRegistry` (`model/mcp/`). All per-file tools require a `file_id` argument.

Base classes:
- `ReadOnlyMcpTool` – holds `FileSessionManager`, provides `resolveModel(arguments)` helper
- `WriteMcpTool` – extends `ReadOnlyMcpTool` (currently no write tools registered)

Session management tools (extend `ReadOnlyMcpTool`, no `file_id` needed):
- `ListFilesTool` – list all open sessions
- `OpenFileTool` – open a JSON + schema file pair, returns `file_id`
- `CloseFileTool` – close a headless session

Per-file read tools (require `file_id`):
- `GetFileInfoTool`, `GetNodeTool`, `GetSchemaForPathTool`, `GetExamplesTool`
- `GetReferenceableObjectsTool`, `GetReferenceableInstancesTool`, `FindReferencesToTool`

`McpArgumentValidator` validates tool input against schemas before execution.

### Headless Mode
Run the MCP server without the GUI (no JavaFX window). Start with:
```bash
./gradlew run --args="--headless"                # default port 4500
./gradlew run --args="--headless --port 5000"    # custom port
```

## Build & Packaging
```bash
./gradlew build          # compile + test
./gradlew run            # run GUI locally
./gradlew run --args="--headless"  # run headless MCP server (no GUI)
./gradlew jpackage       # create native installer (build/jpackage/)
```
Version is read from `src/main/resources/version.properties`.  
`jpackage` bumps major version from `0.x.y` → `1.x.y` automatically (macOS constraint).

## Tests
JUnit 5 + TestFX + Mockito. Tests in `src/test/java/`. Run with `./gradlew test`.

## Settings
App settings JSON (schema in `model/settings/`) can add custom toolbar buttons – see `example_settings.json` in project root.
