package org.openhab.binding.millheat.internal.model;

import java.util.HashMap;
import java.util.Map;

public enum ModeType {
    AlwaysHome(-1),
    Comfort(1),
    Sleep(2),
    Away(3),
    AdvancedAway(4),
    Off(5);

    private static Map<Integer, ModeType> map = new HashMap<>();
    static {
        for (ModeType Mode : ModeType.values()) {
            map.put(Mode.value, Mode);
        }
    }

    public static ModeType valueOf(int Mode) {
        return map.get(Mode);
    }

    private int value;

    private ModeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
