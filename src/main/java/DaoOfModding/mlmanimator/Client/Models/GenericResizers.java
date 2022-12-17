package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.AnimationFramework.resizeModule;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class GenericResizers
{
    public static resizeModule getBodyResizer()
    {
        return new defaultResizeModule(new Vec3(8, 12, 4));
    }

    public static resizeModule getArmResizer()
    {
        return new defaultResizeModule(2, new Vec3(0, 1, 0), new Vec3(0.5, 1, 1), new Vec3(4, 12, 4), new Vec3(0.5, 1, 0), Direction.UP, Direction.DOWN);
    }

    public static resizeModule getSlimArmResizer()
    {
        return new defaultResizeModule(2, new Vec3(0, 1, 0), new Vec3(0.5, 1, 1), new Vec3(3, 12, 4), new Vec3(0.5, 1, 0), Direction.UP, Direction.DOWN);
    }

    public static resizeModule getLegResizer()
    {
        return new defaultResizeModule(2, new Vec3(0, 1, 0), new Vec3(0.5, 1, 0), new Vec3(4, 12, 4), new Vec3(0.5, 1, 1), Direction.UP, Direction.DOWN);
    }

    public static resizeModule getHeadResizer()
    {
        return new defaultResizeModule(new Vec3(8, 8, 8));
    }

    public static resizeModule getElytraResizer()
    {
        return new defaultResizeModule(new Vec3(10, 20, 2));
    }
}
