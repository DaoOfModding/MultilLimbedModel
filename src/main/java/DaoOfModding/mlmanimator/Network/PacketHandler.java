package DaoOfModding.mlmanimator.Network;

import DaoOfModding.mlmanimator.Network.Packets.BoundingBoxPacket;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;

public class PacketHandler
{
    protected static final byte boundingbox = 01;
    protected static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(mlmanimator.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init()
    {
        channel.registerMessage(boundingbox, BoundingBoxPacket.class, BoundingBoxPacket::encode, BoundingBoxPacket::decode, BoundingBoxPacket::handle);
    }

    public static void sendBoundingBoxToServer(Vector3f min, Vector3f max)
    {
        BoundingBoxPacket pack = new BoundingBoxPacket(min, max);
        PacketHandler.channel.sendToServer(pack);
    }
}
