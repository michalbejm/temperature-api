package com.weather.temperature.domain.repository;

import com.weather.temperature.YearlyTemperatureTest;
import com.weather.temperature.domain.entity.YearlyTemperature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class YearlyTemperatureRepositoryTest extends YearlyTemperatureTest {

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
        assertThat(result.get(0).getAverageTemperature()).isEqualByComparingTo("20");
        assertThat(result.get(1).getYear()).isEqualTo(2022);
        assertThat(result.get(1).getAverageTemperature()).isEqualByComparingTo("22");
        assertThat(result.get(2).getYear()).isEqualTo(2024);
        assertThat(result.get(2).getAverageTemperature()).isEqualByComparingTo("24");
    }

    private void createAndPersistYearlyTemperatureEntity(String city, int year, BigDecimal averageTemperature) {
        YearlyTemperature entity = createYearlyTemperature(city, year, averageTemperature);
        repository.save(entity);
    }

}