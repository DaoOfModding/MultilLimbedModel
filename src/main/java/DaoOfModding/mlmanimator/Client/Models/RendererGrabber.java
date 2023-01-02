package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public class RendererGrabber
{
    protected static Field layers;
    protected static Field parrotModel;

    public static void setup()
    {
        // layers - h - f_115291_
        layers = ObfuscationReflectionHelper.findField(LivingEntityRenderer.class,"f_115291_");
        // model - a - f_117290_
        parrotModel = ObfuscationReflectionHelper.findField(ParrotOnShoulderLayer.class,"f_117290_");
    }

    public static ParrotModel getParrotModel(PlayerRenderer render)
    {
        try
        {
            List<RenderLayer<LivingEntity, EntityModel<LivingEntity>>> layerList = (List<RenderLayer<LivingEntity, EntityModel<LivingEntity>>>)layers.get(render);

            for (RenderLayer<LivingEntity, EntityModel<LivingEntity>> layer : layerList)
                if (layer instanceof ParrotOnShoulderLayer)
                    return (ParrotModel)parrotModel.get(layer);
        }
        catch(Exception e)
        {
            mlmanimator.LOGGER.error("Error acquiring player's parrot");
            return null;
        }

        return null;
    }
}
