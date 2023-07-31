package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
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

    protected static Field shaderLightDirections;

    protected static final Vector3f INVENTORY_DIFFUSE_LIGHT_0 = Util.make(new Vector3f(0.2F, -1.0F, -1.0F), Vector3f::normalize);
    protected static final Vector3f INVENTORY_DIFFUSE_LIGHT_1 = Util.make(new Vector3f(-0.2F, -1.0F, 0.0F), Vector3f::normalize);

    public static void setup()
    {
        // crouch - cM - f_108601_
        crouchField = ObfuscationReflectionHelper.findField(LocalPlayer.class,"f_108601_");

        bobView = ObfuscationReflectionHelper.findMethod(GameRenderer.class,"m_109138_", PoseStack.class, float.class);
        bobHurt = ObfuscationReflectionHelper.findMethod(GameRenderer.class,"m_109117_", PoseStack.class, float.class);

        shaderLightDirections = ObfuscationReflectionHelper.findField(RenderSystem.class,"f_157150_");
    }

    // Check if the lighting is setup for rending an entity in the inventory
    public static boolean isRenderingInventory()
    {
        try
        {
            Vector3f[] directions = (Vector3f[])shaderLightDirections.get(RenderSystem.class);

            if (directions[0].equals(INVENTORY_DIFFUSE_LIGHT_0) && directions[1].equals(INVENTORY_DIFFUSE_LIGHT_1))
                return true;
        }
        catch (Exception e)
        {
            mlmanimator.LOGGER.error("Error checking shaderLightDirections: " + e);
        }

        return false;
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
