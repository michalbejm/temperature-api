package com.weather.temperature.service;

import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;

import java.util.List;
import java.util.Map;

public interface YearlyTemperatureService {

    List<YearlyTemperature> findByCityOrderByYearAsc(String city);

    void createYearlyTemperatures(Map<CityWithYear, YearlyTemperatureData> yearlyTemperatureData);
}
