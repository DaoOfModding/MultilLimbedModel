package DaoOfModding.mlmanimator.Client.Poses;

import DaoOfModding.mlmanimator.Client.AnimationFramework.AnimationSpeedCalculator;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class PlayerPose
{
    protected HashMap<String, Integer> priorities = new HashMap<String, Integer>();
    protected HashMap<String, Integer> sizePriorities = new HashMap<String, Integer>();
    protected HashMap<String, Float> sizeSpeed = new HashMap<String, Float>();

    // X = Depth, positive goes backwards, negative goes forward
    // Y = Rotation
    // Z = Left and Right, Positive goes right, negative goes left
    protected HashMap<String, ArrayList<Vec3>> angles = new HashMap<String, ArrayList<Vec3>>();
    protected HashMap<String, ArrayList<Float>> speed = new HashMap<String, ArrayList<Float>>();


    protected HashMap<String, Vec3> offset = new HashMap<String, Vec3>();
    protected HashMap<String, Vec3> sizes = new HashMap<String, Vec3>();
    protected HashMap<String, Integer> aLock = new HashMap<String, Integer>();

    protected boolean disableHeadLook = false;
    protected int disableHeadLookPriority = 0;

    public PlayerPose()
    {
    }

    // Set all angles on the specified limb to the specified values
    public void setAngles(String limb, ArrayList<Vec3> newAngles, ArrayList<Float> newSpeeds, int priority, Vec3 off, int animationLock)
    {
        angles.put(limb, newAngles);
        speed.put(limb, newSpeeds);
        priorities.put(limb, priority);

        if (off != null)
            offset.put(limb, off);

        aLock.put(limb, animationLock);
    }

    // Add the specified offset into the offset vector for the specified limb
    // The offset will be added to the angle at render time
    public void addOffset(String limb, Vec3 offsetVector)
    {
        if (offset.containsKey(limb))
            offsetVector = offset.get(limb).add(offsetVector);

        offset.put(limb, offsetVector);
    }

    public Vec3 getOffset(String limb)
    {
        if (offset.containsKey(limb))
            return offset.get(limb);

        return new Vec3(0, 0, 0);
    }

    public void addSize(String limb, Vec3 size, int priority, float speed)
    {
        if (sizePriorities.containsKey(limb) && sizePriorities.get(limb) >= priority)
            return;

        sizePriorities.put(limb, priority);
        sizeSpeed.put(limb, speed);
        sizes.put(limb, size);
    }

    public Vec3 getSize(String limb)
    {
        if (sizes.containsKey(limb))
            return sizes.get(limb);

        return null;
    }

    public int getSizePriority(String limb)
    {
        if (sizePriorities.containsKey(limb))
            return sizePriorities.get(limb);

        return -1;
    }

    public float getSizeSpeed(String limb)
    {
        if (sizeSpeed.containsKey(limb))
            return sizeSpeed.get(limb);

        return -1;
    }

    public HashMap<String, Vec3> getSizes()
    {
        return sizes;
    }

    public int getAnimationLock(String limb)
    {
        if (aLock.containsKey(limb))
            return aLock.get(limb);

        return -1;
    }

    public void disableHeadLook(boolean disable, int priority)
    {
        if (priority > disableHeadLookPriority)
        {
            disableHeadLookPriority = priority;
            disableHeadLook = disable;
        }
    }

    public boolean isHeadLookDisabled()
    {
        return disableHeadLook;
    }

    public int getDisableHeadLookPriority()
    {
        return disableHeadLookPriority;
    }

    // Returns a list of all limbs used in this pose
    public Set<String> getLimbs()
    {
        return angles.keySet();
    }

    // Get all angle frames for the specified limb
    public ArrayList<Vec3> getAngles(String limb)
    {
        return angles.get(limb);
    }

    // Clear all angle frames for the specified limb
    public void clearAngles(String limb)
    {
        angles.remove(limb);
        priorities.remove(limb);
        speed.remove(limb);
        aLock.remove(limb);
    }

    // Get all angle frames for the specified limb
    public ArrayList<Float> getSpeeds(String limb)
    {
        return speed.get(limb);
    }

    // Adds angle to specified limb with the specified priority level
    public void addAngle(String limb, Vec3 angle, int priority)
    {
        addAngle(limb, angle, priority, AnimationSpeedCalculator.defaultSpeedInTicks, -1);
    }

    // Adds angle to specified limb with the specified priority level, speed and animation lock
    public void addAngle(String limb, Vec3 angle, int priority, Float speedInTicks, int animationLock)
    {
        // If the specified limb has not been initialised, initialise it
        if (!angles.containsKey(limb))
        {
            angles.put(limb, new ArrayList<Vec3>());
            speed.put(limb, new ArrayList<Float>());
        }

        angles.get(limb).add(angle);
        speed.get(limb).add(speedInTicks);

        priorities.put(limb, priority);
        aLock.put(limb, animationLock);
    }

    public float getAnimationSpeed(String limb, int frame)
    {
        return speed.get(limb).get(frame);
    }

    // Check if an angle for this limb exists
    public boolean hasAngle(String limb)
    {
        return (angles.containsKey(limb));
    }

    // Get the angle of the current frame for the specified limb
    public Vec3 getAngle(String limb, int frame)
    {
        return angles.get(limb).get(frame);
    }

    // Get the angle of the first frame for the specified limb
    public Vec3 getAngle(String limb)
    {
        return getAngle(limb, 0);
    }

    // Get the number of frames stored in the specified limb
    public int getFrames(String limb)
    {
        return angles.get(limb).size();
    }

    public int getPriority(String limb)
    {
        return priorities.get(limb);
    }

    // Combines two player poses
    public PlayerPose combine(PlayerPose secondPose)
    {
        PlayerPose copy = secondPose.clone();

        // Loop through every limb in the pose
        for (String limb : angles.keySet())
            // Add the limb to the copy if the copy does not contain that limb or the copy's limb has a lower priority
            if (!copy.hasAngle(limb) || copy.getPriority(limb) < getPriority(limb))
                copy.setAngles(limb, getAngles(limb), getSpeeds(limb), getPriority(limb), getOffset(limb), getAnimationLock(limb));

        for (String limb : sizes.keySet())
            copy.addSize(limb, sizes.get(limb), sizePriorities.get(limb), sizeSpeed.get(limb));

        copy.disableHeadLook(disableHeadLook, disableHeadLookPriority);

        return copy;
    }

    public PlayerPose clone()
    {
        PlayerPose copyPose = new PlayerPose();

        for (String limb : angles.keySet())
            copyPose.setAngles(limb, (ArrayList<Vec3>)angles.get(limb).clone(), (ArrayList<Float>)speed.get(limb).clone(), priorities.get(limb), offset.get(limb), aLock.get(limb));

        for (String limb : sizes.keySet())
            copyPose.addSize(limb, sizes.get(limb), sizePriorities.get(limb), sizeSpeed.get(limb));

        copyPose.disableHeadLook(disableHeadLook, disableHeadLookPriority);

        return copyPose;
    }
}
