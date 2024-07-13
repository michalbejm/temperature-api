package com.weather.temperature;

import com.weather.temperature.domain.entity.YearlyTemperature;

import java.math.BigDecimal;

public class YearlyTemperatureTest {
    protected YearlyTemperature createYearlyTemperature(String city, int year, BigDecimal averageTemperature) {
        YearlyTemperature entity = new YearlyTemperature();
        entity.setCity(city);
        entity.setYear(year);
        entity.setAverageTemperature(averageTemperature);
        return entity;
    }
}
