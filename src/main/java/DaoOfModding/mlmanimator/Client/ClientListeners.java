package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Poses.GenericPoses;
import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientListeners
{
    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.side == LogicalSide.CLIENT && event.phase == TickEvent.Phase.START)
        {
            PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(event.player.getUUID());

            if (handler == null)
                return;

            handler.updatePosition(event.player.position());

            handler.addPose(GenericPoses.Idle);

            // Tell the PoseHandler that the player is not jumping if they are on the ground or in water
            if (event.player.isOnGround() || event.player.isInWater())
                handler.setJumping(false);
            else if (event.player instanceof RemoteClientPlayerEntity)
                if (handler.getMovement().y != 0)
                    handler.setJumping(true);

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
                handler.addPose(GenericPoses.Jumping);
            // If player is moving add the walking pose to the PoseHandler
            else if (handler.getMovement().x != 0 || handler.getMovement().z != 0)
                handler.addPose(GenericPoses.Walking);

            // Update the PoseHandler
            handler.updateRenderPose();
        }
    }

    @SubscribeEvent
    public static void playerJump(LivingEvent.LivingJumpEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(event.getEntity().getUUID());

            if (handler != null)
                handler.setJumping(true);
        }
    }

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
