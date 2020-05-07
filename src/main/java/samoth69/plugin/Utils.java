package samoth69.plugin;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

import java.util.Arrays;
import java.util.Random;

public class Utils {

    private final static DyeColor[] dyeColors = {
        DyeColor.WHITE,
                DyeColor.ORANGE,
                DyeColor.MAGENTA,
                DyeColor.LIGHT_BLUE,
                DyeColor.YELLOW,
                DyeColor.LIME,
                DyeColor.PINK,
                DyeColor.GRAY,
                DyeColor.SILVER,
                DyeColor.CYAN,
                DyeColor.PURPLE,
                DyeColor.BLUE,
                DyeColor.BROWN,
                DyeColor.GREEN,
                DyeColor.RED,
                DyeColor.BLACK
    };

    private final static ChatColor[] chatColors = {
            ChatColor.WHITE,
            ChatColor.GOLD,
            ChatColor.DARK_PURPLE,
            ChatColor.BLUE,
            ChatColor.YELLOW,
            ChatColor.GREEN,
            ChatColor.LIGHT_PURPLE, //on met du violet pour du rose
            ChatColor.DARK_GRAY,
            ChatColor.GRAY,
            ChatColor.DARK_AQUA,
            ChatColor.DARK_PURPLE,
            ChatColor.DARK_BLUE,
            ChatColor.GOLD,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_RED,
            ChatColor.BLACK
    };

    public static String getRandomGlassColor() {
        return dyeColors[new Random().nextInt(16)].toString().toLowerCase();
    }

    public static DyeColor getRandomDyeColor() {
        return dyeColors[new Random().nextInt(16)];
    }

    public static ChatColor getRandomChatColor() {return dyeColorToChatColor(getRandomDyeColor());}

    public static short getRandomShortColor() {return (short)new Random().nextInt(16);}

    public static int getIntFromColor(DyeColor c) {
        return Arrays.asList(dyeColors).indexOf(c);
    }

    public static int getIntFromChatColor(ChatColor c) {
        return Arrays.asList(chatColors).indexOf(c);
    }

    private static DyeColor chatColorToDyeColor(ChatColor c) {
        return dyeColors[Arrays.asList(chatColors).indexOf(c)];
    }

    private static ChatColor dyeColorToChatColor(DyeColor dc) {
        return chatColors[Arrays.asList(dyeColors).indexOf(dc)];
    }

    public static ChatColor getChatColorFromInt(int c) {
        return chatColors[c];
    }

    public static DyeColor getDyeColorFromInt(int c) {
        return dyeColors[c];
    }
}
