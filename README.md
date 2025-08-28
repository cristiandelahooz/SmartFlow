# SmartFlow - Traffic Management System

This project is a distributed and concurrent system for simulating and managing urban traffic, with a focus on preventing collisions and prioritizing emergency vehicles.

## Scenario 1: 4-Way Intersection

The current implementation focuses on a 4-way intersection with the following characteristics:

- **First-Come, First-Served:** Vehicles proceed in the order they arrive.
- **Emergency Vehicle Priority:** Emergency vehicles are prioritized and cross the intersection as soon as the path is clear.
- **Allowed Movements:** Vehicles can turn right, turn left, go straight, or make a U-turn, depending on intersection-specific rules.

### Concurrency Model
The system implements thread-safe traffic simulation using:
- `Vehicle` implements `Runnable` - each vehicle runs as a separate thread
- `PriorityBlockingQueue` - manages vehicle queuing with emergency vehicle priority
- Emergency vehicles have higher priority in the `compareTo()` implementation

### UI Architecture
- JavaFX application with FXML-based views
- `ViewsHandler` utility manages scene transitions between different simulation views
- Main views: MainLayout, IntersectionView, MotorwayView
- CSS styling in `/src/main/resources/style/`

## Menu
<img width="1538" height="962" alt="image" src="https://github.com/user-attachments/assets/a11cde08-2c71-4e55-a0c2-1c0bd02307b0" />

## Intersection
<img width="1540" height="999" alt="image" src="https://github.com/user-attachments/assets/90e39a21-b804-4843-8800-ec76d21c638f" />

## Motorway
<img width="1540" height="961" alt="image" src="https://github.com/user-attachments/assets/ad9c76a3-75ad-4beb-9971-26ff9f7eb7c6" />

## Example
https://github.com/user-attachments/assets/1fe59e19-6764-4d30-9fd2-46b7fccbbd40



## Project Structure

```bash
.
├── compose.yml
├── logs
│   └── smartflow.log
├── lombok.config
├── Makefile
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── src
│   └── main
│       ├── java
│       │   ├── com
│       │   │   └── trafficmanagement
│       │   │       └── smartflow
│       │   │           ├── controller
│       │   │           │   ├── IntersectionViewController.java
│       │   │           │   ├── MainViewController.java
│       │   │           │   ├── MotorwayViewController.java
│       │   │           │   └── TrafficLightController.java
│       │   │           ├── data
│       │   │           │   ├── enums
│       │   │           │   │   ├── Locations.java
│       │   │           │   │   ├── VehicleMovement.java
│       │   │           │   │   └── VehicleType.java
│       │   │           │   └── model
│       │   │           │       ├── Intersection.java
│       │   │           │       ├── IntersectionStateManager.java
│       │   │           │       ├── MotorwayIntersection.java
│       │   │           │       ├── TrafficManager.java
│       │   │           │       └── Vehicle.java
│       │   │           ├── Main.java
│       │   │           ├── service
│       │   │           │   ├── TrafficNavigationService.java
│       │   │           │   ├── VehicleMovementService.java
│       │   │           │   └── VehiclePathCalculatorService.java
│       │   │           ├── ui
│       │   │           │   └── ComboBoxWrapper.java
│       │   │           └── utils
│       │   │               ├── IntersectionConstants.java
│       │   │               ├── MotorwayConstants.java
│       │   │               ├── TrafficLightConstants.java
│       │   │               ├── VehicleConstants.java
│       │   │               └── ViewsHandler.java
│       │   └── module-info.java
│       └── resources
│           ├── fxml
│           │   ├── IntersectionView.fxml
│           │   ├── MainView.fxml
│           │   └── MotorwayView.fxml
│           ├── logback.xml
│           └── style
│               ├── base
│               │   ├── common.css
│               │   ├── components.css
│               │   └── layouts.css
│               ├── themes
│               │   └── traffic-red.css
│               └── views
│                   ├── intersection-view.css
│                   ├── main-view.css
│                   └── motorway-view.css
└── target
    ├── classes
    │   ├── com
    │   │   └── trafficmanagement
    │   │       └── smartflow
    │   │           ├── controller
    │   │           │   ├── IntersectionViewController$1.class
    │   │           │   ├── IntersectionViewController$2.class
    │   │           │   ├── IntersectionViewController.class
    │   │           │   ├── MainViewController.class
    │   │           │   ├── MotorwayViewController$1.class
    │   │           │   ├── MotorwayViewController$2.class
    │   │           │   ├── MotorwayViewController.class
    │   │           │   └── TrafficLightController.class
    │   │           ├── data
    │   │           │   ├── enums
    │   │           │   │   ├── Locations.class
    │   │           │   │   ├── VehicleMovement.class
    │   │           │   │   └── VehicleType.class
    │   │           │   └── model
    │   │           │       ├── Intersection.class
    │   │           │       ├── IntersectionStateManager.class
    │   │           │       ├── MotorwayIntersection.class
    │   │           │       ├── TrafficManager.class
    │   │           │       ├── Vehicle$1.class
    │   │           │       └── Vehicle.class
    │   │           ├── Main.class
    │   │           ├── service
    │   │           │   ├── TrafficNavigationService$1.class
    │   │           │   ├── TrafficNavigationService.class
    │   │           │   ├── VehicleMovementService.class
    │   │           │   ├── VehiclePathCalculatorService$1.class
    │   │           │   └── VehiclePathCalculatorService.class
    │   │           ├── ui
    │   │           │   ├── ComboBoxWrapper$1.class
    │   │           │   └── ComboBoxWrapper.class
    │   │           └── utils
    │   │               ├── IntersectionConstants.class
    │   │               ├── MotorwayConstants.class
    │   │               ├── TrafficLightConstants.class
    │   │               ├── VehicleConstants.class
    │   │               └── ViewsHandler.class
    │   ├── fxml
    │   │   ├── IntersectionView.fxml
    │   │   ├── MainView.fxml
    │   │   └── MotorwayView.fxml
    │   ├── logback.xml
    │   ├── module-info.class
    │   └── style
    │       ├── base
    │       │   ├── common.css
    │       │   ├── components.css
    │       │   └── layouts.css
    │       ├── themes
    │       │   └── traffic-red.css
    │       └── views
    │           ├── intersection-view.css
    │           ├── main-view.css
    │           └── motorway-view.css
    ├── generated-sources
    │   └── annotations
    └── maven-status
        └── maven-compiler-plugin
            └── compile
                └── default-compile
                    ├── createdFiles.lst
                    └── inputFiles.lst

```
