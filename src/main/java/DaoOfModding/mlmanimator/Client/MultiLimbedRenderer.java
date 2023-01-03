package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Models.MultiLimbedModel;
import DaoOfModding.mlmanimator.Client.Models.RendererGrabber;
import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    protected static Field eyeHeightField;
    protected static Field thirdPersonField;
    protected static Field slimField;
    protected static Field childField;
    protected static Field dimensions;
    protected static Method moveTowardsClosestSpaceFunction;
    protected static Method cameraMoveFunction;

    protected static Field layers;
    protected static Field parrotModel;
    protected static Field skullModels;

    protected static final double defaultCameraDistance = 0.3f;
    protected static double decayingDistance = defaultCameraDistance;

    protected static boolean fakeThird = false;

    protected static boolean enableFullBodyFirstPerson = true;

    public static void setup()
    {
        // eyeHeight    - ba - f_19816_
        eyeHeightField = ObfuscationReflectionHelper.findField(Entity.class,"f_19816_");
        // thirdPerson / detached   - m - f_90560_
        thirdPersonField = ObfuscationReflectionHelper.findField(Camera.class, "f_90560_");
        // setPosition  - b - m_90584_
        cameraMoveFunction = ObfuscationReflectionHelper.findMethod(Camera.class, "m_90584_", double.class, double.class, double.class);
        // slim - H - f_103380_
        slimField = ObfuscationReflectionHelper.findField(PlayerModel.class, "f_103380_");
        // children - f_104213_
        childField = ObfuscationReflectionHelper.findField(ModelPart.class, "f_104213_");

        // moveTowardsClosestSpace  - b - m_108704_
        moveTowardsClosestSpaceFunction = ObfuscationReflectionHelper.findMethod(LocalPlayer.class, "m_108704_", double.class, double.class);

        // dimensions - aZ - f_19815_
        dimensions = ObfuscationReflectionHelper.findField(Entity.class,"f_19815_");

        // layers - h - f_115291_
        layers = ObfuscationReflectionHelper.findField(LivingEntityRenderer.class,"f_115291_");
        // model - a - f_117290_
        parrotModel = ObfuscationReflectionHelper.findField(ParrotOnShoulderLayer.class,"f_117290_");

        // skullModels - d - f_174473_
        skullModels = ObfuscationReflectionHelper.findField(CustomHeadLayer.class,"f_174473_");

        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setInt(childField, childField.getModifiers() & ~Modifier.FINAL);
        }
        catch (Exception e) { mlmanimator.LOGGER.error("Error clearing final modifier: " + e); }
    }

    public static void setDimensions(Player entity, EntityDimensions value)
    {
        try
        {
            dimensions.set(entity, value);
        }
        catch (Exception e)
        {
            mlmanimator.LOGGER.error("Error setting dimensions at field " + dimensions.getName() + " in " + dimensions.toString() + ": " + e);
        }
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
        PoseHandler.applyRotations(entityIn, PoseStackIn, 0, partialTicks);

        PoseHandler.doPose(entityIn.getUUID(), partialTicks);

        handler.getPlayerModel().calculateHeightAdjustment(entityIn);
    }

    public static void addChild(ModelPart child, String limbName, ModelPart parent)
    {
        try
        {
            Map<String, ModelPart> children = (Map<String, ModelPart>)childField.get(parent);

            children.put(limbName, child);

            childField.set(parent, children);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error adding child");
        }
    }

    public static void removeChild(String limbName, ModelPart parent)
    {
        try
        {
            Map<String, ModelPart> children = (Map<String, ModelPart>)childField.get(parent);

            children.remove(limbName);

            childField.set(parent, children);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error removing child");
        }
    }

    public static void adjustEyeHeight(AbstractClientPlayer player, PlayerPoseHandler handler)
    {
        float eyeHeight = handler.getPlayerModel().calculateEyeHeight() * -1f;

        try
        {
            eyeHeightField.setFloat(player, eyeHeight);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error adjusting player eye height");
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
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(entityIn.getUUID());

        if(!enableFullBodyFirstPerson)
            doModelCalculations(entityIn, PoseStackIn, partialTicks, handler);

        adjustEyeHeight(entityIn, handler);

        render2FirstPerson(handler.getPlayerModel(), entityIn, partialTicks, PoseStackIn, bufferIn, packedLightIn);

        return enableFullBodyFirstPerson;
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

        adjustEyeHeight(entityIn, handler);

        render2(handler, entityIn, partialTicks, PoseStackIn, bufferIn, packedLightIn);

        // Toggle fake third person back on if necessary
        if (rememberingFake)
            fakeThirdPersonOn();

        return true;
    }

    public static void render2(PlayerPoseHandler handler, AbstractClientPlayer entityIn, float partialTicks, PoseStack PoseStackIn, MultiBufferSource bufferIn, int packedLightIn)
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

        PoseHandler.doPose(entityIn.getUUID(), partialTicks);

        entityModel.calculateHeightAdjustment(entityIn);
        double height = entityModel.getHeightAdjustment();

        PoseStackIn.scale(-1.0F, -1.0F, 1.0F);
        PoseStackIn.translate(0.0D, 0 - height, 0.0D);

        currentModel = entityModel;
        currentEntity = entityIn;
        currentBuffer = bufferIn;

        RenderType rendertype = getRenderType(getSkin(currentEntity));

        entityModel.updateArmorsTextures((LocalPlayer) entityIn);

        if (rendertype != null)
        {
            entityModel.lock();

            // Push the model back so it's not directly bellow the camera in first person
            if(MultiLimbedRenderer.isFakeThirdPerson() && entityIn.getUUID().compareTo(Minecraft.getInstance().player.getUUID()) == 0)
            {
                // TODO, adjust this based on head position? - Maybe done, needs tests
                PoseStackIn.translate(0, 0, getCameraDistance());
            }
            else
            {
                // Don't render custom heads for the player in first person
                entityModel.renderHead(PoseStackIn, Minecraft.getInstance().renderBuffers().bufferSource(),packedLightIn,1.0F, 1.0F, 1.0F, 1.0F, entityIn.tickCount);
            }

            int i = LivingEntityRenderer.getOverlayCoords(entityIn, 0);


            entityModel.render(PoseStackIn, packedLightIn, i, 1.0F, 1.0F, 1.0F, 1.0F);
            entityModel.renderShoulder(PoseStackIn, Minecraft.getInstance().renderBuffers().bufferSource(),packedLightIn,1.0F, 1.0F, 1.0F, 1.0F, entityIn.tickCount);

            entityModel.renderHandItem(false, 0, entityIn, entityIn.getMainHandItem(), PoseStackIn, Minecraft.getInstance().renderBuffers().bufferSource(), packedLightIn);
            entityModel.renderHandItem(true, 1, entityIn, entityIn.getOffhandItem(), PoseStackIn, Minecraft.getInstance().renderBuffers().bufferSource(), packedLightIn);

            entityModel.unlock();
        }

        PoseStackIn.popPose();
    }

    public static double getCameraDistance()
    {
        // System.out.println((currentModel.getSize().getDepth() + currentModel.getHeadPos().z) / 2);
        return (currentModel.getSize().getDepth()  / 2) + currentModel.getHeadPos().z;
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

        if (invis)
        {
            return RenderType.itemEntityTranslucentCull(resourcelocation);
        }
        else if (visible)
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
