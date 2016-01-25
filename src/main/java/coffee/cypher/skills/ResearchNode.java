package coffee.cypher.skills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static coffee.cypher.skills.ResearchMapState.*;

public class ResearchNode {
    protected List<ResearchNode> prerequisites;
    protected String name;

    public ResearchNode(String name, ResearchNode... pre) {
        prerequisites = new ArrayList<>();
        Collections.addAll(prerequisites, pre);
    }


    public NodeState getDefaultState() {
        return NodeState.LOCKED;
    }

    public String getName() {
        return name;
    }
}
