# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SmartFlow is a JavaFX-based traffic management simulation system that models urban traffic intersections with a focus on collision avoidance and emergency vehicle prioritization. The system uses concurrent programming with Java threads and synchronization primitives to simulate realistic traffic behavior.

## Development Commands

### Build and Run
- `make build` or `./mvnw clean install` - Clean and build the project
- `make run` or `./mvnw javafx:run` - Run the JavaFX application
- `make clean` or `./mvnw clean` - Clean build artifacts

### Testing
- `./mvnw test` - Run JUnit 5 tests

## Architecture Overview

### Core Package Structure
- `com.trafficmanagement.smartflow.data/` - Core domain models (Vehicle, Intersection, TrafficLight)
- `com.trafficmanagement.smartflow.controller/` - JavaFX controllers for UI interaction
- `com.trafficmanagement.smartflow.utils/` - Utility classes including ViewsHandler for FXML management
- `com.trafficmanagement.smartflow.data.enums/` - Enumerations (VehicleType)

### Concurrency Model
The system implements thread-safe traffic simulation using:
- `Vehicle` implements `Runnable` - each vehicle runs as a separate thread
- `CyclicBarrier` in `Intersection` - synchronizes vehicle crossing to prevent collisions
- `PriorityBlockingQueue` - manages vehicle queuing with emergency vehicle priority
- Emergency vehicles have higher priority in the `compareTo()` implementation

### UI Architecture
- JavaFX application with FXML-based views
- `ViewsHandler` utility manages scene transitions between different simulation views
- Main views: MainLayout, IntersectionView, MotorwayView
- CSS styling in `/src/main/resources/style/`

### Key Dependencies
- JavaFX 21 for UI
- Lombok for boilerplate reduction
- SLF4J for logging
- JUnit 5 for testing
- ControlsFX, FormsFX, ValidatorFX for enhanced UI components

## Development Guidelines

### Code Style
- Uses Lombok annotations (`@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Slf4j`)
- Follows standard Java package naming conventions
- FXML files located in `/src/main/resources/fxml/`
- CSS stylesheets in `/src/main/resources/style/`

### Module System
Project uses Java modules (module-info.java) with explicit dependencies on JavaFX and third-party libraries.

### Logging
Uses SLF4J with simple logger implementation. Log statements should use appropriate log levels and structured messages.