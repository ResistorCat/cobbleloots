package dev.ripio.cobbleloots.network;

import dev.ripio.cobbleloots.Cobbleloots;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public record CobblelootsLootBallOpenScreenPayload(int entityId, CompoundTag data) implements CustomPacketPayload {
    public static final @NotNull ResourceLocation PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(Cobbleloots.MOD_ID,
            "loot_ball_open_screen");
    public static final @NotNull CustomPacketPayload.Type<CobblelootsLootBallOpenScreenPayload> ID = new CustomPacketPayload.Type<>(PAYLOAD_ID);
    public static final @NotNull StreamCodec<RegistryFriendlyByteBuf, CobblelootsLootBallOpenScreenPayload> CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.INT, CobblelootsLootBallOpenScreenPayload::entityId,
                    ByteBufCodecs.COMPOUND_TAG, CobblelootsLootBallOpenScreenPayload::data,
                    CobblelootsLootBallOpenScreenPayload::new);

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
