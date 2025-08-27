package com.trafficmanagement.smartflow.data.model;


public interface TrafficManager {
  void addToQueue(Vehicle vehicle);

  void leaveIntersection(Vehicle vehicle);

  boolean isMyTurn(Vehicle vehicle);

  void startCrossing(Vehicle vehicle);

  int getPositionInQueue(Vehicle vehicle);

  boolean isEmergencyActive();

  int getId();
}
