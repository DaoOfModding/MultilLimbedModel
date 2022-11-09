package DaoOfModding.mlmanimator.Client.Models.Quads;

import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;


public class Quad
{
    public static enum QuadVertex { TopLeft, TopRight, BottomRight, BottomLeft };

    private static final Vec2 texUV[] = {new Vec2(0, 0), new Vec2(1, 0), new Vec2(1, 1), new Vec2(0, 1)};

    protected Vec3 quadPos[] = new Vec3[4];
    protected Vec3 normal[] = new Vec3[4];
    protected Vector4f color = new Vector4f(1, 1, 1, 1);
    protected ResourceLocation customTexture = new ResourceLocation(mlmanimator.MODID, "textures/blank.png");

    public Quad(Vec3 topLeft, Vec3 topRight, Vec3 bottomRight, Vec3 bottomLeft)
    {
        setPos(topLeft, topRight, bottomRight, bottomLeft);
    }

    public void setPos(Vec3 topLeft, Vec3 topRight, Vec3 bottomRight, Vec3 bottomLeft)
    {
        quadPos[0] = topLeft;
        quadPos[1] = topRight;
        quadPos[2] = bottomRight;
        quadPos[3] = bottomLeft;

        calculateNormals();
    }

    public Vec3 getPos(Quad.QuadVertex vertex)
    {
        return quadPos[vertex.ordinal()];
    }

    public void setPos(QuadVertex vertex, Vec3 newPos)
    {
        quadPos[vertex.ordinal()] = newPos;
        calculateNormals();
    }

    public void setTexture(ResourceLocation texture)
    {
        customTexture = texture;
    }

    protected void calculateNormals()
    {
        normal[0] = calculateNormal(quadPos[0], quadPos[3], quadPos[1]);
        normal[1] = calculateNormal(quadPos[1], quadPos[0], quadPos[2]);
        normal[2] = calculateNormal(quadPos[2], quadPos[1], quadPos[3]);
        normal[3] = calculateNormal(quadPos[3], quadPos[2], quadPos[0]);
    }

    private Vec3 calculateNormal(Vec3 point, Vec3 leftPoint, Vec3 rightPoint)
    {
        return (leftPoint.subtract(point)).cross(rightPoint.subtract(point)).normalize();
    }

    public void setColor(Vector4f newColor)
    {
        color = newColor;
    }

    public void render(PoseStack PoseStackIn, int packedLightIn, int packedOverlayIn)
    {
        VertexConsumer vertexBuilder = MultiLimbedRenderer.getVertexBuilder(customTexture);

        Matrix4f stackPose = PoseStackIn.last().pose();
        Matrix3f stackNormal = PoseStackIn.last().normal();

        // Loop through each vertex
        for (int i = 0; i < 4; i++)
        {
            Vector3f floatingNormal = new Vector3f((float)normal[i].x, (float)normal[i].y, (float)normal[i].z);
            floatingNormal.transform(stackNormal);

            Vector4f floatingPos = new Vector4f((float)quadPos[i].x / 16, (float)quadPos[i].y / 16, (float)quadPos[i].z / 16, 1f);
            floatingPos.transform(stackPose);

            vertexBuilder.vertex(floatingPos.x(), floatingPos.y(), floatingPos.z(), color.x(), color.y(), color.z(), color.w(), texUV[i].x, texUV[i].y, packedOverlayIn, packedLightIn, floatingNormal.x(), floatingNormal.y(), floatingNormal.z());
        }
    }
}
