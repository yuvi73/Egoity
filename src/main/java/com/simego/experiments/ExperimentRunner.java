package com.simego.experiments;

public class ExperimentRunner {
    public static void main(String[] args) {
        System.out.println("Starting Self-Recognition Development Experiment...");
        
        SelfRecognitionExperiment experiment = new SelfRecognitionExperiment();
        experiment.runExperiment();
        
        System.out.println("Experiment completed. Results saved to experiment_results.csv");
    }
} 