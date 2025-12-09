package partie1;

/**
 * Thread représentant un train
 */
public class Train implements Runnable {
    
    // Constantes
    private static final int VITESSE_MIN = 50;   // km/h
    private static final int VITESSE_MAX = 300;  // km/h
    private static final int TEMPS_ARRET = 5000; // ms
    private static final int CAPACITE_MAX = 50;
    
    // Attributs du train
    private final int id;
    private final EspaceQuai espaceQuai;
    private EtatTrain etat;
    private final int vitesse;
    private int placesLibres;
    private int numeroVoie;
    
    /**
     * Constructeur
     * @param id identifiant du train
     * @param espaceQuai référence vers l'espace quai partagé
     */
    public Train(int id, EspaceQuai espaceQuai) {
        this.id = id;
        this.espaceQuai = espaceQuai;
        this.etat = EtatTrain.EN_ROUTE;
        
        // Vitesse aléatoire entre VITESSE_MIN et VITESSE_MAX
        this.vitesse = VITESSE_MIN + (int)(Math.random() * (VITESSE_MAX - VITESSE_MIN + 1));
        
        // Nombre de places libres aléatoire entre 0 et CAPACITE_MAX
        this.placesLibres = (int)(Math.random() * (CAPACITE_MAX + 1));
        
        System.out.println("[Train " + id + "] Créé - Vitesse: " + vitesse + 
                         " km/h, Places: " + placesLibres);
    }
    
    public int getId() {
        return id;
    }
    
    /**
     * Cycle de vie du train
     */
    @Override
    public void run() {
        try {
            // 1. Voyage vers la gare
            etat = EtatTrain.EN_ROUTE;
            int tempsVoyage = 10000 / vitesse;
            System.out.println("[Train " + id + "] En route vers la gare (" + 
                             tempsVoyage + "ms)...");
            Thread.sleep(tempsVoyage);
            
            // 2. Arrivée - Demande d'une voie
            etat = EtatTrain.EN_ATTENTE_VOIE;
            System.out.println("[Train " + id + "] Arrivé, recherche d'une voie...");
            numeroVoie = espaceQuai.reserverVoie(this);
            
            if (numeroVoie < 0) {
                System.out.println("[Train " + id + "] Interruption, abandon");
                return;
            }
            
            // 3. Stationnement en gare
            etat = EtatTrain.EN_GARE;
            System.out.println("[Train " + id + "] En gare sur voie " + 
                             (numeroVoie + 1) + " - Embarquement pendant " + 
                             TEMPS_ARRET + "ms");
            Thread.sleep(TEMPS_ARRET);
            
            // 4. Départ
            System.out.println("[Train " + id + "] Départ de la gare - " + 
                             (CAPACITE_MAX - placesLibres) + " voyageurs à bord");
            espaceQuai.libererVoie(numeroVoie);
            etat = EtatTrain.PARTI;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[Train " + id + "] Interruption");
        }
    }
    
    /**
     * Embarquer un voyageur (appelé par EspaceQuai sous synchronisation)
     * IMPORTANT: Cette méthode est synchronized car elle modifie l'état du train
     * @param voyageurId identifiant du voyageur
     * @return true si embarquement réussi
     */
    public synchronized boolean embarquerVoyageur(int voyageurId) {
        if (etat != EtatTrain.EN_GARE) {
            return false;  // Train pas en gare
        }
        
        if (placesLibres <= 0) {
            return false;  // Plus de places
        }
        
        placesLibres--;
        System.out.println("[Train " + id + "] Voyageur " + voyageurId + 
                         " embarqué ! Places restantes: " + placesLibres);
        return true;
    }
}
