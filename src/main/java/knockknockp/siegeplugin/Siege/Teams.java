package knockknockp.siegeplugin.Siege;

import org.bukkit.ChatColor;

public enum Teams implements ChatColorable {
    NEUTRAL {
        @Override
        public String toString() {
            return "Neutral";
        }

        @Override
        public ChatColor toChatColor() {
            return ChatColor.GRAY;
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
    }
}