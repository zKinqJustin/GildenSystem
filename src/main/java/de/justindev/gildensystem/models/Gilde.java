package de.justindev.gildensystem.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Gilde {
    
    private final int id;
    private final String name;
    private final String kuerzel;
    private final UUID ersteller;
    private final LocalDateTime erstellungsdatum;
    private boolean offen;
    
    public Gilde(int id, String name, String kuerzel, UUID ersteller, LocalDateTime erstellungsdatum, boolean offen) {
        this.id = id;
        this.name = name;
        this.kuerzel = kuerzel;
        this.ersteller = ersteller;
        this.erstellungsdatum = erstellungsdatum;
        this.offen = offen;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getKuerzel() {
        return kuerzel;
    }
    
    public UUID getErsteller() {
        return ersteller;
    }
    
    public LocalDateTime getErstellungsdatum() {
        return erstellungsdatum;
    }
    
    public String getFormattedErstellungsdatum() {
        return erstellungsdatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
    
    public boolean isOffen() {
        return offen;
    }
    
    public void setOffen(boolean offen) {
        this.offen = offen;
    }
}
