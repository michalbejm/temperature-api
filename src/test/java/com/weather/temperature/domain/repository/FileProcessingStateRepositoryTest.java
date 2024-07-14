package com.weather.temperature.domain.repository;

import com.weather.temperature.domain.entity.FileProcessingState;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FileProcessingStateRepositoryTest {

    @Autowired
    private FileProcessingStateRepository fileProcessingStateRepository;

    private static final String DEFAULT_FILE_NAME = "file-name";

    @Test
    void findTopByFilenameOrderByGenerationDesc() {
        // given
        createFileProcessingState(1);
        createFileProcessingState(3);
        createFileProcessingState(2);

        // when
        Optional<FileProcessingState> result = fileProcessingStateRepository
                .findTopByFilenameOrderByGenerationDesc(DEFAULT_FILE_NAME);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getFilename()).isEqualTo(DEFAULT_FILE_NAME);
        assertThat(result.get().getGeneration()).isEqualTo(3);
    }

    private void createFileProcessingState(long generation) {
        FileProcessingState fileProcessingState = new FileProcessingState();
        fileProcessingState.setFilename(DEFAULT_FILE_NAME);
        fileProcessingState.setGeneration(generation);
        fileProcessingStateRepository.save(fileProcessingState);
    }
}