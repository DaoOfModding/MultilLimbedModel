package DaoOfModding.mlmanimator.Network.Packets;

import DaoOfModding.mlmanimator.Common.Reflection;
import DaoOfModding.mlmanimator.Server.ServerListeners;
import DaoOfModding.mlmanimator.mlmanimator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CrawlingPacket extends Packet
{
    boolean crawling;

    public CrawlingPacket(boolean on)
    {
        crawling = on;
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(crawling);
    }

    public static CrawlingPacket decode(FriendlyByteBuf buffer)
    {
        CrawlingPacket returnValue = new CrawlingPacket(false);

        try
        {
            // Read in the sent values
            boolean crawl = buffer.readBoolean();

            return new CrawlingPacket(crawl);

        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e)
        {
            mlmanimator.LOGGER.warn("Exception while reading Crawling message: " + e);
            return returnValue;
        }
    }

    // Read the packet received over the network
    public void handle(Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived.isClient())
        {
            mlmanimator.LOGGER.warn("CrawlingPacket was received by client - This should not happen");
            return;
        }

        ctx.enqueueWork(() -> processPacket(ctx.getSender()));
    }

    // Process received packet on the Server
    protected void processPacket(ServerPlayer sender)
    {
        ServerListeners.setCrawling(sender.getUUID(), crawling);
    }
}
