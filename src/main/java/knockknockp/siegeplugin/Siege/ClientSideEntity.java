package knockknockp.siegeplugin.Siege;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ClientSideEntity {
    private static int lastUnusedId = 0;

    public static byte ENTITY_META_DATA_FLAG_IS_INVISIBLE = 0x20, ENTITY_META_DATA_FLAG_IS_GLOWING = 0x40;

    protected final Player player;
    protected final int id;
    protected final UUID uuid;
    private boolean removed = false;

    private static int getUnusedEntityId(World world) {
        for (; lastUnusedId < Integer.MAX_VALUE; ++lastUnusedId) {
            if (ProtocolLibrary.getProtocolManager().getEntityFromID(world, lastUnusedId) == null) {
                return lastUnusedId++;
            }
        }
        return 0;
    }

    public ClientSideEntity(Player player, Location location, EntityType entityType) {
        this.player = player;
        id = getUnusedEntityId(player.getWorld());
        uuid = UUID.randomUUID();

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        packetContainer.getIntegers().write(0, id);
        packetContainer.getUUIDs().write(0, uuid);
        packetContainer.getEntityTypeModifier().write(0, entityType);

        packetContainer.getDoubles().write(0, location.getX())
            .write(1, location.getY())
            .write(2, location.getZ());

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
    }

    /*
        https://www.spigotmc.org/threads/i-want-to-use-protocollib-to-make-fake-entity-glow.589919/
        I just want to thank CoolLord22 for saving my ass from this agony.
    */
    public void setMetaStatus(byte metaStatus) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, id);

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        wrappedDataWatcher.setEntity(player);
        wrappedDataWatcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), metaStatus);
        packetContainer.getDataValueCollectionModifier().write(0, addWrappedDataValues(wrappedDataWatcher));

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
    }

    protected List<WrappedDataValue> addWrappedDataValues(WrappedDataWatcher wrappedDataWatcher) {
        List<WrappedDataValue> wrappedDataValues = new ArrayList<>();
        wrappedDataWatcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
            WrappedDataWatcher.WrappedDataWatcherObject wrappedDataWatcherObject = entry.getWatcherObject();
            wrappedDataValues.add(new WrappedDataValue(wrappedDataWatcherObject.getIndex(), wrappedDataWatcherObject.getSerializer(), entry.getRawValue()));
        });
        return wrappedDataValues;
    }

    public void setGlowColor(ChatColor chatColor) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        packetContainer.getStrings().write(0, uuid.toString());
        packetContainer.getIntegers().write(0, 0);

        Optional<InternalStructure> optionalInternalStructure = packetContainer.getOptionalStructures().read(0);
        if (!optionalInternalStructure.isPresent()) {
            return;
        }
        InternalStructure internalStructure = optionalInternalStructure.get();
        internalStructure.getIntegers().write(0, 2);
        internalStructure.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, chatColor);
        packetContainer.getOptionalStructures().write(0, Optional.of(internalStructure));

        packetContainer.getModifier().write(2, Lists.newArrayList(player.getName(), uuid.toString()));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
    }

    public void setEffect(PotionEffectType potionEffectType, byte amplifier, int duration) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_EFFECT);
        packetContainer.getIntegers().write(0, id);
        packetContainer.getEffectTypes().write(0, potionEffectType);
        packetContainer.getBytes().write(0, amplifier);
        packetContainer.getIntegers().write(1, duration);

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
    }

    public void remove() {
        if (removed) {
            return;
        }

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        List<Integer> entityIds = new ArrayList<>();
        entityIds.add(id);
        packetContainer.getIntLists().write(0, entityIds);

        removed = true;
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
    }
}