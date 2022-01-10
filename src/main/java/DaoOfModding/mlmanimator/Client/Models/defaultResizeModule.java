package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.AnimationFramework.resizeModule;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

public class defaultResizeModule implements resizeModule {
    protected int depth;

    protected Vector3d usedSize;
    protected Vector3d size;
    protected Vector3d direction;
    protected Vector3d absDirection;
    protected Vector3d rotationPoint;

    protected Vector3d position;
    protected Vector2f textureModifier;

    protected Vector3d spacing;

    protected float delta = 0;

    public defaultResizeModule(int maxDepth, Vector3d direction, Vector3d position, Vector3d fullSize, Vector3d rotationPoint) {
        this(maxDepth, direction, position, fullSize, rotationPoint, new Vector3d(0, 0, 0));
    }

    public defaultResizeModule(int maxDepth, Vector3d direction, Vector3d position, Vector3d fullSize, Vector3d rotationPoint, float delta) {
        this(maxDepth, direction, position, fullSize, rotationPoint, new Vector3d(0, 0, 0), delta);
    }

    // Default resize module for models of depth 1
    public defaultResizeModule(Vector3d fullSize) {
        this(1, new Vector3d(0, 1, 0), new Vector3d(0, 0, 0), fullSize, new Vector3d(0, 0, 0), new Vector3d(0, 0, 0));
    }

    public defaultResizeModule(int maxDepth, Vector3d direction, Vector3d position, Vector3d fullSize, Vector3d rotationPoint, Vector3d spacing)
    {
        this (maxDepth, direction, position, fullSize, rotationPoint, spacing, 0);
    }

    public defaultResizeModule(int maxDepth, Vector3d direction, Vector3d position, Vector3d fullSize, Vector3d rotationPoint, Vector3d spacing, float newDelta)
    {
        depth = maxDepth;
        size = fullSize;
        delta = newDelta;

        usedSize = new Vector3d(0, 0, 0);

        // Ensure the direction vector is normalized
        this.direction = direction.normalize();
        this.absDirection = new Vector3d(Math.abs(direction.x), Math.abs(direction.y), Math.abs(direction.z));
        this.rotationPoint = rotationPoint;
        this.position = position;
        this.spacing = spacing;
    }

    public Vector3d getPosition()
    {
        return position;
    }

    public Vector3d getSize()
    {
        Vector3d directedSize = size.multiply(absDirection);

        // Calculate the size of this model, and the size remaining to make models for
        Vector3d thisSize = size.subtract(directedSize.scale((double)(depth-1)/(double)depth).add(spacing));
        size = size.subtract(directedSize.scale((double)1/(double)depth));

        if (usedSize.length() == 0)
            usedSize = thisSize.add(spacing);
        else
            usedSize = usedSize.add(thisSize.multiply(direction)).add(spacing);


        // TODO: Ensure this texture offset is correct
        Vector3d modifier = thisSize.multiply(direction);
        textureModifier = new Vector2f((float)(modifier.x + modifier.z), (float)modifier.y);

        return thisSize;
    }

    public Vector3d getRotationPoint()
    {
        return rotationPoint;
    }

    public Vector2f getTextureModifier()
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

    public Vector3d getSpacing()
    {
        return spacing;
    }

    public float getDelta()
    {
        return delta;
    }
}
