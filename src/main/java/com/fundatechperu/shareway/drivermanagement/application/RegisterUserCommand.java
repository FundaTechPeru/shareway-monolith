package com.fundatechperu.shareway.drivermanagement.application;

public record RegisterUserCommand(String email, String password, String fullName, String phone) {}
