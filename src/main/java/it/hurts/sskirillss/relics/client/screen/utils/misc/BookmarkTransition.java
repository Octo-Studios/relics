package it.hurts.sskirillss.relics.client.screen.utils.misc;

import net.minecraft.Util;

public class BookmarkTransition {
    public enum Phase {
        NONE,
        ENTER,
        LEAVE
    }

    private static final long WINDOW = 500L;

    private static String selected = null;
    private static String deselected = null;
    private static long time = 0L;

    public static String tabKey(String id) {
        return "tab:" + id;
    }

    public static String pageKey(String id) {
        return "page:" + id;
    }

    public static void mark(String selected, String deselected) {
        BookmarkTransition.selected = selected;
        BookmarkTransition.deselected = deselected;
        BookmarkTransition.time = Util.getMillis();
    }

    public static Phase consume(String key) {
        if (Util.getMillis() - time > WINDOW)
            return Phase.NONE;

        if (key.equals(selected))
            return Phase.ENTER;

        return key.equals(deselected) ? Phase.LEAVE : Phase.NONE;
    }
}
