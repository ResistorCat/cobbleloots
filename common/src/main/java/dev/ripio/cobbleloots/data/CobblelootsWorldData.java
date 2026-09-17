package dev.ripio.cobbleloots.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class CobblelootsWorldData extends SavedData {
    public static final String DATA_NAME = "cobbleloots_world_data";

    private long globalResetTimestamp = 0L;
    private boolean globalRestoreUses = true;
    private final Map<UUID, Long> playerResetTimestamps = new HashMap<>();
    private final Map<UUID, Boolean> playerRestoreUses = new HashMap<>();

    // Undo history
    private long previousGlobalResetTimestamp = 0L;
    private boolean previousGlobalRestoreUses = true;
    private final Map<UUID, Long> previousPlayerResetTimestamps = new HashMap<>();
    private final Map<UUID, Boolean> previousPlayerRestoreUses = new HashMap<>();

    public static final Factory<CobblelootsWorldData> FACTORY = new Factory<>(
        CobblelootsWorldData::new,
        CobblelootsWorldData::load,
        null
    );

    public static CobblelootsWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public void resetAll(long gameTime, boolean restoreUses) {
        this.previousGlobalResetTimestamp = this.globalResetTimestamp;
        this.previousGlobalRestoreUses = this.globalRestoreUses;
        this.previousPlayerResetTimestamps.clear();
        this.previousPlayerResetTimestamps.putAll(this.playerResetTimestamps);
        this.previousPlayerRestoreUses.clear();
        this.previousPlayerRestoreUses.putAll(this.playerRestoreUses);

        this.globalResetTimestamp = gameTime;
        this.globalRestoreUses = restoreUses;
        this.playerResetTimestamps.clear();
        this.playerRestoreUses.clear();
        this.setDirty();
    }

    public void resetPlayers(Collection<UUID> players, long gameTime, boolean restoreUses) {
        this.previousPlayerResetTimestamps.clear();
        this.previousPlayerResetTimestamps.putAll(this.playerResetTimestamps);
        this.previousPlayerRestoreUses.clear();
        this.previousPlayerRestoreUses.putAll(this.playerRestoreUses);

        for (UUID uuid : players) {
            this.playerResetTimestamps.put(uuid, gameTime);
            this.playerRestoreUses.put(uuid, restoreUses);
        }
        this.setDirty();
    }

    public boolean undoAll() {
        if (this.globalResetTimestamp == this.previousGlobalResetTimestamp && this.playerResetTimestamps.equals(this.previousPlayerResetTimestamps)) {
            return false;
        }
        this.globalResetTimestamp = this.previousGlobalResetTimestamp;
        this.globalRestoreUses = this.previousGlobalRestoreUses;
        this.playerResetTimestamps.clear();
        this.playerResetTimestamps.putAll(this.previousPlayerResetTimestamps);
        this.playerRestoreUses.clear();
        this.playerRestoreUses.putAll(this.previousPlayerRestoreUses);
        this.setDirty();
        return true;
    }

    public boolean undoPlayers(Collection<UUID> players) {
        boolean changed = false;
        for (UUID uuid : players) {
            if (this.previousPlayerResetTimestamps.containsKey(uuid)) {
                this.playerResetTimestamps.put(uuid, this.previousPlayerResetTimestamps.get(uuid));
                this.playerRestoreUses.put(uuid, this.previousPlayerRestoreUses.getOrDefault(uuid, true));
                changed = true;
            } else if (this.playerResetTimestamps.remove(uuid) != null) {
                this.playerRestoreUses.remove(uuid);
                changed = true;
            }
        }
        if (changed) {
            this.setDirty();
        }
        return changed;
    }

    public long getEffectiveResetTimestamp(UUID playerUuid) {
        long playerReset = this.playerResetTimestamps.getOrDefault(playerUuid, 0L);
        return Math.max(this.globalResetTimestamp, playerReset);
    }

    public boolean shouldRestoreUses(UUID playerUuid) {
        long playerReset = this.playerResetTimestamps.getOrDefault(playerUuid, 0L);
        if (playerReset >= this.globalResetTimestamp && this.playerRestoreUses.containsKey(playerUuid)) {
            return this.playerRestoreUses.get(playerUuid);
        }
        return this.globalRestoreUses;
    }

    public static CobblelootsWorldData load(CompoundTag tag, HolderLookup.Provider provider) {
        CobblelootsWorldData data = new CobblelootsWorldData();
        data.globalResetTimestamp = tag.getLong("GlobalResetTimestamp");
        data.globalRestoreUses = !tag.contains("GlobalRestoreUses") || tag.getBoolean("GlobalRestoreUses");
        data.previousGlobalResetTimestamp = tag.getLong("PreviousGlobalResetTimestamp");
        data.previousGlobalRestoreUses = !tag.contains("PreviousGlobalRestoreUses") || tag.getBoolean("PreviousGlobalRestoreUses");

        if (tag.contains("PlayerResets")) {
            ListTag list = tag.getList("PlayerResets", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag pTag = list.getCompound(i);
                UUID uuid = pTag.getUUID("UUID");
                data.playerResetTimestamps.put(uuid, pTag.getLong("Timestamp"));
                data.playerRestoreUses.put(uuid, !pTag.contains("RestoreUses") || pTag.getBoolean("RestoreUses"));
            }
        }

        if (tag.contains("PreviousPlayerResets")) {
            ListTag list = tag.getList("PreviousPlayerResets", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag pTag = list.getCompound(i);
                UUID uuid = pTag.getUUID("UUID");
                data.previousPlayerResetTimestamps.put(uuid, pTag.getLong("Timestamp"));
                data.previousPlayerRestoreUses.put(uuid, !pTag.contains("RestoreUses") || pTag.getBoolean("RestoreUses"));
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLong("GlobalResetTimestamp", this.globalResetTimestamp);
        tag.putBoolean("GlobalRestoreUses", this.globalRestoreUses);
        tag.putLong("PreviousGlobalResetTimestamp", this.previousGlobalResetTimestamp);
        tag.putBoolean("PreviousGlobalRestoreUses", this.previousGlobalRestoreUses);

        ListTag list = new ListTag();
        for (Map.Entry<UUID, Long> entry : this.playerResetTimestamps.entrySet()) {
            CompoundTag pTag = new CompoundTag();
            pTag.putUUID("UUID", entry.getKey());
            pTag.putLong("Timestamp", entry.getValue());
            pTag.putBoolean("RestoreUses", this.playerRestoreUses.getOrDefault(entry.getKey(), true));
            list.add(pTag);
        }
        tag.put("PlayerResets", list);

        ListTag prevList = new ListTag();
        for (Map.Entry<UUID, Long> entry : this.previousPlayerResetTimestamps.entrySet()) {
            CompoundTag pTag = new CompoundTag();
            pTag.putUUID("UUID", entry.getKey());
            pTag.putLong("Timestamp", entry.getValue());
            pTag.putBoolean("RestoreUses", this.previousPlayerRestoreUses.getOrDefault(entry.getKey(), true));
            prevList.add(pTag);
        }
        tag.put("PreviousPlayerResets", prevList);

        return tag;
    }
}
