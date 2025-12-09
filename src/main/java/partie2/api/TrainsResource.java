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
 * Ressource REST pour les trains
 * GET  /trains - Lister tous les trains
 * POST /trains - Créer un nouveau train
 */
public class TrainsResource extends ServerResource {
    
    /**
     * GET /trains
     * Liste tous les trains avec leurs états
     */
    @Get("json")
    public Representation listerTrains() {
        try {
            SimulationManager manager = SimulationManager.getInstance();
            List<Map<String, Object>> trains = manager.getTrainsAvecEtats();
            
            // Construction JSON
            JSONObject response = new JSONObject();
            response.put("count", trains.size());
            
            JSONArray trainsArray = new JSONArray();
            for (Map<String, Object> train : trains) {
                JSONObject trainJson = new JSONObject();
                trainJson.put("id", train.get("id"));
                trainJson.put("etat", train.get("etat"));
                trainJson.put("etatDescription", train.get("etatDescription"));
                trainJson.put("vitesse", train.get("vitesse"));
                trainJson.put("placesLibres", train.get("placesLibres"));
                
                int voie = (int) train.get("numeroVoie");
                trainJson.put("numeroVoie", voie >= 0 ? voie + 1 : "N/A");
                
                trainsArray.put(trainJson);
            }
            
            response.put("trains", trainsArray);
            
            System.out.println("[API] GET /trains - " + trains.size() + " trains");
            return new JsonRepresentation(response);
            
        } catch (Exception e) {
            return creerErreur("Erreur listing trains: " + e.getMessage());
        }
    }
    
    /**
     * POST /trains
     * Crée et démarre un nouveau train
     */
    @Post("json")
    public Representation creerTrain() {
        try {
            SimulationManager manager = SimulationManager.getInstance();
            partie2.simulation.Train train = manager.creerTrain();
            
            // Réponse JSON
            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Train créé et démarré");
            response.put("trainId", train.getId());
            response.put("etat", train.getEtat().name());
            response.put("vitesse", train.getVitesse());
            
            System.out.println("[API] POST /trains - Train " + train.getId() + " créé");
            return new JsonRepresentation(response);
            
        } catch (Exception e) {
            return creerErreur("Erreur création train: " + e.getMessage());
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
