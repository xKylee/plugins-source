package net.runelite.client.plugins.zulrah.constants;

import net.runelite.api.coords.LocalPoint;

public enum ZulrahLocation {
    NORTH(6720, 7616),
    EAST(8000, 7360),
    SOUTH(6720, 6208),
    WEST(5440, 7360);

    private final int localX;
    private final int localY;

    ZulrahLocation(int localX, int localY) {
        this.localX = localX;
        this.localY = localY;
    }

    public static ZulrahLocation valueOf(LocalPoint localPoint) {
        ZulrahLocation[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            ZulrahLocation loc = var1[var3];
            if (loc.toLocalPoint().equals(localPoint)) {
                return loc;
            }
        }

        return null;
    }

    public LocalPoint toLocalPoint() {
        return new LocalPoint(this.localX, this.localY);
    }
}
