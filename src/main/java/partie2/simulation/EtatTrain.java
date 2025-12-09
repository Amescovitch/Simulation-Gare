package partie2.simulation;
/**
 * Etats possibles d'un train
 */
public enum EtatTrain {
    EN_ROUTE("En route vers la gare"),
    EN_ATTENTE_VOIE("En attente d'une voie libre"),
    EN_GARE("En gare"),
    PARTI("Parti");
    
    private final String description;
    
    EtatTrain(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
