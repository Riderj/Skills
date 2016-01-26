package coffee.cypher.skills;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public final class Skills {
    private static Logger LOGGER;
    private static boolean initDone = false;
    private static SimpleNetworkWrapper NETWORK;
    private static String ID = "skills";

    static Logger log() {
        if (LOGGER == null) {
            LOGGER = LogManager.getLogger(ID);
        }

        return LOGGER;
    }

    static SimpleNetworkWrapper network() {
        if (NETWORK == null) {
            NETWORK = new SimpleNetworkWrapper(ID);
        }
        return NETWORK;
    }

    private static void init() {
        if (initDone) {
            return;
        }
        log().info("Initializing Skills");

        MinecraftForge.EVENT_BUS.register(new ResearchProperty.Handler());

        network().registerMessage(new StateSyncMessage(), StateSyncMessage.class, 0, Side.CLIENT);
        network().registerMessage(new MapSyncMessage(), MapSyncMessage.class, 1, Side.CLIENT);

        log().info("Skills initialized");
        initDone = true;
    }

    public static void registerResearchMap(ResearchMap map) {
        init();
        ResearchProperty.registerResearchMap(map);
    }

    public static boolean mapExists(String name) {
        init();
        return ResearchProperty.isMapRegistered(name);
    }

    public static void removeResearchMap(String name) {
        init();
        ResearchProperty.deleteResearchMap(name);
    }

    public ResearchMap getResearchMap(String name) {
        init();
        return ResearchProperty.getResearchMap(name);
    }

    public static ResearchMapState getMapState(EntityPlayer player, ResearchMap map) {
        init();
        return ResearchProperty.getResearchMapState(player, map);
    }

    public static void sendMapToClient(EntityPlayerMP player, ResearchMap map) {
        init();
        network().sendTo(new StateSyncMessage(getMapState(player, map)), player);
    }
}
