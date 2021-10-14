package DaoOfModding.mlmanimator.Client.Models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;

public class MultiLimbedModel
{
    private float sizeScale = 1;
    private double defaultHeight = 1.5;

    private static float defaultEyeHeight = 2;

    private float lowestModelHeight = 0;

    private Vector3d lookVector = new Vector3d(0, 0, 0);

    PlayerModel baseModel;

    ExtendableModelRenderer body;
    HashMap<String, ExtendableModelRenderer> limbs = new HashMap<String, ExtendableModelRenderer>();

    ExtendableModelRenderer viewPoint;
    HashMap<String, ExtendableModelRenderer> firstPersonLimbs = new HashMap<String, ExtendableModelRenderer>();

    HashMap<String, ExtendableModelRenderer> allLimbs = new HashMap<String, ExtendableModelRenderer>();

    private boolean lock = false;

    public MultiLimbedModel(PlayerModel model)
    {
        this(model, true);
    }

    public MultiLimbedModel(PlayerModel model, boolean withLimbs)
    {
        baseModel = model;

        if (withLimbs)
            setupDefaultLimbs();
    }

    private void setupDefaultLimbs()
    {
        //TODO: Setup armor models

        ExtendableModelRenderer body = new ExtendableModelRenderer(baseModel, 16, 16);
        body.setPos(0, 0, 0);
        body.extend(GenericResizers.getBodyResizer());

        ExtendableModelRenderer head = new ExtendableModelRenderer(baseModel, 0, 0);
        head.setRotationPoint(new Vector3d(0.5, 0, 0.5));
        head.setPos(0.5F, 0, 0.5F);
        head.extend(GenericResizers.getHeadResizer());
        head.setLooking(true);
        head.setFirstPersonRender(false);

        ExtendableModelRenderer rightArm = new ExtendableModelRenderer(baseModel, 40, 16);
        rightArm.setRotationPoint(new Vector3d(0, 0.75, 0.5));
        rightArm.setPos(0, 0F, 0.5F);
        rightArm.setFixedPosAdjustment(0, 1.5F, 0);
        rightArm.extend(GenericResizers.getLimbResizer());

        ExtendableModelRenderer leftArm = new ExtendableModelRenderer(baseModel, 32, 48);
        leftArm.setRotationPoint(new Vector3d(1, 0.75, 0.5));
        leftArm.setPos(1, 0F, 0.5F);
        leftArm.setFixedPosAdjustment(0, 1.5F, 0);
        leftArm.extend(GenericResizers.getLimbResizer());
        leftArm.mirror = true;

        ExtendableModelRenderer rightLeg = new ExtendableModelRenderer(baseModel, 0, 16);
        rightLeg.setPos(0F, 1.0F, 0.5F);
        rightLeg.setRotationPoint(new Vector3d(1, 1, 0.5));
        rightLeg.extend(GenericResizers.getLimbResizer());

        ExtendableModelRenderer leftLeg = new ExtendableModelRenderer(baseModel, 0, 16);
        leftLeg.setPos(1F, 1.0F, 0.5F);
        leftLeg.setRotationPoint(new Vector3d(0, 1, 0.5));
        leftLeg.extend(GenericResizers.getLimbResizer());
        leftLeg.mirror = true;

        addBody(body);
        addLimb(GenericLimbNames.head, head);
        addLimb(GenericLimbNames.leftArm, leftArm);
        addLimb(GenericLimbNames.rightArm, rightArm);
        addLimb(GenericLimbNames.leftLeg, leftLeg);
        addLimb(GenericLimbNames.rightLeg, rightLeg);
        addLimbReference(GenericLimbNames.lowerLeftArm, leftArm.getChildren().get(0));
        addLimbReference(GenericLimbNames.lowerRightArm, rightArm.getChildren().get(0));
        addLimbReference(GenericLimbNames.lowerLeftLeg, leftLeg.getChildren().get(0));
        addLimbReference(GenericLimbNames.lowerRightLeg, rightLeg.getChildren().get(0));

        setViewPoint(head);
    }

    public PlayerModel getBaseModel()
    {
        return baseModel;
    }

    public void setViewPoint(ExtendableModelRenderer model)
    {
        viewPoint = model;
    }

    // Stop multi-threading breaking stuff via locking
    private void lock()
    {
        while (lock) {}

        lock = true;
    }

    private void unlock()
    {
        lock = false;
    }

    public ExtendableModelRenderer getViewPoint()
    {
        return viewPoint;
    }

    // Returns a list of all limbs on this model
    public Set<String> getLimbs()
    {
        return limbs.keySet();
    }

    // Returns a list of all first person limbs on this model
    public Set<String> getAllLimbs()
    {
        return allLimbs.keySet();
    }

    public ExtendableModelRenderer getBody()
    {
        return body;
    }

    // Apply the supplied rotations to the specified limb
    public void rotateLimb(String limb, Vector3d angles)
    {
        if (!hasLimb(limb))
            return;

        ModelRenderer limbModel = getLimb(limb);

        if (limbModel == null)
            limbModel = getFirstPersonLimb(limb);

        limbModel.xRot = (float) angles.x;
        limbModel.yRot = (float) angles.y;
        limbModel.zRot = (float) angles.z;
    }

