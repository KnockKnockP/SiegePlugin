package knockknockp.siegeplugin.Siege;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ClientSideSlime extends ClientSideEntity {
    public ClientSideSlime(Player player, Location location) {
        super(player, location, EntityType.SLIME);
    }

    public void setSize(int size) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, id);

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        wrappedDataWatcher.setEntity(player);
        wrappedDataWatcher.setObject(16, WrappedDataWatcher.Registry.get(Integer.class), size);
        packetContainer.getDataValueCollectionModifier().write(0, addWrappedDataValues(wrappedDataWatcher));

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
    }
}