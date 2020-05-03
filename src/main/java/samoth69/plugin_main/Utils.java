package samoth69.plugin_main;

import org.bukkit.DyeColor;

import java.util.Random;

public class Utils {

    private final static DyeColor[] colors = {
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

    public static String getRandomGlassColor() {
        return colors[new Random().nextInt(16)].toString().toLowerCase();
    }

    public static DyeColor getRandomDyeColor() {
        return colors[new Random().nextInt(16)];
    }

    public static int getIntFromColor(DyeColor c) {
        int index = 0;
        for (DyeColor dc : colors) {
            if (dc.getColor().asRGB() == c.getColor().asRGB())
                return index;
            index++;
        }
        return -1;
    }
}
