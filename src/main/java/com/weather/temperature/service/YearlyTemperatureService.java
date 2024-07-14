package com.weather.temperature.service;

import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;
import com.weather.temperature.service.dto.YearlyTemperatureDto;

import java.util.List;
import java.util.Map;

public interface YearlyTemperatureService {

    List<YearlyTemperatureDto> findByCityOrderByYearAsc(String city);
}
