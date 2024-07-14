package com.weather.temperature.service.impl;

import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.domain.repository.YearlyTemperatureRepository;
import com.weather.temperature.service.YearlyTemperatureService;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;
import com.weather.temperature.service.dto.YearlyTemperatureDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class YearlyTemperatureServiceImpl implements YearlyTemperatureService {

    private final YearlyTemperatureRepository yearlyTemperatureRepository;

    public YearlyTemperatureServiceImpl(YearlyTemperatureRepository yearlyTemperatureRepository) {
        this.yearlyTemperatureRepository = yearlyTemperatureRepository;
    }

    @Transactional
    @Override
    public List<YearlyTemperatureDto> findByCityOrderByYearAsc(String city) {
        return yearlyTemperatureRepository.findByCityOrderByYearAsc(city).stream().map(this::toDto).toList();
    }

    private YearlyTemperatureDto toDto(YearlyTemperature yearlyTemperature) {
        return new YearlyTemperatureDto(yearlyTemperature.getYear(),
                yearlyTemperature.getTotalTemperature().divide(yearlyTemperature.getCount(), RoundingMode.HALF_UP));
    }
}
