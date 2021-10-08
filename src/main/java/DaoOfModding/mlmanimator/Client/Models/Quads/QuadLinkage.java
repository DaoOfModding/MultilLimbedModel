package DaoOfModding.mlmanimator.Client.Models.Quads;

import net.minecraft.util.math.vector.Vector3d;

public class QuadLinkage
{
    Quad quad;
    Quad.QuadVertex position;
    Vector3d relativePos;

    public QuadLinkage(Quad linkedQuad, Quad.QuadVertex VertexPosition, Vector3d relativePosition)
    {
        quad = linkedQuad;
        position = VertexPosition;
        relativePos = relativePosition;
    }

    public void updatePos(Vector3d pos)
    {
        quad.setPos(position, pos);
    }

    public Vector3d getRelativePos()
    {
        return relativePos;
    }
}
