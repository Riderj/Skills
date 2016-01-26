package coffee.cypher.skills;

import com.google.common.base.Strings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

import java.util.*;

public final class ResearchProperty implements IExtendedEntityProperties {
    public static final String NAME = "skills_researches";
    private static List<ResearchMap> maps = new ArrayList<>();
    private static NBTTagCompound oldMapTag;

    private Map<ResearchMap, ResearchMapState> states;

    private static ResearchProperty get(EntityPlayer p) {
        return (ResearchProperty) p.getExtendedProperties(NAME);
    }

    //NBT storage

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound base = new NBTTagCompound();

        for (Map.Entry<ResearchMap, ResearchMapState> entry : states.entrySet()) {
            base.setTag(entry.getKey().getName(), entry.getValue().serializeNBT());
        }

        compound.setTag(NAME, base);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound base = compound.getCompoundTag(NAME);
        if (base == null) {
            base = new NBTTagCompound();
        }

        for (Map.Entry<ResearchMap, ResearchMapState> entry : states.entrySet()) {
            entry.getValue().deserializeNBT(base.getCompoundTag(entry.getKey().getName()));
        }
    }

    static NBTTagCompound serializeMaps() {
        NBTTagCompound tag = new NBTTagCompound();
        for (ResearchMap map : maps) {
            tag.setTag(map.getName(), map.serializeNBT());
        }
        return tag;
    }

    private static void deserializeMaps(NBTTagCompound tag) {
        Skills.log().info("Updating map information");
        for (String name : tag.getKeySet()) {
            if (!isMapRegistered(name)) {
                registerResearchMap(new ResearchMap(name, Collections.emptyList()));
            }

            getResearchMap(name).deserializeNBT(tag.getCompoundTag(name));
        }
    }

    static void adaptToServerMaps(NBTTagCompound tag) {
        if (oldMapTag == null) {
            oldMapTag = serializeMaps();
        }
        deserializeMaps(tag);
    }

    static void resetClientMaps() {
        deserializeMaps(oldMapTag);
        oldMapTag = null;
    }

    //creating for new player

    @Override
    public void init(Entity entity, World world) {
        states = new HashMap<>();

        for (ResearchMap map : maps) {
            states.put(map, new ResearchMapState(map));
        }
    }

    //research maps

    static void registerResearchMap(ResearchMap map) {
        Skills.log().info("Registering research map " + map.getName() + " with " + map.getNodes().size() + " nodes");
        if (maps.contains(map) || maps.stream().anyMatch(n -> n.getName().equals(map.getName()))) {
            throw new IllegalArgumentException("Research map already registered: " + map.getName());
        }

        if (Strings.isNullOrEmpty(map.getName())) {
            throw new IllegalArgumentException("Attempted to register research map without name");
        }

        maps.add(map);
    }

    static ResearchMap getResearchMap(String map) {
        if (!isMapRegistered(map)) {
            throw new IllegalArgumentException("Research map not registered: " + map);
        }
        return maps.stream().filter(n -> (n.getName().equals(map))).findFirst().get();
    }

    static ResearchMapState getResearchMapState(EntityPlayer player, ResearchMap map) {
        return get(player).states.get(map);
    }

    static boolean isMapRegistered(String map) {
        return maps.stream().anyMatch(n -> (n.getName().equals(map)));
    }

    static void deleteResearchMap(String map) {
        Skills.log().info("Removing research map " + map);
        if (!isMapRegistered(map)) {
            throw new IllegalArgumentException("Research map not registered: " + map);
        }
        maps.removeIf(n -> (n.getName().equals(map)));
    }

    //event handling

    static final class Handler {
        @SubscribeEvent
        public void entityConstruct(EntityEvent.EntityConstructing e)
        {
            if (e.entity instanceof EntityPlayer)
            {
                if (e.entity.getExtendedProperties(NAME) == null)
                    e.entity.registerExtendedProperties(NAME, new ResearchProperty());
            }
        }

        @SubscribeEvent
        public void onClonePlayer(PlayerEvent.Clone event) {
            NBTTagCompound compound = new NBTTagCompound();
            ResearchProperty.get(event.original).saveNBTData(compound);
            ResearchProperty.get(event.entityPlayer).loadNBTData(compound);
        }

        @SubscribeEvent
        public void onPlayerJoin(PlayerLoggedInEvent event) {
            Skills.network().sendTo(new MapSyncMessage(serializeMaps()), (EntityPlayerMP) event.player);
        }

        @SubscribeEvent
        public void onServerLeave(ClientDisconnectionFromServerEvent event) {

        }
    }
}
