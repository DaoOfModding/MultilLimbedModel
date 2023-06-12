package DaoOfModding.mlmanimator.Server;

import DaoOfModding.mlmanimator.Client.Models.MultiLimbedDimensions;
import DaoOfModding.mlmanimator.Common.Reflection;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.UUID;

public class ServerBoundingBoxHandler
{
    protected static HashMap<UUID, MultiLimbedDimensions> playerDimensions = new HashMap<UUID, MultiLimbedDimensions>();
    protected static HashMap<UUID, Float> playerEyeHeight = new HashMap<UUID, Float>();

    public static void setDimensions(UUID player, MultiLimbedDimensions dimensions)
    {
        playerDimensions.put(player, new MultiLimbedDimensions(dimensions));
    }

    public static MultiLimbedDimensions getDimensions(UUID player)
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
        MultiLimbedDimensions dims = getDimensions(player.getUUID());

        if (dims != null)
        {
            Reflection.setDimensions(player, dims);
            player.setBoundingBox(dims.makeBoundingBox(player.position()));
        }

        if (playerEyeHeight.containsKey(player.getUUID()))
            Reflection.adjustEyeHeight(player, getEyeHeight(player.getUUID()));
    }
}
