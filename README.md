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
