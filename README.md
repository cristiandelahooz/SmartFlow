# SmartFlow - Traffic Management System

This project is a distributed and concurrent system for simulating and managing urban traffic, with a focus on preventing collisions and prioritizing emergency vehicles.

## Scenario 1: 4-Way Intersection

The current implementation focuses on a 4-way intersection with the following characteristics:

- **Stop Signs:** All approaches have a stop sign.
- **First-Come, First-Served:** Vehicles proceed in the order they arrive.
- **Emergency Vehicle Priority:** Emergency vehicles are prioritized and cross the intersection as soon as the path is clear.
- **Collision Avoidance:** A `java.util.concurrent.CyclicBarrier` is used to synchronize vehicles at the intersection, ensuring that no more than one vehicle enters the intersection at a time.
- **Allowed Movements:** Vehicles can turn right, turn left, go straight, or make a U-turn, depending on intersection-specific rules.

## Concurrency Model

- **Vehicle Threads:** Each vehicle is simulated as a separate thread (`Runnable`), allowing for concurrent movement and interaction.
- **Intersection Barrier:** Each intersection has a `CyclicBarrier` that all vehicles must wait on before proceeding. This ensures that vehicles cross one at a time in a synchronized manner.
- **Traffic Controller:** The `TrafficController` manages the simulation, processing vehicles from a `PriorityBlockingQueue` and submitting them to a thread pool.

## Project Structure

```bash
smartflow/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/smartflow/
│   │   │       ├── model/              # Vehicle, Intersection, TrafficLight
│   │   │       ├── control/            # TrafficController, TrafficManager
│   │   │       ├── network/            # DistributedNode, gRPC/Socket logic
│   │   │       ├── ui/                 # JavaFX: MainUI and visualization
│   │   │       └── Main.java           # Application entry point
│   │   └── resources/                  # FXML, CSS, config files
├── build.gradle / pom.xml              # Gradle or Maven config
└── README.md
```