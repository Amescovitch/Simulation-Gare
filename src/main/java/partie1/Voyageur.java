package partie1;

/**
 * Thread représentant un voyageur
 */
public class Voyageur implements Runnable {
    
    // Constantes
    private static final int TEMPS_ARRIVEE_MAX = 3000;  // ms
    private static final int TEMPS_ENTRE_TENTATIVES = 2000;  // ms
    private static final int MAX_TENTATIVES = 50;
    
    // Attributs du voyageur
    private final int id;
    private final EspaceVente espaceVente;
    private final EspaceQuai espaceQuai;
    private EtatVoyageur etat;
    
    /**
     * Constructeur
     * @param id identifiant du voyageur
     * @param espaceVente référence vers l'espace vente
     * @param espaceQuai référence vers l'espace quai
     */
    public Voyageur(int id, EspaceVente espaceVente, EspaceQuai espaceQuai) {
        this.id = id;
        this.espaceVente = espaceVente;
        this.espaceQuai = espaceQuai;
        this.etat = EtatVoyageur.EN_ROUTE;
        
        System.out.println("[Voyageur " + id + "] Créé");
    }
    
    /**
     * Cycle de vie du voyageur
     */
    @Override
    public void run() {
        try {
            // 1. Arrivée à la gare
            etat = EtatVoyageur.EN_ROUTE;
            int tempsArrivee = (int)(Math.random() * TEMPS_ARRIVEE_MAX);
            System.out.println("[Voyageur " + id + "] En route vers la gare (" + 
                             tempsArrivee + "ms)...");
            Thread.sleep(tempsArrivee);
            
            // 2. Achat d'un billet
            System.out.println("[Voyageur " + id + "] Arrivé, achat du billet...");
            boolean billetAchete = espaceVente.acheterBillet(id);
            
            if (!billetAchete) {
                System.out.println("[Voyageur " + id + "] Échec achat, abandon");
                return;
            }
            
            etat = EtatVoyageur.MUNI_TICKET;
            
            // 3. Recherche d'un train
            System.out.println("[Voyageur " + id + "] Recherche d'un train...");
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
                System.out.println("[Voyageur " + id + "] Monté dans un train !");
            } else {
                System.out.println("[Voyageur " + id + "] Abandon après " + 
                                 tentatives + " tentatives");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[Voyageur " + id + "] Interruption");
        }
    }
}
