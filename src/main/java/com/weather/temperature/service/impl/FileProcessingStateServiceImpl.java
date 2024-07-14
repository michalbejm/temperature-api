package com.weather.temperature.service.impl;

import com.google.cloud.storage.Blob;
import com.weather.temperature.domain.entity.FileProcessingState;
import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.domain.repository.FileProcessingStateRepository;
import com.weather.temperature.domain.repository.YearlyTemperatureRepository;
import com.weather.temperature.service.FileProcessingStateService;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Service
public class FileProcessingStateServiceImpl implements FileProcessingStateService {

    private final FileProcessingStateRepository fileProcessingStateRepository;
    private final YearlyTemperatureRepository yearlyTemperatureRepository;

    public FileProcessingStateServiceImpl(FileProcessingStateRepository fileProcessingStateRepository,
                                          YearlyTemperatureRepository yearlyTemperatureRepository) {
        this.fileProcessingStateRepository = fileProcessingStateRepository;
        this.yearlyTemperatureRepository = yearlyTemperatureRepository;
    }

    @Override
    public FileProcessingState getFileProcessingState(Blob blob) {
        Optional<FileProcessingState> previousState = fileProcessingStateRepository.findByFilename(blob.getName());

        if (previousState.isPresent()) {
            return previousState.get();
        }
        else {
            FileProcessingState newState = new FileProcessingState();
            newState.setFilename(blob.getName());
            return fileProcessingStateRepository.save(newState);
        }
    }

    @Override
    public void updateProcessingState(FileProcessingState processingState, Map<CityWithYear, YearlyTemperatureData> temperatureData) {
        fileProcessingStateRepository.save(processingState);

        for (Map.Entry<CityWithYear, YearlyTemperatureData> entry : temperatureData.entrySet()) {
            Optional<YearlyTemperature> existingYearlyTemperature = yearlyTemperatureRepository.findByCityAndYear(
                    entry.getKey().city(), entry.getKey().year());
            YearlyTemperature yearlyTemperature = existingYearlyTemperature.orElse(
                    createYearlyTemperature(entry.getKey()));
            yearlyTemperature.setTotalTemperature(
                    yearlyTemperature.getTotalTemperature().add(entry.getValue().getTotalTemperature()));
            yearlyTemperature.setCount(
                    yearlyTemperature.getCount().add(entry.getValue().getCount()));
            yearlyTemperatureRepository.save(yearlyTemperature);
        }
    }

    YearlyTemperature createYearlyTemperature(CityWithYear cityWithYear) {
        YearlyTemperature result = new YearlyTemperature();
        result.setCity(cityWithYear.city());
        result.setYear(cityWithYear.year());
        result.setCount(BigDecimal.ZERO);
        result.setTotalTemperature(BigDecimal.ZERO);
        return result;
    }
}
