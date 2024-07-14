package com.weather.temperature.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class FileProcessingPartialResult {

    @Id
    @GeneratedValue
    private Long id;

    private String city;
    @Column(name = "year_value")
    private int year;
    private BigDecimal totalTemperature;
    private BigDecimal count;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getTotalTemperature() {
        return totalTemperature;
    }

    public void setTotalTemperature(BigDecimal totalTemperature) {
        this.totalTemperature = totalTemperature;
    }

    public BigDecimal getCount() {
        return count;
    }

    public void setCount(BigDecimal count) {
        this.count = count;
    }
}
