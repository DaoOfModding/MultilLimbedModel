package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClientReflection
{
    protected static Field crouchField;

    protected static Method bobView;
    protected static Method bobHurt;

    public static void setup()
    {
        // crouch - cM - f_108601_
        crouchField = ObfuscationReflectionHelper.findField(LocalPlayer.class,"f_108601_");

        bobView = ObfuscationReflectionHelper.findMethod(GameRenderer.class,"m_109138_", PoseStack.class, float.class);
        bobHurt = ObfuscationReflectionHelper.findMethod(GameRenderer.class,"m_109117_", PoseStack.class, float.class);
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

    public static void doBob(PoseStack poseStack)
    {
        try
        {
            GameRenderer renderer = Minecraft.getInstance().gameRenderer;

            bobHurt.invoke(renderer, poseStack, 1.0f);
            if (Minecraft.getInstance().options.bobView().get())
                bobView.invoke(renderer, poseStack, 1.0f);
        }
        catch (Exception e)
        {
            mlmanimator.LOGGER.error("Error doing bob: " + e);
        }
    }
}
