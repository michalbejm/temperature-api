package com.weather.temperature.domain.repository;

import com.weather.temperature.domain.entity.FileProcessingState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FileProcessingStateRepositoryTest {

    @Autowired
    private FileProcessingStateRepository fileProcessingStateRepository;

    private static final String DEFAULT_FILE_NAME = "file-name";

    @Test
    void findTopByFilenameOrderByGenerationDesc() {
        // given
        FileProcessingState fileProcessingState = new FileProcessingState();
        fileProcessingState.setFilename(DEFAULT_FILE_NAME);
        fileProcessingState = fileProcessingStateRepository.save(fileProcessingState);

        // when
        Optional<FileProcessingState> result = fileProcessingStateRepository.findByFilename(DEFAULT_FILE_NAME);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getFilename()).isEqualTo(DEFAULT_FILE_NAME);
        assertThat(result.get().getId()).isEqualTo(fileProcessingState.getId());
    }
}