package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.*;

public class Quad
{
    private static final Vector2f texUV[] = {new Vector2f(0, 0), new Vector2f(1, 0), new Vector2f(1, 1), new Vector2f(0, 1)};

    private Vector3d quadPos[] = new Vector3d[4];
    private Vector3d normal[] = new Vector3d[4];
    private Vector4f color = new Vector4f(1, 1, 1, 1);
    private ResourceLocation customTexture = new ResourceLocation(mlmanimator.MODID, "textures/blank.png");

    public Quad(Vector3d topLeft, Vector3d topRight, Vector3d bottomRight, Vector3d bottomLeft)
    {
        setPos(topLeft, topRight, bottomRight, bottomLeft);
    }

    public void setPos(Vector3d topLeft, Vector3d topRight, Vector3d bottomRight, Vector3d bottomLeft)
    {
        quadPos[0] = topLeft;
        quadPos[1] = topRight;
        quadPos[2] = bottomRight;
        quadPos[3] = bottomLeft;

        calculateNormals();
    }

    public void setTexture(ResourceLocation texture)
    {
        customTexture = texture;
    }

    private void calculateNormals()
    {
        normal[0] = calculateNormal(quadPos[0], quadPos[3], quadPos[1]);
        normal[1] = calculateNormal(quadPos[1], quadPos[0], quadPos[2]);
        normal[2] = calculateNormal(quadPos[2], quadPos[1], quadPos[3]);
        normal[3] = calculateNormal(quadPos[3], quadPos[2], quadPos[0]);
    }

    private Vector3d calculateNormal(Vector3d point, Vector3d leftPoint, Vector3d rightPoint)
    {
        return (leftPoint.subtract(point)).cross(rightPoint.subtract(point)).normalize();
    }

    public void render(MatrixStack matrixStackIn, int packedLightIn, int packedOverlayIn)
    {
        IVertexBuilder vertexBuilder = MultiLimbedRenderer.getVertexBuilder(customTexture);

        Matrix4f stackPose = matrixStackIn.last().pose();
        Matrix3f stackNormal = matrixStackIn.last().normal();

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
