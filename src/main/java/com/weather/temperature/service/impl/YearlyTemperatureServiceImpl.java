package com.weather.temperature.service.impl;

import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.domain.repository.YearlyTemperatureRepository;
import com.weather.temperature.service.YearlyTemperatureService;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    public List<YearlyTemperature> findByCityOrderByYearAsc(String city) {
        return yearlyTemperatureRepository.findByCityOrderByYearAsc(city);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void createYearlyTemperatures(Map<CityWithYear, YearlyTemperatureData> yearlyTemperatureData) {
        yearlyTemperatureRepository.deleteAll();
        yearlyTemperatureRepository.saveAll(yearlyTemperatureData.entrySet().stream()
                .map(e -> createYearlyTemperature(e)).toList());
    }

    private YearlyTemperature createYearlyTemperature(Map.Entry<CityWithYear, YearlyTemperatureData> entry) {
        YearlyTemperature result = new YearlyTemperature();
        result.setCity(entry.getKey().city());
        result.setYear(entry.getKey().year());
        result.setAverageTemperature(entry.getValue().getTotalTemperature().divide(entry.getValue().getCount()));
        return result;
    }
}
