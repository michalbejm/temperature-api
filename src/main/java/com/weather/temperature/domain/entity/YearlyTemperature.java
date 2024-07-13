package com.weather.temperature.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class YearlyTemperature {

    @Id
    @GeneratedValue
    private Long id;

    private String city;

    @Column(name = "year_value")
    private int year;

    private BigDecimal averageTemperature;

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

    public BigDecimal getAverageTemperature() {
        return averageTemperature;
    }

    public void setAverageTemperature(BigDecimal averageTemperature) {
        this.averageTemperature = averageTemperature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        YearlyTemperature that = (YearlyTemperature) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
