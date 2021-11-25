package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Poses.GenericPoses;
import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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

            handler.doDefaultPoses((ClientPlayerEntity)event.player);
            handler.getPlayerModel().tick((ClientPlayerEntity)event.player);
        }
    }

    @SubscribeEvent
    public static void playerInteract(PlayerInteractEvent.LeftClickEmpty event)
    {
        PoseHandler.getPlayerPoseHandler(event.getPlayer().getUUID()).addPose(GenericPoses.slashing);
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

        // Do nothing if pose handler does not exist or can't be setup
        if (!PoseHandler.setupPoseHandler(Minecraft.getInstance().player))
            return;

        // If MultiLimbedRenderer renders the player, cancel the render event
        event.setCanceled(MultiLimbedRenderer.renderFirstPerson(Minecraft.getInstance().player, event.getPartialTicks(), event.getMatrixStack(), event.getBuffers(), event.getLight()));
    }

    @SubscribeEvent
    public static void renderPlayer(RenderPlayerEvent.Pre event)
    {
        // Do nothing if pose handler does not exist or can't be setup
        if (!PoseHandler.setupPoseHandler((AbstractClientPlayerEntity)event.getPlayer()))
            return;

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

    @SubscribeEvent
    public static void cameraFOV(FOVUpdateEvent event)
    {
        // Cap the min and max FOV change to stop ridiculous FOV changes at high/low speeds

        float newFov = event.getNewfov();
        if (newFov < 0.95f)
            newFov = 0.95f;
        if (newFov > 1.15f)
            newFov = 1.15f;

        event.setNewfov(newFov);
    }
}
