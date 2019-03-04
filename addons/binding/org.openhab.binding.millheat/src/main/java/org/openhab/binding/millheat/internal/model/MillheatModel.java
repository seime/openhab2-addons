package org.openhab.binding.millheat.internal.model;

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
}
