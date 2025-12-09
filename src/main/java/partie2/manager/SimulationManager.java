package partie2.manager;

import partie2.simulation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestionnaire central de la simulation
 * Thread-safe pour accès depuis l'API REST
 * SINGLETON pattern pour accès global
 */
public class SimulationManager {
    
    private static SimulationManager instance;
    
    // Collections thread-safe pour tracking des entités
    private final Map<Integer, Train> trains;
    private final Map<Integer, Voyageur> voyageurs;
    private final Map<Integer, Thread> trainsThreads;
    private final Map<Integer, Thread> voyageursThreads;
    
    // Compteurs atomiques pour IDs
    private final AtomicInteger nextTrainId;
    private final AtomicInteger nextVoyageurId;
    
    // Références aux espaces partagés
    private EspaceQuai espaceQuai;
    private EspaceVente espaceVente;
    
    /**
     * Constructeur privé (Singleton)
     */
    private SimulationManager() {
        this.trains = new ConcurrentHashMap<>();
        this.voyageurs = new ConcurrentHashMap<>();
        this.trainsThreads = new ConcurrentHashMap<>();
        this.voyageursThreads = new ConcurrentHashMap<>();
        this.nextTrainId = new AtomicInteger(1);
        this.nextVoyageurId = new AtomicInteger(1);
    }
    
    /**
     * Obtenir l'instance unique (Singleton)
     */
    public static synchronized SimulationManager getInstance() {
        if (instance == null) {
            instance = new SimulationManager();
        }
        return instance;
    }
    
    /**
     * Initialiser les espaces partagés
     */
    public void initialiser(EspaceQuai quai, EspaceVente vente) {
        this.espaceQuai = quai;
        this.espaceVente = vente;
        System.out.println("[SimulationManager] Initialisé");
    }
    
    /**
     * Créer et démarrer un nouveau train
     */
    public synchronized Train creerTrain() {
        int id = nextTrainId.getAndIncrement();
        Train train = new Train(id, espaceQuai);
        trains.put(id, train);
        
        Thread thread = new Thread(train);
        thread.setDaemon(true);
        thread.start();
        trainsThreads.put(id, thread);
        
        System.out.println("[SimulationManager] Train " + id + " créé et démarré");
        return train;
    }
    
    /**
     * Créer et démarrer un nouveau voyageur
     */
    public synchronized Voyageur creerVoyageur() {
        int id = nextVoyageurId.getAndIncrement();
        Voyageur voyageur = new Voyageur(id, espaceVente, espaceQuai);
        voyageurs.put(id, voyageur);
        
        Thread thread = new Thread(voyageur);
        thread.setDaemon(true);
        thread.start();
        voyageursThreads.put(id, thread);
        
        System.out.println("[SimulationManager] Voyageur " + id + " créé et démarré");
        return voyageur;
    }
    
    /**
     * Obtenir la liste de tous les trains avec leurs états
     */
    public List<Map<String, Object>> getTrainsAvecEtats() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Train train : trains.values()) {
            Map<String, Object> trainInfo = new HashMap<>();
            trainInfo.put("id", train.getId());
            trainInfo.put("etat", train.getEtat().name());
            trainInfo.put("etatDescription", train.getEtat().toString());
            trainInfo.put("vitesse", train.getVitesse());
            trainInfo.put("placesLibres", train.getPlacesLibresActuelles());
            trainInfo.put("numeroVoie", train.getNumeroVoie());
            
            result.add(trainInfo);
        }
        
        // Tri par ID
        result.sort(Comparator.comparingInt(t -> (Integer) t.get("id")));
        return result;
    }
    
    /**
     * Obtenir la liste de tous les voyageurs avec leurs états
     */
    public List<Map<String, Object>> getVoyageursAvecEtats() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Voyageur voyageur : voyageurs.values()) {
            Map<String, Object> voyageurInfo = new HashMap<>();
            voyageurInfo.put("id", voyageur.getId());
            voyageurInfo.put("etat", voyageur.getEtat().name());
            voyageurInfo.put("etatDescription", voyageur.getEtat().toString());
            
            result.add(voyageurInfo);
        }
        
        // Tri par ID
        result.sort(Comparator.comparingInt(v -> (Integer) v.get("id")));
        return result;
    }
    
    /**
     * Obtenir un train spécifique
     */
    public Train getTrain(int id) {
        return trains.get(id);
    }
    
    /**
     * Obtenir un voyageur spécifique
     */
    public Voyageur getVoyageur(int id) {
        return voyageurs.get(id);
    }
    
    /**
     * Nombre total de trains
     */
    public int getNombreTrains() {
        return trains.size();
    }
    
    /**
     * Nombre total de voyageurs
     */
    public int getNombreVoyageurs() {
        return voyageurs.size();
    }
}