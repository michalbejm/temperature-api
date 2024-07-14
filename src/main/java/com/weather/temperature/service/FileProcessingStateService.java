package com.weather.temperature.service;

import com.google.cloud.storage.Blob;
import com.weather.temperature.domain.entity.FileProcessingState;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;

import java.util.Map;

public interface FileProcessingStateService {

    FileProcessingState getFileProcessingState(Blob blob);

    void updateProcessingState(FileProcessingState processingState,
                               Map<CityWithYear, YearlyTemperatureData> temperatureData);
}
