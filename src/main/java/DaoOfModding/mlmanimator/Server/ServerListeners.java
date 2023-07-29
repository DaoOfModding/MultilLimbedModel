package DaoOfModding.mlmanimator.Server;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ServerListeners
{
    protected static HashMap<UUID, Boolean> crawling = new HashMap<UUID, Boolean>();

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event)
    {
        // Do nothing on the client
        if (event.side == LogicalSide.CLIENT)
            return;

        // Update the bounding box in case it's been modified to use pose dimensions
        ServerBoundingBoxHandler.updateDimensions(event.player);

        if (event.phase == TickEvent.Phase.END)
        {
            Boolean noCollision = event.player.level.noCollision(event.player, event.player.getBoundingBox());

            if (event.player.hasPose(Pose.SWIMMING) && !event.player.isSwimming())
            {
                // If the player is crawling and doesn't have to be, set to be standing
                if (noCollision && !isCrawling(event.player.getUUID()))
                {
                    if (event.player.isShiftKeyDown())
                        event.player.setPose(Pose.CROUCHING);
                    else
                        event.player.setPose(Pose.STANDING);

                    // Maybe pointless?
                    event.player.getEntityData().clearDirty();

                    ServerBoundingBoxHandler.updateDimensions(event.player);
                }
            }
        }

    }

    @SubscribeEvent
    public static void resize(EntityEvent.Size event)
    {
        if (event.getEntity() instanceof Player)
        {
            // Cancel out the vanilla minecraft changes to dimensions/eye height when the "pose" changes

            event.setNewSize(event.getOldSize());
            event.setNewEyeHeight(event.getOldEyeHeight());
        }
    }

    @SubscribeEvent
    public static void onPlayerHurtInitial(LivingAttackEvent event)
    {
        if (event.getEntity() instanceof Player && !event.getEntity().level.isClientSide)
            if (event.getSource() == DamageSource.IN_WALL)
            {
                // Cancel random instances of damage caused by hitbox changes
                if (!event.getEntity().horizontalCollision && !event.getEntity().verticalCollision)
                    event.setCanceled(true);
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

    @SubscribeEvent
    public static void playerDisconnects(PlayerEvent.PlayerLoggedOutEvent event)
    {
        ServerBoundingBoxHandler.removePlayer(event.getEntity().getUUID());
    }
}
