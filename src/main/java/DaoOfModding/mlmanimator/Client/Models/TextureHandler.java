package DaoOfModding.mlmanimator.Client.Models;

import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

public class TextureHandler
{
    public static final String PLAYER_SKIN = "PLAYERSKIN";
    public static final String HEAD_ARMOR = "HEADARMOR";
    public static final String CHEST_ARMOR = "CHESTARMOR";
    public static final String LEG_ARMOR = "LEGARMOR";
    public static final String FOOT_ARMOR = "FOOTARMOR";

    HashMap<String, ResourceLocation> textures = new HashMap<String, ResourceLocation>();
    HashMap<String, Vec3> textureColor = new HashMap<String, Vec3>();

    public void addTexture(String name, ResourceLocation location)
    {
        textures.put(name, location);
    }

    public ResourceLocation getTexture(String name)
    {
        return textures.get(name);
    }

    public void updateArmorTextures(LocalPlayer player)
    {
        addTexture(PLAYER_SKIN, player.getSkinTextureLocation());
        addTexture(HEAD_ARMOR, (getArmorResource(player, EquipmentSlot.HEAD)));
        addColor(HEAD_ARMOR, getArmorColor(player, EquipmentSlot.HEAD));
        addTexture(CHEST_ARMOR, (getArmorResource(player, EquipmentSlot.CHEST)));
        addColor(CHEST_ARMOR, getArmorColor(player, EquipmentSlot.CHEST));
        addTexture(LEG_ARMOR, (getArmorResource(player, EquipmentSlot.LEGS)));
        addColor(LEG_ARMOR, getArmorColor(player, EquipmentSlot.LEGS));
        addTexture(FOOT_ARMOR, (getArmorResource(player, EquipmentSlot.FEET)));
        addColor(FOOT_ARMOR, getArmorColor(player, EquipmentSlot.FEET));
    }

    public void addColor(String name, Vec3 color)
    {
        if (color == null)
            textureColor.remove(name);
        else
            textureColor.put(name, color);
    }

    public Vec3 getColor(String name)
    {
        if (textureColor.containsKey(name))
            return textureColor.get(name);

        return null;
    }

    public Vec3 getArmorColor(LocalPlayer player, EquipmentSlot slot)
    {
        ItemStack stack = player.getItemBySlot(slot);

        if (stack.getItem() instanceof net.minecraft.world.item.DyeableLeatherItem)
        {
            int color = ((net.minecraft.world.item.DyeableLeatherItem)stack.getItem()).getColor(stack);
            float r = (float)(color >> 16 & 255) / 255.0F;
            float g = (float)(color >> 8 & 255) / 255.0F;
            float b = (float)(color & 255) / 255.0F;

            return new Vec3(r, g, b);
        }
        else
            return null;
    }

    // Ripped pretty much completely from HumanoidArmorLayer
    public static ResourceLocation getArmorResource(Player entity, EquipmentSlot slot)
    {
        ItemStack stack = entity.getItemBySlot(slot);

        if (!(stack.getItem() instanceof ArmorItem))
            return null;

        ArmorItem item = (ArmorItem)stack.getItem();
        String texture = item.getMaterial().getName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');
        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }

        int inner = 1;
        if (slot == EquipmentSlot.LEGS)
            inner = 2;

        String s1 = String.format(java.util.Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, inner, "");

        s1 = net.minecraftforge.client.ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, "");

        return new ResourceLocation(s1);
    }
}
