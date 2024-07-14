package com.weather.temperature.service.dto;

import java.math.BigDecimal;

public record YearlyTemperatureDto(int year, BigDecimal averageTemperature) { }