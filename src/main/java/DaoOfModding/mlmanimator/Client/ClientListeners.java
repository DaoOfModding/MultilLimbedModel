package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Physics.Gravity;
import DaoOfModding.mlmanimator.Client.Poses.GenericPoses;
import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientListeners
{
    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event)
    {
        // Do nothing on the server
        if (event.side == LogicalSide.SERVER)
            return;

        if (event.phase == TickEvent.Phase.START)
        {
            PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(event.player.getUUID());

            if (handler == null)
                return;

            // handler.setDownRotation(new Vector3f(90, 0, 0));

            event.player.setNoGravity(true);

            if (event.player instanceof ClientPlayerEntity)
                Gravity.move((ClientPlayerEntity)event.player);

            Gravity.fall(event.player);

            handler.updatePosition(event.player.position());

            handler.addPose(GenericPoses.Idle);

            // Tell the PoseHandler that the player is not jumping if they are on the ground or in water
            if (event.player.isOnGround() || event.player.isInWater())
                handler.setJumping(false);
            //  Otherwise check if the player is jumping
            else if (!handler.isJumping())
            {
                // Multiply the down vector by -1 to create an up direction vector
                Vector3f up = handler.getDownVector();
                up.mul(-1);

                // Multiply the movement vector by the up direction vector to get the amount of movement going upwards
                Vector3d upMovement = handler.getMovement().multiply(up.x(), up.y(), up.z());

                // Check if there is any upwards movement and set jumping to true if so
                if (upMovement.x > 0 || upMovement.y > 0 || upMovement.z > 0)
                    handler.setJumping(true);
            }

            // If player is in the water
            if (event.player.isInWater())
            {
                // If player is moving in the water apply swimming pose
                if (event.player.isVisuallySwimming())
                {
                    // TODO: Swimming pose
                }
            }
            else if (PoseHandler.isJumping(event.player.getUUID()))
            {
                // Multiply the down vector by -1 to create an up direction vector
                Vector3f up = handler.getDownVector();
                up.mul(-1);

                // Multiply the movement vector by the up direction vector to get the amount of movement going upwards
                Vector3d upMovement = handler.getMovement().multiply(up.x(), up.y(), up.z());

                // Check if there is any upwards movement and apply the jumping pose if so
                if (upMovement.x > 0 || upMovement.y > 0 || upMovement.z > 0)
                    handler.addPose(GenericPoses.Jumping);
            }
            // If player is moving add the walking pose to the PoseHandler
            else if (handler.getMovement().x != 0 || handler.getMovement().z != 0)
                handler.addPose(GenericPoses.Walking);

            // Update the PoseHandler
            handler.updateRenderPose();

            // Disable player movement for the base Minecraft tick
            Gravity.disableMovement(event.player);
        }
        // Re-enable player movement at the end of the player tick
        else if (event.phase == TickEvent.Phase.END)
            Gravity.enableMovement(event.player);
    }
/*
    @SubscribeEvent
    public static void playerJump(LivingEvent.LivingJumpEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            Gravity.unJump((PlayerEntity)event.getEntity());
            Gravity.jump((PlayerEntity)event.getEntity());
        }
    }*/

    @SubscribeEvent
    public static void renderFirstPerson(RenderHandEvent event)
    {
        // Do nothing unless this is trying to render the main hand
        // Otherwise this will run twice a render
        if (event.getHand() != Hand.MAIN_HAND)
        {
            event.setCanceled(true);
            return;
        }

        // If MultiLimbedRenderer renders the player, cancel the render event
        event.setCanceled(MultiLimbedRenderer.renderFirstPerson(Minecraft.getInstance().player, event.getPartialTicks(), event.getMatrixStack(), event.getBuffers(), event.getLight()));
    }

    @SubscribeEvent
    public static void renderPlayer(RenderPlayerEvent.Pre event)
    {
        // If MultiLimbedRenderer renders the player, cancel the render event
        event.setCanceled(MultiLimbedRenderer.render((AbstractClientPlayerEntity)event.getPlayer(), event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight()));
    }

    @SubscribeEvent
    // Toggle off the third person boolean so that the camera will still render in first person
    public static void renderWorldLast(RenderWorldLastEvent event)
    {
        if (Minecraft.getInstance().level == null)
            return;

        MultiLimbedRenderer.fakeThirdPersonOff();
    }

    @SubscribeEvent
    // Toggle on the third person boolean in ActiveRenderInfo to allow the player model to be drawn even when in first person
    public static void cameraSetup(EntityViewRenderEvent.CameraSetup event)
    {
        if (Minecraft.getInstance().level == null)
            return;

        if (MultiLimbedRenderer.fakeThirdPersonOn())
            MultiLimbedRenderer.pushBackCamera(event.getRenderPartialTicks());

        MultiLimbedRenderer.rotateCamera(event);
    }
}
