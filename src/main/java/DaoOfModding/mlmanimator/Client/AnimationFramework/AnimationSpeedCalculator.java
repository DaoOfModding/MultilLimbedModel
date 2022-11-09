package DaoOfModding.mlmanimator.Client.AnimationFramework;

import net.minecraft.world.phys.Vec3;

public class AnimationSpeedCalculator
{
    public static final float defaultSpeedInTicks = 10;
    public static final float defaultSpeedPerTick = 0.05f;

    // Convert a movement in ticks to a speed value
    // Ticks is the amount of ticks it should take for position to change into destination
    public static double ticksToSpeed(Vec3 position, Vec3 destination, float Ticks)
    {
        // TODO: Look at this, speeds seem variable and wrong

        Vec3 toMove = position.subtract(destination);
        Vec3 direction = toMove.normalize();

        double ticksNeeded = 0;

        if (direction.x != 0)
            ticksNeeded = toMove.x / direction.x;

        else if (direction.y != 0)
            ticksNeeded = toMove.y / direction.y;

        else if (direction.z != 0)
            ticksNeeded = toMove.z / direction.z;

        return Math.abs(ticksNeeded / (double)Ticks);
    }
}
