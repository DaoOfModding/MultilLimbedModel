package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.AnimationFramework.resizeModule;
import net.minecraft.util.math.vector.Vector3d;

public class GenericResizers
{
    public static resizeModule getBodyResizer()
    {
        return new defaultResizeModule(new Vector3d(8, 12, 4));
    }

    public static resizeModule getLeftArmResizer()
    {
        return new defaultResizeModule(2, new Vector3d(0, 1, 0), new Vector3d(1, 1, 0), new Vector3d(4, 12, 4), new Vector3d(0, 1, 1));
    }

    public static resizeModule getRightArmResizer()
    {
        return new defaultResizeModule(2, new Vector3d(0, 1, 0), new Vector3d(0, 1, 0), new Vector3d(4, 12, 4), new Vector3d(1, 1, 1));
    }

    public static resizeModule getLegResizer()
    {
        return new defaultResizeModule(2, new Vector3d(0, 1, 0), new Vector3d(0.5, 1, 0), new Vector3d(4, 12, 4), new Vector3d(0.5, 1, 1));
    }

    public static resizeModule getHeadResizer()
    {
        return new defaultResizeModule(new Vector3d(8, 8, 8));
    }
}
