package dev.ripio.cobbleloots.neoforge.event;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.entity.client.CobblelootsLootBallModel;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import static dev.ripio.cobbleloots.entity.neoforge.CobblelootsEntitiesImpl.getLootBallEntityType;

@EventBusSubscriber(modid = Cobbleloots.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CobblelootsEventBusEvents {
  @SubscribeEvent
  public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    event.registerLayerDefinition(CobblelootsLootBallModel.LAYER_LOCATION, CobblelootsLootBallModel::createBodyLayer);
  }

  @SubscribeEvent
  public static void registerAttributes(EntityAttributeCreationEvent event) {
    event.put(getLootBallEntityType(), CobblelootsLootBall.createAttributes().build());
  }
}
