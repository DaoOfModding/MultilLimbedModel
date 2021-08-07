package DaoOfModding.mlmanimator.Client;

import DaoOfModding.mlmanimator.Client.Physics.Gravity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeybindControl
{
    @SubscribeEvent
    public static void onInput(InputEvent event)
    {
        if (Minecraft.getInstance().options.keyJump.getKeyBinding().isDown())
        {
            // Cancel vanilla jump
            Minecraft.getInstance().options.keyJump.getKeyBinding().setDown(false);

            // Do a gravity-based jump
            Gravity.tryJump(Minecraft.getInstance().player);

            // Tell forge a jump has been done
            ForgeHooks.onLivingJump(Minecraft.getInstance().player);
        }
    }
}

