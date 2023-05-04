package DaoOfModding.mlmanimator.Client.Models.Quads;

import DaoOfModding.mlmanimator.Client.Models.ExtendableModelLayer;
import DaoOfModding.mlmanimator.Client.Models.TextureHandler;
import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.ArrayList;


public class Quad
{
    public static enum QuadVertex { TopLeft, TopRight, BottomRight, BottomLeft };

    protected static final UVPair texUV[] = {new UVPair(0, 0), new UVPair(1, 0), new UVPair(1, 1), new UVPair(0, 1)};

    protected Vec3 quadPos[] = new Vec3[4];
    protected Vec3 normal[] = new Vec3[4];
    protected ArrayList<ExtendableModelLayer> layers = new ArrayList<ExtendableModelLayer>();
    protected float alpha = 1;

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

    public void setAlpha(float value)
    {
        alpha = value;
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

    public void addLayer(UVPair tex, UVPair texSize, float extend, String name)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name);

        layers.add(layer);
    }

    public void addLayer(UVPair tex, UVPair texSize, float extend, String name, UVPair UVLock)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name, UVLock);

        layers.add(layer);
    }

    public void addLayer(UVPair tex, UVPair texSize, float extend, String name, boolean mirror)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name, mirror, 1);

        layers.add(layer);
    }

    public void addLayer(UVPair tex, UVPair texSize, float extend, String name, boolean mirror, float textureResize)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name, mirror, textureResize);

        layers.add(layer);
    }

    public void addLayer(UVPair tex, UVPair texSize, float extend, String name, boolean mirror, float textureResize, Direction invisibleDirection)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name, mirror, textureResize);
        layer.setVisable(invisibleDirection, false);

        layers.add(layer);
    }

    public void addSmallLayer(UVPair tex, UVPair texSize, float extend, String name)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name);
        layer.setSmall();

        layers.add(layer);
    }

    public void addSmallLayer(UVPair tex, UVPair texSize, float extend, String name, UVPair UVLock)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name, UVLock);
        layer.setSmall();

        layers.add(layer);
    }

    public void addSmallLayer(UVPair tex, UVPair texSize, float extend, String name, boolean mirror)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name, mirror, 1);
        layer.setSmall();

        layers.add(layer);
    }

    public void addSmallLayer(UVPair tex, UVPair texSize, float extend, String name, boolean mirror, float textureResize)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name, mirror, textureResize);
        layer.setSmall();

        layers.add(layer);
    }

    public void addSmallLayer(UVPair tex, UVPair texSize, float extend, String name, boolean mirror, float textureResize, Direction invisibleDirection)
    {
        ExtendableModelLayer layer = new ExtendableModelLayer(tex, texSize, extend, name, mirror, textureResize);
        layer.setSmall();
        layer.setVisable(invisibleDirection, false);

        layers.add(layer);
    }

    protected void calculateNormals()
    {
        normal[0] = calculateNormal(quadPos[0], quadPos[3], quadPos[1]);
        normal[1] = calculateNormal(quadPos[1], quadPos[0], quadPos[2]);
        normal[2] = calculateNormal(quadPos[2], quadPos[1], quadPos[3]);
        normal[3] = calculateNormal(quadPos[3], quadPos[2], quadPos[0]);
    }

    protected Vec3 calculateNormal(Vec3 point, Vec3 leftPoint, Vec3 rightPoint)
    {
        return (leftPoint.subtract(point)).cross(rightPoint.subtract(point)).normalize();
    }

    public void render(PoseStack PoseStackIn, int packedLightIn, int packedOverlayIn, TextureHandler textures)
    {
        // Draw each model layer
        for (ExtendableModelLayer layer : layers)
        {
            ResourceLocation tex;

            if (layer.useSmall())
                tex = textures.getSmallTexture(layer.getName());
            else
                tex = textures.getTexture(layer.getName());

            if (tex != null)
            {
                VertexConsumer vertexBuilder = MultiLimbedRenderer.getVertexBuilder(tex);

                Vec3 color = textures.getColor(layer.getName());

                Matrix4f stackPose = PoseStackIn.last().pose();
                Matrix3f stackNormal = PoseStackIn.last().normal();

                // Loop through each vertex
                for (int i = 0; i < 4; i++)
                {
                    UVPair UV = layer.getUVlock();

                    if (UV == null)
                        UV = texUV[i];
                    else
                        UV = new UVPair(UV.u() + texUV[i].u() * 0.001f, UV.v() + texUV[i].v() * 0.001f);

                    Vector3f floatingNormal = new Vector3f((float) normal[i].x, (float) normal[i].y, (float) normal[i].z);
                    floatingNormal.transform(stackNormal);

                    Vector4f floatingPos = new Vector4f((float) quadPos[i].x / 16, (float) quadPos[i].y / 16, (float) quadPos[i].z / 16, 1f);
                    floatingPos.transform(stackPose);

                    vertexBuilder.vertex(floatingPos.x(), floatingPos.y(), floatingPos.z(), (float) color.x(), (float) color.y(), (float) color.z(), alpha, UV.u(), UV.v(), packedOverlayIn, packedLightIn, floatingNormal.x(), floatingNormal.y(), floatingNormal.z());
                }
            }
        }
    }
}
