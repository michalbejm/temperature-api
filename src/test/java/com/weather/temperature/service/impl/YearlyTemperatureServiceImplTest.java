package com.weather.temperature.service.impl;

import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.domain.repository.YearlyTemperatureRepository;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;
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
        when(repository.findByCityOrderByYearAsc(city)).thenReturn(List.of());

        // when
        List<YearlyTemperature> result = service.findByCityOrderByYearAsc(city);

        // then
        assertThat(result).isEmpty();
        verify(repository).findByCityOrderByYearAsc(city);
    }
}