package DaoOfModding.mlmanimator.Server;

import net.minecraft.world.entity.Pose;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ServerListeners
{
    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event)
    {
        // Do nothing on the client
        if (event.side == LogicalSide.CLIENT)
            return;

        if (event.phase == TickEvent.Phase.END)
        {
            if (event.player.hasPose(Pose.SWIMMING) && !event.player.isSwimming())
            {
                // If the player is crawling and doesn't have to be, set to be standing
                if (event.player.level.noCollision(event.player, event.player.getBoundingBox()))
                    event.player.setPose(Pose.STANDING);
            }
        }
    }
}
