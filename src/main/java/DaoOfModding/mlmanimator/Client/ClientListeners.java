package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Models.MultiLimbedModel;
import DaoOfModding.mlmanimator.Client.Poses.GenericPoses;
import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import DaoOfModding.mlmanimator.Common.Config;
import DaoOfModding.mlmanimator.Common.Reflection;
import DaoOfModding.mlmanimator.Server.ServerBoundingBoxHandler;
import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
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
    protected static boolean collision = false;

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event)
    {
        // Do nothing on the server
        if (event.side == LogicalSide.SERVER)
            return;

        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(event.player.getUUID());

        if (handler == null)
            return;

        collision = handler.collision;
        handler.collision = false;

        if (event.phase == TickEvent.Phase.START)
        {
            // If there is a collision and the player was crouching in the last few ticks, remain crouching
            if (collision && handler.wasCrouching())
            {
                setCrouch(event.player, true);
                handler.setCrouching(event.player.isCrouching());
            }

            handler.doDefaultPoses(event.player);
            handler.tick((AbstractClientPlayer)event.player);
            handler.getPlayerModel().resetItemScale();
        }
        else if (event.phase == TickEvent.Phase.END)
        {
            // Update the PoseHandler
            handler.updateRenderPose();

            // Delay crawl calculations slightly on game load
            //if (event.player.getUUID().compareTo(Minecraft.getInstance().player.getUUID()) == 0)

            // If player is crawling
            if (event.player.hasPose(Pose.SWIMMING) && !event.player.isSwimming())
            {
                Boolean crawlNoCollision = event.player.level.noCollision(event.player, event.player.getBoundingBox());

                // If player doesn't NEED to crawl then cancel the crawl
                if (crawlNoCollision && !handler.isCrawling())
                {
                    handler.setCrawling(false);

                    if (event.player.isShiftKeyDown())
                        setCrouch(event.player, true);
                    else
                        event.player.setPose(Pose.STANDING);
                }
                else
                {
                    handler.setCrawling(true);
                }
            }
            else
                handler.setCrawling(false);

            handler.setCrouching(event.player.isCrouching());

            handler.getPlayerModel().handleBBChange(event.player, !handler.isCrawling());
        }
    }

    protected static void setCrouch(Player player, boolean on)
    {
        if (player instanceof LocalPlayer)
            ClientReflection.setCrouch((LocalPlayer) player, on);
        else if (on)
            player.setPose(Pose.CROUCHING);
        else
            player.setPose(Pose.STANDING);
    }

    @SubscribeEvent
    public static void movement(MovementInputUpdateEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(event.getEntity().getUUID());

            if (handler == null)
                return;

            // Cancel crouching if the player was not previously crouching and is not holding shift
            if (event.getEntity().isCrouching() && !handler.wasCrouching() && !event.getEntity().isShiftKeyDown())
                setCrouch(event.getEntity(), false);

            // If the player is neither crouching or crawling then cancel movement slowdown
            if (!event.getEntity().isCrouching() && !handler.wasCrouching() && !handler.isCrawling())
                event.getInput().tick(false, 1);

            // Otherwise ensure it is applied
            else
            {
                float f = Mth.clamp(0.3F + EnchantmentHelper.getSneakingSpeedBonus(event.getEntity()), 0.0F, 1.0F);
                event.getInput().tick(true, f);
            }
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
        if (Minecraft.getInstance() == null)
            return;

        if (event.phase == TickEvent.Phase.END)
        {
            // Cancel player repositioning when riding
            if (Minecraft.getInstance().level != null)
                for (Player player : Minecraft.getInstance().level.players())
                    if (player.isPassenger()) {
                        // TODO - Ensure this doesn't need another offset based on model height

                        player.setPosRaw(player.position().x, player.position().y - player.getMyRidingOffset(), player.position().z);
                    }
        }
    }

    @SubscribeEvent
    public static void renderFirstPerson(RenderHandEvent event)
    {
        // TODO: TEMP fix
        if (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof MapItem)
        {
            MultiLimbedRenderer.enableFirstPersonHands = true;
        }
        else
            MultiLimbedRenderer.enableFirstPersonHands = Config.Client.vanillaHands();

        // Do nothing unless this is trying to render the main hand
        // Otherwise this will run twice at render
        if (event.getHand() != InteractionHand.MAIN_HAND)
        {
            if (!MultiLimbedRenderer.shouldRenderHands())
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

        MultiLimbedRenderer.handleLayers((AbstractClientPlayer)event.getEntity(), event.getRenderer());

        // If MultiLimbedRenderer renders the player, cancel the render event
        event.setCanceled(MultiLimbedRenderer.render((AbstractClientPlayer)event.getEntity(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), ClientReflection.isRenderingInventory()));
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
    public static void playerDisconnects(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity().getUUID().compareTo(Minecraft.getInstance().player.getUUID()) == 0)
        {
            PoseHandler.clear();
        }
    }
}
