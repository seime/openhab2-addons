package org.openhab.binding.sensibo.internal.model;

public class Timer {

    public int secondsRemaining;
    public AcState acState;
    public boolean enabled;

    public Timer(org.openhab.binding.sensibo.internal.dto.poddetails.Timer dto) {
        this.secondsRemaining = dto.targetTimeSecondsFromNow;
        this.acState = new AcState(dto.acState);
        this.enabled = dto.enabled;
    }
}
