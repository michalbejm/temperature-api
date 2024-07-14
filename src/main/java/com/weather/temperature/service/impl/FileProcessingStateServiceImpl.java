package com.weather.temperature.service.impl;

import com.google.cloud.storage.Blob;
import com.weather.temperature.domain.entity.FileProcessingPartialResult;
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

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

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
        Optional<FileProcessingState> previousState = fileProcessingStateRepository
                .findTopByFilenameOrderByGenerationDesc(blob.getName());

        FileProcessingState currentState;
        if (previousState.isPresent() &&
                previousState.get().getGeneration().equals(blob.getGeneration())) {
            if (previousState.get().isCompleted()) {
                return null;
            }
            else {
                currentState = previousState.get();
            }
        }
        else {
            currentState = new FileProcessingState();
            currentState.setFilename(blob.getName());
            currentState.setGeneration(blob.getGeneration());
            fileProcessingStateRepository.save(currentState);
        }
        return currentState;
    }

    @Override
    public void updateProcessingState(FileProcessingState processingState, Map<CityWithYear, YearlyTemperatureData> temperatureData) {
        processingState.setPartialResults(temperatureData.entrySet().stream()
                .map(entry -> createFileProcessingPartialResult(entry)).toList());
        fileProcessingStateRepository.save(processingState);
    }

    private FileProcessingPartialResult createFileProcessingPartialResult(Map.Entry<CityWithYear, YearlyTemperatureData> entry) {
        FileProcessingPartialResult result = new FileProcessingPartialResult();
        result.setCity(entry.getKey().city());
        result.setYear(entry.getKey().year());
        result.setTotalTemperature(entry.getValue().getTotalTemperature());
        result.setCount(entry.getValue().getCount());
        return result;
    }

    @Override
    public void completeProcessing(FileProcessingState processingState, Map<CityWithYear, YearlyTemperatureData> temperatureData) {
        processingState.setCompleted(true);
        processingState.setPartialResults(new ArrayList<>());
        processingState.setPosition(null);
        fileProcessingStateRepository.save(processingState);
        yearlyTemperatureRepository.deleteAll();
        yearlyTemperatureRepository.saveAll(temperatureData.entrySet().stream()
                .map(e -> createYearlyTemperature(e)).toList());
    }

    private YearlyTemperature createYearlyTemperature(Map.Entry<CityWithYear, YearlyTemperatureData> entry) {
        YearlyTemperature result = new YearlyTemperature();
        result.setCity(entry.getKey().city());
        result.setYear(entry.getKey().year());
        result.setAverageTemperature(
                entry.getValue().getTotalTemperature().divide(entry.getValue().getCount(), RoundingMode.HALF_UP));
        return result;
    }
}
