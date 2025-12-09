package partie2.api;


import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Serveur API REST Restlet
 */
public class ApiRestServer {
    
    private final Component component;
    private final int port;
    
    /**
     * Constructeur
     */
    public ApiRestServer(int port) {
        this.port = port;
        this.component = new Component();
        
        // Configuration du serveur HTTP
        component.getServers().add(Protocol.HTTP, port);
        
        // Routage des ressources
        component.getDefaultHost().attach("/trains", TrainsResource.class);
        component.getDefaultHost().attach("/voyageurs", VoyageursResource.class);
    }
    
    /**
     * Démarrer le serveur
     */
    public void demarrer() throws Exception {
        component.start();
        System.out.println("[ApiRestServer] Démarré sur port " + port);
    }
    
    /**
     * Arrêter le serveur
     */
    public void arreter() throws Exception {
        component.stop();
        System.out.println("[ApiRestServer] Arrêté");
    }
}
