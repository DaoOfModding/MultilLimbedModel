package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Models.ModelRendererReflection;
import DaoOfModding.mlmanimator.Client.Models.MultiLimbedModel;
import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
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
    // It's a hack to get around the base ModelRenderer render function being full of private variables
    protected static MultiBufferSource currentBuffer;
    protected static MultiLimbedModel currentModel;
    protected static AbstractClientPlayer currentEntity;
    protected static VertexConsumer currentVertexBuilder;
    protected static ResourceLocation lastSkin = null;

    private static Field eyeHeightField;
    private static Field thirdPersonField;
    private static Field slimField;
    private static Field cubeField;
    private static Field childField;
    private static Method cameraMoveFunction;

    private static final double defaultCameraDistance = 0.5f;
    private static double decayingDistance = defaultCameraDistance;

    private static boolean fakeThird = false;

    private static boolean enableFullBodyFirstPerson = true;

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
        // cubes - m - f_104212_
        cubeField = ObfuscationReflectionHelper.findField(ModelPart.class, "f_104212_");
        // children - f_104213_
        childField = ObfuscationReflectionHelper.findField(ModelPart.class, "f_104213_");

        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setInt(cubeField, cubeField.getModifiers() & ~Modifier.FINAL);
            modifiers.setInt(childField, cubeField.getModifiers() & ~Modifier.FINAL);
        }
        catch (Exception e) { mlmanimator.LOGGER.error("Error clearing final modifier: " + e); }

        try { ModelRendererReflection.setupReflection(); }
        catch (Exception e) { mlmanimator.LOGGER.error("Error reflecting ModelPart: " + e); }
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

    // Push the camera to be in front of the player, but not so far in front that it sees through blocks
    public static void pushBackCamera(double partialTicks)
    {
        Camera rendererInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        Entity viewerEntity = rendererInfo.getEntity();

        // Calculate the camera position and player direction
        Vec3 pos = new Vec3(Mth.lerp((double) partialTicks, viewerEntity.xOld, viewerEntity.getX()), Mth.lerp((double) partialTicks, viewerEntity.yOld, viewerEntity.getY()), Mth.lerp((double) partialTicks, viewerEntity.zOld, viewerEntity.getZ()));
        pos = pos.add(0, viewerEntity.getEyeHeight(), 0);

        Vec3 direction = Vec3.directionFromRotation(0, viewerEntity.getViewYRot((float)partialTicks));

        // Calculate the amount the camera needs to be pushed back to not hit a wall, set decayingDistance to equal that amount if it is smaller than it
        double cameraPush = calcCameraDistance(rendererInfo.getEntity(), defaultCameraDistance + 0.15, direction) - 0.15;

        if (cameraPush < decayingDistance)
            decayingDistance = cameraPush;

        // Pushing camera position forward by decayingDistance across the X & Z axies
        pos = pos.add(direction.scale(decayingDistance));

        // Move the camera so that it's just in front of the head rather than inside it
        // Adjust the amount by decayingDistance so that it is pushed back enough to not see through walls
        try
        {
            cameraMoveFunction.invoke(rendererInfo, pos.x, pos.y, pos.z);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error adjusting camera position");
        }
    }

    private static void decayCameraPushback(float partialTick)
    {
        if (decayingDistance == defaultCameraDistance)
            return;

        decayingDistance += partialTick / 40.0;

        if (decayingDistance > defaultCameraDistance)
            decayingDistance = defaultCameraDistance;
    }

    // Calculate the distance the camera should be from the specified entity (up to a max of startingDistance)
    private static double calcCameraDistance(Entity renderViewEntity, double startingDistance, Vec3 direction)
    {
        // Get the players position
        Vec3 pos = renderViewEntity.position().add(0, renderViewEntity.getEyeHeight(), 0);

        // Test against 8 separate points
        for(int i = 0; i < 8; ++i)
        {
            float f = (float) ((i & 1) * 2 - 1);
            float f1 = (float) ((i >> 1 & 1) * 2 - 1);
            float f2 = (float) ((i >> 2 & 1) * 2 - 1);
            f = f * 0.15F;
            f1 = f1 * 0.15F;
            f2 = f2 * 0.15F;

            // Calculate the position to test
            Vec3 modifiedPos = pos.add(f, f1, f2);

            // Calculate the camera position
            Vec3 testPos = modifiedPos.add(direction.scale(startingDistance));

            // Test the distance between the players and camera position, checking if it's blocked by anything visually
            HitResult result = renderViewEntity.level.clip(new ClipContext(modifiedPos, testPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, renderViewEntity));

            // If it is blocked, check the distance and set that to the new camera distance
            if (result.getType() != HitResult.Type.MISS)
            {
                double d0 = result.getLocation().distanceTo(pos);
                if (d0 < startingDistance)
                    startingDistance = d0;
            }
        }

        return startingDistance;
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

        handler.getPlayerModel().calculateHeightAdjustment();
    }

    public static void clearCubes(ModelPart model)
    {
        try
        {
            cubeField.set(model, new ObjectArrayList<>());
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error adjusting model cubes");
        }
    }

    public static void addCube(ModelPart model, ModelPart.Cube toAdd)
    {
        try
        {
            List<ModelPart.Cube> cubes = getCubes(model);

            cubes.add(toAdd);

            cubeField.set(model, cubes);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error adjusting model cubes");
        }
    }

    public static List<ModelPart.Cube> getCubes(ModelPart model)
    {
        try
        {
            return (List<ModelPart.Cube>)cubeField.get(model);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error getting cubes");
        }

        return null;
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

        float eyeHeight = handler.getPlayerModel().calculateEyeHeight() * -1;

        try
        {
            eyeHeightField.setFloat(player, eyeHeight);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error adjusting player eye height");
        }
    }

    public static boolean renderFirstPerson(AbstractClientPlayer entityIn, float partialTicks, PoseStack PoseStackIn, MultiBufferSource bufferIn, int packedLightIn)
    {
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(entityIn.getUUID());

        if(!enableFullBodyFirstPerson)
            doModelCalculations(entityIn, PoseStackIn, partialTicks, handler);

        adjustEyeHeight(entityIn, handler);

        // Decay the camera pushback so it reverts from being pushed back smoothly rather than being jerked forwards
        decayCameraPushback(partialTicks);

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
            entityModel.renderFirstPerson(PoseStackIn, null, packedLightIn, i, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        lastSkin = null;

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

        entityModel.calculateHeightAdjustment();
        double height = entityModel.getHeightAdjustment();

        PoseStackIn.scale(-1.0F, -1.0F, 1.0F);
        PoseStackIn.translate(0.0D, 0 - height, 0.0D);

        currentModel = entityModel;
        currentEntity = entityIn;
        currentBuffer = bufferIn;

        RenderType rendertype = getRenderType(getSkin(currentEntity));

        if (rendertype != null)
        {
            int i = LivingEntityRenderer.getOverlayCoords(entityIn, 0);
            entityModel.render(PoseStackIn, null, packedLightIn, i, 1.0F, 1.0F, 1.0F, 1.0F);

            entityModel.renderHandItem(false, 0, entityIn, entityIn.getMainHandItem(), PoseStackIn, Minecraft.getInstance().renderBuffers().bufferSource(), packedLightIn);
            entityModel.renderHandItem(true, 1, entityIn, entityIn.getOffhandItem(), PoseStackIn, Minecraft.getInstance().renderBuffers().bufferSource(), packedLightIn);
        }

        lastSkin = null;

        PoseStackIn.popPose();
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
        if (lastSkin != resourceLocation)
        {
            RenderType rendertype = getRenderType(resourceLocation);
            currentVertexBuilder = currentBuffer.getBuffer(rendertype);
        }

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
