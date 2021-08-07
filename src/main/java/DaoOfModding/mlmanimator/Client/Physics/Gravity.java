package DaoOfModding.mlmanimator.Client.Physics;

import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class Gravity
{
    private static List<PlayerGravityHandler> gravityHandlers = new ArrayList<PlayerGravityHandler>();

    public static void setupGravityHandler(PlayerEntity player)
    {
        // Do nothing if pose handler for this player already exists
        for (PlayerGravityHandler handler : gravityHandlers)
            if (handler.getID() == player.getUUID())
                return;

        PlayerGravityHandler newHandler = new PlayerGravityHandler(player);
        gravityHandlers.add(newHandler);
    }

    public static PlayerGravityHandler getPlayerGravityHandler(UUID playerID)
    {
        for (PlayerGravityHandler handler : gravityHandlers)
            if (handler.getID() == playerID)
                return handler;

        return null;
    }

    private static int count = 0;
    private static final UUID STOP_MOVE_ID = UUID.randomUUID();
    private static final AttributeModifier STOP_MOVE = new AttributeModifier(STOP_MOVE_ID, "Stop movement", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static double getSpeed(PlayerEntity player)
    {
        // Grab the movement speed attribute
        ModifiableAttributeInstance moveSpeed = player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);

        // Get the players movement speed
        double speed = moveSpeed.getValue();

        return speed;
    }

    public static void enableMovement(PlayerEntity player)
    {
        // Grab the movement speed attribute and remove the modifier that stops movement
        ModifiableAttributeInstance moveSpeed = player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);

        if (moveSpeed.hasModifier(STOP_MOVE))
            moveSpeed.removePermanentModifier(STOP_MOVE_ID);
    }

    public static void disableMovement(PlayerEntity player)
    {
        // Grab the movement speed attribute and add the modifier that stops movement
        ModifiableAttributeInstance moveSpeed = player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);

        if (!moveSpeed.hasModifier(STOP_MOVE))
            moveSpeed.addPermanentModifier(STOP_MOVE);
    }

    public static void fall(PlayerEntity faller)
    {
        if (faller.isInWater() || faller.hasEffect(Effects.LEVITATION) || faller.abilities.flying)
            return;

        PlayerGravityHandler handler = getPlayerGravityHandler(faller.getUUID());

        Vector3d movement = getFluidFallingAdjustedMovement(0.08, faller);

        Vector3d toFall = handler.rotateVectorDown(new Vector3d(0, -0.08f, 0));
        faller.setDeltaMovement(movement.add(toFall));
    }

    public static Vector3d getFluidFallingAdjustedMovement(double gravity, PlayerEntity faller)
    {
        if (!faller.isSprinting())
        {
            Vector3d movement = faller.getDeltaMovement();

            PlayerGravityHandler handler = getPlayerGravityHandler(faller.getUUID());
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
        PlayerGravityHandler handler = getPlayerGravityHandler(test.getUUID());

        // get a vector of the minimum amount the player can fall
        Vector3d fallMove = new Vector3d(handler.getDownVector().x(), handler.getDownVector().y(), handler.getDownVector().z()).scale(1.0E-7D);

        // Get a vector of the resulting player position when they attempt to fall down
        Vector3d vector3d = collide(test, fallMove);

        // If the player can fall then they are not on the ground
        if (fallMove.x == vector3d.x && fallMove.y == vector3d.y && fallMove.z == vector3d.z)
            return false;

        return true;
    }

    public static void move(ClientPlayerEntity player)
    {
        // Only walk if player is standing on the ground and not in water
        if (!Gravity.isOnGround(player) || player.isInWater())
            return;

        double speed = getSpeed(player);

        PlayerGravityHandler handler = getPlayerGravityHandler(player.getUUID());

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
            PlayerGravityHandler handler = getPlayerGravityHandler(jumper.getUUID());
            jumping = handler.rotateVectorDown(jumping);

            // TODO: ... This is jumping in the wrong direction when rotated around anything other than 0 for some reason...!?
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

        PlayerGravityHandler ghandler = getPlayerGravityHandler(jumper.getUUID());

        // Calculate the position of the block below the player
        Vector3d below = jumper.position();
        Vector3f heightAdjustment = new Vector3f(0, handler.getPlayerModel().getHeightAdjustment() + 0.5f, 0);

        // Adjust for gravity if gravity adjusted
        if (gravityAdjusted)
            heightAdjustment = ghandler.rotateVectorDown(heightAdjustment);

        below.subtract(heightAdjustment.x(), heightAdjustment.y(), heightAdjustment.z());

        return new BlockPos(below.x, below.y, below.z);
    }

    // Ripped directly from Entity BECAUSE IT'S FREAKIN PRIVATE >:(
    public static Vector3d collide(Entity player, Vector3d p_213306_1_)
    {
        // TODO: Collide not working with gravity not default

        AxisAlignedBB axisalignedbb = player.getBoundingBox();
        ISelectionContext iselectioncontext = ISelectionContext.of(player);
        VoxelShape voxelshape = player.level.getWorldBorder().getCollisionShape();
        Stream<VoxelShape> stream = VoxelShapes.joinIsNotEmpty(voxelshape, VoxelShapes.create(axisalignedbb.deflate(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(voxelshape);
        Stream<VoxelShape> stream1 = player.level.getEntityCollisions(player, axisalignedbb.expandTowards(p_213306_1_), (p_233561_0_) -> {
            return true;
        });
        ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(stream1, stream));
        Vector3d vector3d = p_213306_1_.lengthSqr() == 0.0D ? p_213306_1_ : player.collideBoundingBoxHeuristically(player, p_213306_1_, axisalignedbb, player.level, iselectioncontext, reuseablestream);
        boolean flag = p_213306_1_.x != vector3d.x;
        boolean flag1 = p_213306_1_.y != vector3d.y;
        boolean flag2 = p_213306_1_.z != vector3d.z;
        boolean flag3 = player.isOnGround() || flag1 && p_213306_1_.y < 0.0D;
        if (player.maxUpStep > 0.0F && flag3 && (flag || flag2)) {
            Vector3d vector3d1 = player.collideBoundingBoxHeuristically(player, new Vector3d(p_213306_1_.x, (double)player.maxUpStep, p_213306_1_.z), axisalignedbb, player.level, iselectioncontext, reuseablestream);
            Vector3d vector3d2 = player.collideBoundingBoxHeuristically(player, new Vector3d(0.0D, (double)player.maxUpStep, 0.0D), axisalignedbb.expandTowards(p_213306_1_.x, 0.0D, p_213306_1_.z), player.level, iselectioncontext, reuseablestream);
            if (vector3d2.y < (double)player.maxUpStep) {
                Vector3d vector3d3 = player.collideBoundingBoxHeuristically(player, new Vector3d(p_213306_1_.x, 0.0D, p_213306_1_.z), axisalignedbb.move(vector3d2), player.level, iselectioncontext, reuseablestream).add(vector3d2);
                if (player.getHorizontalDistanceSqr(vector3d3) > player.getHorizontalDistanceSqr(vector3d1)) {
                    vector3d1 = vector3d3;
                }
            }

            if (player.getHorizontalDistanceSqr(vector3d1) > player.getHorizontalDistanceSqr(vector3d)) {
                return vector3d1.add(player.collideBoundingBoxHeuristically(player, new Vector3d(0.0D, -vector3d1.y + p_213306_1_.y, 0.0D), axisalignedbb.move(vector3d1), player.level, iselectioncontext, reuseablestream));
            }
        }

        return vector3d;
    }
}
