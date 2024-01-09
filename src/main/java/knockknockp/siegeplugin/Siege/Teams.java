package knockknockp.siegeplugin.Siege;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum Teams implements ChatColorable, Woolable, Terracottable, Bannerable, Candlable, Beddable, Dyable {
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

        @Override
        public Material toTerracotta() {
            return Material.GRAY_TERRACOTTA;
        }

        @Override
        public Material toBanner() {
            return Material.GRAY_BANNER;
        }

        @Override
        public Material toCandle() {
            return Material.GRAY_CANDLE;
        }

        @Override
        public Material toBed() {
            return Material.GRAY_BED;
        }

        @Override
        public Material toDye() {
            return Material.GRAY_DYE;
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

        @Override
        public Material toTerracotta() {
            return Material.RED_TERRACOTTA;
        }

        @Override
        public Material toBanner() {
            return Material.RED_BANNER;
        }

        @Override
        public Material toCandle() {
            return Material.RED_CANDLE;
        }

        @Override
        public Material toBed() {
            return Material.RED_BED;
        }

        @Override
        public Material toDye() {
            return Material.RED_DYE;
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

        @Override
        public Material toTerracotta() {
            return Material.BLUE_TERRACOTTA;
        }

        @Override
        public Material toBanner() {
            return Material.BLUE_BANNER;
        }

        @Override
        public Material toCandle() {
            return Material.BLUE_CANDLE;
        }

        @Override
        public Material toBed() {
            return Material.BLUE_BED;
        }

        @Override
        public Material toDye() {
            return Material.BLUE_DYE;
        }
    }
}