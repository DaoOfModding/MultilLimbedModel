package DaoOfModding.mlmanimator.Common;

import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PlayerUtils
{
    public static boolean lookingUp(Player player)
    {
        return player.getLookAngle().y > 0.05;
    }

    public static boolean lookingDown(Player player)
    {
        return player.getLookAngle().y < -0.75;
    }

    // PoseHandlers as client only
    public static Vec3 getDelta(Player player)
    {
        Vec3 currentMotion = player.getDeltaMovement();

        if (player.level.isClientSide)
            currentMotion = PoseHandler.getPlayerPoseHandler(player.getUUID()).getDeltaMovement();

        return currentMotion;
    }

    public static Direction movementDirection(Player player)
    {
        double x = player.xOld - player.xCloak;
        double z = player.zOld - player.zCloak;

        Direction dir;

        if (Math.abs(x) > Math.abs(z))
        {
            if (x > 0)
                dir = Direction.WEST;
            else
                dir = Direction.EAST;
        }
        else
        {
            if (z > 0)
                dir = Direction.NORTH;
            else
                dir = Direction.SOUTH;
        }

        return dir;
    }

    public static Direction invertDirection(Direction dir)
    {
        if (dir == Direction.WEST)
            return Direction.EAST;

        if (dir == Direction.EAST)
            return Direction.WEST;

        if (dir == Direction.SOUTH)
            return Direction.NORTH;

        if (dir == Direction.NORTH)
            return Direction.SOUTH;

        return Direction.DOWN;
    }

    public static Vec3 rotateAroundY(Vec3 position, double angle)
    {
        double angleCos = Math.cos(Math.toRadians(angle));
        double angleSin = Math.sin(Math.toRadians(angle));

        double x = angleCos * position.x + angleSin * position.z;
        double z = -angleSin * position.x + angleCos * position.z;

        return new Vec3(x, position.y, z);
    }

    public static Vector3f rotateAroundY(Vector3f position, double angle)
    {
        double angleCos = Math.cos(Math.toRadians(angle));
        double angleSin = Math.sin(Math.toRadians(angle));

        double x = angleCos * position.x() + angleSin * position.z();
        double z = -angleSin * position.x() + angleCos * position.z();

        return new Vector3f((float)x, position.y(), (float)z);
    }
}
