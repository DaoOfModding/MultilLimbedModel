package DaoOfModding.mlmanimator.Client.Models.Quads;

import net.minecraft.world.phys.Vec3;

public class QuadLinkage
{
    Quad quad;
    Quad.QuadVertex position;
    Vec3 relativePos;

    public QuadLinkage(Quad linkedQuad, Quad.QuadVertex VertexPosition, Vec3 relativePosition)
    {
        quad = linkedQuad;
        position = VertexPosition;
        relativePos = relativePosition;
    }

    public void updatePos(Vec3 pos)
    {
        quad.setPos(position, pos);
    }

    public Vec3 getRelativePos()
    {
        return relativePos;
    }
}
