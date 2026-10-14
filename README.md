# ArchitectureCraft Fabric — Minecraft Java 1.20.1

A community Fabric port targeting Minecraft 1.20.1 and Java 17.

## Progress

**Estimated overall completion: about 45% (implementation milestone; latest build validation pending).** The goal is a playable JAR that can be installed without compiling source code. A successful CI build only verifies compilation and resource processing, not in-game behavior.

## Current implementation

- A **separate Sawbench block** with its own block model, item, loot table, and recipe.
- A separate Sawbench with a server-backed handled screen, two persistent inventory slots (input/output), player inventory slots, shift-click transfer, and four selectable profiles: Beam, Post, Slab, and Pillar.
- Basic input-to-output crafting for registered matching beam/post/slab/pillar variants; supported plank types and stone/cobblestone are checked against registered output IDs.
- Architectural block families including beams, posts, columns, capitals, bases, cornices, brackets, sills, railings, window frames, lattice, panels, and trim.
- Eight named arch profiles (D1, D2, D3A/B/C, D4A/B/C), with prototype material variants for oak, spruce, birch, dark oak, and stone.
- Basic recipes, item models, blockstates, English names, and loot tables for implemented variants.
- GitHub Actions workflow that compiles the project and uploads a JAR artifact.

## Important limitations

This is **not yet 95% equivalent to original ArchitectureCraft**. The Sawbench GUI is an initial inventory-backed implementation, not the complete original interface. It does not yet implement the full upstream material picker, arbitrary-shape cutting, or the full breadth of original recipes. Arch collision/outline profiles are crude voxel approximations, not exact upstream geometry. The latest source revision needs a successful CI build before this new workbench implementation can be considered build-verified. Texture fidelity, rotation/orientation controls, broad material support, and in-game testing remain incomplete.

The target is to reproduce the original separate workbench and its workflow as closely as possible, then verify the resulting JAR in Minecraft 1.20.1 with Fabric API.

## Install a built JAR

Open the [GitHub Actions runs](https://github.com/tymuryatina/architecturecraft-fabric-1.20.1/actions), select a successful build, and download the `architecturecraft-fabric-1.20.1` artifact. Install Fabric Loader for Minecraft 1.20.1 and Fabric API, then put the downloaded mod JAR in the game's `mods` folder. This is an early prototype and is not yet represented as a finished 100% release.

## Upstream

Based on the MIT-licensed [TridentMC/ArchitectureCraft](https://github.com/TridentMC/ArchitectureCraft/tree/1.21) project. This repository is an independent compatibility port, not an official upstream release.
