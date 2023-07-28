package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Models.MultiLimbedModel;
import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import DaoOfModding.mlmanimator.Common.Config;
import DaoOfModding.mlmanimator.Common.Reflection;
import DaoOfModding.mlmanimator.Network.PacketHandler;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class MultiLimbedRenderer
{
    // Yeah, I know this is an AWFUL way to do things
    // It's a hack to get around the base ModelRenderer render function being full of protected variables
    protected static MultiBufferSource currentBuffer;
    protected static MultiLimbedModel currentModel;
    protected static AbstractClientPlayer currentEntity;
    protected static VertexConsumer currentVertexBuilder;

    protected static Field thirdPersonField;
    protected static Field slimField;
    protected static Method moveTowardsClosestSpaceFunction;
    protected static Method cameraMoveFunction;

    protected static Field layers;
    protected static Field parrotModel;
    protected static Field skullModels;

    protected static final double defaultCameraDistance = 0.3f;
    protected static double decayingDistance = defaultCameraDistance;

    protected static boolean fakeThird = false;

    protected static boolean enableFullBodyFirstPerson = true;
    public static boolean enableFirstPersonHands = false;

    public static void setup()
    {
        // thirdPerson / detached   - m - f_90560_
        thirdPersonField = ObfuscationReflectionHelper.findField(Camera.class, "f_90560_");
        // setPosition  - b - m_90584_
        cameraMoveFunction = ObfuscationReflectionHelper.findMethod(Camera.class, "m_90584_", double.class, double.class, double.class);
        // slim - H - f_103380_
        slimField = ObfuscationReflectionHelper.findField(PlayerModel.class, "f_103380_");

        // moveTowardsClosestSpace  - b - m_108704_
        moveTowardsClosestSpaceFunction = ObfuscationReflectionHelper.findMethod(LocalPlayer.class, "m_108704_", double.class, double.class);


        // layers - h - f_115291_
        layers = ObfuscationReflectionHelper.findField(LivingEntityRenderer.class,"f_115291_");
        // model - a - f_117290_
        parrotModel = ObfuscationReflectionHelper.findField(ParrotOnShoulderLayer.class,"f_117290_");

        // skullModels - d - f_174473_
        skullModels = ObfuscationReflectionHelper.findField(CustomHeadLayer.class,"f_174473_");

        enableFullBodyFirstPerson = Config.Client.enableFullBodyFirstPerson.get();
        enableFirstPersonHands = Config.Client.vanillaHands();

    }

    public static void rotateCamera(ViewportEvent.ComputeCameraAngles event)
    {
        if (!fakeThird)
            return;
        
        AbstractClientPlayer player = Minecraft.getInstance().player;
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(player.getUUID());

        if (handler == null)
            return;

        // Adjust the camera pitch based on the direction of the models viewPoint
        double pitch = handler.getPlayerModel().getViewPoint().getNotLookingPitch();
        double oldPitch = handler.getPlayerModel().getViewPoint().getOldNotLookingPitch();

        event.setPitch(event.getPitch() + (float)Mth.lerp(event.getPartialTick(), oldPitch, pitch));

    }

    public static boolean isSlim(PlayerModel model)
    {
        boolean slim;

        try
        {
            slim = slimField.getBoolean(model);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error acquiring model slimness");
            return false;
        }

        return slim;
    }

    // Toggle on the third person boolean in Camera to allow the player model to be drawn even when in first person
    public static boolean fakeThirdPersonOn()
    {
        if (!enableFullBodyFirstPerson)
            return false;

        Camera rendererInfo = Minecraft.getInstance().gameRenderer.getMainCamera();

        if (rendererInfo.isDetached())
            return false;

        fakeThird = true;

        try
        {
            thirdPersonField.setBoolean(rendererInfo, true);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error adjusting third person toggle");
            return false;
        }

        return true;
    }

    public static void moveTowardsClosestSpace(LocalPlayer player, double x, double z)
    {
        try
        {
            moveTowardsClosestSpaceFunction.invoke(player, x, z);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error calling moveTowardsClosestSpace - " + e);
        }
    }

    public static boolean isFakeThirdPerson()
    {
        return fakeThird;
    }

    // Toggle off the third person boolean so that the camera will still render in first person
    public static void fakeThirdPersonOff()
    {
        if (!enableFullBodyFirstPerson)
            return;

        if (!fakeThird)
            return;

        Camera rendererInfo = Minecraft.getInstance().gameRenderer.getMainCamera();

        try
        {
            thirdPersonField.setBoolean(rendererInfo, false);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error adjusting third person toggle");
        }

        fakeThird = false;
    }

    public static void doModelCalculations(AbstractClientPlayer entityIn, PoseStack PoseStackIn, float partialTicks, PlayerPoseHandler handler)
    {
        // Push and pop the pose so that these calculations do not effect render position
        PoseStackIn.pushPose();

        PoseHandler.applyRotations(entityIn, PoseStackIn, 0, partialTicks);

        doPose(entityIn, partialTicks, handler);

        PoseStackIn.popPose();
    }

    public static void doPose(AbstractClientPlayer entityIn, float partialTicks, PlayerPoseHandler handler)
    {
        boolean noCollision = entityIn.level.noCollision(entityIn, entityIn.getBoundingBox());

        PoseHandler.doPose(entityIn.getUUID(), partialTicks);

        handler.getPlayerModel().calculateHeightAdjustment(entityIn);

        // If the new pose causes a collision then cancel the pose change
        if (noCollision && !entityIn.level.noCollision(entityIn, entityIn.getBoundingBox()))
        {
            PoseHandler.revertPose(entityIn.getUUID());
            handler.getPlayerModel().calculateHeightAdjustment(entityIn);
            handler.collision = true;
        }
    }

    public static ParrotModel getParrotModel(PlayerRenderer render)
    {
        try
        {
            List<RenderLayer<LivingEntity, EntityModel<LivingEntity>>> layerList = (List<RenderLayer<LivingEntity, EntityModel<LivingEntity>>>)layers.get(render);

            for (RenderLayer<LivingEntity, EntityModel<LivingEntity>> layer : layerList)
                if (layer instanceof ParrotOnShoulderLayer)
                    return (ParrotModel)parrotModel.get(layer);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error acquiring player's parrot");
            return null;
        }

        return null;
    }

    public static SkullModelBase getSkullModel(PlayerRenderer render, SkullBlock.Type skullType)
    {
        try
        {
            List<RenderLayer<LivingEntity, EntityModel<LivingEntity>>> layerList = (List<RenderLayer<LivingEntity, EntityModel<LivingEntity>>>)layers.get(render);

            for (RenderLayer<LivingEntity, EntityModel<LivingEntity>> layer : layerList)
                if (layer instanceof CustomHeadLayer)
                {
                    Map<SkullBlock.Type, SkullModelBase> skullList = (Map<SkullBlock.Type, SkullModelBase>) skullModels.get(layer);

                    return skullList.get(skullType);
                }
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error acquiring custom head");
            return null;
        }

        return null;
    }

    public static void handleLayers(AbstractClientPlayer player, PlayerRenderer renderer)
    {
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(player.getUUID());
        MultiLimbedModel model = handler.getPlayerModel();

        if (model.getParrotModel() == null)
            model.setParrotModel(getParrotModel(renderer));

        model.updateSkull(player, renderer);
    }

    public static boolean renderFirstPerson(AbstractClientPlayer entityIn, float partialTicks, PoseStack PoseStackIn, MultiBufferSource bufferIn, int packedLightIn)
    {
        // Pop the view bobbing pose so view bobbing doesn't mess with first person models
        PoseStackIn.popPose();

        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(entityIn.getUUID());

        if(!enableFullBodyFirstPerson)
            doModelCalculations(entityIn, PoseStackIn, partialTicks, handler);

        render2FirstPerson(handler.getPlayerModel(), entityIn, partialTicks, PoseStackIn, bufferIn, packedLightIn);

        adjustEyeHeight(entityIn, handler);

        // Push the pose again so there are the right number of stacks
        PoseStackIn.pushPose();
        ClientReflection.doBob(PoseStackIn);

        return !enableFirstPersonHands;
    }

    public static boolean shouldRenderHands()
    {
        return enableFirstPersonHands;
    }

    public static void adjustEyeHeight(AbstractClientPlayer player, PlayerPoseHandler handler)
    {
        float oldHeight = player.getEyeHeight();

        float eyeHeight = handler.getPlayerModel().getEyeHeight() * -1;

        Reflection.adjustEyeHeight(player, eyeHeight);

        if (oldHeight != player.getEyeHeight())
            PacketHandler.sendEyeHeightToServer(eyeHeight);
    }

    public static void render2FirstPerson(MultiLimbedModel entityModel, AbstractClientPlayer entityIn, float partialTicks, PoseStack PoseStackIn, MultiBufferSource bufferIn, int packedLightIn)
    {
        PoseStackIn.pushPose();

        PoseStackIn.scale(-1.0F, -1.0F, 1.0F);

        currentModel = entityModel;
        currentEntity = entityIn;
        currentBuffer = bufferIn;

        RenderType rendertype = getRenderType(getSkin(currentEntity));

        if (rendertype != null)
        {
            int i = LivingEntityRenderer.getOverlayCoords(entityIn, 0);
            entityModel.renderFirstPerson(PoseStackIn, packedLightIn, i, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        PoseStackIn.popPose();
    }

    public static boolean render(AbstractClientPlayer entityIn, float partialTicks, PoseStack PoseStackIn, MultiBufferSource bufferIn, int packedLightIn)
    {
        // Toggle off fake third person if this isn't the player entity
        boolean rememberingFake = isFakeThirdPerson();
        if (rememberingFake && entityIn.getUUID().compareTo(Minecraft.getInstance().player.getUUID()) != 0)
            fakeThirdPersonOff();

        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(entityIn.getUUID());

        boolean hideHands = shouldRenderHands();

        if (!rememberingFake || entityIn.getUUID().compareTo(Minecraft.getInstance().player.getUUID()) != 0)
            hideHands = false;

        render2(handler, entityIn, partialTicks, PoseStackIn, bufferIn, packedLightIn, hideHands);

        adjustEyeHeight(entityIn, handler);

        // Toggle fake third person back on if necessary
        if (rememberingFake)
            fakeThirdPersonOn();

        return true;
    }

    public static void render2(PlayerPoseHandler handler, AbstractClientPlayer entityIn, float partialTicks, PoseStack PoseStackIn, MultiBufferSource bufferIn, int packedLightIn, boolean hideHands)
    {
        PoseStackIn.pushPose();

        MultiLimbedModel entityModel = handler.getPlayerModel();

        boolean shouldSit = PoseHandler.shouldSit(entityIn);

        entityModel.getBaseModel().riding = shouldSit;

        float f = Mth.rotLerp(partialTicks, entityIn.yBodyRotO, entityIn.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, entityIn.yHeadRotO, entityIn.yHeadRot);

        float f2 = f1 - f;
        if (shouldSit && entityIn.getVehicle() instanceof LivingEntity)
        {
            LivingEntity livingentity = (LivingEntity)entityIn.getVehicle();
            f = Mth.rotLerp(partialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
            f2 = f1 - f;
            float f3 = Mth.wrapDegrees(f2);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            f = f1 - f3;
            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }

            f2 = f1 - f;
        }

        float f6 = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
        if (entityIn.getPose() == Pose.SLEEPING) {
            Direction direction = entityIn.getBedOrientation();
            if (direction != null) {
                float f4 = entityIn.getEyeHeight(Pose.STANDING) - 0.1F;
                PoseStackIn.translate((-direction.getStepX() * f4), 0.0D, (-direction.getStepZ() * f4));
            }
        }

        PoseHandler.applyRotations(entityIn, PoseStackIn, f, partialTicks);

        entityModel.setupAnim(f2, f6);

        doPose(entityIn, partialTicks, handler);

        double height = entityModel.getHeightAdjustment();

        PoseStackIn.scale(-1.0F, -1.0F, 1.0F);
        PoseStackIn.translate(0.0D, 0 - height, 0.0D);

        currentModel = entityModel;
        currentEntity = entityIn;
        currentBuffer = bufferIn;

        RenderType rendertype = getRenderType(getSkin(currentEntity));

        entityModel.updateArmorsTextures(entityIn);

        entityModel.lock();

        if (rendertype != null)
        {
            // Push the model back so it's not directly below the camera in first person
            if (MultiLimbedRenderer.isFakeThirdPerson() && entityIn.getUUID().compareTo(Minecraft.getInstance().player.getUUID()) == 0)
            {
                Vec3 pushBack = getCameraDistance();
                PoseStackIn.translate(pushBack.x, pushBack.y, pushBack.z);
            } else {
                // Don't render custom heads for the player in first person
                entityModel.renderHead(PoseStackIn, Minecraft.getInstance().renderBuffers().bufferSource(), packedLightIn, 1.0F, 1.0F, 1.0F, 1.0F, entityIn.tickCount);
            }

            int i = LivingEntityRenderer.getOverlayCoords(entityIn, 0);

            // Cancel out the vanilla model repositioning when crouching
            if (entityIn.isCrouching())
                PoseStackIn.translate(0, -0.125, 0);

            entityModel.render(PoseStackIn, packedLightIn, i, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        entityModel.renderShoulder(PoseStackIn, bufferSource, packedLightIn,1.0F, 1.0F, 1.0F, 1.0F, entityIn.tickCount);

        if (!hideHands)
        {
            entityModel.renderHandItem(false, 0, entityIn, entityIn.getMainHandItem(), PoseStackIn, bufferSource, packedLightIn);
            entityModel.renderHandItem(true, 1, entityIn, entityIn.getOffhandItem(), PoseStackIn, bufferSource, packedLightIn);
        }

        entityModel.unlock();

        PoseStackIn.popPose();
    }

    public static Vec3 getCameraDistance()
    {
        // TODO: This only works if the head is attached directly to the body
        double xlocation = currentModel.getViewPoint().getRotationPoint().x;
        double zlocation = currentModel.getViewPoint().getRotationPoint().z;

        // Convert the yBody rotation to a value between 0-360
        float angle = Mth.wrapDegrees(Minecraft.getInstance().player.yBodyRot);


        // Flip the depth and width around if the body is facing to the left or right
        double zDistance = currentModel.getSize().getDepth();
        double xDistance = currentModel.getSize().getWidth();

        // TODO: ...these angles seem wrong, but it's what is necessary for it to work properly. Perhaps something is borked somewhere else...
        if ((angle > 45 && angle < 135) || (angle > -180 && angle < -135))
        {
            zDistance = currentModel.getSize().getWidth();
            xDistance = currentModel.getSize().getDepth();
        }

        zDistance *= zlocation;
        xDistance *= xlocation;


        // This would be nice if this was moving the camera, but causes to much obvious movement when moving the model
        /*
        // Get the angle the head is looking at compared to the body's angle
        double xzMixer = Math.toDegrees(currentModel.getLookVector().y);

        // If the head is looking entirely to the side (90 degrees) then push back based on the xDistance
        // If it's looking straight ahead push back based on the zDistance
        xzMixer = xzMixer / 90.0;

        double negxDistance = xDistance * xzMixer;

        if (xzMixer < 0)
            xzMixer = xzMixer * -1;

        double distance = zDistance * (1-xzMixer);

        return new Vec3(negxDistance, 0, distance);*/

        return new Vec3(0, 0, zDistance);
    }

    // Returns the vertex builder for the current entity
    public static VertexConsumer getVertexBuilder()
    {
        return getVertexBuilder(getSkin(currentEntity));
    }

    // Returns the vertex builder for the current entity using the supplied skin
    public static VertexConsumer getVertexBuilder(ResourceLocation resourceLocation)
    {
        // If the last vertexbuilder call used the same skin, then don't bother recreating it
        RenderType rendertype = getRenderType(resourceLocation);
        currentVertexBuilder = currentBuffer.getBuffer(rendertype);

        return currentVertexBuilder;
    }

    public static RenderType getRenderType(ResourceLocation resourcelocation)
    {
        boolean invis = currentEntity.isInvisible();
        boolean visible = !invis && !currentEntity.isInvisibleTo(Minecraft.getInstance().player);

        if (visible)
        {
            return currentModel.renderType(resourcelocation);
        }
        else
        {
            return null;
        }
    }

    public static ResourceLocation getSkin(AbstractClientPlayer EntityIn)
    {
        return EntityIn.getSkinTextureLocation();
    }

}
