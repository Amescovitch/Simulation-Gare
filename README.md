# TP5 – Simulation d’une gare (Java + REST)

## 1. Présentation

Ce projet implémente une **simulation concurrente** d’une gare ferroviaire en Java, puis expose cette simulation via une **API REST**.

- **Partie 1** : simulation multithreadée (trains, voyageurs, moniteurs).
- **Partie 2** : ajout d’une API REST pour observer et piloter la simulation (création de trains/voyageurs à chaud).

Points clés :
- utilisation rigoureuse des moniteurs (`synchronized`, `wait`, `notifyAll`),
- aucune exposition directe de l’état des moniteurs (pas de getters/setters),
- threads `daemon` pour une fin de programme propre,
- architecture REST claire et découplée de la logique concurrente.

***

## 2. Structure du projet

### 2.1 Rôles des principales classes

| Composant         | Type                      | Rôle principal                                                        |
|-------------------|---------------------------|------------------------------------------------------------------------|
| `Gare`            | Classe principale         | Lance la simulation, initialise les moniteurs et l’API REST           |
| `Train`           | Thread (Runnable)         | Cycle de vie d’un train (trajet, demande de voie, arrêt, départ)      |
| `Voyageur`        | Thread (Runnable)         | Cycle de vie d’un voyageur (trajet, achat, montée dans un train)      |
| `EspaceQuai`      | Moniteur (objet partagé)  | Gestion des voies et des trains à quai                                |
| `EspaceVente`     | Moniteur (objet partagé)  | Gestion du stock de billets et des achats                             |
| `EtatTrain`       | Enum                      | États d’un train (`EN_ROUTE`, `EN_ATTENTE_VOIE`, `EN_GARE`, `PARTI`)  |
| `EtatVoyageur`    | Enum                      | États d’un voyageur (`EN_ROUTE`, `MUNI_TICKET`, `MONTE_TRAIN`)        |
| `SimulationManager` | Singleton               | Coordonne la création/gestion des trains/voyageurs (interface pour l’API) |
| `ApiRestServer`   | Serveur REST Restlet      | Démarre le serveur HTTP et attache les ressources REST                |
| `TrainsResource`  | Ressource REST            | Endpoint `/trains` (GET, POST)                                        |
| `VoyageursResource` | Ressource REST          | Endpoint `/voyageurs` (GET, POST)                                     |

***

## 3. Pré-requis

- **Java** : JDK 11
- **Maven** : 3.x
- OS : Windows, Linux ou macOS

Vérifier :

```bash
java -version
mvn -version
```

***

## 4. Compilation

Depuis le dossier contenant le `pom.xml` :

```bash
mvn clean compile
```

Vous devez obtenir : `BUILD SUCCESS`.

***

## 5. Lancer la simulation (Partie 2)

Classe main : `partie2.simulation.Gare`.

Sous PowerShell (dans le dossier du `pom.xml`) :

```powershell
mvn exec:java "-Dexec.mainClass=partie2.simulation.Gare"
```

Comportement attendu :

- initialisation des moniteurs (`EspaceQuai`, `EspaceVente`),
- création de plusieurs trains et voyageurs,
- démarrage du serveur REST :

```text
=== SIMULATION GARE AVEC API REST ===
...
[ApiRestServer] Démarré sur port 8182

=== API REST DISPONIBLE ===
URL: http://localhost:8182
GET  /trains      - Lister les trains
POST /trains      - Ajouter un train
GET  /voyageurs   - Lister les voyageurs
POST /voyageurs   - Ajouter un voyageur

Simulation en cours... (Ctrl+C pour arrêter)
```

Pour arrêter : `Ctrl+C`.

***

## 6. API REST – Utilisation

L’API écoute sur : `http://localhost:8182`.

Sous Windows PowerShell, `curl` est un alias de `Invoke-WebRequest`, on utilisera donc `iwr`.

### 6.1 Lister les trains

```powershell
iwr "http://localhost:8182/trains"
```

Réponse (exemple) :

```json
{
  "count": 5,
  "trains": [
    {
      "id": 1,
      "etat": "PARTI",
      "etatDescription": "Parti",
      "vitesse": 258,
      "placesLibres": 2,
      "numeroVoie": 1
    }
  ]
}
```

### 6.2 Lister les voyageurs

```powershell
iwr "http://localhost:8182/voyageurs"
```

Réponse (exemple) :

```json
{
  "count": 10,
  "voyageurs": [
    {
      "id": 1,
      "etat": "MONTE_TRAIN",
      "etatDescription": "Monté dans un train"
    }
  ]
}
```

### 6.3 Créer un nouveau train

```powershell
iwr "http://localhost:8182/trains" -Method Post
```

