package dev.ripio.cobbleloots.network;

import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CobblelootsNetwork {
    public static void handleLootBallUpdate(CobblelootsLootBallUpdatePayload payload, ServerPlayer player) {
        if (!player.isCreative()) {
            return;
        }

        Entity entity = player.level().getEntity(payload.entityId());
        if (!(entity instanceof CobblelootsLootBall lootBall)) {
            return;
        }

        if (player.distanceToSqr(lootBall) > 64 * 64) {
            return;
        }

        CompoundTag tag = payload.data();

        if (tag.contains("LootBallData")) {
            String dataStr = tag.getString("LootBallData");
            if (dataStr.isEmpty()) {
                lootBall.setLootBallDataId(dev.ripio.cobbleloots.util.CobblelootsDefinitions.EMPTY_LOCATION);
            } else {
                ResourceLocation id = ResourceLocation.tryParse(dataStr);
                if (id != null)
                    lootBall.setLootBallDataId(id);
            }
        }

        if (tag.contains("Variant"))
            lootBall.setVariantId(tag.getString("Variant"));

        if (tag.contains("Texture")) {
            String texStr = tag.getString("Texture");
            if (texStr.isEmpty()) {
                lootBall.setTexture(dev.ripio.cobbleloots.util.CobblelootsDefinitions.EMPTY_LOCATION);
            } else {
                ResourceLocation tex = ResourceLocation.tryParse(texStr);
                if (tex != null)
                    lootBall.setTexture(tex);
            }
        }

        if (tag.contains("Invisible"))
            lootBall.setInvisible(tag.getBoolean("Invisible"));
        if (tag.contains("Sparks"))
            lootBall.setSparks(tag.getBoolean("Sparks"));

        if (tag.contains("Uses"))
            lootBall.setRemainingUses(tag.getInt("Uses"));
        if (tag.contains("Multiplier"))
            lootBall.setMultiplier(tag.getFloat("Multiplier"));
        if (tag.contains("XP"))
            lootBall.setXp(tag.getInt("XP"));
        if (tag.contains("PlayerTimer"))
            lootBall.setPlayerTimer(tag.getLong("PlayerTimer"));
        if (tag.contains("DespawnTick"))
            lootBall.setDespawnTick(tag.getLong("DespawnTick"));

        if (tag.contains("GUI_Item_Id")) {
            String idStr = tag.getString("GUI_Item_Id");
            if (idStr.isEmpty()) {
                lootBall.clearContent();
            } else {
                ResourceLocation itemId = ResourceLocation.tryParse(idStr);
                int count = tag.getInt("GUI_Item_Count");
                if (itemId != null && count > 0) {
                    Item item = BuiltInRegistries.ITEM.get(itemId);
                    if (item != Items.AIR) {
                        lootBall.setItem(0, new ItemStack(item, count));
                    } else {
                        lootBall.clearContent();
                    }
                } else {
                    lootBall.clearContent();
                }
            }
        }

        lootBall.setChanged();
        lootBall.updateLootBallClientData();
    }
}
