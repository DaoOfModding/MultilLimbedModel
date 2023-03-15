package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Models.MultiLimbedModel;
import DaoOfModding.mlmanimator.Client.Poses.GenericPoses;
import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import DaoOfModding.mlmanimator.Common.Reflection;
import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Locale;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientListeners
{
    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event)
    {
        // Do nothing on the server
        if (event.side == LogicalSide.SERVER)
            return;

        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(event.player.getUUID());

        if (handler == null)
            return;

        if (event.phase == TickEvent.Phase.START)
        {
            handler.doDefaultPoses(event.player);
            handler.getPlayerModel().tick((AbstractClientPlayer)event.player);
        }
        else if (event.phase == TickEvent.Phase.END)
        {
            // If player is crawling
            if (event.player.hasPose(Pose.SWIMMING) && !event.player.isSwimming())
            {
                // If player doesn't NEED to crawl then cancel the crawl
                if (event.player.level.noCollision(event.player, event.player.getBoundingBox()) && !handler.isCrawling())
                    event.player.setPose(Pose.STANDING);
                else
                    handler.setCrawling(true);
            }
            else
                handler.setCrawling(false);

            // TODO : Hitboxes not working properly when crawling
        }
    }

    @SubscribeEvent
    public static void resize(EntityEvent.Size event)
    {
        if (event.getEntity() instanceof Player)
        {
            // Cancel out the vanilla minecraft changes to dimensions/eye height when the "pose" changes

            event.setNewSize(event.getOldSize());
            event.setNewEyeHeight(event.getOldEyeHeight());
        }
    }

    @SubscribeEvent
    public static void entityTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            // Cancel player repositioning when riding
            if (Minecraft.getInstance().level != null)
                for (Player player : Minecraft.getInstance().level.players())
                    if (player.isPassenger())
                    {
                        // TODO - Ensure this doesn't need another offset based on model height

                        player.setPosRaw(player.position().x, player.position().y - player.getMyRidingOffset(), player.position().z);

                    }
        }
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

        MultiLimbedRenderer.handleLayers((AbstractClientPlayer)event.getEntity(), event.getRenderer());

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
}
