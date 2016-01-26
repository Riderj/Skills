package coffee.cypher.skills;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MapSyncHandler implements IMessageHandler<MapSyncMessage, IMessage> {
    @Override
    public IMessage onMessage(MapSyncMessage message, MessageContext ctx) {
        ResearchProperty.adaptToServerMaps(message.getTag());
        return null;
    }
}
