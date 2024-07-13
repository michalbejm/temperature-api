package com.weather.temperature.service.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.service.YearlyTemperatureService;
import com.weather.temperature.service.config.GcpConfig;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemperatureUpdateServiceImplTest {

    @InjectMocks
    private TemperatureUpdateServiceImpl updateService;

    @Spy
    private Storage storage = LocalStorageHelper.getOptions().getService();

    @Mock
    private GcpConfig gcpConfig;
    @Mock
    private YearlyTemperatureService yearlyTemperatureService;

    private static final String BUCKET_NAME = "bucket_name";
    private static final String FILE_NAME = "file_name";

    @Captor
    ArgumentCaptor<Map<CityWithYear, YearlyTemperatureData>> captor;

    @BeforeEach
    public void setUp() {
        when(gcpConfig.getFileName()).thenReturn(FILE_NAME);
        when(gcpConfig.getBucketName()).thenReturn(BUCKET_NAME);
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

        // when
        updateService.init();

        // then
        verify(yearlyTemperatureService).createYearlyTemperatures(captor.capture());
        assertThat(captor.getValue()).hasSize(3);
        assertThat(captor.getValue().get(new CityWithYear("Warsaw", 2024)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(captor.getValue().get(new CityWithYear("Warsaw", 2024)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(captor.getValue().get(new CityWithYear("Warsaw", 2023)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(captor.getValue().get(new CityWithYear("Warsaw", 2023)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(captor.getValue().get(new CityWithYear("Poznan", 2024)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(captor.getValue().get(new CityWithYear("Poznan", 2024)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(28));
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

        // when
        updateService.init();

        // then
        verify(yearlyTemperatureService).createYearlyTemperatures(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(new CityWithYear("Poznan", 2024)).getCount())
                .isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(captor.getValue().get(new CityWithYear("Poznan", 2024)).getTotalTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(28));
    }
}