package DaoOfModding.mlmanimator.Common;

import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public class Reflection
{
    protected static Field eyeHeightField;
    protected static Field dimensions;

    public static void setup()
    {
        // eyeHeight    - ba - f_19816_
        eyeHeightField = ObfuscationReflectionHelper.findField(Entity.class,"f_19816_");
        // dimensions - aZ - f_19815_
        dimensions = ObfuscationReflectionHelper.findField(Entity.class,"f_19815_");
    }

    public static void setDimensions(Player entity, EntityDimensions value)
    {
        try
        {
            dimensions.set(entity, value);
        }
        catch (Exception e)
        {
            mlmanimator.LOGGER.error("Error setting dimensions at field " + dimensions.getName() + " in " + dimensions.toString() + ": " + e);
        }
    }

    public static void adjustEyeHeight(Entity entity, float height)
    {
        try
        {
            eyeHeightField.setFloat(entity, height);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error adjusting entity eye height");
        }
    }
}
