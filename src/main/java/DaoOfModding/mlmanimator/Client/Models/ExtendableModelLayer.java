package DaoOfModding.mlmanimator.Client.Models;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.world.phys.Vec3;

public class ExtendableModelLayer
{
    String name;

    protected UVPair textureOffset;
    protected UVPair textureSize;

    protected float extended = 0;

    protected ExtendableCube layerCube;

    public ExtendableModelLayer(UVPair texOffset, UVPair texSize, float extend, String newName)
    {
        textureOffset = new UVPair(texOffset.u(), texOffset.v());
        textureSize = new UVPair(texSize.u(), texSize.v());
        extended = extend;
        name = newName;
    }

    public void makeCube(float posX, float posY, float posZ, float width, float height, float depth, boolean mirror, Vec3 fullSize)
    {
        layerCube = new ExtendableCube((int)textureOffset.u(), (int)textureOffset.v(), posX, posY, posZ, width, height, depth, extended, extended, extended, mirror, textureSize.u(), textureSize.v(), fullSize);
    }

    public ExtendableModelLayer clone()
    {
        return new ExtendableModelLayer(textureOffset, textureSize, extended, name);
    }
}
