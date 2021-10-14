package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.Models.Quads.Quad;
import DaoOfModding.mlmanimator.Client.Models.Quads.QuadLinkage;
import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import DaoOfModding.mlmanimator.Client.AnimationFramework.resizeModule;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import org.lwjgl.system.MathUtil;

import java.util.ArrayList;

public class ExtendableModelRenderer extends ModelRenderer
{
    private int textureWidth = 64;
    private int textureHeight = 32;
    private int textureOffsetX;
    private int textureOffsetY;

    private ExtendableModelRenderer parent = null;
    private ArrayList<ExtendableModelRenderer> child = new ArrayList<ExtendableModelRenderer>();
    // List of quads to draw
    private ArrayList<Quad> quads = new ArrayList<Quad>();

    // List of quad vertexes attached to this model
    private ArrayList<QuadLinkage> quadLinkage = new ArrayList<QuadLinkage>();

    private Vector3f[] points = new Vector3f[8];

    private float minHeight = 0;

    private boolean look = false;
    private float notLookingPitch = 0;
    private float oldNotLookingPitch = 0;

    private ResourceLocation customTexture = null;

    private Vector3d rotationOffset = new Vector3d(0, 0 ,0);
    private Vector3d rotationPoint = new Vector3d(0, 0, 0);

    private boolean renderFirstPerson = true;

    private Vector3d thisSize;
    private float thisDelta;

    private Vector3d relativePosition = new Vector3d(0, 0, 0);
    private Vector3d fixedPosition = new Vector3d(0, 0, 0);


    public void setRotationPoint(Vector3d newRotation)
    {
        rotationPoint = new Vector3d(1, 1, 1).subtract(newRotation);
    }

    public Vector3d getRotationPoint()
    {
        return rotationPoint;
    }

    public ExtendableModelRenderer clone()
    {
        ExtendableModelRenderer copy = new ExtendableModelRenderer(textureWidth, textureHeight, textureOffsetX, textureOffsetY);
        copy.setParent(parent);

        copy.points = points.clone();
        copy.minHeight = minHeight;
        copy.look = look;
        copy.customTexture = customTexture;
        copy.rotationOffset = rotationOffset;
        copy.renderFirstPerson = renderFirstPerson;

        copy.generateCube(thisSize.scale(-1).multiply(rotationPoint), (float)thisSize.x, (float)thisSize.y, (float)thisSize.z, thisDelta);

        copy.copyFrom(this);

        for (ExtendableModelRenderer children : child)
            copy.addChild(children.clone());

        for (Quad quad : quads)
            copy.addQuad(quad);

        for (QuadLinkage link : quadLinkage)
            copy.addQuadLinkage(link);

        return copy;
    }

    public ExtendableModelRenderer(Model model)
    {
        super(model);

        textureWidth = model.texWidth;
        textureHeight = model.texHeight;
    }

    public ExtendableModelRenderer(Model model, int texOffX, int texOffY)
    {
        super(model, texOffX, texOffY);

        textureWidth = model.texWidth;
        textureHeight = model.texHeight;
        textureOffsetX = texOffX;
        textureOffsetY = texOffY;
    }

    // 64 is the default size of Player skin models
    public ExtendableModelRenderer(int textureOffsetXIn, int textureOffsetYIn)
    {
        super(64, 64, textureOffsetXIn, textureOffsetYIn);

        textureWidth = 64;
        textureHeight = 64;
        textureOffsetX = textureOffsetXIn;
        textureOffsetY = textureOffsetYIn;
    }

