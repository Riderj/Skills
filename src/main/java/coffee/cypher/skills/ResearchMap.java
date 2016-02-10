package coffee.cypher.skills;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.stream.Collectors;

public class ResearchMap implements INBTSerializable<NBTTagCompound> {
    private final BiMap<ResearchNode, String> nodeIds;
    private final String name;

    public ResearchMap(String name, List<ResearchNode> nodes) {
        this.name = name;

        nodes.forEach(ResearchNode::lock);

        nodeIds = HashBiMap.create(nodes.size());

        for (ResearchNode n : nodes) {
            nodeIds.put(n, n.getName());
        }

        validate();
    }

    private void validate() {
        HashSet<ResearchNode> scanned = new HashSet<>();
        Set<ResearchNode> nodes = nodeIds.keySet();
        boolean scan = nodes.stream().filter(n -> n.getDependencies().isEmpty())
                .allMatch(n -> isBranchValid(n, scanned, new HashSet<>()));
        if (!(scan && (scanned.size() == nodes.size()))) {
            throw new IllegalArgumentException("Research map " + name +
                    " contains dependency cycle and could not be registered");
        }
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

    final ResearchNode getNode(String name) {
        return nodeIds.inverse().get(name);
    }

    final Set<ResearchNode> getNodes() {
        return nodeIds.keySet();
    }

    public final String getName() {
        return name;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        for (ResearchNode e : nodeIds.keySet()) {
            NBTTagCompound nodeTag = e.serialize();

            NBTTagList depTag1 = new NBTTagList();
            e.getDependencies()
                    .stream().map(ResearchNode::getName)
                    .forEach(n -> depTag1.appendTag(new NBTTagString(n)));

            nodeTag.setTag("dependencies", depTag1);

            NBTTagList depTag2 = new NBTTagList();
            e.getDependants()
                    .stream().map(ResearchNode::getName)
                    .forEach(n -> depTag2.appendTag(new NBTTagString(n)));

            nodeTag.setTag("dependants", depTag2);

            tag.setTag(e.getName(), nodeTag);
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

        validate();
    }
}