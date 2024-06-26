package DaoOfModding.mlmanimator.Client.Poses;

import DaoOfModding.mlmanimator.Client.AnimationFramework.AnimationSpeedCalculator;
import DaoOfModding.mlmanimator.Client.Models.ExtendableModelRenderer;
import DaoOfModding.mlmanimator.Client.Models.GenericLimbNames;
import DaoOfModding.mlmanimator.Client.Models.MultiLimbedModel;
import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import DaoOfModding.mlmanimator.Network.PacketHandler;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class PlayerPoseHandler
{
    UUID playerID;

    MultiLimbedModel model;

    PlayerPose currentPose = new PlayerPose();
    PlayerPose renderPose = new PlayerPose();
    PlayerPose oldRenderPose = new PlayerPose();
    PlayerPose animatingPose = new PlayerPose();
    PlayerPose backupPose = new PlayerPose();

    PlayerPose legLockPose = new PlayerPose();
    PlayerPose oldLegLockPose = new PlayerPose();

    boolean locked = false;
    boolean crawling = false;

    boolean crouching = false;
    int crouchingCooldown = 0;
    int forcedCrouchCooldown = 0;

    protected boolean isJumping = false;
    // Ticks before allowing jump to be set to False
    protected int jumpCooldown = 0;

    public boolean disableJumpingAnimationThisTick = false;

    protected HashMap<String, Integer> frame = new HashMap<String, Integer>();
    protected HashMap<String, Float> animationTime = new HashMap<String, Float>();
    protected HashMap<Integer, Integer> aLockedFrame = new HashMap<Integer, Integer>();

    public float fov = 1;


    protected Vec3 oldPos = new Vec3(0, 0, 0);
    protected Vec3 delta = new Vec3(0, 0, 0);

    protected boolean slim = false;

    protected ArrayList<Arm> arms = new ArrayList<Arm>();

    protected boolean attackCancel = false;

    protected Vec3 size = new Vec3(0, 0, 0);

    public static double oneEighty = Math.toRadians(180);
    public static double threeSixty = Math.toRadians(360);

    public boolean collision = false;


    public PlayerPoseHandler(Player player, PlayerModel playerModel)
    {
        playerID = player.getUUID();
        slim = MultiLimbedRenderer.isSlim(playerModel);

        model = new MultiLimbedModel(playerModel);

        Arm Main;
        Arm Off;

        if (player.getMainArm() == HumanoidArm.LEFT)
        {
            Main = new Arm(InteractionHand.MAIN_HAND, GenericLimbNames.leftArm, GenericLimbNames.lowerLeftArm, true);
            Off = new Arm(InteractionHand.OFF_HAND, GenericLimbNames.rightArm, GenericLimbNames.lowerRightArm, false);
        }
        else
        {
            Main = new Arm(InteractionHand.MAIN_HAND, GenericLimbNames.rightArm, GenericLimbNames.lowerRightArm, false);
            Off = new Arm(InteractionHand.OFF_HAND, GenericLimbNames.leftArm, GenericLimbNames.lowerLeftArm, true);
        }

        arms.add(Main);
        arms.add(Off);
    }

    public void tick(AbstractClientPlayer player)
    {
        if (crouchingCooldown > 0)
        {
            crouchingCooldown--;

            if (crouchingCooldown == 0)
                crouching = false;
        }

        if (forcedCrouchCooldown > 0)
            forcedCrouchCooldown--;

        size = new Vec3(1, 1, 1);

        getPlayerModel().tick(player);
    }

    public void setForcedCrouch()
    {
        forcedCrouchCooldown = 10;
    }

    public boolean isForcedCrouch()
    {
        return forcedCrouchCooldown > 0;
    }

    public void setCrouching(boolean on)
    {
        if (on)
        {
                crouchingCooldown = 4;

            crouching = true;
        }
        else
            crouching = false;
    }

    public boolean wasCrouching()
    {
        return crouchingCooldown > 0;
    }

    public boolean isCrouching()
    {
        return crouching;
    }

    public void resize(Vec3 resize)
    {
        size = size.multiply(resize);
    }

    public void setCrawling(boolean on)
    {
        if (on == crawling)
            return;

        PacketHandler.sendCrawlingToServer(on);
        crawling = on;
    }

    public boolean isCrawling()
    {
        return crawling;
    }

    public void addArm(Arm newArm)
    {
        arms.add(newArm);
    }

    public void removeArm(Arm toRemove)
    {
        arms.remove(toRemove);
    }

    public void removeArms()
    {
        arms.clear();
    }

    public ArrayList<Arm> getArms()
    {
        return arms;
    }

    public MultiLimbedModel getPlayerModel()
    {
        return model;
    }

    public Vec3 getDeltaMovement()
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

    public void cancelNextAttackAnimation()
    {
        attackCancel = true;
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
                Vec3 angles = new Vec3(0, 0, 0);
                Vec3 notLooking = new Vec3(0, 0, 0);


                ExtendableModelRenderer baseModel = limbModel;

                // Go through all parents and negate their rotations for this limb
                while (limbModel.getParent() != null)
                {
                    limbModel = limbModel.getParent();
                    angles = angles.subtract(limbModel.getModelPart().xRot, limbModel.getModelPart().yRot, -limbModel.getModelPart().zRot);
                    notLooking = notLooking.add(limbModel.getModelPart().xRot, limbModel.getModelPart().yRot, -limbModel.getModelPart().zRot);
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

    // Revert the last animatingPose change
    public void revertPose()
    {
        // Reset aLocks
        aLockedFrame = new HashMap<Integer, Integer>();

        animatingPose = backupPose;
        currentPose = new PlayerPose();
        renderPose = oldRenderPose;

        lockLegPose(oldLegLockPose);

        resizeLimbs(animatingPose.getSizes());

        // Rotate each limb to the angle stored in the animating pose plus any offset angles
        for(String limb : animatingPose.getLimbs())
            model.rotateLimb(limb, animatingPose.getAngle(limb).add(animatingPose.getOffset(limb)));
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

    protected void resizeLimbs(HashMap<String, Vec3> sizes)
    {
        model.getBody().resetResize();

        for (ExtendableModelRenderer fp : model.getFirstPersonLimbs().values())
            fp.resetResize();

        for (Map.Entry<String, Vec3> set : sizes.entrySet())
        {
            ExtendableModelRenderer limb = model.getLimb(set.getKey());

            if (limb != null)
                limb.resize(set.getValue());
        }

        model.getBody().addToResizeForThisAndChildren(size);
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
            Vec3 angles;

            if (renderPose.hasAngle(limb))
                angles = animateLimb(limb, getLimbPos(limb), partialTicks);
            else
                angles = animateLimb(getLimbPos(limb), new Vec3(0, 0, 0), AnimationSpeedCalculator.defaultSpeedPerTick, partialTicks);

            newRender.addAngle(limb, angles, 1);
            newRender.addOffset(limb, renderPose.getOffset(limb));

            newRender.addSize(limb, animateResize(getLimbSize(limb), new Vec3(1, 1, 1), AnimationSpeedCalculator.defaultSpeedPerTick), 0, 1);
        }

        // Add any size changes in the animating pose to renderPose
        for (String limb: renderPose.getSizes().keySet())
            newRender.addSize(limb, animateResize(getLimbSize(limb), renderPose.getSize(limb), renderPose.getSizeSpeed(limb)), 1, renderPose.getSizeSpeed(limb));

        // Add the ticks that have passed into the animationTime map
        for (String limb : limbs)
            animationTime.put(limb, animationTime.get(limb) + partialTicks);

        backupPose = animatingPose;
        animatingPose = newRender;

        unlock();
    }

    public Vec3 animateResize(Vec3 currentSize, Vec3 targetSize, float speed)
    {
        Vec3 direction = currentSize.subtract(targetSize);

        if (direction.length() == 0)
            return currentSize;
        else if (direction.length() < speed)
            return targetSize;

        direction = direction.normalize().scale(-speed);

        return currentSize.add(direction);
    }

    // Get the limb angles for the specified limb. If the animating pose does not contain any data for the limb, get it from the model
    public Vec3 getLimbPos(String limb)
    {
        if (animatingPose.hasAngle(limb))
            return animatingPose.getAngle(limb, 0);

        return modelFromLimb(limb);
    }

    protected Vec3 modelFromLimb(String limb)
    {
        ExtendableModelRenderer limbModel = model.getLimb(limb);

        if (limbModel == null)
            limbModel = model.getFirstPersonLimb(limb);

        return new Vec3(limbModel.getModelPart().xRot, limbModel.getModelPart().yRot, limbModel.getModelPart().zRot);
    }

    protected Vec3 getLimbSize(String limb)
    {
        ExtendableModelRenderer limbModel = model.getLimb(limb);

        if (limbModel == null)
            limbModel = model.getFirstPersonLimb(limb);

        Vec3 limbSize = limbModel.getSize();
        limbSize = new Vec3(limbSize.x / size.x, limbSize.y / size.y,limbSize.z / size.z);

        return limbSize;
    }

    protected void calculateAnimationLocks(String limb, Vec3 current, float partialTicks)
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
        Vec3 moveTo = renderPose.getAngle(limb, currentFrame);

        // Create a vector of the amount the limb has to move
        Vec3 toMove = new Vec3(current.x - moveTo.x, current.y - moveTo.y, current.z - moveTo.z);
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

    protected Vec3 calculateMovement(Vec3 from, Vec3 to)
    {
        // Flatten the from vector so all angles are between -180 - 180
        from = flattenVector(from);

        // Flatten the to vector based on the angles in the from vector
        to = new Vec3(conditionalFlatten(from.x, to.x), conditionalFlatten(from.y, to.y),conditionalFlatten(from.z, to.z));

        // Flatten the resulting vector so that all angles are between -180 - 180
        return flattenVector(from.subtract(to));
    }

    // Flattens to the 'to' angle if it is too far away from the from angle
    protected double conditionalFlatten(double from, double to)
    {
        if (from > 0 && to < -oneEighty)
            return to + threeSixty;
        else if (from < 0 && to > oneEighty)
            return to - threeSixty;

        return to;
    }

    // Flatten the  vector so that all angles are between -180 - 180
    protected Vec3 flattenVector(Vec3 vec)
    {
        return new Vec3(flattenAngle(vec.x), flattenAngle(vec.y), flattenAngle(vec.z));
    }

    // Flatten the supplied angle so that it is between -180 - 180
    protected double flattenAngle(double angle)
    {
        if (angle > oneEighty)
            return angle - threeSixty;
        else if (angle < -oneEighty)
            return angle + threeSixty;

        return angle;
    }

    // Return a vector moving the specified vector towards the renderPose
    protected Vec3 animateLimb(String limb, Vec3 current, float partialTicks)
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
        Vec3 moveTo = renderPose.getAngle(limb, currentFrame);

        // Create a vector of the amount the limb has to move
        Vec3 toMove = calculateMovement(current, moveTo);
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
    protected Vec3 animateLimb(Vec3 current, Vec3 moveTo, double aSpeed, float partialTicks)
    {
        // Create a vector of the amount the limb has to move
        Vec3 toMove = new Vec3(current.x - moveTo.x, current.y - moveTo.y, current.z - moveTo.z);
        double moveAmount = toMove.length();

        // If the limbs don't need to move, do nothing more
        if (moveAmount == 0)
            return current;

        aSpeed = aSpeed * partialTicks;

        // If the limbs have to move more that the animation speed, reduce the amount to the animation speed
        if (moveAmount > aSpeed)
            toMove = toMove.normalize().scale(aSpeed);

        // Return a vector of the current position moved towards moveTo by the animation speed
        return calculateMovement(current, moveTo);
    }

    // movementDelta does not work for remote clients, so have to calculate it here instead
    protected void calculateDelta(Player player)
    {
        if (player.isLocalPlayer())
            delta = player.getDeltaMovement();
        else
            delta = player.position().subtract(oldPos);

        oldPos = player.position();
    }

    public void doDefaultPoses(Player player)
    {
        oldLegLockPose = legLockPose;
        legLockPose = null;

        calculateDelta(player);

        addPose(GenericPoses.Idle);

        lockLegPose(GenericPoses.Idle);

        // Tell the PoseHandler that the player is not jumping if they are on the ground or in water
        if (player.isOnGround() || player.isInWater())
            setJumping(false);
            //  Otherwise check if the player is jumping
        else if (!isJumping())
        {
            // Check if there is any upwards movement and set jumping to true if so
            if (getDeltaMovement().y > 0)
                setJumping(true);
        }

        if (player.isPassenger())
        {
            // TODO: Account for horses rearing up - Is this needed? Seems to instantly kick you off the horse
            addPose(GenericPoses.Sitting);
            lockLegPose(GenericPoses.Sitting);
        }

        if (player.isSleeping())
        {
            addPose(GenericPoses.Sleeping);
        }
        else if (player.isAutoSpinAttack())
        {
            addPose(GenericPoses.SpinAttack);
        }
        else if (isCrawling())
        {
            if (getDeltaMovement().x != 0 || getDeltaMovement().z != 0)
            {
                addPose(GenericPoses.CrawlingWalk);
                lockLegPose(GenericPoses.Crawling);
            }
            else
            {
                addPose(GenericPoses.Crawling);
                lockLegPose(GenericPoses.Crawling);
            }
        }
        else if (player.isSwimming())
        {
            // If player is moving in the water apply swimming pose
            if (getDeltaMovement().length() > 0)
            {
                PlayerPose swimPose = GenericPoses.SwimmingMoving.clone();
                rotateBody(swimPose, player, GenericPoses.swimBodyPriority);

                addPose(swimPose);
            }
            else
                addPose(GenericPoses.Swimming);
        }
        else if (player.isFallFlying())
        {
            PlayerPose flyFalling = GenericPoses.FlyFalling.clone();
            rotateBody(flyFalling, player, GenericPoses.flyFallingPriority);

            addPose(flyFalling);
        }
        else if (isJumping())
        {
            if (disableJumpingAnimationThisTick)
                disableJumpingAnimationThisTick = false;
            else
                addPose(GenericPoses.Jumping);
        }
        // If player is moving add the walking pose to the PoseHandler
        else if ((player.isOnGround() || player.isInWater()) && (getDeltaMovement().x != 0 || getDeltaMovement().z != 0))
            addPose(GenericPoses.getWalkingPose(player));

        if (player.isCrouching() && !player.isSwimming() && (player.isOnGround() || !player.isInWater()))
        {
            lockLegPose(GenericPoses.Crouching);

            if (getDeltaMovement().x != 0 || getDeltaMovement().z != 0)
            {
                addPose(GenericPoses.getWalkingPose(player));
                addPose(GenericPoses.CrouchingWalk);
            }
            else
                addPose(GenericPoses.Crouching);
        }

        for (Arm arm : arms)
            doArmPose(player, arm);
    }

    public void lockLegPose(PlayerPose pose)
    {
        legLockPose = pose;

        for (String leg : GenericPoses.getLegs())
        {
            if (model.hasLimb(leg))
            {
                if (pose == null)
                    model.getLimb(leg).lockHitboxAnimation(null);
                else if (pose.hasAngle(leg))
                    model.getLimb(leg).lockHitboxAnimation(pose.getAngles(leg).get(0));
            }
        }
    }

    public ArmPose convertArmPose(Arm arm, ArmPose pose)
    {
        ArmPose newPose = pose.clone();
        newPose.setUpperArm(arm.upperLimb);
        newPose.setLowerArm(arm.lowerLimb);
        newPose.setMirrored(arm.mirrored);

        if (pose.holding)
        {
            Vec3 holdingVector = model.getHoldingVector();

            if (newPose.mirror)
                holdingVector = holdingVector.multiply(1, -1 ,-1);

            // Remove existing angles and re-add them with the holding vector added to them
            newPose.clearAngles(ArmPose.upperArm);

            for (Vec3 angle : pose.getAngles(ArmPose.upperArm))
                newPose.addAngle(ArmPose.upperArm, angle.add(holdingVector), pose.getPriority(ArmPose.upperArm));
        }

        return newPose;
    }

    public void tryAttackPose(Player player, Arm arm)
    {
        if (attackCancel)
        {
            attackCancel = false;
            player.swinging = false;
        }
        else
            addPose(convertArmPose(arm, GenericPoses.slashing));
    }

    public void doArmPose(Player player, Arm arm)
    {
        // TODO: Adjust Arm to work better with custom arms
        ItemStack itemstack = player.getItemInHand(arm.hand);

        if (itemstack.isEmpty())
        {
            if (player.swinging && arm.hand == player.swingingArm)
                tryAttackPose(player, arm);
        }
        else
        {
            if (player.getUseItemRemainingTicks() > 0)
            {
                UseAnim useanim = itemstack.getUseAnimation();
                if (useanim == UseAnim.BLOCK)
                    addPose(convertArmPose(arm, GenericPoses.block));
                else if (useanim == UseAnim.BOW)
                {
                    addPose(convertArmPose(arm, GenericPoses.bow));

                    for (Arm notArm : arms)
                        if (arm != notArm)
                            addPose(convertArmPose(notArm, GenericPoses.bowOff));
                }
                else if (useanim == UseAnim.SPEAR)
                    addPose(convertArmPose(arm, GenericPoses.spear));
                else if (useanim == UseAnim.CROSSBOW)
                {
                    addPose(convertArmPose(arm, GenericPoses.crossbow));

                    for (Arm notArm : arms)
                        if (arm != notArm)
                            addPose(convertArmPose(notArm, GenericPoses.crossbowOff));
                }
                else if (useanim == UseAnim.SPYGLASS)
                    addPose(convertArmPose(arm, GenericPoses.spyglass));
                else if (useanim == UseAnim.TOOT_HORN)
                    addPose(convertArmPose(arm, GenericPoses.horn));
                else if (useanim == UseAnim.EAT)
                    addPose(convertArmPose(arm, GenericPoses.eat));
                else if (useanim == UseAnim.DRINK)
                    addPose(convertArmPose(arm, GenericPoses.drink));
            }
            else
            {
                if (player.swinging && arm.hand == player.swingingArm)
                    tryAttackPose(player, arm);
                else if (itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(itemstack))
                {
                    addPose(convertArmPose(arm, GenericPoses.bow));

                    for (Arm notArm : arms)
                        if (arm != notArm)
                            addPose(convertArmPose(notArm, GenericPoses.bowOff));
                }
                else
                    addPose(convertArmPose(arm, GenericPoses.Holding));
            }
        }
    }

    public void rotateBody(PlayerPose pose, Player player, int priority)
    {
        double yLook = 1 - getDeltaMovement().normalize().y;
        double xLook = 0;

        if (player.isOnGround())
            yLook = 1.1;

        Vec3 vec3 = player.getLookAngle();
        Vec3 vec31 = getDeltaMovement();

        double d0 = vec31.horizontalDistanceSqr();
        double d1 = vec3.horizontalDistanceSqr();
        if (d0 > 0.0D && d1 > 0.0D)
        {
            double d2 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1);
            double d3 = vec31.x * vec3.z - vec31.z * vec3.x;

            // Stop random freezing by D2 being SLIGHTLY higher than 1
            if (d2 > 1)
                d2 = 1;

            xLook = Math.signum(d3) * Math.acos(d2);
        }

        pose.addAngle(GenericLimbNames.body, new Vec3(Math.toRadians(90 * yLook), 0, -xLook), priority);
    }
}
