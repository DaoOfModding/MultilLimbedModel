package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Models.ExtendableModelRenderer;
import DaoOfModding.mlmanimator.Client.Models.ModelRendererReflection;
import DaoOfModding.mlmanimator.Client.Models.MultiLimbedModel;
import DaoOfModding.mlmanimator.Client.Poses.PlayerPoseHandler;
import DaoOfModding.mlmanimator.Client.Poses.PoseHandler;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MultiLimbedRenderer
{
    // Yeah, I know this is an AWFUL way to do things
    // It's a hack to get around the base ModelRenderer render function being full of private variables
    protected static IRenderTypeBuffer currentBuffer;
    protected static MultiLimbedModel currentModel;
    protected static AbstractClientPlayerEntity currentEntity;
    protected static IVertexBuilder currentVertexBuilder;
    protected static ResourceLocation lastSkin = null;

    private static Field eyeHeightField;
    private static Field thirdPersonField;
    private static Field slimField;
    private static Method cameraMoveFunction;

    private static final double defaultCameraDistance = 0.5f;
    private static double decayingDistance = defaultCameraDistance;

    private static boolean fakeThird = false;

    private static boolean enableFullBodyFirstPerson = true;

    public static void setup()
    {
        // eyeHeight
        eyeHeightField = ObfuscationReflectionHelper.findField(Entity.class,"field_213326_aJ");
        // thirdPerson
        thirdPersonField = ObfuscationReflectionHelper.findField(ActiveRenderInfo.class, "field_216799_k");
        // setPosition
        cameraMoveFunction = ObfuscationReflectionHelper.findMethod(ActiveRenderInfo.class, "func_216775_b", double.class, double.class, double.class);
        // slim
        slimField = ObfuscationReflectionHelper.findField(PlayerModel.class, "field_178735_y");

        try { ModelRendererReflection.setupReflection(); }
        catch (Exception e) { mlmanimator.LOGGER.error("Error reflecting ModelRenderer: " + e); }
    }

    public static void rotateCamera(EntityViewRenderEvent.CameraSetup event)
    {
        if (!fakeThird)
            return;
        
        AbstractClientPlayerEntity player = Minecraft.getInstance().player;
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(player.getUUID());

        if (handler == null)
            return;

        // Adjust the camera pitch based on the direction of the models viewPoint
        double pitch = handler.getPlayerModel().getViewPoint().getNotLookingPitch();
        double oldPitch = handler.getPlayerModel().getViewPoint().getOldNotLookingPitch();

        event.setPitch(event.getPitch() + (float)MathHelper.lerp(event.getRenderPartialTicks(), oldPitch, pitch));

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

    // Toggle on the third person boolean in ActiveRenderInfo to allow the player model to be drawn even when in first person
    public static boolean fakeThirdPersonOn()
    {
        if (!enableFullBodyFirstPerson)
            return false;

        ActiveRenderInfo rendererInfo = Minecraft.getInstance().gameRenderer.getMainCamera();

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
        ActiveRenderInfo rendererInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        Entity viewerEntity = rendererInfo.getEntity();

        // Calculate the camera position and player direction
        Vector3d pos = new Vector3d(MathHelper.lerp((double) partialTicks, viewerEntity.xOld, viewerEntity.getX()), MathHelper.lerp((double) partialTicks, viewerEntity.yOld, viewerEntity.getY()), MathHelper.lerp((double) partialTicks, viewerEntity.zOld, viewerEntity.getZ()));;
        pos = pos.add(0, viewerEntity.getEyeHeight(), 0);

        Vector3d direction = Vector3d.directionFromRotation(0, viewerEntity.getViewYRot((float)partialTicks));

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
    private static double calcCameraDistance(Entity renderViewEntity, double startingDistance, Vector3d direction)
    {
        // Get the players position
        Vector3d pos = renderViewEntity.position().add(0, renderViewEntity.getEyeHeight(), 0);

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
            Vector3d modifiedPos = pos.add(f, f1, f2);

            // Calculate the camera position
            Vector3d testPos = modifiedPos.add(direction.scale(startingDistance));

            // Test the distance between the players and camera position, checking if it's blocked by anything visually
            RayTraceResult raytraceresult = renderViewEntity.level.clip(new RayTraceContext(modifiedPos, testPos, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, renderViewEntity));

            // If it is blocked, check the distance and set that to the new camera distance
            if (raytraceresult.getType() != RayTraceResult.Type.MISS)
            {
                double d0 = raytraceresult.getLocation().distanceTo(pos);
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

        ActiveRenderInfo rendererInfo = Minecraft.getInstance().gameRenderer.getMainCamera();

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

    public static void doModelCalculations(AbstractClientPlayerEntity entityIn, MatrixStack matrixStackIn, float partialTicks, PlayerPoseHandler handler)
    {
        PoseHandler.applyRotations(entityIn, matrixStackIn, 0, partialTicks);

        PoseHandler.doPose(entityIn.getUUID(), partialTicks);

        handler.getPlayerModel().calculateHeightAdjustment();
    }

    public static void adjustEyeHeight(AbstractClientPlayerEntity player, PlayerPoseHandler handler)
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

    public static boolean renderFirstPerson(AbstractClientPlayerEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(entityIn.getUUID());

        if(!enableFullBodyFirstPerson)
            doModelCalculations(entityIn, matrixStackIn, partialTicks, handler);

        adjustEyeHeight(entityIn, handler);

        // Decay the camera pushback so it reverts from being pushed back smoothly rather than being jerked forwards
        decayCameraPushback(partialTicks);

        render2FirstPerson(handler.getPlayerModel(), entityIn, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        return enableFullBodyFirstPerson;
    }

    public static void render2FirstPerson(MultiLimbedModel entityModel, AbstractClientPlayerEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        matrixStackIn.pushPose();

        matrixStackIn.scale(-1.0F, -1.0F, 1.0F);

        currentModel = entityModel;
        currentEntity = entityIn;
        currentBuffer = bufferIn;

        RenderType rendertype = getRenderType(getSkin(currentEntity));

        if (rendertype != null)
        {
            int i = LivingRenderer.getOverlayCoords(entityIn, 0);
            entityModel.renderFirstPerson(matrixStackIn, null, packedLightIn, i, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        lastSkin = null;

        matrixStackIn.popPose();
    }

    public static boolean render(AbstractClientPlayerEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        // Toggle off fake third person if this isn't the player entity
        boolean rememberingFake = isFakeThirdPerson();
        if (rememberingFake && entityIn.getUUID().compareTo(Minecraft.getInstance().player.getUUID()) != 0)
            fakeThirdPersonOff();

        PlayerPoseHandler handler = PoseHandler.getPlayerPoseHandler(entityIn.getUUID());

        adjustEyeHeight(entityIn, handler);

        render2(handler, entityIn, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        // Toggle fake third person back on if necessary
        if (rememberingFake)
            fakeThirdPersonOn();

        return true;
    }

    public static void render2(PlayerPoseHandler handler, AbstractClientPlayerEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        matrixStackIn.pushPose();

        MultiLimbedModel entityModel = handler.getPlayerModel();
        boolean shouldSit = PoseHandler.shouldSit(entityIn);

        entityModel.getBaseModel().riding = shouldSit;

        float f = MathHelper.rotLerp(partialTicks, entityIn.yBodyRotO, entityIn.yBodyRot);
        float f1 = MathHelper.rotLerp(partialTicks, entityIn.yHeadRotO, entityIn.yHeadRot);

        float f2 = f1 - f;
        if (shouldSit && entityIn.getVehicle() instanceof LivingEntity)
        {
            LivingEntity livingentity = (LivingEntity)entityIn.getVehicle();
            f = MathHelper.rotLerp(partialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
            f2 = f1 - f;
            float f3 = MathHelper.wrapDegrees(f2);
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

        float f6 = MathHelper.lerp(partialTicks, entityIn.xRotO, entityIn.xRot);
        if (entityIn.getPose() == Pose.SLEEPING) {
            Direction direction = entityIn.getBedOrientation();
            if (direction != null) {
                float f4 = entityIn.getEyeHeight(Pose.STANDING) - 0.1F;
                matrixStackIn.translate((-direction.getStepX() * f4), 0.0D, (-direction.getStepZ() * f4));
            }
        }

        PoseHandler.applyRotations(entityIn, matrixStackIn, f, partialTicks);

        entityModel.setupAnim(f2, f6);

        PoseHandler.doPose(entityIn.getUUID(), partialTicks);

        entityModel.calculateHeightAdjustment();
        double height = entityModel.getHeightAdjustment();

        matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
        matrixStackIn.translate(0.0D, 0 - height, 0.0D);

        currentModel = entityModel;
        currentEntity = entityIn;
        currentBuffer = bufferIn;

        RenderType rendertype = getRenderType(getSkin(currentEntity));

        if (rendertype != null)
        {
            int i = LivingRenderer.getOverlayCoords(entityIn, 0);
            entityModel.render(matrixStackIn, null, packedLightIn, i, 1.0F, 1.0F, 1.0F, 1.0F);

            entityModel.renderHandItem(false, 0, entityIn, entityIn.getMainHandItem(), matrixStackIn, Minecraft.getInstance().renderBuffers().bufferSource(), packedLightIn);
            entityModel.renderHandItem(true, 1, entityIn, entityIn.getOffhandItem(), matrixStackIn, Minecraft.getInstance().renderBuffers().bufferSource(), packedLightIn);
        }

        lastSkin = null;

        matrixStackIn.popPose();
    }

    // Returns the vertex builder for the current entity
    public static IVertexBuilder getVertexBuilder()
    {
        return getVertexBuilder(getSkin(currentEntity));
    }

    // Returns the vertex builder for the current entity using the supplied skin
    public static IVertexBuilder getVertexBuilder(ResourceLocation resourceLocation)
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
        boolean glowing = currentEntity.isGlowing();

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
            return glowing ? RenderType.outline(resourcelocation) : null;
        }
    }

    public static ResourceLocation getSkin(AbstractClientPlayerEntity EntityIn)
    {
        return EntityIn.getSkinTextureLocation();
    }
}
