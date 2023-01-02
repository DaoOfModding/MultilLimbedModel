package DaoOfModding.mlmanimator.Client.Models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExtendableCube
{
    protected final Polygon[] polygons;
    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;

    public boolean up = true;
    public boolean down = true;
    public boolean north = true;
    public boolean east = true;
    public boolean south = true;
    public boolean west = true;

    public ExtendableCube(int textureOffsetX, int textureOffsetY, float posX, float posY, float posZ, float width, float height, float depth, float expansionX, float expansionY, float expansionZ, boolean mirror, float texSixeX, float texSixeY, Vec3 fullSize)
    {
        this.minX = posX;
        this.minY = posY;
        this.minZ = posZ;
        this.maxX = posX + width;
        this.maxY = posY + height;
        this.maxZ = posZ + depth;
        this.polygons = new Polygon[6];
        float f = posX + width;
        float f1 = posY + height;
        float f2 = posZ + depth;

        if (mirror)
        {
            float f3 = f;
            f = posX;
            posX = f3;

            expansionX = -expansionX;
        }

        Vertex vertex7 = new Vertex(posX, posY, posZ, -expansionX, -expansionY, -expansionZ, 0.0F, 0.0F);
        Vertex vertex = new Vertex(f, posY, posZ, expansionX, -expansionY, -expansionZ, 0.0F, 8.0F);
        Vertex vertex1 = new Vertex(f, f1, posZ, expansionX, expansionY, -expansionZ, 8.0F, 8.0F);
        Vertex vertex2 = new Vertex(posX, f1, posZ, -expansionX, expansionY, -expansionZ, 8.0F, 0.0F);
        Vertex vertex3 = new Vertex(posX, posY, f2, -expansionX, -expansionY, expansionZ, 0.0F, 0.0F);
        Vertex vertex4 = new Vertex(f, posY, f2, expansionX, -expansionY, expansionZ, 0.0F, 8.0F);
        Vertex vertex5 = new Vertex(f, f1, f2, expansionX, expansionY, expansionZ, 8.0F, 8.0F);
        Vertex vertex6 = new Vertex(posX, f1, f2, -expansionX, expansionY, expansionZ, 8.0F, 0.0F);
        float f4 = (float)textureOffsetX;
        float f5 = (float)textureOffsetX + depth;
        float f6 = (float)textureOffsetX + depth + width;
        float f7 = (float)textureOffsetX + depth + width + width;
        float f8 = (float)textureOffsetX + depth + width + depth;
        float f9 = (float)textureOffsetX + depth + width + depth + width;
        float f10 = (float)textureOffsetY;
        float f11 = (float)textureOffsetY + depth;
        float f12 = (float)textureOffsetY + depth + height;

        // Fix to enable the bottom of model to render the correct texture when extended
        float f10Full = (float)textureOffsetY - (float)fullSize.y;
        float f11Full = (float)textureOffsetY - (float)fullSize.y + depth;
        
        this.polygons[2] = new Polygon(new Vertex[]{vertex4, vertex3, vertex7, vertex}, f5, f10, f6, f11, texSixeX, texSixeY, mirror, Direction.DOWN);
        this.polygons[3] = new Polygon(new Vertex[]{vertex1, vertex2, vertex6, vertex5}, f6, f11Full, f7, f10Full, texSixeX, texSixeY, mirror, Direction.UP);
        this.polygons[1] = new Polygon(new Vertex[]{vertex7, vertex3, vertex6, vertex2}, f4, f11, f5, f12, texSixeX, texSixeY, mirror, Direction.WEST);
        this.polygons[4] = new Polygon(new Vertex[]{vertex, vertex7, vertex2, vertex1}, f5, f11, f6, f12, texSixeX, texSixeY, mirror, Direction.NORTH);
        this.polygons[0] = new Polygon(new Vertex[]{vertex4, vertex, vertex1, vertex5}, f6, f11, f8, f12, texSixeX, texSixeY, mirror, Direction.EAST);
        this.polygons[5] = new Polygon(new Vertex[]{vertex3, vertex4, vertex5, vertex6}, f8, f11, f9, f12, texSixeX, texSixeY, mirror, Direction.SOUTH);
    }


    protected void compile(PoseStack.Pose PoseStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, Vec3 resize)
    {
        Matrix4f matrix4f = PoseStackIn.pose();
        Matrix3f normalMatrix = PoseStackIn.normal();

        for (int j = 0; j < 6; j++)
        {
            if (isVisable(j))
            {
                Polygon texturedquad = polygons[j];

                Vector3f normals = texturedquad.normal.copy();
                normals.transform(normalMatrix);
                float f = normals.x();
                float f1 = normals.y();
                float f2 = normals.z();


                Vertex[] vertices = texturedquad.vertices;
                for (int i = 0; i < 4; ++i) {
                    Vector3f vertex = vertices[i].pos;

                    float f3 = vertex.x() / 16.0F * (float) resize.x + vertices[i].repos.x() / 16.0f;
                    float f4 = vertex.y() / 16.0F * (float) resize.y + vertices[i].repos.y() / 16.0f;
                    float f5 = vertex.z() / 16.0F * (float) resize.z + vertices[i].repos.z() / 16.0f;

                    Vector4f vector4f = new Vector4f(f3, f4, f5, 1.0F);
                    vector4f.transform(matrix4f);

                    bufferIn.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha, vertices[i].u, vertices[i].v, packedOverlayIn, packedLightIn, f, f1, f2);
                }
            }
        }
    }

    protected boolean isVisable(int num)
    {
        if (num == 1 && east)
            return true;
        else if (num == 0 && west)
            return true;
        else if (num == 3 && down)
            return true;
        else if (num == 2 && up)
            return true;
        else if (num == 5 && north)
            return true;
        else if (num == 4 && south)
            return true;

        return false;
    }

    protected void setVisable(Direction dir, boolean on)
    {
        if (dir == Direction.EAST)
            east = on;
        else if (dir == Direction.WEST)
            west = on;
        else if (dir == Direction.SOUTH)
            south = on;
        else if (dir == Direction.NORTH)
            north = on;
        else if (dir == Direction.UP)
            up = on;
        else if (dir == Direction.DOWN)
            down = on;
    }

    @OnlyIn(Dist.CLIENT)
    static class Polygon
    {
        public final Vertex[] vertices;
        public final Vector3f normal;

        public Polygon(Vertex[] vertexList, float texOffset1, float texOffset2, float texOffset3, float texOffset4, float texSizeX, float texSizeY, boolean mirror, Direction dir)
        {
            this.vertices = vertexList;

            vertexList[0] = vertexList[0].remap(texOffset3 / texSizeX, texOffset2 / texSizeY);
            vertexList[1] = vertexList[1].remap(texOffset1 / texSizeX, texOffset2 / texSizeY);
            vertexList[2] = vertexList[2].remap(texOffset1 / texSizeX, texOffset4 / texSizeY);
            vertexList[3] = vertexList[3].remap(texOffset3 / texSizeX, texOffset4 / texSizeY);
            if (mirror)
            {
                int i = vertexList.length;

                for(int j = 0; j < i / 2; ++j)
                {
                    Vertex vertex = vertexList[j];
                    vertexList[j] = vertexList[i - 1 - j];
                    vertexList[i - 1 - j] = vertex;
                }
            }

            this.normal = dir.step();
            if (mirror)
                this.normal.mul(-1.0F, 1.0F, 1.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Vertex
    {
        public final Vector3f pos;
        public final float u;
        public final float v;

        public final Vector3f repos;

        public Vertex(float x, float y, float z, float reX, float reY, float reZ,  float texU, float texV)
        {
            this(new Vector3f(x, y, z), new Vector3f(reX, reY, reZ), texU, texV);
        }

        public Vertex remap(float texU, float texV)
        {
            return new Vertex(this.pos, this.repos, texU, texV);
        }

        public Vertex(Vector3f position, Vector3f reposition, float texU, float texV)
        {
            this.pos = position;
            this.repos = reposition;
            this.u = texU;
            this.v = texV;
        }
    }
}
