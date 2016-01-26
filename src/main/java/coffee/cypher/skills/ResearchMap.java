package coffee.cypher.skills;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class ResearchMap implements INBTSerializable<NBTTagCompound> {
    private final BiMap<ResearchNode, Integer> nodeIds;
    private final String name;

    public ResearchMap(String name, List<ResearchNode> nodes) {
        this.name = name;

        nodes.forEach(ResearchNode::lock);

        if (!isValid(nodes)) {
            throw new IllegalArgumentException("Research map " + name +
                    " contains dependency cycle and could not be registered");
        }

        nodeIds = HashBiMap.create(nodes.size());
        int i = 0;
        for (ResearchNode n : nodes) {
            nodeIds.put(n, i++);
        }
    }

    private boolean isValid(List<ResearchNode> nodes) {
        HashSet<ResearchNode> scanned = new HashSet<>();
        boolean scan = nodes.stream().filter(n -> n.getDependencies().isEmpty())
                .allMatch(n -> isBranchValid(n, scanned, new HashSet<>()));
        return scan && (scanned.size() == nodes.size());
    }

    private boolean isBranchValid(ResearchNode start, Set<ResearchNode> scanned, Set<ResearchNode> scanning) {
        scanning.add(start);
        for (ResearchNode next : start.getDependants()) {
            if (scanned.contains(next)) {
                return true;
            }

            if (scanning.contains(next)) {
                return false;
            }

            if (!isBranchValid(next, scanned, scanning)) {
                return false;
            }
        }
        scanning.remove(start);
        scanned.add(start);
        return true;
    }

    final int getId(ResearchNode node) {
        return nodeIds.get(node);
    }

    final ResearchNode getNode(int id) {
        return nodeIds.inverse().get(id);
    }

    final BiMap<ResearchNode, Integer> getNodes() {
        return nodeIds;
    }

    public final String getName() {
        return name;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        for (BiMap.Entry<ResearchNode, Integer> e : nodeIds.entrySet()) {
            NBTTagCompound nodeTag = e.getKey().serialize();

            int[] dependencies = e.getKey().getDependencies().stream().mapToInt(nodeIds::get).toArray();
            nodeTag.setIntArray("dependencies", dependencies);

            int[] dependants = e.getKey().getDependants().stream().mapToInt(nodeIds::get).toArray();
            nodeTag.setIntArray("dependants", dependants);

            tag.setTag(e.getValue().toString(), nodeTag);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        nodeIds.clear();
        for (String e : tag.getKeySet()) {
            int id = Integer.parseInt(e);
            ResearchNode node = ResearchNode.deserialize(tag.getCompoundTag(e));
            nodeIds.put(node, id);
        }
        
        for (Map.Entry<ResearchNode, Integer> e : nodeIds.entrySet()) {
            e.getKey().dependencies.clear();
            for (int id : tag.getCompoundTag(e.getValue().toString()).getIntArray("dependencies")) {
                e.getKey().dependencies.add(getNode(id));
            }

            e.getKey().dependants.clear();
            for (int id : tag.getCompoundTag(e.getValue().toString()).getIntArray("dependants")) {
                e.getKey().dependants.add(getNode(id));
            }
        }
    }
}