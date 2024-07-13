package com.weather.temperature.service.dto;

import java.math.BigDecimal;

public class YearlyTemperatureData {

    private BigDecimal totalTemperature;
    private BigDecimal count;

    public YearlyTemperatureData(BigDecimal temperature) {
        this.totalTemperature = temperature;
        this.count = BigDecimal.ONE;
    }

    public void addTemperature(BigDecimal temperature) {
        totalTemperature = totalTemperature.add(temperature);
        count = count.add(BigDecimal.ONE);
    }

    public BigDecimal getTotalTemperature() {
        return totalTemperature;
    }

    public BigDecimal getCount() {
        return count;
    }
}
