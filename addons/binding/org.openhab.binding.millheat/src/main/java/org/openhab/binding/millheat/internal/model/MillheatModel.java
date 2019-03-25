package org.openhab.binding.millheat.internal.model;

import org.eclipse.jdt.annotation.NonNull;

public class MillheatModel {

    public long lastUpdated;

    public Home[] homes;

    public Heater findHeaterById(String id) {
        if (homes != null) {
            for (Home home : homes) {
                for (Heater heater : home.independentHeaters) {
                    if (id.equals(heater.id)) {
                        return heater;
                    }
                }

                for (Room room : home.rooms) {
                    for (Heater heater : room.heaters) {
                        if (id.equals(heater.id)) {
                            return heater;
                        }
                    }

                }
            }

        }
        return null;
    }

    public Heater findHeaterByMacOrId(String macAddress, String id) {
        Heater h = null;
        if (macAddress != null) {
            h = findHeaterByMac(macAddress);
        }

        if (h == null && id != null) {
            h = findHeaterById(id);
        }

        return h;
    }

    public Heater findHeaterByMac(String macAddress) {
        if (homes != null) {
            for (Home home : homes) {
                for (Heater heater : home.independentHeaters) {
                    if (macAddress.equals(heater.macAddress)) {
                        return heater;
                    }
                }

                for (Room room : home.rooms) {
                    for (Heater heater : room.heaters) {
                        if (macAddress.equals(heater.macAddress)) {
                            return heater;
                        }
                    }

                }
            }

        }
        return null;
    }

    public Room findRoomById(String id) {
        if (homes != null) {
            for (Home home : homes) {

                for (Room room : home.rooms) {
                    if (id.equals(room.id)) {
                        return room;
                    }
                }
            }

        }
        return null;
    }

    public Home findHomeByRoomId(@NonNull String id) {
        if (homes != null) {
            for (Home home : homes) {

                for (Room room : home.rooms) {
                    if (id.equals(room.id)) {
                        return home;
                    }
                }
            }

        }
        return null;
    }
}
