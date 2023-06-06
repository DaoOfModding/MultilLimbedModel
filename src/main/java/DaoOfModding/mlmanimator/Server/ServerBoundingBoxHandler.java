package DaoOfModding.mlmanimator.Server;

import DaoOfModding.mlmanimator.Common.Reflection;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.UUID;

public class ServerBoundingBoxHandler
{
    protected static HashMap<UUID, EntityDimensions> playerDimensions = new HashMap<UUID, EntityDimensions>();
    protected static HashMap<UUID, Float> playerEyeHeight = new HashMap<UUID, Float>();

    public static void setDimensions(UUID player, EntityDimensions dimensions)
    {
        playerDimensions.put(player, dimensions);
    }

    public static EntityDimensions getDimensions(UUID player)
    {
        return playerDimensions.get(player);
    }

    public static void setEyeHeight(UUID player, float height)
    {
        playerEyeHeight.put(player, height);
    }

    public static float getEyeHeight(UUID player)
    {
        return playerEyeHeight.get(player);
    }

    public static void removePlayer(UUID player)
    {
        playerDimensions.remove(player);
        playerEyeHeight.remove(player);
    }

    public static void updateDimensions(Player player)
    {
        EntityDimensions dims = getDimensions(player.getUUID());

        if (dims != null)
        {
            Reflection.setDimensions(player, dims);
            player.setBoundingBox(dims.makeBoundingBox(player.position()));
        }

        if (playerEyeHeight.containsKey(player.getUUID()))
            Reflection.adjustEyeHeight(player, getEyeHeight(player.getUUID()));
    }
}
