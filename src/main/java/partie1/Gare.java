package partie1;
/**
 * Classe principale de la simulation de la gare
 * TP5 - Simulation Multithreadée
 */
public class Gare {
    
    // Constantes de configuration
    private static final int NB_VOIES = 3;
    private static final int NB_BILLETS_INITIAUX = 20;
    private static final int NB_TRAINS = 10;
    private static final int NB_VOYAGEURS = 15;
    
    public static void main(String[] args) {
        System.out.println("=== DEMARRAGE DE LA SIMULATION DE LA GARE ===\n");
        
        // Création des espaces partagés (moniteurs)
        EspaceQuai espaceQuai = new EspaceQuai(NB_VOIES);
        EspaceVente espaceVente = new EspaceVente(NB_BILLETS_INITIAUX);
        
        // Création et démarrage des trains
        System.out.println("Création de " + NB_TRAINS + " trains...");
        for (int i = 1; i <= NB_TRAINS; i++) {
            Thread train = new Thread(new Train(i, espaceQuai));
            train.setDaemon(true);  // Thread daemon selon les bonnes pratiques
            train.start();
        }
        
        // Création et démarrage des voyageurs
        System.out.println("Création de " + NB_VOYAGEURS + " voyageurs...\n");
        for (int i = 1; i <= NB_VOYAGEURS; i++) {
            Thread voyageur = new Thread(new Voyageur(i, espaceVente, espaceQuai));
            voyageur.setDaemon(true);  // Thread daemon
            voyageur.start();
        }
        
        // Attente pour observer la simulation
        try {
            Thread.sleep(5000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n=== FIN DE LA SIMULATION ===");
    }
}
