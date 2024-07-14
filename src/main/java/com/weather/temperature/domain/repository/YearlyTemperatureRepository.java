package com.weather.temperature.domain.repository;

import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.service.dto.CityWithYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface YearlyTemperatureRepository extends JpaRepository<YearlyTemperature, Long> {

    List<YearlyTemperature> findByCityOrderByYearAsc(String city);
    Optional<YearlyTemperature> findByCityAndYear(String city, int year);
}
