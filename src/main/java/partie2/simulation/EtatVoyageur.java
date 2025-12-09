package partie2.simulation;

public enum EtatVoyageur {
	EN_ROUTE("En route vers la gare"),
    MUNI_TICKET("Muni d'un ticket"),
    MONTE_TRAIN("Monté dans un train");
    
    private final String description;
    
    EtatVoyageur(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
