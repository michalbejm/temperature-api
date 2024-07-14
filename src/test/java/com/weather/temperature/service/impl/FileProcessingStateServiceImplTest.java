package com.weather.temperature.service.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import com.weather.temperature.domain.entity.FileProcessingState;
import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.domain.repository.FileProcessingStateRepository;
import com.weather.temperature.domain.repository.YearlyTemperatureRepository;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileProcessingStateServiceImplTest {

    @InjectMocks
    FileProcessingStateServiceImpl service;

    @Mock
    private FileProcessingStateRepository fileProcessingStateRepository;
    @Mock
    private YearlyTemperatureRepository yearlyTemperatureRepository;

    Storage storage = LocalStorageHelper.getOptions().getService();

    private static final String BUCKET_NAME = "bucket";
    private static final String FILE_NAME = "file";
    private static final Long GENERATION = 1L;

    @Captor
    ArgumentCaptor<YearlyTemperature> captor;

    @Test
    void getFileProcessingStateShouldReturnNewStateWhenNoExistingStateWasFound() {
        // given
        BlobInfo blobInfo = BlobInfo.newBuilder(
                BlobId.of(BUCKET_NAME, FILE_NAME, GENERATION)).build();
        storage.create(blobInfo);
        Blob blob = storage.get(BUCKET_NAME, FILE_NAME);

        when(fileProcessingStateRepository.findByFilename(FILE_NAME)).thenReturn(Optional.empty());
        when(fileProcessingStateRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

        // when
        FileProcessingState result = service.getFileProcessingState(blob);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo(FILE_NAME);
        assertThat(result.getPosition()).isEqualTo(0L);
    }

    @Test
    void getFileProcessingStateShouldReturnExistingStateWhenFound() {
        // given
        BlobInfo blobInfo = BlobInfo.newBuilder(
                BlobId.of(BUCKET_NAME, FILE_NAME, GENERATION)).build();
        storage.create(blobInfo);
        Blob blob = storage.get(BUCKET_NAME, FILE_NAME);

        FileProcessingState existingState = new FileProcessingState();
        existingState.setFilename(FILE_NAME);
        when(fileProcessingStateRepository.findByFilename(FILE_NAME))
                .thenReturn(Optional.of(existingState));

        // when
        FileProcessingState result = service.getFileProcessingState(blob);

        // then
        assertThat(result).isEqualTo(existingState);
    }

    @Test
    void updateProcessingStateShouldCreateNewEntityIfDoesNotExist() {
        // given
        FileProcessingState fileProcessingState = new FileProcessingState();
        fileProcessingState.setFilename(FILE_NAME);

        Map<CityWithYear, YearlyTemperatureData> temperatureData = Map.of(
                new CityWithYear("Warsaw", 2024),
                new YearlyTemperatureData(BigDecimal.valueOf(50L), BigDecimal.valueOf(2L)));

        when(yearlyTemperatureRepository.findByCityAndYear("Warsaw", 2024)).thenReturn(Optional.empty());

        // when
        service.updateProcessingState(fileProcessingState, temperatureData);

        // then
        verify(fileProcessingStateRepository).save(fileProcessingState);
        verify(yearlyTemperatureRepository).save(captor.capture());
        assertThat(captor.getValue().getCity()).isEqualTo("Warsaw");
        assertThat(captor.getValue().getYear()).isEqualTo(2024);
        assertThat(captor.getValue().getTotalTemperature()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(captor.getValue().getCount()).isEqualByComparingTo(BigDecimal.valueOf(2));
    }

    @Test
    void updateProcessingStateShouldUpdateExistingEntity() {
        // given
        FileProcessingState fileProcessingState = new FileProcessingState();
        fileProcessingState.setFilename(FILE_NAME);

        Map<CityWithYear, YearlyTemperatureData> temperatureData = Map.of(
                new CityWithYear("Warsaw", 2024),
                new YearlyTemperatureData(BigDecimal.valueOf(50L), BigDecimal.valueOf(2L)));

        YearlyTemperature yearlyTemperature = new YearlyTemperature();
        yearlyTemperature.setCity("Warsaw");
        yearlyTemperature.setYear(2024);
        yearlyTemperature.setCount(BigDecimal.valueOf(3));
        yearlyTemperature.setTotalTemperature(BigDecimal.valueOf(100));

        when(yearlyTemperatureRepository.findByCityAndYear("Warsaw", 2024)).thenReturn(Optional.of(yearlyTemperature));

        // when
        service.updateProcessingState(fileProcessingState, temperatureData);

        // then
        verify(fileProcessingStateRepository).save(fileProcessingState);
        verify(yearlyTemperatureRepository).save(captor.capture());
        assertThat(captor.getValue().getCity()).isEqualTo("Warsaw");
        assertThat(captor.getValue().getYear()).isEqualTo(2024);
        assertThat(captor.getValue().getTotalTemperature()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(captor.getValue().getCount()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }
}