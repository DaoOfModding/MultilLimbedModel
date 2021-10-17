package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.mlmanimator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public class ModelRendererReflection
{
    // Reflection into ModelRenderer private fields and classes
    private static Field cubeField;
    private static Field polygons;
    private static Field polygonNormal;
    private static Field vertices;
    private static Field pos;
    private static Field u;
    private static Field v;
    private static Class texturedQuad;
    private static Class positionTextureVertex;

    public static void setupReflection() throws Exception
    {
        cubeField = ObfuscationReflectionHelper.findField(ModelRenderer.class,"field_78804_l");
        polygons = ObfuscationReflectionHelper.findField(ModelRenderer.ModelBox.class,"field_78254_i");

        positionTextureVertex = Class.forName("net.minecraft.client.renderer.model.ModelRenderer$PositionTextureVertex");
        texturedQuad = Class.forName("net.minecraft.client.renderer.model.ModelRenderer$TexturedQuad");

        polygonNormal = ObfuscationReflectionHelper.findField(texturedQuad, "field_228312_b_");
        vertices = ObfuscationReflectionHelper.findField(texturedQuad, "field_78239_a");

        pos = ObfuscationReflectionHelper.findField(positionTextureVertex, "field_78243_a");
        u = ObfuscationReflectionHelper.findField(positionTextureVertex, "field_78241_b");
        v = ObfuscationReflectionHelper.findField(positionTextureVertex, "field_78242_c");
    }

    private static Object getField(Field field, Object fieldClass)
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

    public static ObjectList<ModelRenderer.ModelBox> getModelCubes(ModelRenderer renderer)
    {
        return  (ObjectList<ModelRenderer.ModelBox>)getField(cubeField, renderer);
    }

    // Returns an array of TexturedQuad Object's
    public static Object[] getPolygons(ModelRenderer.ModelBox cubeBox)
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
