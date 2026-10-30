package com.fundatechperu.shareway.drivermanagement.application;

public record VehicleCommand(
    String plate, String brand, String model, int year, String color, int seats) {}
