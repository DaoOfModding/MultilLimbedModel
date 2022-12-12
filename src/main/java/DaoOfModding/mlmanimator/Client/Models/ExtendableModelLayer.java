package DaoOfModding.mlmanimator.Client.Models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;

public class ExtendableModelLayer
{
    String name;

    protected int textureOffsetX;
    protected int textureOffsetY;

    protected int textureSizeX;
    protected int textureSizeY;

    protected float extended = 0;

    protected ExtendableCube layerCube;

    public ExtendableModelLayer(int texX, int texY, int texSizeX, int textSizeY, float extend, String newName)
    {
        textureOffsetX = texX;
        textureOffsetY = texY;
        textureSizeX = texSizeX;
        textureSizeY = textSizeY;
        extended = extend;
        name = newName;
    }

    public void makeCube(float posX, float posY, float posZ, float width, float height, float depth, boolean mirror, Vec3 fullSize)
    {
        layerCube = new ExtendableCube(textureOffsetX, textureOffsetY, posX, posY, posZ, width, height, depth, extended, extended, extended, mirror, textureSizeX, textureSizeY, fullSize);
    }

    public ExtendableModelLayer clone()
    {
        return new ExtendableModelLayer(textureOffsetX, textureOffsetY, textureSizeX, textureSizeY, extended, name);
    }
}
