package si.deisinger.providers.model.ampeco;

import java.util.List;

public class AmpecoDetailedLocation {
    public List<Locations> locations;

    public static class Locations {
        public Long id;
        public String name;
        public String address;
        public String location;
        public List<Zone> zones;
        public String updatedAt;
        public String timezone;

    }

    public static class Connector {
        public String name;
        public String icon;
        public String format;
        public String status;

        @Override
        public String toString() {
            return "{" + "name='" + name + '\'' + ", icon='" + icon + '\'' + ", format='" + format + '\'' + ", status='" + status + '\'' + '}';
        }
    }

    public static class Evse {
        public String id;
        public String identifier;
        public String networkId;
        public int maxPower;
        public String currentType;
        public String status;
        public boolean managedByOperator;
        public int reservationMinutes;
        public boolean isAvailable;
        public String tariffId;
        public List<Connector> connectors;
        public String roamingEvseId;
        public String qrUrl;
        public String capabilities;
        public boolean hasParkingBarrier;
        public boolean canReserve;
        public boolean isTemporarilyUnavailable;
        public boolean isLongTermUnavailable;

        @Override
        public String toString() {
            return "{" + "id='" + id + '\'' + ", identifier='" + identifier + '\'' + ", networkId='" + networkId + '\'' + ", maxPower=" + maxPower + ", currentType='" + currentType + '\'' + ", status='" + status + '\'' + ", managedByOperator="
                    + managedByOperator + ", reservationMinutes=" + reservationMinutes + ", isAvailable=" + isAvailable + ", tariffId='" + tariffId + '\'' + ", connectors=" + connectors + ", roamingEvseId='" + roamingEvseId + '\'' + ", qrUrl='" + qrUrl + '\''
                    + ", capabilities='" + capabilities + '\'' + ", hasParkingBarrier=" + hasParkingBarrier + ", canReserve=" + canReserve + ", isTemporarilyUnavailable=" + isTemporarilyUnavailable + ", isLongTermUnavailable=" + isLongTermUnavailable + '}';
        }
    }

    public static class Zone {
        public List<Evse> evses;

        @Override
        public String toString() {
            return "{" + "evses=" + evses + '}';
        }
    }

    @Override
    public String toString() {
        return "{" + "locations=" + locations + '}';
    }
}
