package partie2.simulation;
/**
 * Classe principale de la simulation de la gare
 * TP5 - Simulation Multithreadée
 */

import partie2.api.ApiRestServer;
import partie2.manager.SimulationManager;

/**
 * Classe principale de la simulation de la gare avec API REST
 * TP5 - Partie 2
 */
public class Gare {
    
    // Constantes de configuration
    private static final int NB_VOIES = 3;
    private static final int NB_BILLETS_INITIAUX = 25;
    private static final int NB_TRAINS_INITIAUX = 5;
    private static final int NB_VOYAGEURS_INITIAUX = 10;
    private static final int PORT_API = 8182;
    
    public static void main(String[] args) {
        System.out.println("=== SIMULATION GARE AVEC API REST ===\n");
        
        // Création des espaces partagés
        EspaceQuai espaceQuai = new EspaceQuai(NB_VOIES);
        EspaceVente espaceVente = new EspaceVente(NB_BILLETS_INITIAUX);
        
        // Initialisation du gestionnaire
        SimulationManager manager = SimulationManager.getInstance();
        manager.initialiser(espaceQuai, espaceVente);
        
        // Création des trains initiaux
        System.out.println("Création de " + NB_TRAINS_INITIAUX + " trains initiaux...");
        for (int i = 0; i < NB_TRAINS_INITIAUX; i++) {
            manager.creerTrain();
        }
        
        // Création des voyageurs initiaux
        System.out.println("Création de " + NB_VOYAGEURS_INITIAUX + " voyageurs initiaux...\n");
        for (int i = 0; i < NB_VOYAGEURS_INITIAUX; i++) {
            manager.creerVoyageur();
        }
        
        // Démarrage du serveur API REST
        try {
            ApiRestServer apiServer = new ApiRestServer(PORT_API);
            apiServer.demarrer();
            
            System.out.println("\n=== API REST DISPONIBLE ===");
            System.out.println("URL: http://localhost:" + PORT_API);
            System.out.println("GET  /trains      - Lister les trains");
            System.out.println("POST /trains      - Ajouter un train");
            System.out.println("GET  /voyageurs   - Lister les voyageurs");
            System.out.println("POST /voyageurs   - Ajouter un voyageur\n");
            
        } catch (Exception e) {
            System.err.println("Erreur démarrage API: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Le programme reste actif pour l'API
        System.out.println("Simulation en cours... (Ctrl+C pour arrêter)\n");
    }
}