    // Returns true if this model contains the specified limb
    public boolean hasLimb(String limb)
    {
        return allLimbs.containsKey(limb);
    }

    public ExtendableModelRenderer getLimb(String limb)
    {
        return limbs.get(limb);
    }

    public void addBody(ExtendableModelRenderer bodyModel)
    {
        lock();

        ExtendableModelRenderer newBody = bodyModel.clone();

        limbs.put(GenericLimbNames.body, newBody);
        allLimbs.put(GenericLimbNames.body, newBody);

        if (body != null)
            body.fosterChildren(newBody);

        body = newBody;

        unlock();
    }

    // Adds specified limb onto the body
    public void addLimb(String limb, ExtendableModelRenderer limbModel)
    {
        addLimb(limb, limbModel, GenericLimbNames.body);
    }

    // Adds specified limb onto the specified limb
    public void addLimb(String limb, ExtendableModelRenderer limbModel, String addTo)
    {
        lock();

        ExtendableModelRenderer toAdd = getLimb(addTo);

        if (toAdd == null)
            toAdd = getFirstPersonLimb(addTo);

        if (toAdd != null)
        {
            toAdd.addChild(limbModel);
            addLimbReference(limb, limbModel);
        }

        unlock();
    }

    public void removeLimb(String limb)
    {
        lock();

        body.removeChild(limbs.get(limb));
        limbs.remove(limb);
        allLimbs.remove(limb);

        unlock();
    }

    // Adds specified limb into the first person render list
    public void addFirstPersonLimb(String limb, ExtendableModelRenderer limbModel)
    {
        firstPersonLimbs.put(limb, limbModel);
        allLimbs.put(limb, limbModel);
    }

    public ExtendableModelRenderer getFirstPersonLimb(String limb)
    {
        return firstPersonLimbs.get(limb);
    }


    // Add a limb for reference purposes
    // Usually used for referencing child limbs
    public void addLimbReference(String limb, ExtendableModelRenderer limbModel)
    {
        limbs.put(limb, limbModel);
        allLimbs.put(limb, limbModel);
    }

    public void prepareMobModel(PlayerEntity entityIn, float limbSwing, float limbSwingAmount, float partialTick)
    {
        // baseModel.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
    }

    public void setupAnim(float netHeadYaw, float headPitch)
    {
        lookVector = new Vector3d(headPitch * ((float)Math.PI / 180F), netHeadYaw * ((float)Math.PI / 180F), 0);

        // baseModel.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public Vector3d getLookVector()
    {
        return lookVector;
    }

    public RenderType renderType(ResourceLocation resourcelocation)
    {
        return baseModel.renderType(resourcelocation);
    }

    public void renderFirstPerson(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        lock();

        matrixStackIn.pushPose();

        for (ExtendableModelRenderer model : firstPersonLimbs.values())
            model.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        matrixStackIn.popPose();

        unlock();
    }

    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        lock();

        matrixStackIn.pushPose();

        // Scale the model to match the scale size, and move it up or down so it's standing at the right height
        matrixStackIn.translate(0.0D, (1-sizeScale) * defaultHeight, 0.0D);
        matrixStackIn.scale(sizeScale, sizeScale, sizeScale);

        // Render the body, as all limbs are children or sub-children of the body, this should render everything
        body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        matrixStackIn.popPose();

        unlock();
    }

    public float calculateEyeHeight()
    {
        ExtendableModelRenderer viewModel = viewPoint;

        LinkedList<ExtendableModelRenderer> parts = new LinkedList<ExtendableModelRenderer>();
        parts.push(viewPoint);

        // Cycle through the viewModel and add each element to the linked list
        while(viewModel.getParent() != null)
        {
            viewModel = viewModel.getParent();
            parts.push(viewModel);
        }

        // Rotate the MatrixStack around each parent part (but do not apply viewPoint rotations)
        MatrixStack stack = new MatrixStack();

        while(parts.size() > 1)
        {
            viewModel = parts.pop();
            viewModel.rotateMatrix(stack);
        }

        viewModel = parts.pop();

        // Return the rotated height (moved down by the default eye height so it's not at the top of the head)
        return (float)((viewModel.getTop(stack) + defaultEyeHeight) * sizeScale / 16) - getHeightAdjustment();
    }

    // Calculate the height adjustment for each limb
    public void calculateHeightAdjustment()
    {
        body.calculateMinHeight(new MatrixStack());

        lowestModelHeight = getHeightAdjustment(body, Float.MAX_VALUE * -1) * sizeScale / 16;
    }

    // Find the limb at the lowest height and return it's height
    public float getHeightAdjustment()
    {
        return lowestModelHeight;
    }

    // Find the minimum height of the provided limb and all of it's children compared to the provided value
    private float getHeightAdjustment(ExtendableModelRenderer limbModel, float lowest)
    {
        float testHeight = limbModel.getMinHeight();

        if (testHeight > lowest)
            lowest = testHeight;

        for (ExtendableModelRenderer testLimb : limbModel.getChildren())
            lowest = getHeightAdjustment(testLimb, lowest);

        return lowest;
    }
}
