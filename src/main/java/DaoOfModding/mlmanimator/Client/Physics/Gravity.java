package DaoOfModding.mlmanimator.Client.Physics;

import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.UUID;

public class Gravity
{
    private static int count = 0;
    private static final UUID STOP_MOVE_ID = UUID.randomUUID();
    private static final AttributeModifier STOP_MOVE = new AttributeModifier(STOP_MOVE_ID, "Stop movement", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static double getSpeed(PlayerEntity player)
    {
        // Grab the movement speed attribute and remove the modifier that stops movement
        ModifiableAttributeInstance moveSpeed = player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);

        if (moveSpeed.hasModifier(STOP_MOVE))
            moveSpeed.removePermanentModifier(STOP_MOVE_ID);

        // Get the players movement speed
        double speed = moveSpeed.getValue();

        // Add the modifier to stop movement
        moveSpeed.addPermanentModifier(STOP_MOVE);

        return speed;
    }

    public static void fall(PlayerEntity faller)
    {
        if (faller.isInWater() || faller.hasEffect(Effects.LEVITATION) || faller.abilities.flying)
            return;

        // Do nothing if the PlayerPoseHandler has yet to load
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(faller.getUUID());

        if (handler == null)
            return;

        Vector3d movement = getFluidFallingAdjustedMovement(0.08, faller);

        Vector3d toFall = handler.rotateVectorDown(new Vector3d(0, -0.08f, 0));
        faller.setDeltaMovement(movement.add(toFall));
    }

    public static Vector3d getFluidFallingAdjustedMovement(double gravity, PlayerEntity faller)
    {
        if (!faller.isSprinting())
        {
            Vector3d movement = faller.getDeltaMovement();

            PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(faller.getUUID());
            Vector3f down = handler.getDownVector();
            Vector3d downMovement = movement.multiply(down.x(), down.y(), down.z());
            double totalDownMovement = (downMovement.x + downMovement.y + downMovement.z) * -1;

            boolean belowZero = ((downMovement.x + downMovement.y + downMovement.z) <= 0);

            double d0;
            if (belowZero && Math.abs(totalDownMovement - 0.005D) >= 0.003D && Math.abs(totalDownMovement - gravity / 16.0D) < 0.003D)
                d0 = -0.003D;
            else
                d0 = totalDownMovement - gravity / 16.0D;

            // Multiply the down direction by the amount to move down to get the final down vector
            down.mul((float)d0);

            // Subtract the previous down movement from the movement vector and add the new down movement to it
            Vector3d finalMovement = (movement.subtract(downMovement)).add(down.x(), down.y(), down.z());

            return finalMovement;
        } else
            return faller.getDeltaMovement();
    }

    // Negate the effects of a jump
    public static void unJump(PlayerEntity jumper)
    {
        // Do nothing if the PlayerPoseHandler has yet to load
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(jumper.getUUID());

        if (handler == null)
            return;

        // Get the jump vector and remove it from the players delta movement
        Vector3d jumping = getJumpVector(jumper, false);
        jumper.setDeltaMovement(jumper.getDeltaMovement().subtract(jumping));
    }

    public static void tryJump(PlayerEntity jumper)
    {
        // Do nothing if the PlayerPoseHandler has yet to load
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(jumper.getUUID());

        if (handler == null)
            return;

        if (!handler.isJumping() && isOnGround(jumper))
            jump(jumper);
    }

    public static boolean isOnGround(PlayerEntity test)
    {
        // TODO: this

        return test.isOnGround();
    }

    public static void move(ClientPlayerEntity player)
    {
        // Only walk if player is standing on the ground and not in water
        if (!Gravity.isOnGround(player) || player.isInWater())
            return;

        double speed = getSpeed(player);

        // Do nothing if the PlayerPoseHandler has yet to load
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(player.getUUID());

        if (handler == null)
            return;

        // Get the forward movement vector when player is facing (0, 0, 1)
        Vector3f movementf = new Vector3f(player.input.leftImpulse, 0, player.input.forwardImpulse);
        movementf.normalize();

        // Get the direction the player is looking, ignoring how far up or down they are looking
        Vector3d direction = player.getForward();
        direction = direction.multiply(1, 0, 1);
        direction = direction.normalize();

        // Calculate the amount the movement vector has to be rotated around the Y axis to match the players forward direction
        Quaternion yRot = Vector3f.YP.rotation((float)Math.atan2(direction.x(), direction.z()));

        // Rotate the forward movement vector around the yRot quaternion
        movementf.transform(yRot);

        // Rotate the direction to be moving in accordance with gravity
        Vector3d movement = handler.rotateVectorDown(new Vector3d(movementf.x(), movementf.y(), movementf.z()));

        // Calculate the amount to move this tick and the max speed that player can be moving
        movement = movement.multiply(speed, speed, speed);

        player.setDeltaMovement(player.getDeltaMovement().add(movement));
    }

    // Make the specified player jump adhering to gravity
    private static void jump(PlayerEntity jumper)
    {
        // Do nothing if the PlayerPoseHandler has yet to load
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(jumper.getUUID());

        if (handler == null)
            return;

        handler.setJumping(true);

        // Get the jump vector and add it to the players delta movement
        Vector3d jumping = getJumpVector(jumper, true).multiply(0.5, 0.5, 0.5);

        jumper.setDeltaMovement(jumper.getDeltaMovement().add(jumping));
    }

    // Returns a vector containing the momentum of a player jump
    public static Vector3d getJumpVector(PlayerEntity jumper, boolean gravityAdjusted)
    {
        float f = getJumpPower(jumper, gravityAdjusted);
        if (jumper.hasEffect(Effects.JUMP))
            f += 0.1F * (float)(jumper.getEffect(Effects.JUMP).getAmplifier() + 1);

        Vector3d jumping = new Vector3d(0, f, 0);

        if (jumper.isSprinting())
        {
            float f1 = jumper.yRot * ((float)Math.PI / 180F);
            jumping.add((-MathHelper.sin(f1) * 0.2F), 0.0D, (MathHelper.cos(f1) * 0.2F));
        }

        // If this jump is gravity adjusted rotate it to be jumping against gravity
        if (gravityAdjusted)
        {
            PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(jumper.getUUID());
            jumping = handler.rotateVectorDown(jumping);
        }

        return jumping;
    }

    public static float getJumpPower(PlayerEntity jumper, boolean gravityAdjusted)
    {
        float f = jumper.level.getBlockState(jumper.blockPosition()).getBlock().getJumpFactor();
        float f1 = jumper.level.getBlockState(getBlockPosBelow(jumper, gravityAdjusted)).getBlock().getJumpFactor();

        return (double)f == 1.0D ? f1 : f;
    }

    public static BlockPos getBlockPosBelow(PlayerEntity jumper, boolean gravityAdjusted)
    {
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(jumper.getUUID());

        // Calculate the position of the block below the player
        Vector3d below = jumper.position();
        Vector3f heightAdjustment = new Vector3f(0, handler.getPlayerModel().getHeightAdjustment() + 0.5f, 0);

        // Adjust for gravity if gravity adjusted
        if (gravityAdjusted)
            heightAdjustment = handler.rotateVectorDown(heightAdjustment);

        below.subtract(heightAdjustment.x(), heightAdjustment.y(), heightAdjustment.z());

        return new BlockPos(below.x, below.y, below.z);
    }
}
