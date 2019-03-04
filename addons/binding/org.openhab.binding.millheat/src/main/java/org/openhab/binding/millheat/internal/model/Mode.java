package org.openhab.binding.millheat.internal.model;

import java.time.LocalDateTime;

public class Mode {

    public Mode(ModeType mode, LocalDateTime start, LocalDateTime end) {
        super();
        this.mode = mode;
        this.start = start;
        this.end = end;
    }

    public ModeType mode;
    public LocalDateTime start;
    public LocalDateTime end;
}
