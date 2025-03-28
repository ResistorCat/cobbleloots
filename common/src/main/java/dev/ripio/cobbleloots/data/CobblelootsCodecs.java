package dev.ripio.cobbleloots.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallHeight;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallSource;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public class CobblelootsCodecs {
  private static final Integer DEFAULT_WEIGHT = 1;
  private static final ResourceLocation EMPTY = CobblelootsDefinitions.EMPTY_LOCATION;
  public static final TagKey<Biome> EMPTY_BIOME_TAG = TagKey.create(Registries.BIOME, EMPTY);
  private static final int MAX_ABSOLUTE_HEIGHT = 10_000_000;
  private static final CobblelootsLootBallHeight DEFAULT_HEIGHT = new CobblelootsLootBallHeight(-MAX_ABSOLUTE_HEIGHT, MAX_ABSOLUTE_HEIGHT);

  public static final Codec<CobblelootsLootBallHeight> LOOT_BALL_HEIGHT_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          Codec.INT.optionalFieldOf("min", -MAX_ABSOLUTE_HEIGHT).forGetter(CobblelootsLootBallHeight::getMin),
          Codec.INT.optionalFieldOf("max", MAX_ABSOLUTE_HEIGHT).forGetter(CobblelootsLootBallHeight::getMax)
      ).apply(instance, CobblelootsLootBallHeight::new)
  );

  public static final Codec<CobblelootsLootBallSource> LOOT_BALL_SOURCE_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          Codec.STRING.fieldOf("type").forGetter(CobblelootsLootBallSource::getType),
          TagKey.codec(Registries.BIOME).optionalFieldOf("biome", EMPTY_BIOME_TAG).forGetter(CobblelootsLootBallSource::getBiome),
          LOOT_BALL_HEIGHT_CODEC.optionalFieldOf("height", DEFAULT_HEIGHT).forGetter(CobblelootsLootBallSource::getHeight),
          Codec.INT.optionalFieldOf("weight", DEFAULT_WEIGHT).forGetter(CobblelootsLootBallSource::getWeight)
      ).apply(instance, CobblelootsLootBallSource::new)
  );

  public static final Codec<CobblelootsLootBallData> LOOT_BALL_CODEC = Codec.recursive(
      "LootBallData",
      selfCodec -> RecordCodecBuilder.create(
          instance -> instance.group(
              ComponentSerialization.flatCodec(30).optionalFieldOf("name", Component.empty()).forGetter(CobblelootsLootBallData::getName),
              Codec.BOOL.optionalFieldOf("announce", false).forGetter(CobblelootsLootBallData::getAnnounce),
              Codec.INT.optionalFieldOf("weight", DEFAULT_WEIGHT).forGetter(CobblelootsLootBallData::getWeight),
              ResourceLocation.CODEC.optionalFieldOf("loot_table", EMPTY).forGetter(CobblelootsLootBallData::getLootTable),
              ResourceLocation.CODEC.optionalFieldOf("texture", EMPTY).forGetter(CobblelootsLootBallData::getTexture),
              LOOT_BALL_SOURCE_CODEC.listOf().optionalFieldOf("sources", List.of()).forGetter(CobblelootsLootBallData::getSources),
              selfCodec.listOf().optionalFieldOf("variants", List.of()).forGetter(CobblelootsLootBallData::getVariants)
          ).apply(instance, CobblelootsLootBallData::new)
      )
  );
}
