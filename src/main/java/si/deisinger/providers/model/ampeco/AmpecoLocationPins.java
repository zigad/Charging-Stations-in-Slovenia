package si.deisinger.providers.model.ampeco;

import java.util.ArrayList;

public class AmpecoLocationPins {
    public ArrayList<Pin> pins;

    public static class Pin {
        public Long id;
        public String geo;
    }
}
