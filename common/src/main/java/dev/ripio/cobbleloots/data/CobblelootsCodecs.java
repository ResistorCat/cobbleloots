package dev.ripio.cobbleloots.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallSources;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallVariant;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static dev.ripio.cobbleloots.data.custom.filter.CobblelootsFilters.LOOT_BALL_SOURCE_CODEC;

public class CobblelootsCodecs {
  public static final CobblelootsLootBallSources DEFAULT_LOOT_BALL_SOURCES = new CobblelootsLootBallSources(List.of(), List.of(), List.of(), List.of());

  public static final Codec<CobblelootsLootBallSources> LOOT_BALL_SOURCES_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          LOOT_BALL_SOURCE_CODEC.listOf().optionalFieldOf("generation", List.of()).forGetter(CobblelootsLootBallSources::getGeneration),
          LOOT_BALL_SOURCE_CODEC.listOf().optionalFieldOf("spawning", List.of()).forGetter(CobblelootsLootBallSources::getSpawning),
          LOOT_BALL_SOURCE_CODEC.listOf().optionalFieldOf("fishing", List.of()).forGetter(CobblelootsLootBallSources::getFishing),
          LOOT_BALL_SOURCE_CODEC.listOf().optionalFieldOf("archaeology", List.of()).forGetter(CobblelootsLootBallSources::getArchaeology)
      ).apply(instance, CobblelootsLootBallSources::new)
  );

  public static final Codec<CobblelootsLootBallVariant> LOOT_BALL_VARIANT_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          ComponentSerialization.CODEC.fieldOf("name").forGetter(CobblelootsLootBallVariant::getName),
          ResourceLocation.CODEC.optionalFieldOf("loot_table", CobblelootsDefinitions.EMPTY_LOCATION).forGetter(CobblelootsLootBallVariant::getLootTable),
          ResourceLocation.CODEC.optionalFieldOf("texture", CobblelootsDefinitions.EMPTY_LOCATION).forGetter(CobblelootsLootBallVariant::getTexture)
          ).apply(instance, CobblelootsLootBallVariant::new)
  );

  public static final Codec<CobblelootsLootBallData> LOOT_BALL_DATA_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          ComponentSerialization.CODEC.fieldOf("name").forGetter(CobblelootsLootBallData::getName),
          ResourceLocation.CODEC.optionalFieldOf("loot_table", CobblelootsDefinitions.EMPTY_LOCATION).forGetter(CobblelootsLootBallData::getLootTable),
          ResourceLocation.CODEC.optionalFieldOf("texture", CobblelootsDefinitions.EMPTY_LOCATION).forGetter(CobblelootsLootBallData::getTexture),
          LOOT_BALL_SOURCES_CODEC.optionalFieldOf("sources", DEFAULT_LOOT_BALL_SOURCES).forGetter(CobblelootsLootBallData::getSources),
          LOOT_BALL_VARIANT_CODEC.listOf().optionalFieldOf("variants", List.of()).forGetter(CobblelootsLootBallData::getVariants)
      ).apply(instance, CobblelootsLootBallData::new)
  );

}
