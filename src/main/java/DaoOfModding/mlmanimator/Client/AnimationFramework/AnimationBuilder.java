package DaoOfModding.mlmanimator.Client.AnimationFramework;

import DaoOfModding.mlmanimator.Client.Poses.PlayerPose;
import net.minecraft.util.math.vector.Vector3d;

public class AnimationBuilder
{

    // Generate a pose for limbs based on the angles supplied
    // Inverts the angles for limb2
    public static PlayerPose generateRepeatingMirroredLimbs(String limb1, String limb2, Vector3d[] angles, int priority, int framesPerAngle)
    {
        return generateRepeatingMirroredLimbs(limb1, limb2, angles, priority, framesPerAngle, -1);
    }

    // Generate a pose for limbs based on the angles supplied
    // Plays the second limb going backwards
    public static PlayerPose generateRepeatingMirroredLimbs(String limb1, String limb2, Vector3d[] angles, int priority, float speedInTicks, int animationLock)
    {
        PlayerPose LegPose = new PlayerPose();

        // Generate each frame
        for (int i = 0; i < angles.length; i++)
        {
            LegPose.addAngle(limb1, angles[i], priority, speedInTicks, animationLock);
            LegPose.addAngle(limb2, angles[angles.length - (i + 1)], priority, speedInTicks, animationLock);
        }

        return LegPose;
    }
}
