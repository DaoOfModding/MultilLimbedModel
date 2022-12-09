package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public class ModelRendererReflection
{
    // Reflection into ModelRenderer private fields and classes
    private static Field polygons;
    private static Field polygonNormal;
    private static Field vertices;
    private static Field pos;
    private static Field u;
    private static Field v;
    private static Class polygon;
    private static Class vertex;

    public static void setupReflection() throws Exception
    {
        // polygons - g - f_104341_
        polygons = ObfuscationReflectionHelper.findField(ModelPart.Cube.class,"f_104341_");

        vertex = Class.forName("net.minecraft.client.model.geom.ModelPart$Vertex");
        polygon = Class.forName("net.minecraft.client.model.geom.ModelPart$Polygon");

        // normal - b - f_104360_
        polygonNormal = ObfuscationReflectionHelper.findField(polygon, "f_104360_");
        // vertices - a - f_104359_
        vertices = ObfuscationReflectionHelper.findField(polygon, "f_104359_");

        // pos - a - f_104371_
        pos = ObfuscationReflectionHelper.findField(vertex, "f_104371_");
        // u - b - f_104372_
        u = ObfuscationReflectionHelper.findField(vertex, "f_104372_");
        // v - c - f_104373_
        v = ObfuscationReflectionHelper.findField(vertex, "f_104373_");
    }

    public static Object getField(Field field, Object fieldClass)
    {
        try
        {
            return field.get(fieldClass);
        }
        catch (Exception e)
        {
            mlmanimator.LOGGER.error("Error reflecting field " + field.getName() + " in " + fieldClass.toString() + ": " + e);

            return null;
        }
    }

    public static Vector3f getPositionTextureVertexPos(Object PositionTextureVertex)
    {
        return (Vector3f) getField(pos, PositionTextureVertex);
    }

    public static float getU(Object PositionTextureVertex)
    {
        return (float) getField(u, PositionTextureVertex);
    }

    public static float getV(Object PositionTextureVertex)
    {
        return (float) getField(v, PositionTextureVertex);
    }

    // Returns an array of TexturedQuad Object's
    public static Object[] getPolygons(ModelPart.Cube cubeBox)
    {
        return (Object[]) getField(polygons, cubeBox);
    }

    // Takes in a ModelRender.TexturedQuad Object, returns an array of PositionTextureVertex Object's
    public static Object[] getVertices(Object textQuad)
    {
        return (Object[]) getField(vertices, textQuad);
    }

    // Takes in a ModelRender.TexturedQuad Object
    public static Vector3f getPolygonNormals(Object textQuad)
    {
        return (Vector3f) getField(polygonNormal, textQuad);
    }
}
