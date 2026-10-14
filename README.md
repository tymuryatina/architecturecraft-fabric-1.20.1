# ArchitectureCraft Fabric (Minecraft 1.20.1)

An early Fabric-native port/prototype of architectural building blocks, targeting Minecraft Java 1.20.1 and Java 17. This is **not yet a full feature-equivalent port** of the original ArchitectureCraft: it currently provides a set of static shape blocks and a sawbench placeholder. The original dynamic material/shape system, sawbench UI/recipes, rotation variants, and original models/textures still need porting.

## Build

Run `gradle build` with Java 17. The GitHub Actions workflow builds the mod and uploads the JAR as an artifact.

## Install

Install Fabric Loader for Minecraft 1.20.1 and Fabric API, then put the built `architecturecraft-fabric-*.jar` in your `mods` folder.

## License

MIT, matching the original project's license; see upstream https://github.com/TridentMC/ArchitectureCraft.
