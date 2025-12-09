package partie2.simulation;

/**
 * Thread représentant un train
 * MODIFIÉ: Ajout accesseurs pour API REST
 */
public class Train implements Runnable {
    
    // Constantes
    private static final int VITESSE_MIN = 50;
    private static final int VITESSE_MAX = 300;
    private static final int TEMPS_ARRET = 8000; // ms
    private static final int CAPACITE_MAX = 50;
    
    // Attributs
    private final int id;
    private final EspaceQuai espaceQuai;
    private volatile EtatTrain etat;  // volatile pour visibilité thread-safe
    private final int vitesse;
    private final int capaciteInitiale;
    private int placesLibres;
    private int numeroVoie;
    
    /**
     * Constructeur
     */
    public Train(int id, EspaceQuai espaceQuai) {
        this.id = id;
        this.espaceQuai = espaceQuai;
        this.etat = EtatTrain.EN_ROUTE;
        this.numeroVoie = -1;
        
        // Vitesse aléatoire
        this.vitesse = VITESSE_MIN + (int)(Math.random() * (VITESSE_MAX - VITESSE_MIN + 1));
        
        // Places aléatoires
        this.capaciteInitiale = (int)(Math.random() * (CAPACITE_MAX + 1));
        this.placesLibres = capaciteInitiale;
        
        System.out.println("[Train " + id + "] Créé - Vitesse: " + vitesse + 
                         " km/h, Capacité: " + capaciteInitiale);
    }
    
    // ========== ACCESSEURS POUR API REST ==========
    
    public int getId() {
        return id;
    }
    
    public EtatTrain getEtat() {
        return etat;
    }
    
    public int getVitesse() {
        return vitesse;
    }
    
    public synchronized int getPlacesLibresActuelles() {
        return placesLibres;
    }
    
    public int getNumeroVoie() {
        return numeroVoie;
    }
    
    // ========== LOGIQUE MÉTIER ==========
    
    @Override
    public void run() {
        try {
            // 1. Voyage vers la gare
            etat = EtatTrain.EN_ROUTE;
            int tempsVoyage = 10000 / vitesse;
            System.out.println("[Train " + id + "] En route (" + tempsVoyage + "ms)...");
            Thread.sleep(tempsVoyage);
            
            // 2. Arrivée - Demande voie
            etat = EtatTrain.EN_ATTENTE_VOIE;
            System.out.println("[Train " + id + "] Arrivé, recherche voie...");
            numeroVoie = espaceQuai.reserverVoie(this);
            
            if (numeroVoie < 0) {
                etat = EtatTrain.PARTI;
                return;
            }
            
            // 3. En gare
            etat = EtatTrain.EN_GARE;
            System.out.println("[Train " + id + "] En gare (voie " + (numeroVoie + 1) + 
                             ") - Embarquement " + TEMPS_ARRET + "ms");
            Thread.sleep(TEMPS_ARRET);
            
            // 4. Départ
            int passagers = capaciteInitiale - placesLibres;
            System.out.println("[Train " + id + "] Départ - " + passagers + " voyageurs");
            espaceQuai.libererVoie(numeroVoie);
            etat = EtatTrain.PARTI;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            etat = EtatTrain.PARTI;
        }
    }
    
    /**
     * Embarquer un voyageur
     * Synchronized car modifie l'état partagé
     */
    public synchronized boolean embarquerVoyageur(int voyageurId) {
        if (etat != EtatTrain.EN_GARE || placesLibres <= 0) {
            return false;
        }
        
        placesLibres--;
        System.out.println("[Train " + id + "] Voyageur " + voyageurId + 
                         " embarqué ! Places restantes: " + placesLibres);
        return true;
    }
}
