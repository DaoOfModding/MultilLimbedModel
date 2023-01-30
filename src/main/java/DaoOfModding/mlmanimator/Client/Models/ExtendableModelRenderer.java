package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.Models.Quads.Quad;
import DaoOfModding.mlmanimator.Client.Models.Quads.QuadLinkage;
import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import DaoOfModding.mlmanimator.Client.AnimationFramework.resizeModule;
import DaoOfModding.mlmanimator.Common.PlayerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class ExtendableModelRenderer
{
    protected ModelPart mPart;

    protected ArrayList<ExtendableModelLayer> layers = new ArrayList<ExtendableModelLayer>();

    protected ExtendableModelRenderer parent = null;
    protected ArrayList<ExtendableModelRenderer> child = new ArrayList<ExtendableModelRenderer>();
    // List of quads to draw
    protected ArrayList<Quad> quads = new ArrayList<Quad>();

    // List of quad vertexes attached to this model
    protected ArrayList<QuadLinkage> quadLinkage = new ArrayList<QuadLinkage>();

    protected Vector3f[] points = new Vector3f[8];

    protected MultiLimbedDimensions dimensions = new MultiLimbedDimensions();

    protected boolean look = false;
    protected float notLookingPitch = 0;
    protected float oldNotLookingPitch = 0;

    protected Vec3 rotationOffset = new Vec3(0, 0 ,0);
    protected Vec3 rotationPoint = new Vec3(0, 0, 0);

    protected boolean renderFirstPerson = true;

    protected Vec3 thisSize = new Vec3(1, 1, 1);
    protected Vec3 defaultResize = new Vec3(1, 1, 1);
    protected Vec3 defaultSize;
    protected float thisDelta;

    protected Vec3 relativePosition = new Vec3(0, 0, 0);
    protected Vec3 fixedPosition = new Vec3(0, 0, 0);

    protected int rotationDepth = 0;

    protected String name;

    protected boolean oldVisability;

    protected boolean hasHitbox = true;

    protected Vec3 usedSize;

    public ExtendableModelRenderer(String limbName)
    {
        name = limbName;

        mPart = new ModelPart(new ArrayList<>(), new HashMap<String, ModelPart>());
    }

    public ExtendableModelRenderer clone()
    {
        ExtendableModelRenderer copy = new ExtendableModelRenderer(name);
        copy(copy);

        return copy;
    }

    public ModelPart getModelPart()
    {
        return mPart;
    }

    public Vec3 getSize()
    {
        return thisSize;
    }

    public Vec3 getResize()
    {
        return defaultResize.multiply(thisSize);
    }

    public void addLayer(UVPair tex, UVPair texSize, float extend, String name)
    {
        layers.add(new ExtendableModelLayer(tex, texSize, extend, name));
    }

    public void addLayer(UVPair tex, UVPair texSize, float extend, String name, boolean mirror)
    {
        layers.add(new ExtendableModelLayer(tex, texSize, extend, name, mirror, 1));
    }

    public void addLayer(UVPair tex, UVPair texSize, float extend, String name, boolean mirror, float textureResize)
    {
        layers.add(new ExtendableModelLayer(tex, texSize, extend, name, mirror, textureResize));
    }

    public void addLayer(UVPair tex, UVPair texSize, float extend, String name, Direction invisibleDirection)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name);
        layer.setVisable(invisibleDirection, false);

        layers.add(layer);
    }

    public void copy(ExtendableModelRenderer copy)
    {
        copy.setParent(parent);

        copy.dimensions = dimensions;
        copy.look = look;
        copy.rotationOffset = rotationOffset;
        copy.rotationPoint = rotationPoint;
        copy.renderFirstPerson = renderFirstPerson;

        copy.relativePosition = relativePosition;
        copy.fixedPosition = fixedPosition;

        copy.defaultSize = defaultSize;
        copy.thisSize = thisSize;
        copy.defaultResize = defaultResize;
        copy.thisDelta = thisDelta;

        copy.hasHitbox = hasHitbox;

        copy.usedSize = usedSize;

        for (ExtendableModelLayer layer : layers)
            copy.layers.add(layer.clone());

        copy.generateCube();

        for (ExtendableModelRenderer children : child)
            copy.addChild(children.clone());

        for (Quad quad : quads)
            copy.addQuad(quad);

        for (QuadLinkage link : quadLinkage)
            copy.addQuadLinkage(link);
    }

    public void setHitbox(boolean on)
    {
        hasHitbox = on;
    }

    public Vec3 getDefaultSize()
    {
        return defaultSize;
    }

    public void setNotLookingPitch(float pitch)
    {
        oldNotLookingPitch = notLookingPitch;
        notLookingPitch = pitch;
    }

    public void addQuad(Quad newQuad)
    {
        quads.add(newQuad);
    }

    public void removeQuad(Quad quad)
    {
        if (quads.contains(quad))
            quads.remove(quad);
    }

    public void setName(String newName)
    {
        name = newName;
    }

    public void addQuadLinkage(QuadLinkage link)
    {
        if (!hasQuadLinkage(link))
            quadLinkage.add(link);
    }

    public void removeQuadLinkage(QuadLinkage link)
    {
        if (hasQuadLinkage(link))
            quadLinkage.remove(link);
    }

    public boolean hasQuadLinkage(QuadLinkage link)
    {
        if (quadLinkage.contains(link))
            return true;

        return false;
    }

    public void transferQuadLinkages(ExtendableModelRenderer model)
    {
        for (QuadLinkage link : quadLinkage)
            model.addQuadLinkage(link);
    }

    public void setRotationPoint(Vec3 newRotation)
    {
        rotationPoint = new Vec3(1, 1, 1).subtract(newRotation);
    }

    public Vec3 getRotationPoint()
    {
        return rotationPoint;
    }

    public float getNotLookingPitch()
    {
        return notLookingPitch;
    }

    public float getOldNotLookingPitch()
    {
        return oldNotLookingPitch;
    }

    public void setFirstPersonRender(boolean render)
    {
        renderFirstPerson = render;
    }

    public void setFirstPersonRenderForSelfAndChildren(boolean render)
    {
        renderFirstPerson = render;

        for (ExtendableModelRenderer children : child)
            children.setFirstPersonRenderForSelfAndChildren(render);
    }

    public void setRotationOffset(Vec3 offset)
    {
        rotationOffset = offset;
    }

    // Move all children from this model to another
    public void fosterChildren(ExtendableModelRenderer toMove)
    {
        for (ExtendableModelRenderer fosterChild : child)
            toMove.addChild(fosterChild);

        for (Quad fosterQuad : quads)
            toMove.addQuad(fosterQuad);

        child.clear();
    }

    // Set whether this model should be looking in the direction of the player
    public void setLooking(boolean isLooking)
    {
        look = isLooking;
    }

    public Boolean isLooking()
    {
        return look;
    }

    public ExtendableModelRenderer getParent()
    {
        return parent;
    }

    public void setParent(ExtendableModelRenderer parent)
    {
        this.parent = parent;
    }

    public void setRotationDepth(int newDepth)
    {
        rotationDepth = newDepth;
    }

    public void rotate(float xRotation, float yRotation, float zRotation)
    {
        xRotation = xRotation / ((float)rotationDepth+1);
        yRotation = yRotation / ((float)rotationDepth+1);
        zRotation = zRotation / ((float)rotationDepth+1);

        mPart.xRot = xRotation;
        mPart.yRot = yRotation;
        mPart.zRot = zRotation;

        int traverse = rotationDepth;

        ExtendableModelRenderer traversing = this;

        while (traverse > 0)
        {
            traversing = traversing.getParent();
            traversing.rotate(xRotation, yRotation, zRotation);

            traverse--;
        }
    }

    public ArrayList<ExtendableModelRenderer> getChildren()
    {
        return child;
    }

    // Extend the model, creating depth amount of boxes equaling a total of fullSize extending towards direction
    // Each Model will rotate around the midpoint of the previous model, a 1 or -1 in the rotationPoint will move that point to the edge of the specified side
    public void extend(resizeModule resizer)
    {
        usedSize = resizer.getOriginalSize();

        defaultSize = resizer.getSize();
        thisDelta = resizer.getDelta();

        Vec2 texModifier = resizer.getTextureModifier();

        // Add a box of the appropriate size to this model
        generateCube();

        // Set the top of the model to not be drawn if this isn't the top
        if (usedSize.length() > 0)
            setVisibleDirection(resizer.getTop(), false);

        // Return this model if at max depth
        if (!resizer.continueResizing())
            return;

        // Set the bottom of the model to not be drawn if this isn't the bottom
        setVisibleDirection(resizer.getBottom(), false);

        // Create the next model and add it as a child of this one
        ExtendableModelRenderer newModel = new ExtendableModelRenderer(name + "+");
        newModel.setRotationPoint(resizer.getRotationPoint());

        newModel.setPos((float)resizer.getPosition().x, (float)resizer.getPosition().y, (float)resizer.getPosition().z);
        newModel.setFixedPosAdjustment((float)resizer.getSpacing().x, (float)resizer.getSpacing().y, (float)resizer.getSpacing().z);

        for (ExtendableModelLayer layer : layers)
            newModel.addLayer(new UVPair(layer.textureOffset.u() + (int)texModifier.x, layer.textureOffset.v() + (int)texModifier.y), layer.textureSize, layer.extended + resizer.getDelta(), layer.name);

        newModel.setParent(this);

        addChild(newModel);

        // Continue the extension
        newModel.extend(resizer.nextLevel());
    }

    public void setVisibleDirection(@Nullable Direction dir, boolean on)
    {
        if (dir == null)
            return;

        for (ExtendableModelLayer layer : layers)
            layer.setVisable(dir, on);
    }

    public void setDefaultResize(Vec3 newSize)
    {
        defaultResize = newSize;
    }

    public void resize(Vec3 newSize)
    {
        thisSize = newSize;
    }

    public void addChild(ExtendableModelRenderer c)
    {
        MultiLimbedRenderer.addChild(c.mPart, c.name, mPart);

        child.add(c);
        c.setParent(this);
    }

    public void removeChild(ExtendableModelRenderer toRemove)
    {
        MultiLimbedRenderer.removeChild(toRemove.name, mPart);

        child.remove(toRemove);
        toRemove.setParent(null);
    }

    // Generate the cube for this model
    public void generateCube()
    {
        Vec3 pos = getRotationPoint();

        pos = defaultSize.scale(-1).multiply(pos);

        float width = (float)(defaultSize.x);
        float height = (float)(defaultSize.y);
        float depth = (float)(defaultSize.z);

        float x1 = (float)pos.x;
        float y1 = (float)pos.y;
        float z1 = (float)pos.z;

        float x2 = x1 + width;
        float y2 = y1 + height;
        float z2 = z1 + depth;

        points[0] = new Vector3f(x1, y1, z1);
        points[1] = new Vector3f(x1, y1, z2);
        points[2] = new Vector3f(x1, y2, z1);
        points[3] = new Vector3f(x1, y2, z2);
        points[4] = new Vector3f(x2, y1, z1);
        points[5] = new Vector3f(x2, y1, z2);
        points[6] = new Vector3f(x2, y2, z1);
        points[7] = new Vector3f(x2, y2, z2);

        for (ExtendableModelLayer layer : layers)
            layer.makeCube((float) pos.x, (float) pos.y, (float) pos.z, width, height, depth, usedSize);
    }

    public void setUsedSize(Vec3 newUsed)
    {
        usedSize = newUsed;
        generateCube();
    }

    // Toggle all parts set not to be visible in first person so that they don't render
    public void toggleFirstPersonVisability(boolean on)
    {
        if (!renderFirstPerson)
        {
            if (on)
            {
                oldVisability = mPart.visible;
                mPart.visible = false;
            }
            else
                mPart.visible = oldVisability;
        }

        for (ExtendableModelRenderer childSearch : getChildren())
            childSearch.toggleFirstPersonVisability(on);
    }

    public void render(PoseStack PoseStackIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, TextureHandler textures)
    {
        mPart.xRot += rotationOffset.x;
        mPart.yRot += rotationOffset.y;
        mPart.zRot += rotationOffset.z;

        // If rendering in first person and this model is set not to render in first person, just render it's children
        if (MultiLimbedRenderer.isFakeThirdPerson() && !renderFirstPerson)
            fakerender(PoseStackIn, packedLightIn, packedOverlayIn, red, green, blue, alpha, textures);
        else
        {
            if (MultiLimbedRenderer.isFakeThirdPerson())
                toggleFirstPersonVisability(true);

            //mPart.render(PoseStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            renderCube(PoseStackIn, packedLightIn, packedOverlayIn, red, green, blue, alpha, textures);

            if (MultiLimbedRenderer.isFakeThirdPerson())
                toggleFirstPersonVisability(false);

            renderQuads(PoseStackIn, packedLightIn, packedOverlayIn);
        }

        mPart.xRot -= rotationOffset.x;
        mPart.yRot -= rotationOffset.y;
        mPart.zRot -= rotationOffset.z;
    }

    public void renderQuads(PoseStack PoseStackIn, int packedLightIn, int packedOverlayIn)
    {
        for (Quad quad : quads)
            quad.render(PoseStackIn, packedLightIn, packedOverlayIn);
    }

    public void renderCube(PoseStack PoseStackIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, TextureHandler textures)
    {
        if (mPart.visible)
        {
            PoseStackIn.pushPose();
            mPart.translateAndRotate(PoseStackIn);

            // Draw each model layer
            for (ExtendableModelLayer layer : layers)
            {
                ResourceLocation tex = textures.getTexture(layer.name);

                if (tex != null)
                {
                    VertexConsumer bufferIn = MultiLimbedRenderer.getVertexBuilder(tex);
                    Vec3 color = textures.getColor(layer.name);

                    if (color != null)
                        layer.layerCube.compile(PoseStackIn.last(), bufferIn, packedLightIn, packedOverlayIn, (float) color.x, (float) color.y, (float) color.z, alpha, getResize());
                    else
                        layer.layerCube.compile(PoseStackIn.last(), bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha, getResize());
                }
            }

            for(ExtendableModelRenderer child : getChildren())
                child.render(PoseStackIn, packedLightIn, packedOverlayIn, red, green, blue, alpha, textures);

            PoseStackIn.popPose();
        }
    }

    public void setPos(float xPos, float yPos, float zPos)
    {
        relativePosition = new Vec3(xPos, yPos, zPos);
    }

    // Values to be added to the models position irregardless of it's parent
    public void setFixedPosAdjustment(float xPos, float yPos, float zPos)
    {
        fixedPosition = new Vec3(xPos, yPos, zPos);
    }

    // Update this models position based on it's parents position and it's relative position
    public void updatePosition()
    {
        if (getParent() == null)
        {
            mPart.x = (float)relativePosition.x;
            mPart.y = (float)relativePosition.y;
            mPart.z = (float)relativePosition.z;
        }
        else
        {
            Vec3 pos = getParent().translateRelativePosition(relativePosition);
            mPart.x = (float)pos.x;
            mPart.y = (float)pos.y;
            mPart.z = (float)pos.z;
        }

        mPart.x += fixedPosition.x;
        mPart.y += fixedPosition.y;
        mPart.z += fixedPosition.z;
    }

    // Render all children for this model, but not the model itself
    public void fakerender(PoseStack PoseStackIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, TextureHandler textures)
    {
        if (mPart.visible)
        {
            if (child.size() > 0)
            {
                PoseStackIn.pushPose();
                mPart.translateAndRotate(PoseStackIn);

                for(ExtendableModelRenderer children : child)
                    children.render(PoseStackIn, packedLightIn, packedOverlayIn, red, green, blue, alpha, textures);

                PoseStackIn.popPose();
            }
        }
    }

    public void translatePoseStackToThis(PoseStack PoseStackIn)
    {
        if (parent != null)
            parent.translatePoseStackToThis(PoseStackIn);

        mPart.translateAndRotate(PoseStackIn);
    }

    // Get the minimum height of any point on this model
    public float getMinHeight()
    {
        return dimensions.getMinHeight();
    }

    public MultiLimbedDimensions calculateMinHeight(PoseStack PoseStackIn, double yRot)
    {
        // Update the position of this model first
        updatePosition();

        PoseStackIn.pushPose();

        // Don't rotate the model if it's looking in the direction of the camera - seems to exaggerate positions for some reason
        if (!isLooking())
            rotateMatrix(PoseStackIn);
        else
            PoseStackIn.translate(mPart.x, mPart.y, mPart.z);

        Matrix4f rotator = PoseStackIn.last().pose();

        float min = Float.MAX_VALUE * -1;

        Vec3 resize = getResize();

        dimensions.reset();

        for (Vector3f point : points)
        {
            Vector4f vector4f = new Vector4f(point.x() * (float)resize.x, point.y() * (float)resize.y, point.z() * (float)resize.z, 1.0F);
            vector4f.transform(rotator);

            // Rotate this part to be facing the same direction as the body
            point = new Vector3f(vector4f.x(), vector4f.y(), vector4f.z());
            point = PlayerUtils.rotateAroundY(point, yRot);

            dimensions.updateSize(point);

            if (vector4f.y() > min)
                min = vector4f.y();
        }

        // Update any quad linkages now so it doesn't have to run through the same loop again
        updateQuadLinkages(rotator);

        MultiLimbedDimensions totalDimensions = new MultiLimbedDimensions(dimensions);

        // Calculate the min height of children
        for (ExtendableModelRenderer testChild : child)
            totalDimensions.combine(testChild.calculateMinHeight(PoseStackIn, yRot));

        PoseStackIn.popPose();

        // Do not return size for this or any children if the model does not have a hitbox
        if (!hasHitbox)
            return new MultiLimbedDimensions();

        return totalDimensions;
    }

    public MultiLimbedDimensions getDimensions()
    {
        return dimensions;
    }

    public Vec3 translateRelativePosition(Vec3 relativePos)
    {
        Vec3 minPos = new Vec3(points[0].x(), points[0].y(), points[0].z());
        Vec3 maxPos = new Vec3(points[7].x(), points[7].y(), points[7].z());

        Vec3 resize = getResize();

        minPos = minPos.multiply(resize);
        maxPos = maxPos.multiply(resize);

        Vec3 translatedPos = minPos.multiply(new Vec3(1, 1, 1).subtract(relativePos)).add(maxPos.multiply(relativePos));

        return translatedPos;
    }

    // Update the position of any linked quads vertices
    public void updateQuadLinkages(Matrix4f rotator)
    {
        for (QuadLinkage link : quadLinkage)
        {
            Vec3 relativePos = link.getRelativePos();

            Vec3 position = translateRelativePosition(relativePos);

            Vector4f positon4f = new Vector4f((float)position.x, (float)position.y, (float)position.z, 1);
            positon4f.transform(rotator);

            link.updatePos(new Vec3(positon4f.x(), positon4f.y(), positon4f.z()));
        }
    }

    // Returns the height of the middle of this model
    public float getMidPoint(PoseStack PoseStackIn)
    {
        Matrix4f rotator = PoseStackIn.last().pose();

        Vec3 modelMidPoint = new Vec3(mPart.x, mPart.y, mPart.z);
        modelMidPoint = modelMidPoint.subtract(getDefaultSize().multiply(getResize().scale(0.5)));

        Vector4f vector4f = new Vector4f((float)modelMidPoint.x(), (float)modelMidPoint.y(), (float)modelMidPoint.z(), 1.0F);
        vector4f.transform(rotator);

        return vector4f.y();
    }

    // Returns the height at the top of this model
    public float getTopPoint(PoseStack PoseStackIn)
    {
        Matrix4f rotator = PoseStackIn.last().pose();
        Vector4f vector4f = new Vector4f(mPart.x, mPart.y, mPart.z, 1.0F);
        vector4f.transform(rotator);

        return vector4f.y();
    }

    // Move matrix to the position and rotation of this model
    public void moveToThisModel(PoseStack PoseStackIn, Vec3 position)
    {
        rotateAroundParents(PoseStackIn);

        Vec3 resizeVec = getDefaultSize().multiply(getResize());

        // Move to the specified position on this model
        PoseStackIn.translate((resizeVec.x / 16) * position.x, (resizeVec.y / 16) * position.y, (resizeVec.z / 16) * position.z);
    }

    public void rotateAroundParents(PoseStack PoseStackIn)
    {
        if (this.parent != null)
            this.parent.rotateAroundParents(PoseStackIn);

        mPart.translateAndRotate(PoseStackIn);
    }

    public void rotateMatrix(PoseStack PoseStackIn)
    {
        mPart.xRot += rotationOffset.x;
        mPart.yRot += rotationOffset.y;
        mPart.zRot += rotationOffset.z;


        PoseStackIn.translate(mPart.x, mPart.y, mPart.z);
        if (mPart.zRot != 0.0F) {
            PoseStackIn.mulPose(Vector3f.ZP.rotation(mPart.zRot));
        }

        if (mPart.yRot != 0.0F) {
            PoseStackIn.mulPose(Vector3f.YP.rotation(mPart.yRot));
        }

        if (mPart.xRot != 0.0F) {
            PoseStackIn.mulPose(Vector3f.XP.rotation(mPart.xRot));
        }

        mPart.xRot -= rotationOffset.x;
        mPart.yRot -= rotationOffset.y;
        mPart.zRot -= rotationOffset.z;
    }

    public void tick(AbstractClientPlayer player)
    {
        for (ExtendableModelRenderer thisChild : getChildren())
            thisChild.tick(player);
    }
}
