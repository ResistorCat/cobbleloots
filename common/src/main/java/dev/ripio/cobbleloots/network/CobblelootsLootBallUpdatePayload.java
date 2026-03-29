package dev.ripio.cobbleloots.network;

import dev.ripio.cobbleloots.Cobbleloots;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public record CobblelootsLootBallUpdatePayload(int entityId, CompoundTag data) implements CustomPacketPayload {
    public static final @NotNull ResourceLocation PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(Cobbleloots.MOD_ID,
            "loot_ball_update");
    public static final @NotNull CustomPacketPayload.Type<CobblelootsLootBallUpdatePayload> ID = new CustomPacketPayload.Type<>(PAYLOAD_ID);
    public static final @NotNull StreamCodec<RegistryFriendlyByteBuf, CobblelootsLootBallUpdatePayload> CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.INT, CobblelootsLootBallUpdatePayload::entityId,
                    ByteBufCodecs.COMPOUND_TAG, CobblelootsLootBallUpdatePayload::data,
                    CobblelootsLootBallUpdatePayload::new);

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
