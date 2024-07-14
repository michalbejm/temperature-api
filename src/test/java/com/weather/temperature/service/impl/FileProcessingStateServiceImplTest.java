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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
    ArgumentCaptor<FileProcessingState> stateCaptor;

    @Captor
    ArgumentCaptor<List<YearlyTemperature>> listCaptor;

    @Test
    void getFileProcessingStateShouldReturnNewStateWhenNoExistingStateWasFound() {
        // given
        BlobInfo blobInfo = BlobInfo.newBuilder(
                BlobId.of(BUCKET_NAME, FILE_NAME, GENERATION)).build();
        storage.create(blobInfo);
        Blob blob = storage.get(BUCKET_NAME, FILE_NAME);

        when(fileProcessingStateRepository.findTopByFilenameOrderByGenerationDesc(FILE_NAME))
                .thenReturn(Optional.empty());

        // when
        FileProcessingState result = service.getFileProcessingState(blob);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo(FILE_NAME);
        assertThat(result.getGeneration()).isEqualTo(GENERATION);
        assertThat(result.isCompleted()).isFalse();
        assertThat(result.getPosition()).isEqualTo(0L);
    }

    @Test
    void getFileProcessingStateShouldReturnNewStateWhenNewGenerationIsAvailable() {
        // given
        BlobInfo blobInfo = BlobInfo.newBuilder(
                BlobId.of(BUCKET_NAME, FILE_NAME, GENERATION)).build();
        storage.create(blobInfo);
        Blob blob = storage.get(BUCKET_NAME, FILE_NAME);

        FileProcessingState existingState = new FileProcessingState();
        existingState.setFilename(FILE_NAME);
        existingState.setGeneration(GENERATION-1);
        when(fileProcessingStateRepository.findTopByFilenameOrderByGenerationDesc(FILE_NAME))
                .thenReturn(Optional.empty());

        // when
        FileProcessingState result = service.getFileProcessingState(blob);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo(FILE_NAME);
        assertThat(result.getGeneration()).isEqualTo(GENERATION);
        assertThat(result.isCompleted()).isFalse();
        assertThat(result.getPosition()).isEqualTo(0L);
    }

    @Test
    void getFileProcessingStateShouldReturnNullWhenProcessingOfSameGenerationWasCompleted() {
        // given
        BlobInfo blobInfo = BlobInfo.newBuilder(
                        BlobId.of(BUCKET_NAME, FILE_NAME, GENERATION)).build();
        storage.create(blobInfo);
        Blob blob = storage.get(BUCKET_NAME, FILE_NAME);

        FileProcessingState existingState = new FileProcessingState();
        existingState.setFilename(FILE_NAME);
        existingState.setGeneration(GENERATION);
        existingState.setCompleted(true);
        when(fileProcessingStateRepository.findTopByFilenameOrderByGenerationDesc(FILE_NAME))
                .thenReturn(Optional.of(existingState));

        // when
        FileProcessingState result = service.getFileProcessingState(blob);

        // then
        assertThat(result).isNull();
    }

    @Test
    void getFileProcessingStateShouldReturnExistingStateWhenProcessingOfSameGenerationWasNotCompleted() {
        // given
        BlobInfo blobInfo = BlobInfo.newBuilder(
                BlobId.of(BUCKET_NAME, FILE_NAME, GENERATION)).build();
        storage.create(blobInfo);
        Blob blob = storage.get(BUCKET_NAME, FILE_NAME);

        FileProcessingState existingState = new FileProcessingState();
        existingState.setFilename(FILE_NAME);
        existingState.setGeneration(GENERATION);
        existingState.setCompleted(false);
        when(fileProcessingStateRepository.findTopByFilenameOrderByGenerationDesc(FILE_NAME))
                .thenReturn(Optional.of(existingState));

        // when
        FileProcessingState result = service.getFileProcessingState(blob);

        // then
        assertThat(result).isEqualTo(existingState);
    }

    @Test
    void updateProcessingState() {
        // given
        FileProcessingState fileProcessingState = new FileProcessingState();
        fileProcessingState.setFilename(FILE_NAME);
        fileProcessingState.setGeneration(GENERATION);

        Map<CityWithYear, YearlyTemperatureData> temperatureData = Map.of(
                new CityWithYear("Warsaw", 2024),
                new YearlyTemperatureData(BigDecimal.valueOf(50L), BigDecimal.valueOf(2L)));

        // when
        service.updateProcessingState(fileProcessingState, temperatureData);

        // then
        verify(fileProcessingStateRepository).save(stateCaptor.capture());
        assertThat(stateCaptor.getValue().getPartialResults()).hasSize(1);
        assertThat(stateCaptor.getValue().getPartialResults().getFirst().getCity()).isEqualTo("Warsaw");
        assertThat(stateCaptor.getValue().getPartialResults().getFirst().getYear()).isEqualTo(2024);
        assertThat(stateCaptor.getValue().getPartialResults().getFirst().getTotalTemperature()).isEqualByComparingTo(BigDecimal.valueOf(50L));
        assertThat(stateCaptor.getValue().getPartialResults().getFirst().getCount()).isEqualByComparingTo(BigDecimal.valueOf(2L));
    }

    @Test
    void completeProcessing() {
        // given
        FileProcessingState fileProcessingState = new FileProcessingState();
        fileProcessingState.setFilename(FILE_NAME);
        fileProcessingState.setGeneration(GENERATION);

        Map<CityWithYear, YearlyTemperatureData> temperatureData = Map.of(
                new CityWithYear("Warsaw", 2024),
                new YearlyTemperatureData(BigDecimal.valueOf(50L), BigDecimal.valueOf(2L)));

        // when
        service.completeProcessing(fileProcessingState, temperatureData);

        // then
        verify(fileProcessingStateRepository).save(stateCaptor.capture());
        assertThat(stateCaptor.getValue().isCompleted()).isTrue();
        assertThat(stateCaptor.getValue().getPartialResults()).isEmpty();
        assertThat(stateCaptor.getValue().getPosition()).isNull();

        verify(yearlyTemperatureRepository).deleteAll();
        verify(yearlyTemperatureRepository).saveAll(listCaptor.capture());
        assertThat(listCaptor.getValue()).hasSize(1);
        assertThat(listCaptor.getValue().getFirst().getCity()).isEqualTo("Warsaw");
        assertThat(listCaptor.getValue().getFirst().getYear()).isEqualTo(2024);
        assertThat(listCaptor.getValue().getFirst().getAverageTemperature()).isEqualByComparingTo(BigDecimal.valueOf(25L));
    }
}