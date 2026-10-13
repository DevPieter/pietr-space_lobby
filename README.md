# pietr-space_lobby

A personal Minecraft lobby built with [Minestom](https://minestom.net/).

This project is mainly a place for me to experiment and show off my Java and Minestom experience. It is still a work in progress, so things may change or break.

> [!NOTE]
> This is a personal showcase project, not a finished general-purpose lobby framework. Feel free to look around and use it as inspiration.

## How it is structured

The lobby is being split into a small core and optional gameplay plugins. The core takes care of the shared systems, while plugins add things for players to do.

Plugins can use the core features through the lobby API instead of rebuilding the same systems themselves.

### Core features

These features will stay in the main server:

- **World** - loads and manages lobby worlds and their settings
- **Tips** - shows helpful messages to players without constantly spamming them
- **Structures** - loads reusable Minecraft structures from NBT files
- **Signs** - uses marked signs in a world to configure features without hardcoding locations
- **ResourcePack** - registers, sends, and tracks resource packs for players

### Moving to plugins

These gameplay features currently live in the main project, but will be moved into separate plugins:

- **Cooking** - custom campfire cooking
- **Creatures** - custom lobby creatures and behavior
- **Farming** - growing, harvesting, and replanting crops
- **Fishing** - custom rods, bobbers, and catches
- **Jukebox** - custom music and jukebox behavior
- **Parkour** - courses, checkpoints, stats, and replays
- **Portals** - configurable areas that move players between locations
- **Sitting** - lets players sit on supported blocks

Plugin support is being built on the [`feature/plugins`](../../tree/feature/plugins) branch. It currently includes JAR loading, plugin metadata, initialization hooks, and the start of the public lobby API.

## Roadmap / WIP

- **Plugin support** ([`feature/plugins`](../../tree/feature/plugins)) - finish separating gameplay features from the core and expose the shared core systems through the API.
- **Maze** ([`feature/maze`](../../tree/feature/maze)) - automatically generate and build mazes with different difficulty levels. The goal is a complete maze challenge with a flow similar to parkour.
- **Horse racing** ([`feature/horses`](../../tree/feature/horses)) - custom horse races with obstacles, crashes, rechargeable boosts, power-ups, and upgrades. The basic horse and boost systems are already being tested.
- **Mining** ([`feature/mining`](../../tree/feature/mining)) - fully custom mining with block hardness, tool strength, tool speed, breaking stages, and blocks that regenerate over time.

## Running it

You need Java 25.

Build the server with:

```bash
./gradlew build
```

On Windows, use:

```powershell
.\gradlew.bat build
```

The built jar can be found in `build/libs`. The server runs on port `25565`.

The lobby also expects world, structure, and config data. Their locations can be changed with the `WORLD_PATH_BASE`, `STRUCTURE_PATH_BASE`, and `APP_CONFIG_PATH` environment variables.
