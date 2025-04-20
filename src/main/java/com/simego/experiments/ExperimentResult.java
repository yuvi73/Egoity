package com.simego.experiments;

import java.util.ArrayList;
import java.util.List;

public class ExperimentResult {
    private final int simulationId;
    private final List<Double> selfConsistency;
    private final List<Double> agency;
    private final List<Double> selfRecognition;
    private final List<Double> memoryCount;
    private final List<Double> fear;
    private final List<Double> joy;
    private final List<Double> pride;
    private final List<Double> stress;
    private final List<Double> mirroring;
    private final List<Double> successfulImitations;
    private int stepsToSelfRecognition;

    public ExperimentResult(int simulationId) {
        this.simulationId = simulationId;
        this.selfConsistency = new ArrayList<>();
        this.agency = new ArrayList<>();
        this.selfRecognition = new ArrayList<>();
        this.memoryCount = new ArrayList<>();
        this.fear = new ArrayList<>();
        this.joy = new ArrayList<>();
        this.pride = new ArrayList<>();
        this.stress = new ArrayList<>();
        this.mirroring = new ArrayList<>();
        this.successfulImitations = new ArrayList<>();
        this.stepsToSelfRecognition = -1;
    }

    public void addMetrics(double[] metrics) {
        selfConsistency.add(metrics[0]);
        agency.add(metrics[1]);
        selfRecognition.add(metrics[2]);
        memoryCount.add(metrics[3]);
        fear.add(metrics[4]);
        joy.add(metrics[5]);
        pride.add(metrics[6]);
        stress.add(metrics[7]);
        mirroring.add(metrics[8]);
        successfulImitations.add(metrics[9]);
    }

    public int getSimulationId() {
        return simulationId;
    }

    public List<Double> getSelfConsistency() {
        return selfConsistency;
    }

    public List<Double> getAgency() {
        return agency;
    }

    public List<Double> getSelfRecognition() {
        return selfRecognition;
    }

    public List<Double> getMemoryCount() {
        return memoryCount;
    }

    public List<Double> getFear() {
        return fear;
    }

    public List<Double> getJoy() {
        return joy;
    }

    public List<Double> getPride() {
        return pride;
    }

    public List<Double> getStress() {
        return stress;
    }

    public List<Double> getMirroring() {
        return mirroring;
    }

    public List<Double> getSuccessfulImitations() {
        return successfulImitations;
    }

    public int getStepsToSelfRecognition() {
        return stepsToSelfRecognition;
    }

    public void setStepsToSelfRecognition(int steps) {
        this.stepsToSelfRecognition = steps;
    }
} 