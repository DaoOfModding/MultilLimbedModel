package DaoOfModding.mlmanimator.Client.Poses;

import DaoOfModding.mlmanimator.Client.AnimationFramework.AnimationSpeedCalculator;
import DaoOfModding.mlmanimator.Client.Models.ExtendableModelRenderer;
import DaoOfModding.mlmanimator.Client.Models.GenericLimbNames;
import DaoOfModding.mlmanimator.Client.Models.MultiLimbedModel;
import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.*;
import org.lwjgl.system.CallbackI;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerPoseHandler
{
    UUID playerID;

    MultiLimbedModel model;

    PlayerPose currentPose = new PlayerPose();
    PlayerPose renderPose = new PlayerPose();
    PlayerPose oldRenderPose = new PlayerPose();
    PlayerPose animatingPose = new PlayerPose();
    boolean locked = false;

    private boolean isJumping = false;
    // Ticks before allowing jump to be set to False
    private int jumpCooldown = 0;

    public boolean disableJumpingAnimationThisTick = false;

    private HashMap<String, Integer> frame = new HashMap<String, Integer>();
    private HashMap<String, Float> animationTime = new HashMap<String, Float>();
    private HashMap<Integer, Integer> aLockedFrame = new HashMap<Integer, Integer>();

    private HashMap<String, Float> animationResizeTime = new HashMap<String, Float>();

    public float fov = 1;


    private Vector3d oldPos = new Vector3d(0, 0, 0);
    private Vector3d delta = new Vector3d(0, 0, 0);

    private boolean slim = false;

    public PlayerPoseHandler(UUID id, PlayerModel playerModel)
    {
        playerID = id;
        slim = MultiLimbedRenderer.isSlim(playerModel);

        model = new MultiLimbedModel(playerModel);
    }

    public MultiLimbedModel getPlayerModel()
    {
        return model;
    }

    public Vector3d getDeltaMovement()
    {
        return delta;
    }

    public void setPlayerModel(MultiLimbedModel newModel)
    {
        model = newModel;
    }

    public boolean isSlim()
    {
        return slim;
    }

    public UUID getID()
    {
        return playerID;
    }

    public void addPose(PlayerPose pose)
    {
        lock();

        // Loop through every limb in the new pose
        for (String limb : pose.getLimbs())
            // If the current pose has no angle set for this limb, or the new pose has a higher priority for this limb
            // Then add the limb pose from the new pose to the current pose
            if (!currentPose.hasAngle(limb) || currentPose.getPriority(limb) < pose.getPriority(limb))
                currentPose.setAngles(limb, pose.getAngles(limb), pose.getSpeeds(limb), pose.getPriority(limb), pose.getOffset(limb), pose.getAnimationLock(limb));

        for (String limb : pose.getSizes().keySet())
            currentPose.addSize(limb, pose.getSize(limb), pose.getSizePriority(limb), pose.getSizeSpeed(limb));

        currentPose.disableHeadLook(pose.isHeadLookDisabled(), pose.getDisableHeadLookPriority());

        unlock();
    }

    public boolean isHeadLookDisabled()
    {
        lock();

        boolean test = renderPose.isHeadLookDisabled();

        unlock();

        return test;
    }
    // Updated each ExtendableModel in the player model to be looking in the direction of the player if it is set to do so
    protected void updateHeadLook(boolean lookWithCamera)
    {
        // Loop through all limbs and check if they are set to be looking, updated the current pose for them if they are
        for (String limb : model.getAllLimbs())
        {
            ExtendableModelRenderer limbModel = model.getLimb(limb);

            if (limbModel == null)
                limbModel = model.getFirstPersonLimb(limb);

            if (limbModel.isLooking())
            {
                Vector3d angles = new Vector3d(0, 0, 0);
                Vector3d notLooking = new Vector3d(0, 0, 0);


                ExtendableModelRenderer baseModel = limbModel;

                // Go through all parents and negate their rotations for this limb
                while (limbModel.getParent() != null)
                {
                    limbModel = limbModel.getParent();
                    angles = angles.subtract(limbModel.xRot, limbModel.yRot, limbModel.zRot);
                    notLooking = notLooking.add(limbModel.xRot, limbModel.yRot, limbModel.zRot);
                }

                // Add the base head's angles to this model if this limb should currently be looking with the camera
                if (lookWithCamera)
                {
                    angles = angles.add(model.getLookVector().x, model.getLookVector().y, model.getLookVector().z);
                    currentPose.addAngle(limb, angles, 0, 1f, -1);

                    baseModel.setNotLookingPitch(0);
                }
                // Otherwise set the not-looking pitch
                else
                    baseModel.setNotLookingPitch((float)Math.toDegrees(notLooking.x));
            }
        }
    }

    protected void updateJumping()
    {
        if (isJumping)
        {
            if (jumpCooldown > 0)
                jumpCooldown -= 1;
        }
    }

    public boolean isJumping()
    {
        if (jumpCooldown > 0)
            return true;

        return isJumping;
    }

    public void updateRenderPose()
    {
        lock();

        oldRenderPose = renderPose;
        renderPose = currentPose;

        currentPose = new PlayerPose();

        updateHeadLook(!renderPose.isHeadLookDisabled());

        updateJumping();

        unlock();
    }

    public void setJumping(boolean jump)
    {
        if (!jump)
            if (jumpCooldown > 0)
                return;

        if (jumpCooldown <= 0 && jump)
            jumpCooldown = 5;

        isJumping = jump;
    }

    // Should be called whenever the renderPose is interacted with to stop multithreading breaking everything
    public void lock()
    {
        while (locked) { }

        locked = true;
    }

    public void unlock()
    {
        locked = false;
    }

    // Animate the model as defined in the animatingPose
    public void doPose(float partialTicks)
    {
        // Reset aLocks
        aLockedFrame = new HashMap<Integer, Integer>();

        // Generate the animating pose based on the the target angles in the render pose
        animateLimbs(partialTicks);

        resizeLimbs(animatingPose.getSizes());

        // Rotate each limb to the angle stored in the animating pose plus any offset angles
        for(String limb : animatingPose.getLimbs())
            model.rotateLimb(limb, animatingPose.getAngle(limb).add(animatingPose.getOffset(limb)));
    }

    private void resizeLimbs(HashMap<String, Vector3d> sizes)
    {
        for (Map.Entry<String, Vector3d> set : sizes.entrySet())
        {
            ExtendableModelRenderer limb = model.getLimb(set.getKey());

            if (limb != null)
                limb.resize(set.getValue());
        }
    }

    // Move each limb towards the currentPose by the animation speed
    public void animateLimbs(float partialTicks)
    {
        lock();

        PlayerPose newRender = new PlayerPose();

        Set<String> limbs = model.getAllLimbs();

        // Reset stored animation data for any limbs whose target position has changed
        for (String limb : limbs)
            if (!animationTime.containsKey(limb) || renderPose.hasAngle(limb) && oldRenderPose.hasAngle(limb) && !renderPose.getAngle(limb).equals(oldRenderPose.getAngle(limb)))
                animationTime.put(limb, 0f);


        // Calculate animation locks
        for (String limb : limbs)
            if (renderPose.hasAngle(limb))
                calculateAnimationLocks(limb, getLimbPos(limb), partialTicks);

        // If renderPose has a pose for a limb, move to that position, otherwise move to the base model pose
        for (String limb : limbs)
        {
            Vector3d angles;

            if (renderPose.hasAngle(limb))
                angles = animateLimb(limb, getLimbPos(limb), partialTicks);
            else
                angles = animateLimb(getLimbPos(limb), new Vector3d(0, 0, 0), AnimationSpeedCalculator.defaultSpeedPerTick, partialTicks);

            newRender.addAngle(limb, angles, 1);
            newRender.addOffset(limb, renderPose.getOffset(limb));

            newRender.addSize(limb, animateResize(getLimbSize(limb), new Vector3d(1, 1, 1), AnimationSpeedCalculator.defaultSpeedPerTick), 0, 1);
        }

        // Add any size changes in the animating pose to renderPose
        for (String limb: renderPose.getSizes().keySet())
            newRender.addSize(limb, animateResize(getLimbSize(limb), renderPose.getSize(limb), renderPose.getSizeSpeed(limb)), 1, renderPose.getSizeSpeed(limb));

        // Add the ticks that have passed into the animationTime map
        for (String limb : limbs)
            animationTime.put(limb, animationTime.get(limb) + partialTicks);

        animatingPose = newRender;

        unlock();
    }

    public Vector3d animateResize(Vector3d currentSize, Vector3d targetSize, float speed)
    {
        Vector3d direction = currentSize.subtract(targetSize);

        if (direction.length() == 0)
            return currentSize;
        else if (direction.length() < speed)
            return targetSize;

        direction = direction.normalize().scale(-speed);

        return currentSize.add(direction);
    }

    // Get the limb angles for the specified limb. If the animating pose does not contain any data for the limb, get it from the model
    public Vector3d getLimbPos(String limb)
    {
        if (animatingPose.hasAngle(limb))
            return animatingPose.getAngle(limb, 0);

        return modelFromLimb(limb);
    }

    private Vector3d modelFromLimb(String limb)
    {
        ModelRenderer limbModel = model.getLimb(limb);

        if (limbModel == null)
            limbModel = model.getFirstPersonLimb(limb);

        return new Vector3d(limbModel.xRot, limbModel.yRot, limbModel.zRot);
    }

    private Vector3d getLimbSize(String limb)
    {
        ExtendableModelRenderer limbModel = model.getLimb(limb);

        if (limbModel == null)
            limbModel = model.getFirstPersonLimb(limb);

        return limbModel.getSize();
    }

    private void calculateAnimationLocks(String limb, Vector3d current, float partialTicks)
    {
        // If frame doesn't exist for this limb yet, set it to 0
        if (!frame.containsKey(limb))
            frame.put(limb, 0);

        // Do nothing if this limb only has one frame
        if (renderPose.getFrames(limb) == 1)
            return;

        int aLock = renderPose.getAnimationLock(limb);

        // Do nothing if this limb is not animation locked
        if (aLock == -1)
            return;

        int currentFrame = frame.get(limb);

        // Reset the currentFrame to 0 if it is greater than the number of frames that exist
        if (renderPose.getAngles(limb).size() <= currentFrame)
            currentFrame = 0;

        // Grab the renderPos angle for the specified limb
        Vector3d moveTo = renderPose.getAngle(limb, currentFrame);

        // Create a vector of the amount the limb has to move
        Vector3d toMove = new Vector3d(current.x - moveTo.x, current.y - moveTo.y, current.z - moveTo.z);
        double moveAmount = toMove.length();

        // Advance the current frame if the limb has nothing more to move
        if (moveAmount == 0)
            currentFrame++;

        // Reset the frame if the current frame is larger than the maximum frame for this limb
        if (currentFrame >= renderPose.getFrames(limb))
            currentFrame = 0;

        // If there is already a frame stored in the animation lock map for this animation lock
        if (aLockedFrame.containsKey(aLock))
        {
            int lockedFrame = aLockedFrame.get(aLock);

            // Do nothing if this frame is the same as the locked frame
            if (lockedFrame == currentFrame)
                return;

            if (currentFrame == 0)
                if (lockedFrame > 1)
                    return;

            if (lockedFrame == 0)
                if (currentFrame > 1)
                {
                    aLockedFrame.put(aLock, currentFrame);
                    return;
                }

            if (lockedFrame > currentFrame)
                aLockedFrame.put(aLock, currentFrame);
        }
        else
            aLockedFrame.put(aLock, currentFrame);
    }

    // Return a vector moving the specified vector towards the renderPose
    private Vector3d animateLimb(String limb, Vector3d current, float partialTicks)
    {
        // If frame doesn't exist for this limb yet, set it to 0
        if (!frame.containsKey(limb))
        {
            frame.put(limb, 0);

            // Reset the time stored in this frame
            animationTime.put(limb, 0f);
        }
        // If the current frame is greater than the amount of frames the renderPose has, reset to frame 0
        else if (frame.get(limb) >= renderPose.getFrames(limb))
        {
            frame.put(limb, 0);

            // Reset the time stored in this frame
            animationTime.put(limb, 0f);
        }

        int currentFrame = frame.get(limb);

        // Grab the angle to move to for the specified limb for this frame
        Vector3d moveTo = renderPose.getAngle(limb, currentFrame);

        // Create a vector of the amount the limb has to move
        Vector3d toMove = current.subtract(moveTo);
        double moveAmount = toMove.length();

        // If the limbs don't need to move and the renderPose has only one frame do nothing more, otherwise advance the frame and try again
        if (moveAmount == 0)
        {
            if (renderPose.getFrames(limb) == 1)
                return current;
            else
            {
                int aLock = renderPose.getAnimationLock(limb);

                // Do nothing if frame locked to this frame
                if (aLockedFrame.containsKey(aLock))
                    if (aLockedFrame.get(aLock) == currentFrame)
                        return current;

                currentFrame++;
                frame.put(limb, currentFrame);

                // Reset the time stored in this frame
                animationTime.put(limb, 0f);

                return(animateLimb(limb, current, partialTicks));
            }
        }

        // Ensure there is a value stored in animationTime for this limb
        if (!animationTime.containsKey(limb))
            animationTime.put(limb, 0f);

        // Calculate the amount of ticks remaining for this animation
        float TicksRemaining = renderPose.getAnimationSpeed(limb, currentFrame) - animationTime.get(limb);

        // If no ticks remaining then instantly move to the specified position
        if (TicksRemaining <= 0)
            return moveTo;

        double aSpeed = AnimationSpeedCalculator.ticksToSpeed(current, moveTo, TicksRemaining) * partialTicks;

        // If the limbs have to move less that the animation speed, instantly move to the specified position
        if (moveAmount <= aSpeed)
            return moveTo;

        toMove = toMove.normalize().scale(aSpeed);

        // Return a vector of the current positions moved towards moveTo by the animation speed
        return current.subtract(toMove);
    }

    // Return a vector moving the specified vector towards the 'moveTo' vector
    private Vector3d animateLimb(Vector3d current, Vector3d moveTo, double aSpeed, float partialTicks)
    {
        // Create a vector of the amount the limb has to move
        Vector3d toMove = new Vector3d(current.x - moveTo.x, current.y - moveTo.y, current.z - moveTo.z);
        double moveAmount = toMove.length();

        // If the limbs don't need to move, do nothing more
        if (moveAmount == 0)
            return current;

        aSpeed = aSpeed * partialTicks;

        // If the limbs have to move more that the animation speed, reduce the amount to the animation speed
        if (moveAmount > aSpeed)
            toMove = toMove.normalize().scale(aSpeed);

        // Return a vector of the current position moved towards moveTo by the animation speed
        return current.subtract(toMove);
    }

    // movementDelta does not work for remote clients, so have to calculate it here instead
    private void calculateDelta(PlayerEntity player)
    {
        if (player instanceof ClientPlayerEntity)
            delta = player.getDeltaMovement();
        else
            delta = player.position().subtract(oldPos);
        
        oldPos = player.position();
    }

    public void doDefaultPoses(PlayerEntity player)
    {
        calculateDelta(player);

        addPose(GenericPoses.Idle);

        // Add holding animations if the player is holding an item
        if (!player.getMainHandItem().isEmpty())
        {
            PlayerPose holding = GenericPoses.HoldingMain.clone();
            Vector3d holdingVector = model.getHoldingVector();
            holdingVector = holdingVector.add(Math.toRadians(-30), Math.toRadians(-5), 0 );
            holding.addAngle(GenericLimbNames.rightArm, holdingVector, 1);

            addPose(holding);
        }

        if (!player.getOffhandItem().isEmpty())
        {
            PlayerPose holding = GenericPoses.HoldingOff.clone();
            Vector3d holdingVector = model.getHoldingVector();
            holdingVector = holdingVector.add(Math.toRadians(-30), Math.toRadians(5), 0 );
            holding.addAngle(GenericLimbNames.leftArm, holdingVector, 1);

            addPose(holding);
        }

        // Tell the PoseHandler that the player is not jumping if they are on the ground or in water
        if (player.isOnGround() || player.isInWater())
            setJumping(false);
            //  Otherwise check if the player is jumping
        else if (!isJumping())
        {
            // Check if there is any upwards movement and set jumping to true if so
            if (player.getDeltaMovement().y > 0)
                setJumping(true);
        }

        // If player is in the water
        if (player.isInWater())
        {
            // If player is moving in the water apply swimming pose
            if (getDeltaMovement().length() > 0)
            {
                double yLook = 1 - getDeltaMovement().normalize().y;

                PlayerPose swimPose = GenericPoses.SwimmingMoving.clone();
                swimPose.addAngle(GenericLimbNames.body, new Vector3d(Math.toRadians(90 * yLook), 0, 0), GenericPoses.swimBodyPriority);

                addPose(swimPose);
            }
            else
                addPose(GenericPoses.Swimming);
        }
        else if (PoseHandler.isJumping(player.getUUID()))
        {
            if (disableJumpingAnimationThisTick)
                disableJumpingAnimationThisTick = false;
            else
                addPose(GenericPoses.Jumping);
        }
        // If player is moving add the walking pose to the PoseHandler
        else if (player.isOnGround() && (getDeltaMovement().x != 0 || getDeltaMovement().z != 0))
            addPose(GenericPoses.Walking);

        // Update the PoseHandler
        updateRenderPose();
    }
}
