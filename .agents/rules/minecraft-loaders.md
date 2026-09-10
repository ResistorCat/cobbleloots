---
trigger: always_on
description: Minecraft loader guidelines and pinned dependencies
---

# Minecraft Loaders & Pinned Dependencies Guide

This document outlines the active versions, registration lifecycle, and platform-specific idioms for **Minecraft 1.21.1** in Cobbleloots.

---

## 1. Pinned Versions (`gradle.properties`)

| Dependency | Active Version | Notes |
| :--- | :--- | :--- |
| **Minecraft** | `1.21.1` | Mojang official mappings (`mappings loom.officialMojangMappings()`) |
| **Java** | `21` | Toolchain Java 21, `--release 21` |
| **Kotlin** | `2.2.20` | `org.jetbrains.kotlin.jvm` |
| **Cobblemon** | `1.7.3+1.21.1` | Target API `1.7.0` |
| **Fabric Loader** | `0.17.2` | Target version `0.17.2` |
| **Fabric API** | `0.116.6+1.21.1` | Target MC 1.21.1 |
| **Fabric Kotlin** | `1.13.6+kotlin.2.2.20` | Fabric Language Kotlin |
| **NeoForge** | `21.1.182` | Target version `21` |
| **NeoForge Kotlin** | `5.10.0` | Kotlin for Forge |
| **MidnightLib** | `1.9.2+1.21.1` | Cross-platform in-game configuration |

> [!IMPORTANT]
> **Maintenance Instruction**: If any dependency in [`gradle.properties`](file:///gradle.properties) is bumped or updated, the agent responsible for the change **must** update this table and verify if any loader API methods have changed.

---

## 2. Fabric Loader Guidelines (1.21.1)

Fabric entry point is located in `fabric/src/main/java/dev/ripio/cobbleloots/fabric/CobblelootsFabric.java`.

### Lifecycle & Initializers
- Implements `net.fabricmc.api.ModInitializer`.
- Invoked via `onInitialize()` during game startup.
- Client-only code must implement `net.fabricmc.api.ClientModInitializer` and be declared in `fabric.mod.json` under `"client"`.

### Networking in Fabric 1.21.1
- Custom packet payloads implement `net.minecraft.network.protocol.common.custom.CustomPacketPayload`.
- Registered using Fabric Networking API v1 (`PayloadTypeRegistry.playS2C()` / `PayloadTypeRegistry.playC2S()`).
- Packet receivers registered with `ServerPlayNetworking.registerGlobalReceiver()` or `ClientPlayNetworking.registerGlobalReceiver()`.

### Configuration & ModMenu
- Handled through `MidnightLib` and ModMenu integration in `CobblelootsModMenu.java`.

---

## 3. NeoForge Guidelines (21.1.182)

NeoForge entry point is located in `neoforge/src/main/java/dev/ripio/cobbleloots/neoforge/CobblelootsNeoForge.java`.

### Lifecycle & Event Buses
- Main class annotated with `@Mod("cobbleloots")`.
- The constructor accepts `IEventBus modEventBus`.
- NeoForge splits event buses into:
  - **Mod Event Bus** (`modEventBus`): Registration, setup, client setup, payload handlers.
  - **NeoForge Event Bus** (`NeoForge.EVENT_BUS`): Game events (player ticks, block breaks, entity spawns, server lifecycle).

### Networking in NeoForge 21.1
- Uses `RegisterPayloadHandlersEvent` on the Mod Event Bus.
- Do **not** use legacy SimpleChannel. Register payloads via:
  ```java
  @SubscribeEvent
  public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
      final PayloadRegistrar registrar = event.registrar(Cobbleloots.MOD_ID);
      registrar.playBidirectional(
          MyPacketPayload.TYPE,
          MyPacketPayload.CODEC,
          MyPacketPayloadHandler::handle
      );
  }
  ```

### Configuration & Client Screens
- Server/common configuration uses `MidnightConfig`.
- In-game config screens use NeoForge's `IConfigScreenFactory` registered via `ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, ...)`.
- **Warning**: Ensure config screen registration is only loaded on the client (or wrapped in client-only checks) to avoid server crashes.