    public ExtendableModelRenderer(int textureWidthIn, int textureHeightIn, int textureOffsetXIn, int textureOffsetYIn)
    {
        super(textureWidthIn, textureHeightIn, textureOffsetXIn, textureOffsetYIn);

        textureWidth = textureWidthIn;
        textureHeight = textureHeightIn;
        textureOffsetX = textureOffsetXIn;
        textureOffsetY = textureOffsetYIn;

        this.x = 0;
        this.y = 0;
        this.z = 0;
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

    public void addQuadLinkage(QuadLinkage link)
    {
        quadLinkage.add(link);
    }

    public void removeQuadLinkage(QuadLinkage link)
    {
        if (quadLinkage.contains(link))
            quadLinkage.remove(link);
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

    public void setRotationOffset(Vector3d offset)
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

    public void setCustomTexture(ResourceLocation newLocation)
    {
        customTexture = newLocation;
    }

    // Set this custom texture for this model and all children
    public void setCustomTextureForFamily(ResourceLocation newLocation)
    {
        customTexture = newLocation;

        for (ExtendableModelRenderer childTexture : child)
            childTexture.setCustomTextureForFamily(newLocation);
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

    public ArrayList<ExtendableModelRenderer> getChildren()
    {
        return child;
    }

    // Extend the model, creating depth amount of boxes equaling a total of fullSize extending towards direction
    // Each Model will rotate around the midpoint of the previous model, a 1 or -1 in the rotationPoint will move that point to the edge of the specified side
    public void extend(resizeModule resizer)
    {
        thisSize = resizer.getSize();
        thisDelta = resizer.getDelta();

        Vector3d rotationPoint = getRotationPoint();
        rotationPoint = thisSize.scale(-1).multiply(rotationPoint);

        Vector2f texModifier = resizer.getTextureModifier();

        // Add a box of the appropriate size to this model
        generateCube(rotationPoint, (float)thisSize.x, (float)thisSize.y, (float)thisSize.z, thisDelta);

        // Return this model if at max depth
        if (!resizer.continueResizing())
            return;


        // Create the next model and add it as a child of this one
        ExtendableModelRenderer newModel = new ExtendableModelRenderer(textureWidth, textureHeight, textureOffsetX + (int)texModifier.x, textureOffsetY + (int)texModifier.y);
        newModel.setRotationPoint(resizer.getRotationPoint());

        newModel.setPos((float)resizer.getPosition().x, (float)resizer.getPosition().y, (float)resizer.getPosition().z);

        newModel.mirror = this.mirror;
        newModel.setParent(this);

        addChild(newModel);

        // Continue the extension
        newModel.extend(resizer.nextLevel());
    }

    @Override
    public void addChild(ModelRenderer c)
    {
        super.addChild(c);

        child.add((ExtendableModelRenderer)c);
        ((ExtendableModelRenderer)c).setParent(this);
    }

    // SERIOUSLY vanilla minecraft, why does this not exist?
    public void removeChild(ExtendableModelRenderer toRemove)
    {
        child.remove(toRemove);

        // AWFUL hack to make this model no longer render, since there is NO WAY to remove a child from the ModelRenderer's childModels list
        toRemove.visible = false;

        toRemove.setParent(null);
    }

    // Generate the cube for this model
    public void generateCube(Vector3d pos, float width, float height, float depth, float delta)
    {
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

        addBox((float)pos.x, (float)pos.y, (float)pos.z, width, height, depth, delta);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        // Grab the vertex builder based on the texture to use for this model
        if (customTexture == null)
            bufferIn = MultiLimbedRenderer.getVertexBuilder();
        else
            bufferIn = MultiLimbedRenderer.getVertexBuilder(customTexture);

        xRot += rotationOffset.x;
        yRot += rotationOffset.y;
        zRot += rotationOffset.z;

        // If rendering in first person and this model is set not to render in first person, just render it's children
        if (MultiLimbedRenderer.isFakeThirdPerson() && !renderFirstPerson)
            fakerender(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        else
        {
            super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

            for (Quad quad : quads)
                quad.render(matrixStackIn, packedLightIn, packedOverlayIn);
        }

        xRot -= rotationOffset.x;
        yRot -= rotationOffset.y;
        zRot -= rotationOffset.z;
    }

    @Override
    public void setPos(float xPos, float yPos, float zPos)
    {
        relativePosition = new Vector3d(xPos, yPos, zPos);
    }

    // Values to be added to the models position irregardless of it's parent
    public void setFixedPosAdjustment(float xPos, float yPos, float zPos)
    {
        fixedPosition = new Vector3d(xPos, yPos, zPos);
    }

    // Update this models position based on it's parents position and it's relative position
    public void updatePosition()
    {
        if (getParent() == null)
        {
            x = (float)relativePosition.x;
            y = (float)relativePosition.y;
            z = (float)relativePosition.z;
        }
        else
        {
            Vector3d pos = getParent().translateRelativePosition(relativePosition);
            x = (float)pos.x;
            y = (float)pos.y;
            z = (float)pos.z;
        }

        x += fixedPosition.x;
        y += fixedPosition.y;
        z += fixedPosition.z;
    }

    // Render all children for this model, but not the model itself
    public void fakerender(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        if (this.visible)
        {
            if (child.size() > 0)
            {
                matrixStackIn.pushPose();
                translateAndRotate(matrixStackIn);

                for(ExtendableModelRenderer children : child)
                    children.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

                matrixStackIn.popPose();
            }
        }
    }

    // Get the minimum height of any point on this model
    public float getMinHeight()
    {
        return minHeight;
    }

    public void calculateMinHeight(MatrixStack matrixStackIn)
    {
        // Update the position of this model first
        updatePosition();

        matrixStackIn.pushPose();
        rotateMatrix(matrixStackIn);

        Matrix4f rotator = matrixStackIn.last().pose();


        float min = Float.MAX_VALUE * -1;

        for (Vector3f point : points)
        {
            Vector4f vector4f = new Vector4f(point.x(), point.y(), point.z(), 1.0F);
            vector4f.transform(rotator);

            if (vector4f.y() > min)
                min = vector4f.y();
        }

        minHeight = min;

        // Update any quad linkages now so it doesn't have to run through the same loop again
        updateQuadLinkages(rotator);

        // Calculate the min height of children
        for (ExtendableModelRenderer testChild : child)
            testChild.calculateMinHeight(matrixStackIn);

        matrixStackIn.popPose();
    }

    public Vector3d translateRelativePosition(Vector3d relativePos)
    {
        Vector3d minPos = new Vector3d(points[0].x(), points[0].y(), points[0].z());
        Vector3d maxPos = new Vector3d(points[7].x(), points[7].y(), points[7].z());

        Vector3d translatedPos = minPos.multiply(new Vector3d(1, 1, 1).subtract(relativePos)).add(maxPos.multiply(relativePos));

        return translatedPos;
    }

    // Update the position of any linked quads vertices
    public void updateQuadLinkages(Matrix4f rotator)
    {
        for (QuadLinkage link : quadLinkage)
        {
            Vector3d relativePos = link.getRelativePos();

            Vector3d position = translateRelativePosition(relativePos);

            Vector4f positon4f = new Vector4f((float)position.x, (float)position.y, (float)position.z, 1);
            positon4f.transform(rotator);

            link.updatePos(new Vector3d(positon4f.x(), positon4f.y(), positon4f.z()));
        }
    }

    // Returns a the height of the topmost point of this model
    public float getTop(MatrixStack matrixStackIn)
    {
        Matrix4f rotator = matrixStackIn.last().pose();

        float max = Float.MAX_VALUE;

        for (Vector3f point : points)
        {
            Vector4f vector4f = new Vector4f(point.x(), point.y(), point.z(), 1.0F);
            vector4f.transform(rotator);

            if (vector4f.y() < max)
                max = vector4f.y();
        }

        return max;
    }

    public void rotateMatrix(MatrixStack matrixStackIn)
    {
        xRot += rotationOffset.x;
        yRot += rotationOffset.y;
        zRot += rotationOffset.z;


        matrixStackIn.translate((double)(this.x), (double)(this.y), (double)(this.z));
        if (this.zRot != 0.0F) {
            matrixStackIn.mulPose(Vector3f.ZP.rotation(this.zRot));
        }

        if (this.yRot != 0.0F) {
            matrixStackIn.mulPose(Vector3f.YP.rotation(this.yRot));
        }

        if (this.xRot != 0.0F) {
            matrixStackIn.mulPose(Vector3f.XP.rotation(this.xRot));
        }

        xRot -= rotationOffset.x;
        yRot -= rotationOffset.y;
        zRot -= rotationOffset.z;
    }
}
