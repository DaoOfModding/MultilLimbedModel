package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class MultiLimbedModel
{
    private float sizeScale = 1;
    private double defaultHeight = 1.5;

    private static float defaultEyeHeight = 2;

    private float lowestModelHeight = 0;

    private Vec3 lookVector = new Vec3(0, 0, 0);

    private MultiLimbedDimensions size = null;

    private boolean slim = false;

    PlayerModel baseModel;

    ExtendableModelRenderer body;
    HashMap<String, ExtendableModelRenderer> limbs = new HashMap<String, ExtendableModelRenderer>();

    ExtendableModelRenderer viewPoint;
    HashMap<String, ExtendableModelRenderer> firstPersonLimbs = new HashMap<String, ExtendableModelRenderer>();

    HashMap<String, ExtendableModelRenderer> allLimbs = new HashMap<String, ExtendableModelRenderer>();

    HashMap<String, ParticleEmitter> emitters = new HashMap<String, ParticleEmitter>();

    private boolean lock = false;

    ArrayList<ExtendableModelRenderer> hands = new ArrayList<>();

    TextureHandler textures = new TextureHandler();


    public MultiLimbedModel(PlayerModel model)
    {
        this(model, true);
    }

    public MultiLimbedModel(PlayerModel model, boolean withLimbs)
    {
        baseModel = model;

        slim = MultiLimbedRenderer.isSlim(model);

        if (withLimbs)
            setupDefaultLimbs();
    }

    public TextureHandler getTextureHandler()
    {
        return textures;
    }

    public void setTextureHandler(TextureHandler handler)
    {
        textures = handler;
    }

    private void setupDefaultLimbs()
    {
        //TODO: Setup armor models
        //TODO: Setup Jacket/Sleeve/Pants layer
        ExtendableModelRenderer body = new ExtendableModelRenderer(GenericLimbNames.body);
        GenericTextureValues.addGenericBodyLayers(body);
        body.setPos(0, 0, 0);
        body.setRotationPoint(new Vec3(0.5, 0.5, 0.5));
        body.extend(GenericResizers.getBodyResizer());

        ExtendableModelRenderer head = new ExtendableModelRenderer( GenericLimbNames.head);
        GenericTextureValues.addGenericHeadLayers(head);
        head.setRotationPoint(new Vec3(0.5, 0, 0.5));
        head.setPos(0.5F, 0, 0.5F);
        head.extend(GenericResizers.getHeadResizer());
        head.setLooking(true);
        head.setFirstPersonRender(false);

        ExtendableModelRenderer rightArm = new ExtendableModelRenderer(GenericLimbNames.rightArm);
        GenericTextureValues.addGenericRightArmLayers(rightArm);
        rightArm.setRotationPoint(new Vec3(0.5D, 0.66D, 0.5D));
        rightArm.setPos(0.0F, 0.0F, 0.5F);
        if (slim)
        {
            rightArm.setFixedPosAdjustment(-1.5F, 2F, 0.0F);
            rightArm.extend(GenericResizers.getSlimArmResizer());
        }
        else
        {
            rightArm.setFixedPosAdjustment(-2F, 2F, 0.0F);
            rightArm.extend(GenericResizers.getArmResizer());
        }

        rightArm.setHitbox(false);

        ExtendableModelRenderer leftArm = new ExtendableModelRenderer(GenericLimbNames.leftArm);
        GenericTextureValues.addGenericLeftArmLayers(leftArm);
        leftArm.setRotationPoint(new Vec3(0.5D, 0.66D, 0.5D));
        leftArm.setPos(1.0F, 0.0F, 0.5F);
        if (slim)
        {
            leftArm.setFixedPosAdjustment(1.5F, 2F, 0.0F);
            leftArm.extend(GenericResizers.getSlimArmResizer());
        }
        else
        {
            leftArm.setFixedPosAdjustment(2F, 2F, 0.0F);
            leftArm.extend(GenericResizers.getArmResizer());
        }

        leftArm.setHitbox(false);

        ExtendableModelRenderer rightLeg = new ExtendableModelRenderer(GenericLimbNames.rightLeg);
        GenericTextureValues.addGenericRightLegLayers(rightLeg);
        rightLeg.setPos(0.25F, 1.0F, 0.5F);
        rightLeg.setRotationPoint(new Vec3(0.5, 0.66, 0.5));
        rightLeg.setFixedPosAdjustment(0F, 2F, 0.0F);
        rightLeg.extend(GenericResizers.getLegResizer());

        ExtendableModelRenderer leftLeg = new ExtendableModelRenderer(GenericLimbNames.leftLeg);
        GenericTextureValues.addGenericLeftLegLayers(leftLeg);
        leftLeg.setPos(0.75F, 1.0F, 0.5F);
        leftLeg.setRotationPoint(new Vec3(0.5, 0.66, 0.5));
        leftLeg.setFixedPosAdjustment(0F, 2F, 0.0F);
        leftLeg.extend(GenericResizers.getLegResizer());

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


        ExtendableModelRenderer leftWing = new ExtendableModelRenderer(GenericLimbNames.leftWingElytra);
        leftWing.addLayer(GenericTextureValues.elytra, GenericTextureValues.armor_Size, GenericTextureValues.innerExtention, TextureHandler.ELYTRA);
        leftWing.setRotationPoint(new Vec3(1, 1, 1));
        leftWing.setDefaultResize(new Vec3(1, 1, 2));
        leftWing.setPos(0F, 0, 1F);
        leftWing.setFixedPosAdjustment(0, 2f, 0.01f);
        leftWing.extend(GenericResizers.getElytraResizer());
        leftWing.setHitbox(false);

        ExtendableModelRenderer rightWing = new ExtendableModelRenderer(GenericLimbNames.rightWingElytra);
        rightWing.addLayer(GenericTextureValues.elytra, GenericTextureValues.armor_Size, GenericTextureValues.innerExtention, TextureHandler.ELYTRA, true);
        rightWing.setRotationPoint(new Vec3(0, 1, 1));
        rightWing.setDefaultResize(new Vec3(1, 1, 2));
        rightWing.setPos(1F, 0, 1F);
        rightWing.setFixedPosAdjustment(0, 2f, 0);
        rightWing.extend(GenericResizers.getElytraResizer());
        rightWing.setHitbox(false);

        addLimb(GenericLimbNames.leftWingElytra, leftWing);
        addLimb(GenericLimbNames.rightWingElytra, rightWing);

        // TODO : Add cape, ears, ParrotOnShoulder?, BeeStinger?

        setViewPoint(head);
        setHand(0, rightArm.getChildren().get(0));
        setHand(1, leftArm.getChildren().get(0));
    }

    public PlayerModel getBaseModel()
    {
        return baseModel;
    }

    public void setHand(int slot, ExtendableModelRenderer hand)
    {
        hands.add(slot, hand);
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
    public void rotateLimb(String limb, Vec3 angles)
    {
        if (!hasLimb(limb))
            return;

        ExtendableModelRenderer limbModel = getLimb(limb);

        if (limbModel == null)
            limbModel = getFirstPersonLimb(limb);

        limbModel.rotate((float)angles.x, (float) angles.y, (float) angles.z);
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

    public void tick(Player player)
    {
        lock();

        body.tick(player);
        updateElytra(player);

        unlock();
    }

    public void updateElytra(Player player)
    {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

        // Set the elytra parts to be invisible if the eyltra is not equipped
        if (chest.getItem() != Items.ELYTRA)
        {
            getLimb(GenericLimbNames.rightWingElytra).mPart.visible = false;
            getLimb(GenericLimbNames.leftWingElytra).mPart.visible = false;

            return;
        }
        else
        {
            getLimb(GenericLimbNames.rightWingElytra).mPart.visible = true;
            getLimb(GenericLimbNames.leftWingElytra).mPart.visible = true;
        }
    }

    // Adds specified limb onto the body
    public void addLimb(String limb, ExtendableModelRenderer limbModel)
    {
        addLimb(limb, limbModel, GenericLimbNames.body);
    }

    // Updates the armor textures for all player body parts
    public void updateArmorsTextures(LocalPlayer player)
    {
        textures.updateArmorTextures(player);
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

    public void setupAnim(float netHeadYaw, float headPitch)
    {
        lookVector = new Vec3(headPitch * ((float)Math.PI / 180F), netHeadYaw * ((float)Math.PI / 180F), 0);

        // baseModel.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public Vec3 getLookVector()
    {
        return lookVector;
    }

    // Get a vector indicating the direction a hand holding an item should be rotated
    public Vec3 getHoldingVector()
    {
        return new Vec3(getLookVector().x() / 1.5, getLookVector().y(), getLookVector().z());
    }

    public RenderType renderType(ResourceLocation resourcelocation)
    {
        return baseModel.renderType(resourcelocation);
    }

    // Render models that only appear in first person
    public void renderFirstPerson(PoseStack PoseStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        lock();

        PoseStackIn.pushPose();

        for (ExtendableModelRenderer model : firstPersonLimbs.values())
            model.render(PoseStackIn, packedLightIn, packedOverlayIn, red, green, blue, alpha, textures);

        PoseStackIn.popPose();

        unlock();
    }

    public void render(PoseStack PoseStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        lock();

        PoseStackIn.pushPose();

        // Scale the model to match the scale size, and move it up or down so it's standing at the right height
        PoseStackIn.translate(0.0D, (1-sizeScale) * defaultHeight, 0.0D);

        PoseStackIn.scale(sizeScale, sizeScale, sizeScale);

        // Render the body, as all limbs are children or sub-children of the body, this should render everything
        body.render(PoseStackIn, packedLightIn, packedOverlayIn, red, green, blue, alpha, textures);

        PoseStackIn.popPose();

        unlock();
    }

    public void renderHandItem(boolean left, int slot, LivingEntity entityIn, ItemStack item, PoseStack PoseStackIn, MultiBufferSource renderTypeBuffer, int packedLightIn)
    {
        // Don't do anything if nothing is held
        if (item.isEmpty())
            return;

        // Don't do anything if this hand doesn't exist
        ExtendableModelRenderer hand = hands.get(slot);
        if (hand == null)
            return;

        PoseStackIn.pushPose();

        //TODO: FIX THIS

        // Transform the camera based depending on which hand is holding the item
        ItemTransforms.TransformType cameraTransform = ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
        if (left)
            cameraTransform = ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;

        // Rotate the item to the hand
        hand.moveToThisModel(PoseStackIn, new Vec3(0, 1, -1));

        // Turn the item around to fit in the hand
        PoseStackIn.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
        PoseStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F));

        // Render the item
        Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(entityIn, item, cameraTransform, left, PoseStackIn, renderTypeBuffer, packedLightIn);


        PoseStackIn.popPose();
    }

    public float calculateEyeHeight()
    {
        ExtendableModelRenderer viewModel = viewPoint;

        LinkedList<ExtendableModelRenderer> parts = new LinkedList<ExtendableModelRenderer>();
        parts.push(viewPoint);

        // Cycle through the viewModel and add each parent element to the linked list
        while(viewModel.getParent() != null)
        {
            viewModel = viewModel.getParent();
            parts.push(viewModel);
        }

        // Rotate the PoseStack around each parent part
        PoseStack stack = new PoseStack();

        while(parts.size() > 0)
        {
            viewModel = parts.pop();
            viewModel.rotateMatrix(stack);
        }

        // Return the the height at the top of this model
        return (viewModel.getTopPoint(stack) * sizeScale / 16) - getHeightAdjustment();
    }

    // Calculate the height adjustment for each limb
    public void calculateHeightAdjustment(Player player)
    {
        size = body.calculateMinHeight(new PoseStack(), 360 - player.yBodyRot);
        size.scaleValues(sizeScale / 16f);
        size = new MultiLimbedDimensions(size);

        for (ExtendableModelRenderer model : firstPersonLimbs.values())
            model.calculateMinHeight(new PoseStack(), 0);

        MultiLimbedRenderer.setDimensions(player, new EntityDimensions(size.getBiggestWidth(), size.getHeight(), false));
        player.setBoundingBox(size.makeBoundingBox(player.position()));
    }

    // Find the limb at the lowest height and return it's height
    public float getHeightAdjustment()
    {
        if (size != null)
            return size.minSize.y();

        return 0;
    }
}
