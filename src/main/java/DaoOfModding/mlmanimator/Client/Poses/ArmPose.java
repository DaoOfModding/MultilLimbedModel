package DaoOfModding.mlmanimator.Client.Poses;

import DaoOfModding.mlmanimator.Client.Models.GenericLimbNames;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class ArmPose extends PlayerPose
{
    public static String upperArm = "PoseUpperArm";
    public static String lowerArm = "PoseLowerArm";

    String upperArmLimb = GenericLimbNames.rightArm;
    String lowerArmLimb = GenericLimbNames.lowerRightArm;

    Boolean mirror = false;

    public void setUpperArm(String newUpper)
    {
        upperArmLimb = newUpper;
    }

    public void setLowerArm(String newLower)
    {
        lowerArmLimb = newLower;
    }

    public void setMirrored(boolean on)
    {
        mirror = on;
    }

    private String limbConvert(String limb)
    {
        if (limb.compareTo(upperArmLimb) == 0)
            return upperArm;
        else if (limb.compareTo(lowerArmLimb) == 0)
            return lowerArm;

        return limb;
    }

    public float getAnimationSpeed(String limb, int frame)
    {
        return super.getAnimationSpeed(limbConvert(limb), frame);
    }

    // Check if an angle for this limb exists
    public boolean hasAngle(String limb)
    {
        return super.hasAngle(limbConvert(limb));
    }

    // Get the angle of the current frame for the specified limb
    public Vec3 getAngle(String limb, int frame)
    {
        Vec3 angle = super.getAngle(limbConvert(limb), frame);

        // TODO: This may need to be adjusted
        if (mirror)
            angle = angle.multiply(1, -1, -1);

        return angle;
    }

    // Get the angle of the first frame for the specified limb
    public Vec3 getAngle(String limb)
    {
        return getAngle(limbConvert(limb), 0);
    }

    // Get the number of frames stored in the specified limb
    public int getFrames(String limb)
    {
        return super.getFrames(limbConvert(limb));
    }

    public int getPriority(String limb)
    {
        return super.getPriority(limbConvert(limb));
    }

    // Returns a list of all limbs used in this pose
    public Set<String> getLimbs()
    {
        TreeSet<String> limbs = new TreeSet<String>();

        for (String limb : super.getLimbs())
        {
            if (limb.compareTo(upperArm) == 0)
                limbs.add(upperArmLimb);
            else if (limb.compareTo(lowerArm) == 0)
                limbs.add(lowerArmLimb);
            else
                limbs.add(limb);
        }

        return limbs;
    }

    // Get all angle frames for the specified limb
    public ArrayList<Vec3> getAngles(String limb)
    {
        if (!mirror)
            return super.getAngles(limbConvert(limb));

        ArrayList<Vec3> mirroredAngles = (ArrayList<Vec3>)super.getAngles(limbConvert(limb)).clone();

        for (int i = 0; i < mirroredAngles.size(); i++)
            mirroredAngles.set(i, mirroredAngles.get(i).multiply(1, -1, -1));

        return mirroredAngles;
    }

    // Get all angle frames for the specified limb
    public ArrayList<Float> getSpeeds(String limb)
    {
        return super.getSpeeds(limbConvert(limb));
    }

    public ArmPose clone()
    {
        ArmPose copyPose = new ArmPose();

        for (String limb : angles.keySet())
            copyPose.setAngles(limb, (ArrayList<Vec3>)angles.get(limb).clone(), (ArrayList<Float>)speed.get(limb).clone(), priorities.get(limb), offset.get(limb), aLock.get(limb));

        for (String limb : sizes.keySet())
            copyPose.addSize(limb, sizes.get(limb), sizePriorities.get(limb), sizeSpeed.get(limb));

        copyPose.disableHeadLook(disableHeadLook, disableHeadLookPriority);

        copyPose.setLowerArm(lowerArm);
        copyPose.setUpperArm(upperArm);
        copyPose.setMirrored(mirror);

        return copyPose;
    }
}
