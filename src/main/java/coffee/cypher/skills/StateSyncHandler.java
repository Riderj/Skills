package coffee.cypher.skills;

import coffee.cypher.skills.ResearchMapState.NodeState;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Map;

public class StateSyncHandler implements IMessageHandler<StateSyncMessage, IMessage> {
    @Override
    public IMessage onMessage(StateSyncMessage message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> this.apply(message.getMap(), message.getStates()));
        return null;
    }

    public void apply(ResearchMap map, Map<Integer, NodeState> states) {
        ResearchMapState curState = ResearchProperty.getResearchMapState(Minecraft.getMinecraft().thePlayer, map);
        curState.setState(states);
    }
}
