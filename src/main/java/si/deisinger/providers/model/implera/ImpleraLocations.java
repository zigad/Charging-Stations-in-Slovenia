package si.deisinger.providers.model.implera;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class ImpleraLocations {

    @JacksonXmlElementWrapper(useWrapping = false)
    public List<marker> marker;

    public static class marker {
        public Long id;
        public String name;
        public String address;
        public String town;
        public double lat;
        public double lng;
        public int deluje;
        public Object cena_KW;
        public Object reklamni_tekst_baner;
        public Object cena_na_vklop;
        public Object opis;
        public String opis_tip_prikljucka_vticnice;
    }
}
