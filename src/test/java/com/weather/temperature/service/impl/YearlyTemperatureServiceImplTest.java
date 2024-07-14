package com.weather.temperature.service.impl;

import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.domain.repository.YearlyTemperatureRepository;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;
import com.weather.temperature.service.dto.YearlyTemperatureDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YearlyTemperatureServiceImplTest {

    @InjectMocks
    private YearlyTemperatureServiceImpl service;

    @Mock
    private YearlyTemperatureRepository repository;

    @Test
    void findByCityOrderByYearAsc() {
        // given
        String city = "Warsaw";
        YearlyTemperature yearlyTemperature = new YearlyTemperature();
        yearlyTemperature.setCity(city);
        yearlyTemperature.setYear(2024);
        yearlyTemperature.setTotalTemperature(BigDecimal.valueOf(50L));
        yearlyTemperature.setCount(BigDecimal.valueOf(2L));
        when(repository.findByCityOrderByYearAsc(city)).thenReturn(List.of(yearlyTemperature));

        // when
        List<YearlyTemperatureDto> result = service.findByCityOrderByYearAsc(city);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().year()).isEqualTo(2024);
        assertThat(result.getFirst().averageTemperature()).isEqualByComparingTo(BigDecimal.valueOf(25L));
        verify(repository).findByCityOrderByYearAsc(city);
    }
}