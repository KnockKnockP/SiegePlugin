package knockknockp.siegeplugin.Siege;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum Teams implements ChatColorable, Woolable, Terracottable {
    NEUTRAL {
        @Override
        public String toString() {
            return "Neutral";
        }

        @Override
        public ChatColor toChatColor() {
            return ChatColor.GRAY;
        }

        @Override
        public Material toWool() {
            return Material.GRAY_WOOL;
        }
    },
    RED {
        @Override
        public String toString() {
            return "Red";
        }

        @Override
        public ChatColor toChatColor() {
            return ChatColor.RED;
        }

        @Override
        public Material toWool() {
            return Material.RED_WOOL;
        }

    },
    BLUE {
        @Override
        public String toString() {
            return "Blue";
        }

        @Override
        public ChatColor toChatColor() {
            return ChatColor.BLUE;
        }

        @Override
        public Material toWool() {
            return Material.BLUE_WOOL;
        }

    }
}