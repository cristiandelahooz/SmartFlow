package com.trafficmanagement.smartflow.controller;

import static com.trafficmanagement.smartflow.utils.TrafficLightConstants.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TrafficLightController {
  private final Map<Integer, AtomicBoolean> lightStates = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  public TrafficLightController() {
    for (int ind = MIN_LIGHT_ID; ind <= TOTAL_TRAFFIC_LIGHTS; ind++) {
      lightStates.put(ind, new AtomicBoolean(INITIAL_LIGHT_STATE));
    }
    log.info(
        "traffic_light_controller_initialized lightCount={} initialState=red",
        TOTAL_TRAFFIC_LIGHTS);
    startCycle();
  }

  private void startCycle() {
      lightStates.get(LIGHT_3).set(true);
      lightStates.get(LIGHT_5).set(true);
    scheduler.scheduleAtFixedRate(
        () -> {
            toggleLight(LIGHT_1);
            toggleLight(LIGHT_6);
            toggleLight(LIGHT_2);
            toggleLight(LIGHT_4);
            toggleLight(LIGHT_5);
            toggleLight(LIGHT_3);
        },
        INITIAL_DELAY_SECONDS,
        CYCLE_INTERVAL_SECONDS,
        TimeUnit.SECONDS);
  }

  private void toggleLight(int lightId) {
    boolean newState = !isGreen(lightId);
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
      log.warn(
          "emergency_override_activated lightId={} forcedState={}",
          lightId,
          green ? "green" : "red");
    }
  }

  public void shutdown() {
    scheduler.shutdown();
    log.info("traffic_light_controller_shutdown schedulerShutdown=true");
  }
}
