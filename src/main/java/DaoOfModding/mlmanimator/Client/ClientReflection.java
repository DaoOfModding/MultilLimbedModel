package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public class ClientReflection
{
    protected static Field crouchField;

    public static void setup()
    {
        // crouch - cM - f_108601_
        crouchField = ObfuscationReflectionHelper.findField(LocalPlayer.class,"f_108601_");
    }

    public static void setCrouch(LocalPlayer player, boolean crouch)
    {
        try
        {
            crouchField.set(player, crouch);
        }
        catch (Exception e)
        {
            mlmanimator.LOGGER.error("Error setting crouch at field " + crouchField.getName() + " in " + player.toString() + ": " + e);
        }
    }
}
