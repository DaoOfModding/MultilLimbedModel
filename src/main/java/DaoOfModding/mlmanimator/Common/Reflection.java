package DaoOfModding.mlmanimator.Common;

import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Reflection
{
    protected static Field dimensions;
    protected static Field dimensionsHeight;

    public static void setup()
    {
        // dimensions - aZ - f_19815_
        dimensions = ObfuscationReflectionHelper.findField(Entity.class,"f_19815_");
        // dimensions.height - b - f_20378_
        dimensionsHeight = ObfuscationReflectionHelper.findField(EntityDimensions.class,"f_20378_");

        try
        {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(dimensionsHeight, dimensionsHeight.getModifiers() & ~Modifier.FINAL);
        }
        catch (Exception e)
        {
            mlmanimator.LOGGER.error("Error stripping final tag from " + dimensionsHeight.toString() + ": " + e);
        }
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

    public static void setDimensionsHeight(EntityDimensions input, float height)
    {
        try
        {
            dimensionsHeight.set(input, height);
        }
        catch (Exception e)
        {
            mlmanimator.LOGGER.error("Error setting height for dimensions at " + dimensionsHeight.toString() + ": " + e);
        }
    }
}
