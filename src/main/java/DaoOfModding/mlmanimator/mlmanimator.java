package DaoOfModding.mlmanimator;

import DaoOfModding.mlmanimator.Client.ClientReflection;
import DaoOfModding.mlmanimator.Client.Poses.GenericPoses;
import DaoOfModding.mlmanimator.Client.MultiLimbedRenderer;
import DaoOfModding.mlmanimator.Common.Config;
import DaoOfModding.mlmanimator.Common.Reflection;
import DaoOfModding.mlmanimator.Network.PacketHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod("mlmanimator")
public class mlmanimator {
    public static final String MODID = "mlmanimator";

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public mlmanimator()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonInit);
        modEventBus.addListener(this::clientInit);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.Client.spec, "mlmanimator.toml");
    }

    protected void commonInit(final FMLCommonSetupEvent event)
    {
        GenericPoses.init();
        Reflection.setup();
        PacketHandler.init();
    }

    protected void clientInit(final FMLClientSetupEvent event)
    {
        MultiLimbedRenderer.setup();
        ClientReflection.setup();
    }
}
