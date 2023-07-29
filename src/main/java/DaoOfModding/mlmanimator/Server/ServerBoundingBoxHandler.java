package DaoOfModding.mlmanimator.Server;

import DaoOfModding.mlmanimator.Client.Models.MultiLimbedDimensions;
import DaoOfModding.mlmanimator.Common.Reflection;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.UUID;

public class ServerBoundingBoxHandler
{
    protected static HashMap<UUID, PlayerBoundBoxData> playerData = new HashMap<UUID, PlayerBoundBoxData>();

    protected static PlayerBoundBoxData getPlayerData(UUID player)
    {
        if (!playerData.containsKey(player))
            playerData.put(player, new PlayerBoundBoxData());

        return playerData.get(player);
    }

    public static void setDimensions(UUID player, MultiLimbedDimensions dimensions)
    {
        getPlayerData(player).playerDimensions = new MultiLimbedDimensions(dimensions);
    }

    public static MultiLimbedDimensions getDimensions(UUID player)
    {
        return getPlayerData(player).playerDimensions;
    }

    public static void setEyeHeight(UUID player, float height)
    {
        getPlayerData(player).playerEyeHeight = height;
    }

    public static float getEyeHeight(UUID player)
    {
        return getPlayerData(player).playerEyeHeight;
    }

    public static void removePlayer(UUID player)
    {
        playerData.remove(player);
    }

    public static void updateDimensions(Player player)
    {
        MultiLimbedDimensions dims = getDimensions(player.getUUID());

        if (dims == null)
            return;

        Reflection.setDimensions(player, dims);
        player.setBoundingBox(dims.makeBoundingBox(player.position()));
        Reflection.adjustEyeHeight(player, getEyeHeight(player.getUUID()));
    }
}
