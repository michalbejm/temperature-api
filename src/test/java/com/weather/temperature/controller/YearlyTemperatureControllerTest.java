package com.weather.temperature.controller;

import com.weather.temperature.service.YearlyTemperatureService;
import com.weather.temperature.service.dto.YearlyTemperatureDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(YearlyTemperatureController.class)
class YearlyTemperatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private YearlyTemperatureService yearlyTemperatureService;

    @Test
    void getYearlyTemperatures() throws Exception {
        // given
        String city = "Warsaw";
        YearlyTemperatureDto temperature1 = new YearlyTemperatureDto(2023, BigDecimal.valueOf(10L));
        YearlyTemperatureDto temperature2 = new YearlyTemperatureDto(2024, BigDecimal.valueOf(11L));
        when(yearlyTemperatureService.findByCityOrderByYearAsc(city)).thenReturn(List.of(
                temperature1, temperature2));

        // when/then
        mockMvc.perform(MockMvcRequestBuilders.get("/yearly-temperatures?city="+city).accept(MediaType.ALL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].year", is(2023)))
                .andExpect(jsonPath("$[0].averageTemperature", is(10)))
                .andExpect(jsonPath("$[1].year", is(2024)))
                .andExpect(jsonPath("$[1].averageTemperature", is(11)));
    }
}