Réponse (exemple) :

```json
{
  "success": true,
  "message": "Train créé et démarré",
  "trainId": 6,
  "etat": "EN_ROUTE",
  "vitesse": 259
}
```

### 6.4 Créer un nouveau voyageur

```powershell
iwr "http://localhost:8182/voyageurs" -Method Post
```

Réponse (exemple) :

```json
{
  "success": true,
  "message": "Voyageur créé et démarré",
  "voyageurId": 11,
  "etat": "EN_ROUTE"
}
```

En relançant `GET /trains` et `GET /voyageurs`, vous voyez le `count` et les listes se mettre à jour.

***

## 7. Design concurrent

### 7.1 Moniteurs

- `EspaceQuai` et `EspaceVente` sont des **moniteurs Java** :
  - état privé (voies, trains à quai, nombre de billets),
  - méthodes critiques `synchronized`,
  - utilisation de `wait()` / `notifyAll()` pour les attentes conditionnelles.

Exemples :

- `EspaceVente.acheterBillet(int)` :
  - `synchronized`,
  - test du stock, puis décrément atomique de `billetsDisponibles`,
  - **aucun getter/setter** pour manipuler le stock.

- `EspaceQuai.reserverVoie(Train)` :
  - `synchronized`,
  - boucle `while (toutesVoiesOccupees()) wait();`,
  - attribution de la première voie libre, stockage du train dans `trainsEnGare`,
  - `notifyAll()` dans `libererVoie(int)`.

### 7.2 Pas de getters/setters sur l’état des moniteurs

- Les variables d’état (`billetsDisponibles`, `voiesOccupees`, `trainsEnGare`) ne sont jamais exposées via des getters/setters publics.
- Toutes les modifications se font **à l’intérieur** des méthodes `synchronized` du moniteur.
- Objectif : éviter les scénarios “test puis action” répartis sur plusieurs appels, qui ouvriraient des fenêtres de race conditions.

### 7.3 Threads daemon

- Tous les threads `Train` et `Voyageur` sont créés ainsi :

  ```java
  Thread t = new Thread(new Train(...));
  t.setDaemon(true);
  t.start();
  ```

- Cela garantit que la JVM peut s’arrêter dès que le thread principal termine (pas de threads “zombies”).

***

## 8. Intégration de l’API REST

### 8.1 SimulationManager (pont entre simulation et API)

- `SimulationManager` est un **singleton** (`getInstance()`) qui :
  - reçoit les références des moniteurs (`EspaceQuai`, `EspaceVente`),
  - crée les objets `Train` / `Voyageur` et leurs `Thread` (daemon),
  - stocke les instances dans des `ConcurrentHashMap`,
  - fournit :
    - `creerTrain()`, `creerVoyageur()`,
    - `getTrainsAvecEtats()`, `getVoyageursAvecEtats()`.

- C’est la **seule** porte d’entrée pour l’API vers la simulation, ce qui centralise la synchronisation et évite les accès sauvages aux objets métier.

### 8.2 Ressources REST

- `TrainsResource` :
  - `GET /trains` : construit un JSON à partir de `SimulationManager.getTrainsAvecEtats()`.
  - `POST /trains` : appelle `SimulationManager.creerTrain()` puis retourne les infos du nouveau train.

- `VoyageursResource` :
  - `GET /voyageurs` : idem pour les voyageurs.
  - `POST /voyageurs` : crée un voyageur à chaud.

- `ApiRestServer` :
  - crée un `Component` Restlet avec un serveur HTTP (port 8182),
  - attache `/trains` et `/voyageurs` aux ressources correspondantes.

***

## 9. Section “pour le correcteur”

- **Synchronisation** :
  - utilisation systématique de moniteurs Java (`synchronized`) pour les objets partagés,
  - `wait()` / `notifyAll()` + `while (condition)` pour les attentes conditionnelles,
  - aucune attente active (pas de busy waiting).

- **Encapsulation** :
  - état des moniteurs **non exposé** (pas de getters/setters) : seules les opérations métier sont publiques.

- **Threads** :
  - `Train` et `Voyageur` sont exécutés dans des `Thread` marqués `setDaemon(true)`,
  - gestion propre des interruptions (`InterruptedException`).

- **API REST** :
  - architecture claire : simulation (moniteurs + threads) totalement séparée de la couche REST,
  - `SimulationManager` assure la cohérence et le thread-safety entre API et simulation,
  - endpoints simples et RESTful (`GET`/`POST`, JSON).

***

Souhaites-tu qu’on ajoute un mini schéma texte du cycle de vie d’un train ou d’un voyageur dans ce README pour t’aider à l’expliquer à l’oral ?
