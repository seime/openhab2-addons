package org.openhab.binding.millheat.internal.model;

import java.util.HashMap;
import java.util.Map;

public enum ModeType {
    AlwaysHome(-1),
    Sleep(2),
    Comfort(1),
    Away(3),
    Off(5),
    AdvancedAway(-4);

    private int value;
    private static Map<Integer, ModeType> map = new HashMap<>();

    private ModeType(int value) {
        this.value = value;
    }

    static {
        for (ModeType Mode : ModeType.values()) {
            map.put(Mode.value, Mode);
        }
    }

    public static ModeType valueOf(int Mode) {
        return map.get(Mode);
    }

    public int getValue() {
        return value;
    }
}
