package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import DaoOfModding.mlmanimator.Common.Reflection;
import DaoOfModding.mlmanimator.Network.PacketHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class MultiLimbedModel
{
    protected float sizeScale = 1;
    protected double defaultHeight = 1.5;

    protected static float defaultEyeHeight = 2;

    protected Vec3 lookVector = new Vec3(0, 0, 0);

    protected MultiLimbedDimensions size = null;

    protected boolean slim = false;

    PlayerModel baseModel;

    ExtendableModelRenderer body;
    HashMap<String, ExtendableModelRenderer> limbs = new HashMap<String, ExtendableModelRenderer>();

    ExtendableModelRenderer leftShoulderModel;
    ExtendableModelRenderer rightShoulderModel;

    ExtendableModelRenderer viewPoint;
    HashMap<String, ExtendableModelRenderer> firstPersonLimbs = new HashMap<String, ExtendableModelRenderer>();

    HashMap<String, ExtendableModelRenderer> allLimbs = new HashMap<String, ExtendableModelRenderer>();

    HashMap<String, ParticleEmitter> emitters = new HashMap<String, ParticleEmitter>();

    protected boolean lock = false;

    ArrayList<ExtendableModelRenderer> hands = new ArrayList<>();

    TextureHandler textures = new TextureHandler();

    ParrotModel parrot = null;
    RenderType leftShoulder = null;
    RenderType rightShoulder = null;

    SkullModelBase skullmodelbase = null;
    RenderType skullrendertype = null;

    float eyeHeight = 0;
    float eyePushBack = 0;


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

    protected void setupDefaultLimbs()
    {
        ExtendableModelRenderer body = new ExtendableModelRenderer(GenericLimbNames.body);
        GenericTextureValues.addGenericBodyLayers(body);
        body.setPos(0, 0, 0);
        body.setRotationPoint(new Vec3(0.5, 0.5, 0.5));
        body.extend(GenericResizers.getBodyResizer());

        ExtendableModelRenderer head = new ExtendableModelRenderer(GenericLimbNames.head);
        GenericTextureValues.addGenericHeadLayers(head);
        head.setRotationPoint(new Vec3(0.5, 0, 0.5));
        head.setPos(0.5F, 0, 0.5F);
        head.extend(GenericResizers.getHeadResizer());
        head.setLooking(true);
        head.setFirstPersonRender(false);
        head.setHitbox(false);

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


        ExtendableElytraRenderer leftWing = new ExtendableElytraRenderer(GenericLimbNames.leftWingElytra);
        leftWing.addLayer(GenericTextureValues.elytra, GenericTextureValues.armor_Size, GenericTextureValues.innerExtention, TextureHandler.ELYTRA);
        leftWing.setRotationPoint(new Vec3(0, 1, 1));
        leftWing.setDefaultResize(new Vec3(1, 1, 2));
        leftWing.setPos(1F, 0, 1F);
        leftWing.setFixedPosAdjustment(0, 0f, 0.01f);       // This is to ensure the elytra wings are drawn at SLIGHTLY different positions
        leftWing.extend(GenericResizers.getElytraResizer());
        leftWing.setHitbox(false);
        leftWing.mPart.visible = false;

        ExtendableElytraRenderer rightWing = new ExtendableElytraRenderer(GenericLimbNames.rightWingElytra);
        rightWing.addLayer(GenericTextureValues.elytra, GenericTextureValues.armor_Size, GenericTextureValues.innerExtention, TextureHandler.ELYTRA, true);
        rightWing.setRotationPoint(new Vec3(1, 1, 1));
        rightWing.setDefaultResize(new Vec3(1, 1, 2));
        rightWing.setPos(0F, 0, 1F);
        rightWing.extend(GenericResizers.getElytraResizer());
        rightWing.setHitbox(false);
        rightWing.mPart.visible = false;

        ExtendableCloakRenderer cloak = new ExtendableCloakRenderer(GenericLimbNames.cloak);
        cloak.addLayer(GenericTextureValues.cloak, GenericTextureValues.armor_Size, GenericTextureValues.innerExtention, TextureHandler.CLOAK);
        cloak.setRotationPoint(new Vec3(0.5, 1, 1));
        cloak.setDefaultResize(new Vec3(1, 1, 1));
        cloak.setPos(0.5F, 0, 1F);
        cloak.extend(GenericResizers.getCloakResizer());
        leftWing.setFixedPosAdjustment(0, 0f, 0.01f);
        cloak.setHitbox(false);
        cloak.mPart.visible = false;

        addLimb(GenericLimbNames.leftWingElytra, leftWing);
        addLimb(GenericLimbNames.rightWingElytra, rightWing);
        addLimb(GenericLimbNames.cloak, cloak);

        // BeeStinger/Arrows are things stuck in the player, may ignore for now
        // Ears are for ONE custom skin, screw that

        setViewPoint(head);
        setHand(0, rightArm.getChildren().get(0));
        setHand(1, leftArm.getChildren().get(0));
        setLeftShoulder(leftArm);
        setRightShoulder(rightArm);
    }

    public void setLeftShoulder(ExtendableModelRenderer lShoulder)
    {
        leftShoulderModel = lShoulder;
    }

    public void setRightShoulder(ExtendableModelRenderer rShoulder)
    {
        rightShoulderModel = rShoulder;
    }

    public PlayerModel getBaseModel()
    {
        return baseModel;
    }

    public void setHand(int slot, ExtendableModelRenderer hand)
    {
        hands.add(slot, hand);
    }

    public ExtendableModelRenderer getHand(int slot)
    {
        return hands.get(slot);
    }

    public ArrayList<ExtendableModelRenderer> getHands()
    {
        return hands;
    }

    public void setViewPoint(ExtendableModelRenderer model)
    {
        viewPoint = model;
    }

    // Stop multi-threading breaking stuff via locking
    public void lock()
    {
        while (lock) {}

        lock = true;
    }

    public void unlock()
    {
        lock = false;
    }

    public ParrotModel getParrotModel()
    {
        return parrot;
    }

    public void setParrotModel(ParrotModel parrotModel)
    {
        parrot = parrotModel;
    }

    public void updateSkull(AbstractClientPlayer player, PlayerRenderer renderer)
    {
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.HEAD);

        SkullBlock.Type skullType = null;

        if (!itemstack.isEmpty())
            if (itemstack.getItem() instanceof BlockItem)
                if (((BlockItem)itemstack.getItem()).getBlock() instanceof AbstractSkullBlock)
                    skullType = ((AbstractSkullBlock)((BlockItem)itemstack.getItem()).getBlock()).getType();

        if (skullType == null)
        {
            skullmodelbase = null;
            skullrendertype = null;
            return;
        }

        GameProfile gameprofile = null;
        if (itemstack.hasTag()) {
            CompoundTag compoundtag = itemstack.getTag();
            if (compoundtag.contains("SkullOwner", 10)) {
                gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
            }
        }

        skullmodelbase = MultiLimbedRenderer.getSkullModel(renderer, skullType);
        skullrendertype = SkullBlockRenderer.getRenderType(skullType, gameprofile);
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

    public void tick(AbstractClientPlayer player)
    {
        lock();

        body.tick(player);
        updateShoulder(player);

        unlock();
    }

    protected void updateShoulder(Player player)
    {
        // Do nothing if the parrot model does not currently exist
        if (parrot == null)
            return;

        if (player.getShoulderEntityLeft().getString("id").equals("minecraft:parrot"))
            leftShoulder = parrot.renderType(ParrotRenderer.PARROT_LOCATIONS[player.getShoulderEntityLeft().getInt("Variant")]);
        else
            leftShoulder = null;

        if (player.getShoulderEntityRight().getString("id").equals("minecraft:parrot"))
            rightShoulder = parrot.renderType(ParrotRenderer.PARROT_LOCATIONS[player.getShoulderEntityRight().getInt("Variant")]);
        else
            rightShoulder = null;
    }

    // Adds specified limb onto the body
    public void addLimb(String limb, ExtendableModelRenderer limbModel)
    {
        addLimb(limb, limbModel, GenericLimbNames.body);
    }

    // Updates the armor textures for all player body parts
    public void updateArmorsTextures(AbstractClientPlayer player)
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

    public HashMap<String, ExtendableModelRenderer> getFirstPersonLimbs()
    {
        return firstPersonLimbs;
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
    public void renderFirstPerson(PoseStack PoseStackIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        lock();

        PoseStackIn.pushPose();

        for (ExtendableModelRenderer model : firstPersonLimbs.values())
            model.render(PoseStackIn, packedLightIn, packedOverlayIn, red, green, blue, alpha, textures);

        PoseStackIn.popPose();

        unlock();
    }

    public void render(PoseStack PoseStackIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        PoseStackIn.pushPose();

        // Scale the model to match the scale size, and move it up or down so it's standing at the right height
        PoseStackIn.translate(0.0D, (1-sizeScale) * defaultHeight, 0.0D);

        PoseStackIn.scale(sizeScale, sizeScale, sizeScale);

        // Render the body, as all limbs are children or sub-children of the body, this should render everything
        body.render(PoseStackIn, packedLightIn, packedOverlayIn, red, green, blue, alpha, textures);

        PoseStackIn.popPose();
    }

    public void renderHead(PoseStack PoseStackIn, MultiBufferSource renderTypeBuffer, int packedLightIn, float red, float green, float blue, float alpha, int ticks)
    {
        // If there is no custom skull, make the head visible and do nothing
        if (skullmodelbase == null)
        {
            getViewPoint().mPart.visible = true;
            return;
        }

        // Set the players head to be invisible
        getViewPoint().mPart.visible = false;

        PoseStackIn.pushPose();

        // Move the PoseStack to the head's position
        getViewPoint().translatePoseStackToThis(PoseStackIn);

        Vec3 size = getViewPoint().getSize();
        PoseStackIn.scale((float)size.x() + 0.1875F, ((float)size.y() + 0.1875F) * -1, ((float)size.z() + 0.1875F) * -1);
        //PoseStackIn.scale(1F, -1F, -1F);

        // TODO: Ensure this works properly with different head sizes/positions
        Vec3 move = getViewPoint().translateRelativePosition(new Vec3(0, 1, 0)).scale(sizeScale/8f);
        PoseStackIn.translate(move.x, move.y, move.z);

        SkullBlockRenderer.renderSkull(null, 180.0F, red, PoseStackIn, renderTypeBuffer, packedLightIn, skullmodelbase, skullrendertype);

        PoseStackIn.popPose();
    }

    public void renderShoulder(PoseStack PoseStackIn, MultiBufferSource renderTypeBuffer, int packedLightIn, float red, float green, float blue, float alpha, int ticks)
    {
        //This appears sorta janky when moving, but so does VANILLA minecraft, so...
        if (leftShoulder != null && leftShoulderModel != null)
        {
            PoseStackIn.pushPose();

            leftShoulderModel.translatePoseStackToThis(PoseStackIn);
            // WHY 6 AND NOT 16!? I DON'T KNOW
            Vec3 move = leftShoulderModel.translateRelativePosition(new Vec3(0.5, -1, 0.5)).scale(sizeScale/6f);
            PoseStackIn.translate(move.x, move.y -0.125f, move.z);

            VertexConsumer vertexconsumer = renderTypeBuffer.getBuffer(leftShoulder);
            parrot.renderOnShoulder(PoseStackIn, vertexconsumer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, alpha, ticks);

            PoseStackIn.popPose();
        }

        if (rightShoulder != null && rightShoulderModel != null)
        {
            PoseStackIn.pushPose();

            leftShoulderModel.translatePoseStackToThis(PoseStackIn);
            // WHY 6 AND NOT 16!? I DON'T KNOW
            Vec3 move = rightShoulderModel.translateRelativePosition(new Vec3(0.5, -1, 0.5)).scale(sizeScale/6f);
            PoseStackIn.translate(move.x, move.y -0.125f, move.z);

            VertexConsumer vertexconsumer = renderTypeBuffer.getBuffer(rightShoulder);
            parrot.renderOnShoulder(PoseStackIn, vertexconsumer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, alpha, ticks);

            PoseStackIn.popPose();
        }
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

        // If the item being held is a used spyglass, attach to the head instead
        if (entityIn.getUseItemRemainingTicks() > 0 && item.getUseAnimation() == UseAnim.SPYGLASS)
                getViewPoint().moveToThisModel(PoseStackIn, new Vec3(-0.25, -0.5, -1));
        // Rotate the item to the hand
        else
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
        PoseStack stack2 = new PoseStack();
        getViewPoint().translatePoseStackToThis(stack2);

        Vec3 move = getViewPoint().translateRelativePosition(getViewPoint().getRotationPoint()).scale(sizeScale/8f);
        move = move.subtract(getViewPoint().fixedPosition.scale(sizeScale/16f));
        stack2.translate(move.x, move.y, move.z);

        Vector4f testVec = new Vector4f(0f, 0f, 0f, 2f);
        testVec.transform(stack2.last().pose());

        eyeHeight = (testVec.y() / 2f) - getHeightAdjustment();
        eyePushBack = testVec.z();

        // Check to make sure the eye height is not higher than the hitbox
        float height = getHeight();

        if (eyeHeight < height)
            return height;

        if (eyeHeight > -0.1f)
            return -0.1f;

        return eyeHeight;
    }

    public float getEyePushBack()
    {
        return eyePushBack;
    }

    // Calculate the height adjustment for each limb
    public void calculateHeightAdjustment(Player player)
    {
        // Only calculate sizes at 90 degree angles
        float rotation = (int)((player.yBodyRot + 45) / 90) * 90;

        size = body.calculateMinHeight(new PoseStack(), 360 - rotation);

        // Add a small amount of additional padding at the top of the bounding box
        size.increaseHeight(1);

        size.scaleValues(sizeScale / 16f);
        size = new MultiLimbedDimensions(size);

        for (ExtendableModelRenderer model : firstPersonLimbs.values())
            model.calculateMinHeight(new PoseStack(), 0);

        float oldWidth = player.getBbWidth();
        float oldHeight = player.getBbHeight();

        Reflection.setDimensions(player, new EntityDimensions(size.getSmallestWidth(), size.getHeight(), false));
        player.setBoundingBox(size.makeBoundingBox(player.position()));



        // Send the updated bounding box to the server if it has changed size
        if (player.isLocalPlayer() && (oldWidth != player.getBbWidth() || oldHeight != player.getBbHeight()))
        {
            PacketHandler.sendBoundingBoxToServer(size.minSize, size.maxSize);
        }
    }

    public MultiLimbedDimensions getSize()
    {
        return size;
    }

    public Vec3 getMidPos()
    {
        return getViewPoint().getDimensions().getMidPoint().scale(sizeScale / 8f);
    }

    public float getHeight()
    {
        if (size != null)
            return size.maxSize.y() - size.minSize.y();

        return 0;
    }

    // Find the limb at the lowest height and return it's height
    public float getHeightAdjustment()
    {
        if (size != null)
            return size.minSize.y();

        return 0;
    }
}
