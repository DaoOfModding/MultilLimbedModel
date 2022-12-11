package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Models.MultiLimbedModel;
import DaoOfModding.mlmanimator.Client.Poses.GenericPoses;
import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.Shapes;
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

            handler.doDefaultPoses(event.player);
            handler.getPlayerModel().tick(event.player);
        }
    }

    @SubscribeEvent
    public static void playerInteract(PlayerInteractEvent.LeftClickEmpty event)
    {
        PoseHandler.getPlayerPoseHandler(event.getEntity().getUUID()).addPose(GenericPoses.slashing);
    }

    @SubscribeEvent
    public static void renderFirstPerson(RenderHandEvent event)
    {
        // Do nothing unless this is trying to render the main hand
        // Otherwise this will run twice at render
        if (event.getHand() != InteractionHand.MAIN_HAND)
        {
            event.setCanceled(true);
            return;
        }

        // Do nothing if pose handler does not exist or can't be setup
        if (!PoseHandler.setupPoseHandler(Minecraft.getInstance().player))
            return;

        // If MultiLimbedRenderer renders the player, cancel the render event
        event.setCanceled(MultiLimbedRenderer.renderFirstPerson(Minecraft.getInstance().player, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight()));
    }

    @SubscribeEvent
    public static void renderPlayer(RenderPlayerEvent.Pre event)
    {
        // Do nothing if pose handler does not exist or can't be setup
        if (!PoseHandler.setupPoseHandler((AbstractClientPlayer)event.getEntity()))
            return;
/*
        // Update the players bounding box
        PoseHandler.getPlayerPoseHandler(event.getEntity().getUUID()).getPlayerModel().updateBoundingBox(event.getEntity());*/

        // If MultiLimbedRenderer renders the player, cancel the render event
        event.setCanceled(MultiLimbedRenderer.render((AbstractClientPlayer)event.getEntity(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight()));
    }

    @SubscribeEvent
    // Toggle off the third person boolean so that the camera will still render in first person
    public static void renderWorldLast(RenderLevelLastEvent event)
    {
        if (Minecraft.getInstance().level == null)
            return;

        MultiLimbedRenderer.fakeThirdPersonOff();
    }

    @SubscribeEvent
    // Toggle on the third person boolean in Camera to allow the player model to be drawn even when in first person
    public static void cameraSetup(ViewportEvent.ComputeCameraAngles event)
    {
        if (Minecraft.getInstance().level == null)
            return;

        if (MultiLimbedRenderer.fakeThirdPersonOn()) {
            //MultiLimbedRenderer.pushBackCamera(event.getPartialTick());
        }

        MultiLimbedRenderer.rotateCamera(event);

        event.getCamera().tick();
    }

    @SubscribeEvent
    public static void cameraFOV(ViewportEvent.ComputeFov event)
    {
        // Cap the min and max FOV change to stop ridiculous FOV changes at high/low speeds

        double newFov = event.getFOV();

        // TODO: This value has changed, default is at 70 now
        /*if (newFov < 0.95)
            newFov = 0.95;
        if (newFov > 1.15)
            newFov = 1.15;*/

        //System.out.println("FOV: " + newFov);

        event.setFOV(newFov);
    }
}
