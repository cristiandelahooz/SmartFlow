package com.trafficmanagement.smartflow.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import static com.trafficmanagement.smartflow.utils.TrafficLightConstants.*;

@Slf4j
public class TrafficLightController {
    // Mapa para el estado visual de los semáforos (true = verde, false = rojo)
    private final Map<Integer, AtomicBoolean> lightStates = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TrafficLightController() {
        for (int ind = MIN_LIGHT_ID; ind <= TOTAL_TRAFFIC_LIGHTS; ind++) {
            lightStates.put(ind, new AtomicBoolean(INITIAL_LIGHT_STATE));
        }
        log.info("traffic_light_controller_initialized lightCount={} initialState=red", TOTAL_TRAFFIC_LIGHTS);
        startCycle();
    }

    private void startCycle() {
        // Ciclo de cambio de luces cada 12 segundos.
        scheduler.scheduleAtFixedRate(() -> {
            // Semáforos 1 (izq) y 6 (der) son independientes y opuestos
            toggleLight(1);
            toggleLight(6);

            // Semáforos 2 y 3 (intersección 2) son opuestos
            boolean is2Green = lightStates.get(2).get();
            lightStates.get(2).set(!is2Green);
            lightStates.get(3).set(is2Green);

            // Semáforos 4 y 5 (intersección 3) son opuestos
            boolean is4Green = lightStates.get(4).get();
            lightStates.get(4).set(!is4Green);
            lightStates.get(5).set(is4Green);

        }, INITIAL_DELAY_SECONDS, CYCLE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void toggleLight(int lightId) {
        boolean newState = !lightStates.get(lightId).get();
        lightStates.get(lightId).set(newState);
        log.debug("traffic_light_toggled lightId={} state={}", lightId, newState ? "green" : "red");
    }

    public boolean isGreen(int lightId) {
        if (lightId < MIN_LIGHT_ID || lightId > MAX_LIGHT_ID) return false;
        return lightStates.get(lightId).get();
    }
    
    public void setEmergencyGreen(int lightId, boolean green) {
        if (lightId >= MIN_LIGHT_ID && lightId <= MAX_LIGHT_ID) {
             lightStates.get(lightId).set(green);
             log.warn("emergency_override_activated lightId={} forcedState={}", lightId, green ? "green" : "red");
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        log.info("traffic_light_controller_shutdown schedulerShutdown=true");
    }
}