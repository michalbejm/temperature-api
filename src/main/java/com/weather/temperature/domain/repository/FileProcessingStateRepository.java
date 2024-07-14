package com.weather.temperature.domain.repository;

import com.weather.temperature.domain.entity.FileProcessingState;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileProcessingStateRepository extends JpaRepository<FileProcessingState, Long> {

    Optional<FileProcessingState> findByFilename(String filename);
}
