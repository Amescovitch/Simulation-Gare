package partie1;

/**
 * Moniteur gérant la vente de billets
 * OBJET PARTAGÉ - Nécessite synchronisation
 */
public class EspaceVente {
    
    // Variables d'état du moniteur (pas de getters/setters selon consignes)
    private int billetsDisponibles;
    
    /**
     * Constructeur
     * @param nbBilletsInitiaux nombre initial de billets disponibles
     */
    public EspaceVente(int nbBilletsInitiaux) {
        this.billetsDisponibles = nbBilletsInitiaux;
        System.out.println("[EspaceVente] Initialisé avec " + nbBilletsInitiaux + " billets");
    }
    
    /**
     * Acheter un billet
     * SECTION CRITIQUE - Exclusion mutuelle nécessaire
     * @param voyageurId identifiant du voyageur
     * @return true si achat réussi, false sinon
     */
    public synchronized boolean acheterBillet(int voyageurId) {
        if (billetsDisponibles <= 0) {
            System.out.println("[EspaceVente] Voyageur " + voyageurId + 
                             " - Plus de billets disponibles !");
            return false;
        }
        
        // Simulation du temps d'impression du ticket (variable pour réalisme)
        int tempsImpression = 500 + (int)(Math.random() * 1000);
        System.out.println("[EspaceVente] Voyageur " + voyageurId + 
                         " - Impression du billet en cours... (" + tempsImpression + "ms)");
        
        try {
            Thread.sleep(tempsImpression);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        billetsDisponibles--;
        System.out.println("[EspaceVente] Voyageur " + voyageurId + 
                         " - Billet acheté ! Restants: " + billetsDisponibles);
        return true;
    }
}
