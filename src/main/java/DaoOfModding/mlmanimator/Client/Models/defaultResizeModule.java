package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.AnimationFramework.resizeModule;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

public class defaultResizeModule implements resizeModule
{
    int depth;

    Vector3d usedSize;
    Vector3d size;
    Vector3d direction;
    Vector3d rotationPoint;

    Vector3d position;
    Vector2f textureModifier;

    Vector3d spacing;

    public defaultResizeModule(int maxDepth, Vector3d direction, Vector3d position, Vector3d fullSize, Vector3d rotationPoint)
    {
        this(maxDepth, direction, position, fullSize, rotationPoint, new Vector3d(0, 0, 0));
    }

    public defaultResizeModule(int maxDepth, Vector3d direction, Vector3d position, Vector3d fullSize, Vector3d rotationPoint, Vector3d spacing)
    {
        depth = maxDepth;
        size = fullSize;

        usedSize = new Vector3d(0, 0, 0);

        // Ensure the direction vector is normalized
        this.direction = direction.normalize();
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
        Vector3d directedSize = size.multiply(direction);

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

        return this;
    }

    public boolean continueResizing()
    {
        if (depth == 1)
            return false;

        return true;
    }

    public float getDelta()
    {
        return 0;
    }
}
