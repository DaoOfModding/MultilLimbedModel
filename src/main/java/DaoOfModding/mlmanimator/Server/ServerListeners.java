package DaoOfModding.mlmanimator.Server;

import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ServerListeners
{
    protected static HashMap<UUID, Boolean> crawling = new HashMap<UUID, Boolean>();

    public static double test = 0;

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event)
    {
        // Do nothing on the client
        if (event.side == LogicalSide.CLIENT)
            return;

        // Update the bounding box in case it's been modified to use pose dimensions
        event.player.setBoundingBox(event.player.getDimensions(null).makeBoundingBox(event.player.position()));

        if (event.phase == TickEvent.Phase.END)
        {
            if (event.player.hasPose(Pose.SWIMMING) && !event.player.isSwimming())
            {
                // If the player is crawling and doesn't have to be, set to be standing
                if (event.player.level.noCollision(event.player, event.player.getBoundingBox()) && !isCrawling(event.player.getUUID()))
                {
                    event.player.setPose(Pose.STANDING);

                    // Maybe pointless?
                    event.player.getEntityData().clearDirty();
                }
            }
        }
    }

    public static void setCrawling(UUID player, boolean crawl)
    {
        crawling.put(player, crawl);
    }

    public static boolean isCrawling(UUID player)
    {
        if (!crawling.containsKey(player))
            return false;

        return crawling.get(player);
    }
}
