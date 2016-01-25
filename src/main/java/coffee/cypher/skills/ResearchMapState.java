package coffee.cypher.skills;

import com.google.common.collect.BiMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public class ResearchMapState implements INBTSerializable<NBTTagCompound> {
    private Map<Integer, NodeState> state;
    private final ResearchMap map;

    public ResearchMapState(ResearchMap ref) {
        map = ref;
        state = new HashMap<>();
        for (BiMap.Entry<ResearchNode, Integer> entry : map.getNodes().entrySet()) {
            state.put(entry.getValue(), entry.getKey().getDefaultState());
        }
    }

    public NodeState getStateOfNode(ResearchNode node) {
        return state.get(map.getId(node));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (BiMap.Entry<ResearchNode, Integer> e : map.getNodes().entrySet()) {
            tag.setInteger(e.getKey().getName(), state.get(e.getValue()).ordinal());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        for (BiMap.Entry<ResearchNode, Integer> e : map.getNodes().entrySet()) {
            state.put(e.getValue(), NodeState.values()[nbt.getInteger(e.getKey().getName())]);
        }
    }

    public enum NodeState {
        LOCKED,
        AVAILABLE,
        RESEARCHED
    }
}
