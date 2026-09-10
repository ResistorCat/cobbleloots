# Architectury Multi-Loader Rules & Best Practices

Cobbleloots targets both **Fabric** and **NeoForge** using the **Architectury Loom** multi-loader framework. This guide specifies how to structure cross-platform code and avoid platform leakage.

---

## 1. Project Organization

```
cobbleloots/
├── common/       # Loader-agnostic code and data (compileOnly on loaders)
├── fabric/       # Fabric implementation and bundle jar
└── neoforge/     # NeoForge implementation and bundle jar
```

### Dependency Flow
- `fabric` depends on `common` via `project(path: ':common', configuration: 'namedElements')`.
- `neoforge` depends on `common` via `project(path: ':common', configuration: 'namedElements')`.
- `common` must **never** depend on `fabric` or `neoforge`.

---

## 2. Platform Isolation Rules

1. **Zero Platform Imports in Common**:
   - Never import `net.fabricmc.*` in `common/`.
   - Never import `net.neoforged.*` in `common/`.
   - If a feature requires loader-specific mechanics, use the `@ExpectPlatform` pattern or an abstract service interface.

2. **The `@ExpectPlatform` Pattern**:
   - In `common/`: Declare an abstract/dummy method annotated with `@dev.architectury.injectables.annotations.ExpectPlatform`:
     ```java
     package dev.ripio.cobbleloots.platform;
     
     import dev.architectury.injectables.annotations.ExpectPlatform;
     import java.nio.file.Path;

     public class PlatformHelper {
         @ExpectPlatform
         public static Path getConfigDirectory() {
             throw new AssertionError();
         }
     }
     ```
   - In `fabric/`: Implement `dev.ripio.cobbleloots.platform.fabric.PlatformHelperImpl`:
     ```java
     package dev.ripio.cobbleloots.platform.fabric;
     
     import net.fabricmc.loader.api.FabricLoader;
     import java.nio.file.Path;

     public class PlatformHelperImpl {
         public static Path getConfigDirectory() {
             return FabricLoader.getInstance().getConfigDir();
         }
     }
     ```
   - In `neoforge/`: Implement `dev.ripio.cobbleloots.platform.neoforge.PlatformHelperImpl`:
     ```java
     package dev.ripio.cobbleloots.platform.neoforge;
     
     import net.neoforged.fml.loading.FMLPaths;
     import java.nio.file.Path;

     public class PlatformHelperImpl {
         public static Path getConfigDirectory() {
             return FMLPaths.CONFIGDIR.get();
         }
     }
     ```

3. **Data & Assets**:
   - All standard loot tables, receipts, ball models, block states, and textures must live in:
     `common/src/main/resources/assets/cobbleloots/`
     `common/src/main/resources/data/cobbleloots/`
   - Architectury bundles these automatically into both the Fabric and NeoForge JARs.

4. **Testing & Verification**:
   - After writing cross-platform code, always run `./gradlew build` to ensure both loader remapping tasks (`fabric:remapJar` and `neoforge:remapJar`) succeed without unfulfilled `@ExpectPlatform` stubs.
