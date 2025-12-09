package partie2.api;

import partie2.manager.SimulationManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.util.List;
import java.util.Map;

/**
 * Ressource REST pour les voyageurs
 * GET  /voyageurs - Lister tous les voyageurs
 * POST /voyageurs - Créer un nouveau voyageur
 */
public class VoyageursResource extends ServerResource {
    
    /**
     * GET /voyageurs
     * Liste tous les voyageurs avec leurs états
     */
    @Get("json")
    public Representation listerVoyageurs() {
        try {
            SimulationManager manager = SimulationManager.getInstance();
            List<Map<String, Object>> voyageurs = manager.getVoyageursAvecEtats();
            
            // Construction JSON
            JSONObject response = new JSONObject();
            response.put("count", voyageurs.size());
            
            JSONArray voyageursArray = new JSONArray();
            for (Map<String, Object> voyageur : voyageurs) {
                JSONObject voyageurJson = new JSONObject();
                voyageurJson.put("id", voyageur.get("id"));
                voyageurJson.put("etat", voyageur.get("etat"));
                voyageurJson.put("etatDescription", voyageur.get("etatDescription"));
                
                voyageursArray.put(voyageurJson);
            }
            
            response.put("voyageurs", voyageursArray);
            
            System.out.println("[API] GET /voyageurs - " + voyageurs.size() + " voyageurs");
            return new JsonRepresentation(response);
            
        } catch (Exception e) {
            return creerErreur("Erreur listing voyageurs: " + e.getMessage());
        }
    }
    
    /**
     * POST /voyageurs
     * Crée et démarre un nouveau voyageur
     */
    @Post("json")
    public Representation creerVoyageur() {
        try {
            SimulationManager manager = SimulationManager.getInstance();
            partie2.simulation.Voyageur voyageur = manager.creerVoyageur();
            
            // Réponse JSON
            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Voyageur créé et démarré");
            response.put("voyageurId", voyageur.getId());
            response.put("etat", voyageur.getEtat().name());
            
            System.out.println("[API] POST /voyageurs - Voyageur " + voyageur.getId() + " créé");
            return new JsonRepresentation(response);
            
        } catch (Exception e) {
            return creerErreur("Erreur création voyageur: " + e.getMessage());
        }
    }
    
    /**
     * Créer une réponse d'erreur
     */
    private Representation creerErreur(String message) {
        try {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("error", message);
            return new JsonRepresentation(error);
        } catch (Exception e) {
            return null;
        }
    }
}
