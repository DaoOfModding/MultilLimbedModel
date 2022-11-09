package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.AnimationFramework.resizeModule;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class defaultResizeModule implements resizeModule {
    protected int depth;

    protected Vec3 usedSize;
    protected Vec3 size;
    protected Vec3 direction;
    protected Vec3 absDirection;
    protected Vec3 rotationPoint;

    protected Vec3 position;
    protected Vec2 textureModifier;

    protected Vec3 spacing;

    protected float delta = 0;

    public defaultResizeModule(int maxDepth, Vec3 direction, Vec3 position, Vec3 fullSize, Vec3 rotationPoint) {
        this(maxDepth, direction, position, fullSize, rotationPoint, new Vec3(0, 0, 0));
    }

    public defaultResizeModule(int maxDepth, Vec3 direction, Vec3 position, Vec3 fullSize, Vec3 rotationPoint, float delta) {
        this(maxDepth, direction, position, fullSize, rotationPoint, new Vec3(0, 0, 0), delta);
    }

    // Default resize module for models of depth 1
    public defaultResizeModule(Vec3 fullSize) {
        this(1, new Vec3(0, 1, 0), new Vec3(0, 0, 0), fullSize, new Vec3(0, 0, 0), new Vec3(0, 0, 0));
    }

    public defaultResizeModule(int maxDepth, Vec3 direction, Vec3 position, Vec3 fullSize, Vec3 rotationPoint, Vec3 spacing)
    {
        this (maxDepth, direction, position, fullSize, rotationPoint, spacing, 0);
    }

    public defaultResizeModule(int maxDepth, Vec3 direction, Vec3 position, Vec3 fullSize, Vec3 rotationPoint, Vec3 spacing, float newDelta)
    {
        depth = maxDepth;
        size = fullSize;
        delta = newDelta;

        usedSize = new Vec3(0, 0, 0);

        // Ensure the direction vector is normalized
        this.direction = direction.normalize();
        this.absDirection = new Vec3(Math.abs(direction.x), Math.abs(direction.y), Math.abs(direction.z));
        this.rotationPoint = rotationPoint;
        this.position = position;
        this.spacing = spacing;
    }

    public Vec3 getPosition()
    {
        return position;
    }

    public Vec3 getSize()
    {
        Vec3 directedSize = size.multiply(absDirection);

        // Calculate the size of this model, and the size remaining to make models for
        Vec3 thisSize = size.subtract(directedSize.scale((double)(depth-1)/(double)depth).add(spacing));
        size = size.subtract(directedSize.scale((double)1/(double)depth));

        if (usedSize.length() == 0)
            usedSize = thisSize.add(spacing);
        else
            usedSize = usedSize.add(thisSize.multiply(direction)).add(spacing);


        // TODO: Ensure this texture offset is correct
        Vec3 modifier = thisSize.multiply(direction);
        textureModifier = new Vec2((float)(modifier.x + modifier.z), (float)modifier.y);

        return thisSize;
    }

    public Vec3 getRotationPoint()
    {
        return rotationPoint;
    }

    public Vec2 getTextureModifier()
    {
        return textureModifier;
    }

    public resizeModule nextLevel()
    {
        depth -= 1;

        // SLIGHTLY increase delta so that overlapping textures don't bug out
        delta += 0.01f;

        return this;
    }

    public boolean continueResizing()
    {
        if (depth == 1)
            return false;

        return true;
    }

    public Vec3 getSpacing()
    {
        return spacing;
    }

    public float getDelta()
    {
        return delta;
    }
}
