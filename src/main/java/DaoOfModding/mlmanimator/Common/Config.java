package DaoOfModding.mlmanimator.Common;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;

public class Config
{
    public static class Client
    {
        public static final ConfigValue<Boolean> enableFullBodyFirstPerson;
        protected static final ConfigValue<Boolean> enableFirstPersonHands;

        public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec spec;

        static
        {
            builder.push("First Person Rendering");
            enableFullBodyFirstPerson = builder.comment("Enable to view your entire body when in first person").define("Enable Full Body First Person", true);
            enableFirstPersonHands = builder.comment("Enable to force the default minecraft hands when viewing your body in first person").define("Enable Vanilla Hands", false);
            builder.pop();

            spec = builder.build();
        }

        public static boolean vanillaHands()
        {
            if (enableFirstPersonHands.get() || !enableFullBodyFirstPerson.get())
                return true;

            return false;
        }
    }
}
