package DaoOfModding.mlmanimator.Client.Physics;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
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

        //newHandler.setDownRotation(new Vector3f(90,0,0));
    }
    private static final UUID STOP_MOVE_ID = UUID.randomUUID();
    private static final AttributeModifier STOP_MOVE = new AttributeModifier(STOP_MOVE_ID, "Stop movement", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);

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

    public static PlayerGravityHandler getPlayerGravityHandler(UUID playerID)
    {
        for (PlayerGravityHandler handler : gravityHandlers)
            if (handler.getID() == playerID)
                return handler;

        return null;
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
