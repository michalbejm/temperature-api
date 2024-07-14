package com.weather.temperature.controller;

import com.weather.temperature.domain.entity.YearlyTemperature;
import com.weather.temperature.service.YearlyTemperatureService;
import com.weather.temperature.service.dto.YearlyTemperatureDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class YearlyTemperatureController {

    private final YearlyTemperatureService yearlyTemperatureService;

    public YearlyTemperatureController(YearlyTemperatureService yearlyTemperatureService) {
        this.yearlyTemperatureService = yearlyTemperatureService;
    }

    @GetMapping("/yearly-temperatures")
    public List<YearlyTemperatureDto> getYearlyTemperatures(@RequestParam String city) {
        return yearlyTemperatureService.findByCityOrderByYearAsc(city);
    }
}
