package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import DaoOfModding.mlmanimator.Common.PlayerUtils;
import com.mojang.math.Vector3f;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MultiLimbedDimensions extends EntityDimensions
{
    Vector3f minSize;
    Vector3f maxSize;

    //protected final float clearance = 0.1f;
    protected final float clearance = 0f;

    public MultiLimbedDimensions()
    {
        super(0, 0, true);

        reset();
    }

    public MultiLimbedDimensions(Vector3f min, Vector3f max)
    {
        super(0, 0, true);

        minSize = min;
        maxSize = max;
    }

    public MultiLimbedDimensions(MultiLimbedDimensions copy)
    {
        super(copy.getSmallestWidth(), copy.getHeight(), true);

        minSize = copy.minSize.copy();
        maxSize = copy.maxSize.copy();
    }

    @Override
    public AABB makeBoundingBox(Vec3 position)
    {
        /*
        Vector3f miniSize = new Vector3f((int)(minSize.x() / 0.15f) * 0.15f, minSize.y(), (int)(minSize.z() / 0.15f) * 0.15f);
        Vector3f maxiSize = new Vector3f((int)(maxSize.x() / 0.15f) * 0.15f, maxSize.y(), (int)(maxSize.z() / 0.15f) * 0.15f);

        return new AABB(maxiSize.x() + position.x, position.y, maxiSize.z() + position.z, miniSize.x() + position.x, position.y + getHeight(), miniSize.z() + position.z);
*/
        float size = getSmallestWidth() / 2f;
        return new AABB(position.x + size, position.y, size + position.z, position.x - size, position.y + getHeight(), position.z - size);
    }

    public void reset()
    {
        // Make sure it's not smaller than the default minecraft guy - cuz that makes movement break for some reason :/
        minSize = new Vector3f(-4.8f, 0, -4.8f);
        maxSize = new Vector3f(4.8f, 0, 4.8f);
    }

    public float getBiggestWidth()
    {
        float width2 = getWidth();
        float depth = getDepth();

        /*float test = width + depth;
        test *= 5;
        test = (int) test;
        test = test / 10f;

        if (test < 0.6)
            test = 0.6f;

        return test;*/

        if (width2 > depth)
            return width2;

        return depth;
    }

    public float getSmallestWidth()
    {
        float width2 = getWidth();
        float depth = getDepth();

        /*float test = width + depth;
        test *= 5;
        test = (int) test;
        test = test / 10f;

        if (test < 0.6)
            test = 0.6f;

        return test;*/

        if (width2 < depth)
            return width2;

        return depth;
    }

    public float getWidth()
    {
        return (int)((maxSize.x() - minSize.x()) / 0.6f) * 0.6f;
    }

    public float getHeight()
    {
        return (maxSize.y() - minSize.y()) * -1 - clearance;
    }

    public float getDepth()
    {
        return (int)((maxSize.z() - minSize.z()) / 0.6f) * 0.6f;
    }

    public void updateSize(Vector3f point)
    {
        Vector3f newPoint = new Vector3f(point.x(), (point.y()), point.z());

        if (minSize.x() > newPoint.x())
            minSize.setX(newPoint.x());
        if (minSize.y() < newPoint.y())
            minSize.setY(newPoint.y());
        if (minSize.z() > newPoint.z())
            minSize.setZ(newPoint.z());

        newPoint = new Vector3f(point.x(), (point.y()), point.z());

        if (maxSize.x() < newPoint.x())
            maxSize.setX(newPoint.x());
        if (maxSize.y() > newPoint.y())
            maxSize.setY(newPoint.y());
        if (maxSize.z() < newPoint.z())
            maxSize.setZ(newPoint.z());
    }

    public float getMinHeight()
    {
        return minSize.y();
    }

    public void combine(MultiLimbedDimensions d2)
    {
        updateSize(d2.minSize);
        updateSize(d2.maxSize);
    }

    public void scaleValues(float value)
    {
        maxSize.mul(value);
        minSize.mul(value);
    }

    public void scaleValuesExceptY(float value)
    {
        maxSize.setX(maxSize.x() * value);
        maxSize.setZ(maxSize.z() * value);
        minSize.setX(minSize.x() * value);
        minSize.setZ(minSize.z() * value);
    }

    public Vec3 getMidPoint()
    {
        return (new Vec3((maxSize.x() + minSize.x()) / 2, (maxSize.y() + minSize.y()) / 2, (maxSize.z() + minSize.z()) / 2));
    }

    // Rotate around Y axis
    public void rotate(float value)
    {
        maxSize = PlayerUtils.rotateAroundY(maxSize, value);
        minSize = PlayerUtils.rotateAroundY(minSize, value);
    }
}
