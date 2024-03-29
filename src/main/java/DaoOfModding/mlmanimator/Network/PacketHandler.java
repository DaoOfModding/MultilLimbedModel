package DaoOfModding.mlmanimator.Network;

import DaoOfModding.mlmanimator.Network.Packets.BoundingBoxPacket;
import DaoOfModding.mlmanimator.Network.Packets.CrawlingPacket;
import DaoOfModding.mlmanimator.Network.Packets.EyeHeightPacket;
import DaoOfModding.mlmanimator.mlmanimator;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;

public class PacketHandler
{
    protected static final byte boundingbox = 01;
    protected static final byte eyeheight = 02;
    protected static final byte crawling = 03;
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
        channel.registerMessage(eyeheight, EyeHeightPacket.class, EyeHeightPacket::encode, EyeHeightPacket::decode, EyeHeightPacket::handle);
        channel.registerMessage(crawling, CrawlingPacket.class, CrawlingPacket::encode, CrawlingPacket::decode, CrawlingPacket::handle);
    }

    public static void sendBoundingBoxToServer(Vector3f min, Vector3f max)
    {
        BoundingBoxPacket pack = new BoundingBoxPacket(min, max);
        PacketHandler.channel.sendToServer(pack);
    }

    public static void sendEyeHeightToServer(float height)
    {
        EyeHeightPacket pack = new EyeHeightPacket(height);
        PacketHandler.channel.sendToServer(pack);
    }

    public static void sendCrawlingToServer(boolean crawl)
    {
        CrawlingPacket pack = new CrawlingPacket(crawl);
        PacketHandler.channel.sendToServer(pack);
    }
}
