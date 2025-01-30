package com.example.carsharingservice.mapper;

import com.example.carsharingservice.config.MapperConfig;
import com.example.carsharingservice.dto.car.CarRequestDto;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    CarResponseDto toDto(Car car);

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    Car toModel(CarRequestDto requestDto);
}
