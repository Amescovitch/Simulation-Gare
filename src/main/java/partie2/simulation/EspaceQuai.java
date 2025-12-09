package partie2.simulation;


/**
 * Moniteur gérant les voies
 * Identique à Partie 1
 */
public class EspaceQuai {
    
    private final boolean[] voiesOccupees;
    private final Train[] trainsEnGare;
    private final int nbVoies;
    
    public EspaceQuai(int nbVoies) {
        this.nbVoies = nbVoies;
        this.voiesOccupees = new boolean[nbVoies];
        this.trainsEnGare = new Train[nbVoies];
        
        for (int i = 0; i < nbVoies; i++) {
            voiesOccupees[i] = false;
            trainsEnGare[i] = null;
        }
        
        System.out.println("[EspaceQuai] Initialisé - " + nbVoies + " voies");
    }
    
    public synchronized int reserverVoie(Train train) {
        while (toutesVoiesOccupees()) {
            try {
                System.out.println("[EspaceQuai] Train " + train.getId() + " - Attente voie...");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }
        
        int voie = trouverVoieLibre();
        voiesOccupees[voie] = true;
        trainsEnGare[voie] = train;
        
        System.out.println("[EspaceQuai] Train " + train.getId() + " -> Voie " + (voie + 1));
        return voie;
    }
    
    public synchronized void libererVoie(int numeroVoie) {
        if (numeroVoie >= 0 && numeroVoie < nbVoies) {
            Train train = trainsEnGare[numeroVoie];
            voiesOccupees[numeroVoie] = false;
            trainsEnGare[numeroVoie] = null;
            
            System.out.println("[EspaceQuai] Voie " + (numeroVoie + 1) + " libérée");
            notifyAll();
        }
    }
    
    public synchronized boolean monterDansTrain(int voyageurId) {
        for (int i = 0; i < nbVoies; i++) {
            if (trainsEnGare[i] != null && trainsEnGare[i].embarquerVoyageur(voyageurId)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean toutesVoiesOccupees() {
        for (boolean occupee : voiesOccupees) {
            if (!occupee) return false;
        }
        return true;
    }
    
    private int trouverVoieLibre() {
        for (int i = 0; i < nbVoies; i++) {
            if (!voiesOccupees[i]) return i;
        }
        return -1;
    }
}
