# ArchitectureCraft Fabric — Minecraft Java 1.20.1

A community Fabric port prototype targeting Minecraft 1.20.1 and Java 17.

## Current implementation

- 34+ registered architectural block variants, including beams, posts, columns, capitals, bases, cornices, brackets, sills, railings, window frames, lattice and panels.
- Custom block models and voxel selection/collision shapes for the implemented profiles.
- Basic crafting recipes, item models, English names and block loot tables.
- GitHub Actions build workflow that compiles the project and uploads the JAR artifact.

## Important limitations

This is **not yet feature-complete ArchitectureCraft**. The original dynamic shape/material system is not ported. The sawbench is currently a registered block placeholder and does not yet have the original working GUI or cutting logic. Rotation/orientation controls, advanced geometry, original textures, and in-game testing still need work. A successful Gradle build only confirms compilation and resource processing; it does not prove that every feature works in-game.

## Build and install

Build with Java 17 and Gradle using `gradle build`. Or open the latest successful run under [GitHub Actions](https://github.com/tymuryatina/architecturecraft-fabric-1.20.1/actions) and download its JAR artifact.

Install Fabric Loader for Minecraft 1.20.1 plus Fabric API, then place the JAR in the game's `mods` folder.

## Upstream

Based on the MIT-licensed [TridentMC/ArchitectureCraft](https://github.com/TridentMC/ArchitectureCraft) project. This repository is an independent compatibility port; it is not an official upstream release.
