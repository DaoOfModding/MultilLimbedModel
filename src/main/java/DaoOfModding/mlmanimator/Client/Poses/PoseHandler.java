package DaoOfModding.mlmanimator.Client.Poses;

import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PoseHandler
{
    private static List<PlayerPoseHandler> poses = new ArrayList<PlayerPoseHandler>();

    public static boolean setupPoseHandler(AbstractClientPlayerEntity player)
    {
        // Do nothing if pose handler for this player already exists
        for (PlayerPoseHandler handler : poses)
            if (handler.getID() == player.getUUID())
                return true;

        if (!player.isSkinLoaded())
            return false;

        PlayerRenderer renderer = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().get(player.getModelName());

        PlayerPoseHandler newHandler = new PlayerPoseHandler(player.getUUID(), renderer.getModel());
        poses.add(newHandler);

        return true;
    }

    public static PlayerPoseHandler getPlayerPoseHandler(UUID playerID)
    {
        for (PlayerPoseHandler handler : poses)
            if (handler.getID() == playerID)
                return handler;

        return null;
    }

    public static void applyRotations(PlayerEntity entityLiving, MatrixStack matrixStackIn, float rotationYaw, float partialTicks)
    {
        Pose pose = entityLiving.getPose();
        if (pose != Pose.SLEEPING) {
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F - rotationYaw));
        }

        if (entityLiving.deathTime > 0)
        {
            float f = ((float)entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(f * 90.0F));
        }
        else if (pose == Pose.SLEEPING)
        {
            Direction direction = entityLiving.getBedOrientation();
            float f1 = direction != null ? getFacingAngle(direction) : rotationYaw;
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(f1));
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(270.0F));
        }
    }

    public static boolean isJumping(UUID playerID)
    {
        PlayerPoseHandler handler = getPlayerPoseHandler(playerID);

        if (handler != null)
            return handler.isJumping();

        return false;
    }

    public static Vector3d getMovement(UUID playerID)
    {
        PlayerPoseHandler handler = getPlayerPoseHandler(playerID);

        if (handler != null)
            return handler.getMovement();

        return new Vector3d(0, 0, 0);
    }

    public static void doPose(UUID playerID, float partialTicks)
    {
        PlayerPoseHandler handler = getPlayerPoseHandler(playerID);

        if (handler != null)
            handler.doPose(partialTicks);
    }

    public static void addPose(UUID PlayerID, PlayerPose pose)
    {
        PlayerPoseHandler handler = getPlayerPoseHandler(PlayerID);

        if (handler != null)
            handler.addPose(pose);
    }

    public static boolean shouldSit(PlayerEntity entityIn)
    {
        return entityIn.isPassenger() && entityIn.getVehicle().shouldRiderSit();
    }

    private static float getFacingAngle(Direction facingIn)
    {
        switch(facingIn) {
            case SOUTH:
                return 90.0F;
            case WEST:
                return 0.0F;
            case NORTH:
                return 270.0F;
            case EAST:
                return 180.0F;
            default:
                return 0.0F;
        }
    }
}
