# ArchitectureCraft Fabric — Minecraft Java 1.20.1

A community Fabric port prototype targeting Minecraft 1.20.1 and Java 17.

## Current implementation

- 50 registered architectural block variants, including beams, posts, columns, capitals, bases, cornices, brackets, sills, railings, window frames, lattice and panels.
- Custom block models and voxel selection/collision shapes for the implemented profiles. Beams now place along the clicked face axis (X/Y/Z), with matching models and collision.
- Basic crafting recipes, item models, English names and block loot tables.\n- A basic sawbench interaction with four selectable profiles (beam, post, slab, pillar): click it with an empty hand to cycle mode, then use planks or stone to cut a matching shape where available.
- GitHub Actions build workflow that compiles the project and uploads the JAR artifact.

## Important limitations

This is **not yet feature-complete ArchitectureCraft**. The original dynamic shape/material system is not ported. The sawbench now has a basic right-click cutting interaction, but does not yet have the original selection GUI, persistent inventory slots, or arbitrary-shape cutting logic. Rotation/orientation controls, advanced geometry, original textures, and in-game testing still need work. A successful Gradle build only confirms compilation and resource processing; it does not prove that every feature works in-game.

## Build and install

Build with Java 17 and Gradle using `gradle build`. Or open the latest successful run under [GitHub Actions](https://github.com/tymuryatina/architecturecraft-fabric-1.20.1/actions) and download its JAR artifact.

Install Fabric Loader for Minecraft 1.20.1 plus Fabric API, then place the JAR in the game's `mods` folder.

## Upstream

Based on the MIT-licensed [TridentMC/ArchitectureCraft](https://github.com/TridentMC/ArchitectureCraft) project. This repository is an independent compatibility port; it is not an official upstream release.
