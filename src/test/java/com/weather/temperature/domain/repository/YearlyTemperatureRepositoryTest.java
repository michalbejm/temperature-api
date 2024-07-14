package com.weather.temperature.domain.repository;

import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.service.dto.CityWithYear;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class YearlyTemperatureRepositoryTest {

    @Autowired
    private YearlyTemperatureRepository repository;

    @Test
    void findByCityOrderByYearAsc() {
        // given
        createAndPersistYearlyTemperatureEntity("Poznan", 2021, new BigDecimal("21.00"));
        createAndPersistYearlyTemperatureEntity("Warsaw", 2022, new BigDecimal("22.00"));
        createAndPersistYearlyTemperatureEntity("Warsaw", 2020, new BigDecimal("20.00"));
        createAndPersistYearlyTemperatureEntity("Warsaw", 2024, new BigDecimal("24.00"));
        createAndPersistYearlyTemperatureEntity("Wroclaw", 2023, new BigDecimal("22.00"));

        // when
        List<YearlyTemperature> result = repository.findByCityOrderByYearAsc("Warsaw");

        // then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(yearlyTemperature -> yearlyTemperature.getCity().equals("Warsaw"));
        assertThat(result.get(0).getYear()).isEqualTo(2020);
        assertThat(result.get(0).getTotalTemperature()).isEqualByComparingTo("20");
        assertThat(result.get(1).getYear()).isEqualTo(2022);
        assertThat(result.get(1).getTotalTemperature()).isEqualByComparingTo("22");
        assertThat(result.get(2).getYear()).isEqualTo(2024);
        assertThat(result.get(2).getTotalTemperature()).isEqualByComparingTo("24");
    }

    @Test
    void findByCitiesWithYear() {
        // given
        createAndPersistYearlyTemperatureEntity("Warsaw", 2023, new BigDecimal("23.10"));
        createAndPersistYearlyTemperatureEntity("Warsaw", 2024, new BigDecimal("24.10"));
        createAndPersistYearlyTemperatureEntity("Wroclaw", 2023, new BigDecimal("23.20"));
        createAndPersistYearlyTemperatureEntity("Wroclaw", 2024, new BigDecimal("24.20"));

        // when
        Optional<YearlyTemperature> result = repository.findByCityAndYear("Warsaw", 2024);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCity()).isEqualTo("Warsaw");
        assertThat(result.get().getYear()).isEqualTo(2024);
        assertThat(result.get().getTotalTemperature()).isEqualByComparingTo("24.1");
    }

    private void createAndPersistYearlyTemperatureEntity(String city, int year, BigDecimal totalTemperature) {
        YearlyTemperature entity = new YearlyTemperature();
        entity.setCity(city);
        entity.setYear(year);
        entity.setTotalTemperature(totalTemperature);
        entity.setCount(BigDecimal.ONE);
        repository.save(entity);
    }

}