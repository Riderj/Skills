package coffee.cypher.skills;

import com.google.common.base.Strings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchProperty implements IExtendedEntityProperties {
    public static final String NAME = "skills_researches";
    private static List<ResearchMap> maps = new ArrayList<>();

    private Map<ResearchMap, ResearchMapState> states;
    private EntityPlayer player;
    private World world;

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

        for (Map.Entry<ResearchMap, ResearchMapState> entry : states.entrySet()) {
            entry.getValue().deserializeNBT(base.getCompoundTag(entry.getKey().getName()));
        }
    }

    //creating for new player

    @Override
    public void init(Entity entity, World world) {
        player = (EntityPlayer) entity;
        states = new HashMap<>();

        for (ResearchMap map : maps) {
            states.put(map, new ResearchMapState(map));
        }

        this.world = world;
    }

    //research maps

    public static void registerResearchMap(ResearchMap map) {
        if (maps.contains(map)) {
            throw new IllegalArgumentException("Research map already registered: " + map);
        }

        if (Strings.isNullOrEmpty(map.getName())) {
            throw new IllegalArgumentException("Attempted to register research map without name");
        }

        maps.add(map);
    }

    public static ResearchMapState getResearchMapState(EntityPlayer player, ResearchMap map) {
        return get(player).states.get(map);
    }

    //event handling

    public static class Handler {
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
    }
}
