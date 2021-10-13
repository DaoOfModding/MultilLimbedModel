package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.AnimationFramework.resizeModule;
import net.minecraft.util.math.vector.Vector3d;

public class GenericResizers
{
    public static resizeModule getBodyResizer()
    {
        return new defaultResizeModule(1, new Vector3d(0, 1, 0), new Vector3d(0, 0, 0), new Vector3d(8, 12, 4), new Vector3d(0, 1 ,0));
    }

    public static resizeModule getLimbResizer()
    {
        return new defaultResizeModule(2, new Vector3d(0, 1, 0), new Vector3d(0.5, 1, 0), new Vector3d(4, 12, 4), new Vector3d(0.5, 1, 1));
    }

    public static resizeModule getHeadResizer()
    {
        return new defaultResizeModule(1, new Vector3d(0, 1, 0), new Vector3d(0.5, 0, 0.5), new Vector3d(8, 8, 8), new Vector3d(0.5, 0, 0.5));
    }
}
