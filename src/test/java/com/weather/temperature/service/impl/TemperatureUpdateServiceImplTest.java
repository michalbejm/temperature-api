package com.weather.temperature.service.impl;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import com.weather.temperature.domain.entity.FileProcessingPartialResult;
import com.weather.temperature.domain.entity.FileProcessingState;
import com.weather.temperature.service.FileProcessingStateService;
import com.weather.temperature.service.config.GcpConfig;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemperatureUpdateServiceImplTest {

    @InjectMocks
    private TemperatureUpdateServiceImpl updateService;

    @Spy
    private Storage storage = LocalStorageHelper.getOptions().getService();

    @Mock
    private GcpConfig gcpConfig;
    @Mock
    private FileProcessingStateService fileProcessingStateService;

    private static final String BUCKET_NAME = "bucket_name";
    private static final String FILE_NAME = "file_name";

    @Captor
    ArgumentCaptor<FileProcessingState> stateCaptor;
    @Captor
    ArgumentCaptor<Map<CityWithYear, YearlyTemperatureData>> mapCaptor;


    @BeforeEach
    public void setUp() {
        when(gcpConfig.getFileName()).thenReturn(FILE_NAME);
        when(gcpConfig.getBucketName()).thenReturn(BUCKET_NAME);
        when(gcpConfig.getProjectId()).thenReturn("project");
        when(gcpConfig.getPubsubSubscription()).thenReturn("subscription");
        when(gcpConfig.getLinesPerUpdate()).thenReturn(1000L);
    }
    @Test
    void shouldProcessValidCsvFileOnInit() {
        // given
        String fileContent = """
                Warsaw;2024-07-13 12:00:00.000;30.0
                Warsaw;2024-07-12 12:00:00.000;20.0
                Warsaw;2023-07-12 12:00:00.000;10.0
                Poznan;2024-07-13 12:00:00.000;28.0""";
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, FILE_NAME)).build();
        storage.create(blobInfo, fileContent.getBytes());
        FileProcessingState fileProcessingState = new FileProcessingState();
        when(fileProcessingStateService.getFileProcessingState(any())).thenReturn(fileProcessingState);

        // when
        updateService.init();

        // then
        verify(fileProcessingStateService).completeProcessing(stateCaptor.capture(), mapCaptor.capture());
        assertThat(mapCaptor.getValue()).hasSize(3);
        assertThat(mapCaptor.getValue().get(new CityWithYear("Warsaw", 2024)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Warsaw", 2024)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Warsaw", 2023)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Warsaw", 2023)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Poznan", 2024)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Poznan", 2024)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(28));
        assertThat(stateCaptor.getValue()).isEqualTo(fileProcessingState);
    }

    @Test
    void shouldSkipInvalidLinesWhenProcessingCsvFileOnInit() {
        // given
        String fileContent = """
                Warsaw;2024-07-13 12:00:00.000
                Warsaw;20;20.0
                Warsaw;2023-07-12 12:00:00.000;XYZ
                Poznan;2024-07-13 12:00:00.000;28.0""";
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, FILE_NAME)).build();
        storage.create(blobInfo, fileContent.getBytes());
        FileProcessingState fileProcessingState = new FileProcessingState();
        when(fileProcessingStateService.getFileProcessingState(any())).thenReturn(fileProcessingState);

        // when
        updateService.init();

        // then
        verify(fileProcessingStateService).completeProcessing(stateCaptor.capture(), mapCaptor.capture());
        assertThat(mapCaptor.getValue()).hasSize(1);
        assertThat(mapCaptor.getValue().get(new CityWithYear("Poznan", 2024)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Poznan", 2024)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(28));
        assertThat(stateCaptor.getValue()).isEqualTo(fileProcessingState);
    }

    @Test
    void shouldUpdateStateEveryNLines() {
        // given
        String fileContent = """
                Warsaw;2024-07-13 12:00:00.000;30.0
                Warsaw;2024-07-12 12:00:00.000;20.0
                Warsaw;2023-07-12 12:00:00.000;10.0
                Poznan;2024-07-13 12:00:00.000;28.0
                Poznan;2024-07-13 12:00:00.000;28.0""";
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, FILE_NAME)).build();
        storage.create(blobInfo, fileContent.getBytes());
        FileProcessingState fileProcessingState = new FileProcessingState();
        when(fileProcessingStateService.getFileProcessingState(any())).thenReturn(fileProcessingState);
        when(gcpConfig.getLinesPerUpdate()).thenReturn(2L);

        // when
        updateService.init();

        // then
        verify(fileProcessingStateService).completeProcessing(stateCaptor.capture(), mapCaptor.capture());
        assertThat(stateCaptor.getValue()).isEqualTo(fileProcessingState);
        verify(fileProcessingStateService, times(2)).updateProcessingState(stateCaptor.capture(), mapCaptor.capture());
    }

    @Test
    void shouldStartFromTheGivenPoint() {
        // given
        String fileContent = """
                Test;2024;10
                Warsaw;2023-07-12 12:00:00.000;10.0
                Poznan;2024-07-13 12:00:00.000;28.0""";
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, FILE_NAME)).build();
        storage.create(blobInfo, fileContent.getBytes());

        FileProcessingState fileProcessingState = new FileProcessingState();
        fileProcessingState.setPosition(13L);   // length of the firstline

        FileProcessingPartialResult partialResult = new FileProcessingPartialResult();
        partialResult.setCity("Warsaw");
        partialResult.setYear(2024);
        partialResult.setCount(BigDecimal.valueOf(2L));
        partialResult.setTotalTemperature(BigDecimal.valueOf(50L));
        fileProcessingState.setPartialResults(List.of(partialResult));

        when(fileProcessingStateService.getFileProcessingState(any())).thenReturn(fileProcessingState);

        // when
        updateService.init();

        // then
        verify(fileProcessingStateService).completeProcessing(stateCaptor.capture(), mapCaptor.capture());
        assertThat(mapCaptor.getValue()).hasSize(3);
        assertThat(mapCaptor.getValue().get(new CityWithYear("Warsaw", 2024)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Warsaw", 2024)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Warsaw", 2023)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Warsaw", 2023)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Poznan", 2024)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(mapCaptor.getValue().get(new CityWithYear("Poznan", 2024)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(28));
        assertThat(stateCaptor.getValue()).isEqualTo(fileProcessingState);
    }
}