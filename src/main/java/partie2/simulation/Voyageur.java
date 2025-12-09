package partie2.simulation;


/**
 * Thread représentant un voyageur
 * MODIFIÉ: Ajout accesseurs pour API REST
 */
public class Voyageur implements Runnable {
    
    // Constantes
    private static final int TEMPS_ARRIVEE_MAX = 3000;
    private static final int TEMPS_ENTRE_TENTATIVES = 2000;
    private static final int MAX_TENTATIVES = 50;
    
    // Attributs
    private final int id;
    private final EspaceVente espaceVente;
    private final EspaceQuai espaceQuai;
    private volatile EtatVoyageur etat;  // volatile pour visibilité
    
    /**
     * Constructeur
     */
    public Voyageur(int id, EspaceVente espaceVente, EspaceQuai espaceQuai) {
        this.id = id;
        this.espaceVente = espaceVente;
        this.espaceQuai = espaceQuai;
        this.etat = EtatVoyageur.EN_ROUTE;
        
        System.out.println("[Voyageur " + id + "] Créé");
    }
    
    // ========== ACCESSEURS POUR API REST ==========
    
    public int getId() {
        return id;
    }
    
    public EtatVoyageur getEtat() {
        return etat;
    }
    
    // ========== LOGIQUE MÉTIER ==========
    
    @Override
    public void run() {
        try {
            // 1. Arrivée
            etat = EtatVoyageur.EN_ROUTE;
            int tempsArrivee = (int)(Math.random() * TEMPS_ARRIVEE_MAX);
            System.out.println("[Voyageur " + id + "] En route (" + tempsArrivee + "ms)...");
            Thread.sleep(tempsArrivee);
            
            // 2. Achat billet
            System.out.println("[Voyageur " + id + "] Achat billet...");
            if (!espaceVente.acheterBillet(id)) {
                System.out.println("[Voyageur " + id + "] Échec achat, abandon");
                return;
            }
            
            etat = EtatVoyageur.MUNI_TICKET;
            
            // 3. Recherche train
            System.out.println("[Voyageur " + id + "] Recherche train...");
            boolean monte = false;
            int tentatives = 0;
            
            while (!monte && tentatives < MAX_TENTATIVES) {
                monte = espaceQuai.monterDansTrain(id);
                
                if (!monte) {
                    tentatives++;
                    Thread.sleep(TEMPS_ENTRE_TENTATIVES);
                }
            }
            
            if (monte) {
                etat = EtatVoyageur.MONTE_TRAIN;
                System.out.println("[Voyageur " + id + "] Monté dans train !");
            } else {
                System.out.println("[Voyageur " + id + "] Abandon (" + tentatives + " tentatives)");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
