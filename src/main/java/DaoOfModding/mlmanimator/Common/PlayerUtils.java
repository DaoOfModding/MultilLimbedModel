package DaoOfModding.mlmanimator.Common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class PlayerUtils
{
    public static boolean lookingUp(PlayerEntity player)
    {
        return player.getLookAngle().y > 0.05;
    }

    public static boolean lookingDown(PlayerEntity player)
    {
        return player.getLookAngle().y < -0.75;
    }

    public static Direction movementDirection(PlayerEntity player)
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

    public static Vector3d rotateAroundY(Vector3d position, double angle)
    {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double x = angleCos * position.x + angleSin * position.z;
        double z = -angleSin * position.x + angleCos * position.z;

        return new Vector3d(x, position.y, z);
    }
}
