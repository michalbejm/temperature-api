package com.weather.temperature.domain.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class FileProcessingState {
    @Id
    @GeneratedValue
    private Long id;

    private String filename;
    private Long generation;
    private boolean completed = false;
    private Long position = 0L;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "processing_state_id")
    private List<FileProcessingPartialResult> partialResults = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getGeneration() {
        return generation;
    }

    public void setGeneration(Long generation) {
        this.generation = generation;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public List<FileProcessingPartialResult> getPartialResults() {
        return partialResults;
    }

    public void setPartialResults(List<FileProcessingPartialResult> partialResults) {
        this.partialResults = partialResults;
    }
}
