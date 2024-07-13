package com.weather.temperature.domain.repository;

import com.weather.temperature.domain.entity.YearlyTemperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YearlyTemperatureRepository extends JpaRepository<YearlyTemperature, Long> {

    List<YearlyTemperature> findByCityOrderByYearAsc(String city);
}
