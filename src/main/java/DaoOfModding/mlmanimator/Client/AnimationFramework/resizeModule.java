package DaoOfModding.mlmanimator.Client.AnimationFramework;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface resizeModule
{
    public Vec3 getSize();
    public Vec3 getRotationPoint();
    public Vec3 getPosition();
    public Vec2 getTextureModifier();
    public float getDelta();
    public resizeModule nextLevel();
    public boolean continueResizing();
    public Vec3 getOriginalSize();
    public Vec3 getSpacing();
    public Direction getTop();
    public Direction getBottom();
}
