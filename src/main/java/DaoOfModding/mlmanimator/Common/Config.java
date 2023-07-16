package DaoOfModding.mlmanimator.Common;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;

public class Config
{
    public static class Client
    {
        public static final ConfigValue<Boolean> enableFullBodyFirstPerson;
        public static final ConfigValue<Boolean> enableFirstPersonHands;

        public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec spec;

        static
        {
            builder.push("First Person Rendering");
            enableFullBodyFirstPerson = builder.comment("Enable to view your entire body when in first person").define("Enable Full Body First Person", true);
            enableFirstPersonHands = builder.comment("Enable to view the default minecraft hands in first person, You do NOT want BOTH of these values to be false").define("Enable First Person Hands", false);
            builder.pop();

            spec = builder.build();
        }
    }
}
