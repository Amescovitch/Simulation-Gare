package partie1;


/**
 * Moniteur gérant les voies et l'embarquement
 * OBJET PARTAGÉ - Synchronisation critique
 */
public class EspaceQuai {
    
    // Variables d'état du moniteur
    private final boolean[] voiesOccupees;
    private final Train[] trainsEnGare;
    private final int nbVoies;
    
    /**
     * Constructeur
     * @param nbVoies nombre de voies disponibles
     */
    public EspaceQuai(int nbVoies) {
        this.nbVoies = nbVoies;
        this.voiesOccupees = new boolean[nbVoies];
        this.trainsEnGare = new Train[nbVoies];
        
        for (int i = 0; i < nbVoies; i++) {
            voiesOccupees[i] = false;
            trainsEnGare[i] = null;
        }
        
        System.out.println("[EspaceQuai] Initialisé avec " + nbVoies + " voies");
    }
    
    /**
     * Réserver une voie libre pour un train
     * SECTION CRITIQUE avec attente conditionnelle
     * @param train le train demandant une voie
     * @return numéro de la voie attribuée
     */
    public synchronized int reserverVoie(Train train) {
        // Attente passive tant qu'aucune voie n'est libre
        while (toutesVoiesOccupees()) {
            try {
                System.out.println("[EspaceQuai] Train " + train.getId() + 
                                 " - Aucune voie libre, attente...");
                wait();  // Libère le moniteur et attend
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }
        
        // Recherche d'une voie libre
        int voieAttribuee = trouverVoieLibre();
        voiesOccupees[voieAttribuee] = true;
        trainsEnGare[voieAttribuee] = train;
        
        System.out.println("[EspaceQuai] Train " + train.getId() + 
                         " - Voie " + (voieAttribuee + 1) + " attribuée");
        return voieAttribuee;
    }
    
    /**
     * Libérer une voie
     * SECTION CRITIQUE avec notification
     * @param numeroVoie numéro de la voie à libérer
     */
    public synchronized void libererVoie(int numeroVoie) {
        if (numeroVoie >= 0 && numeroVoie < nbVoies) {
            Train train = trainsEnGare[numeroVoie];
            voiesOccupees[numeroVoie] = false;
            trainsEnGare[numeroVoie] = null;
            
            System.out.println("[EspaceQuai] Train " + train.getId() + 
                             " - Voie " + (numeroVoie + 1) + " libérée");
            
            notifyAll();  // Réveille tous les trains en attente
        }
    }
    
    /**
     * Tenter de monter dans un train
     * SECTION CRITIQUE - Parcours des trains à quai
     * @param voyageurId identifiant du voyageur
     * @return true si embarquement réussi, false sinon
     */
    public synchronized boolean monterDansTrain(int voyageurId) {
        // Parcours de tous les trains en gare
        for (int i = 0; i < nbVoies; i++) {
            if (trainsEnGare[i] != null) {
                Train train = trainsEnGare[i];
                
                // Tentative d'embarquement (délégué au train)
                if (train.embarquerVoyageur(voyageurId)) {
                    return true;
                }
            }
        }
        
        // Aucun train disponible avec des places
        System.out.println("[EspaceQuai] Voyageur " + voyageurId + 
                         " - Aucun train disponible, réessaiera plus tard");
        return false;
    }
    
    /**
     * Vérifier si toutes les voies sont occupées
     * @return true si toutes occupées
     */
    private boolean toutesVoiesOccupees() {
        for (boolean occupee : voiesOccupees) {
            if (!occupee) return false;
        }
        return true;
    }
    
    /**
     * Trouver une voie libre
     * @return numéro de la première voie libre, -1 si aucune
     */
    private int trouverVoieLibre() {
        for (int i = 0; i < nbVoies; i++) {
            if (!voiesOccupees[i]) {
                return i;
            }
        }
        return -1;
    }
}
