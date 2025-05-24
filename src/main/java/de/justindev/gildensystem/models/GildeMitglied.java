package de.justindev.gildensystem.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class GildeMitglied {
    
    private final int gildenId;
    private final UUID spielerUuid;
    private final String spielerName;
    private GildeRang rang;
    private final LocalDateTime beitrittsdatum;
    
    public GildeMitglied(int gildenId, UUID spielerUuid, String spielerName, GildeRang rang, LocalDateTime beitrittsdatum) {
        this.gildenId = gildenId;
        this.spielerUuid = spielerUuid;
        this.spielerName = spielerName;
        this.rang = rang;
        this.beitrittsdatum = beitrittsdatum;
    }
    
    public int getGildenId() {
        return gildenId;
    }
    
    public UUID getSpielerUuid() {
        return spielerUuid;
    }
    
    public String getSpielerName() {
        return spielerName;
    }
    
    public GildeRang getRang() {
        return rang;
    }
    
    public void setRang(GildeRang rang) {
        this.rang = rang;
    }
    
    public LocalDateTime getBeitrittsdatum() {
        return beitrittsdatum;
    }
    
    public String getFormattedBeitrittsdatum() {
        return beitrittsdatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}
