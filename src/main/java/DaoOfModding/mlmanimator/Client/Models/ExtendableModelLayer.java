package DaoOfModding.mlmanimator.Client.Models;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class ExtendableModelLayer
{
    protected String name;

    protected UVPair textureOffset;
    protected UVPair textureSize;

    protected float extended = 0;

    protected ExtendableCube layerCube;

    protected boolean mirr;

    protected HashMap<Direction, Boolean> visability = new HashMap<Direction, Boolean>();

    protected float textureResize;

    protected UVPair UVlock = null;

    protected boolean useSmall = false;

    public ExtendableModelLayer(UVPair texOffset, UVPair texSize, float extend, String newName)
    {
        this(texOffset, texSize, extend, newName, false, 1);
    }

    public ExtendableModelLayer(UVPair texOffset, UVPair texSize, float extend, String newName, UVPair lockedUV)
    {
        this(texOffset, texSize, extend, newName, false, 1);

        lockUV(lockedUV);
    }

    public ExtendableModelLayer(UVPair texOffset, UVPair texSize, float extend, String newName, boolean mirror, float texResize)
    {
        textureOffset = new UVPair(texOffset.u(), texOffset.v());
        textureSize = new UVPair(texSize.u(), texSize.v());
        extended = extend;
        name = newName;
        mirr = mirror;
        textureResize = texResize;
    }

    public ExtendableModelLayer(UVPair texOffset, UVPair texSize, float extend, String newName, boolean mirror, float texResize, UVPair lockedUV)
    {
        this(texOffset, texSize, extend, newName, mirror, texResize);

        lockUV(lockedUV);
    }

    public void setSmall()
    {
        useSmall = true;
    }

    public boolean useSmall()
    {
        return useSmall;
    }

    public void lockUV(UVPair lock)
    {
        UVlock = lock;
    }

    public UVPair getUVlock()
    {
        return UVlock;
    }

    public UVPair getTextureOffset()
    {
        return textureOffset;
    }

    public String getName()
    {
        return name;
    }

    public void makeCube(float posX, float posY, float posZ, float width, float height, float depth, Vec3 fullSize)
    {
        layerCube = new ExtendableCube((int)textureOffset.u(), (int)textureOffset.v(), posX, posY, posZ, width, height, depth, extended, extended, extended, mirr, textureSize.u(), textureSize.v(), fullSize, textureResize);

        updateCubeVisability();
    }

    public void setVisable(Direction dir, Boolean on)
    {
        visability.put(dir, on);

        if (layerCube != null)
            updateCubeVisability();
    }

    protected void updateCubeVisability()
    {
        for (Map.Entry<Direction, Boolean> set : visability.entrySet())
            layerCube.setVisable(set.getKey(), set.getValue());
    }

    public ExtendableModelLayer clone()
    {
        return new ExtendableModelLayer(textureOffset, textureSize, extended, name, mirr, textureResize);
    }
}
