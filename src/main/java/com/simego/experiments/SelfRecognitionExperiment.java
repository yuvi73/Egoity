package com.simego.experiments;

import com.simego.core.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.nio.file.Paths;
import java.io.PrintWriter;
import java.io.File;

public class SelfRecognitionExperiment {
    private static final int NUM_SIMULATIONS = 10; // Increased number of simulations
    private static final int MAX_STEPS = 2000; // Increased steps for longer interactions
    private static final int GRID_SIZE = 8; // Even smaller grid for more frequent interactions
    private static final int NUM_AGENTS = 6; // More agents for richer social dynamics
    private static final int METRICS_INTERVAL = 5; // More frequent metrics collection
    private static final double RECOGNITION_THRESHOLD = 0.65; // Slightly lower threshold
    private static final int STEP_DELAY_MS = 150; // Longer delay for better processing
    
    private List<ExperimentResult> results;
    private Random random;
    private final String resultsPath;
    
    public SelfRecognitionExperiment() {
        this.results = new ArrayList<>();
        this.random = new Random();
        this.resultsPath = Paths.get(System.getProperty("user.dir"), "experiment_results.csv").toString();
    }
    
    public void runExperiment() {
        System.out.println("Running " + NUM_SIMULATIONS + " simulations...");
        
        for (int i = 0; i < NUM_SIMULATIONS; i++) {
            System.out.println("Running simulation " + (i + 1) + "/" + NUM_SIMULATIONS);
            runSingleSimulation(i);
        }
        
        saveResults();
    }
    
    private void runSingleSimulation(int simulationId) {
        Environment environment = new Environment(GRID_SIZE, GRID_SIZE);
        List<Agent> agents = new ArrayList<>();
        ExperimentResult result = new ExperimentResult(simulationId);
        
        // Initialize agents with varied initial states
        for (int i = 0; i < NUM_AGENTS; i++) {
            Position position = new Position(
                random.nextInt(GRID_SIZE),
                random.nextInt(GRID_SIZE)
            );
            Agent agent = new Agent(position);
            
            // Initialize agent's emotional state and self-model with more variation
            agent.getEmotionalState().setPride(random.nextDouble() * 0.4);
            agent.getEmotionalState().setJoy(random.nextDouble() * 0.5);
            agent.getEmotionalState().setStress(random.nextDouble() * 0.3);
            agent.getEmotionalState().setFear(random.nextDouble() * 0.3);
            
            agent.getSelfModel().setSelfConsistency(0.6);
            agent.getSelfModel().setAgency(0.5);
            agent.getSelfModel().setMirroring(0.3);
            
            agents.add(agent);
            environment.addAgent(agent);
        }
        
        System.out.println("Starting simulation " + (simulationId + 1) + "...");
        long startTime = System.currentTimeMillis();
        
        // Run simulation
        for (int step = 0; step < MAX_STEPS; step++) {
            // Show progress every 100 steps
            if (step % 100 == 0) {
                long currentTime = System.currentTimeMillis();
                long elapsedSeconds = (currentTime - startTime) / 1000;
                System.out.printf("Step %d/%d (%.1f%%) - Elapsed: %d seconds%n",
                    step, MAX_STEPS, (step * 100.0 / MAX_STEPS), elapsedSeconds);
            }
            
            // Update environment and agents
            for (Agent agent : agents) {
                // First, let agents perceive their environment
                List<Stimulus> stimuli = environment.getStimuliAt(agent.getPosition());
                agent.perceive(stimuli);
                
                // Then let them act
                agent.act(environment);
                
                // Finally, let them reflect on their experiences
                agent.reflect();
                
                // Add delay to allow for processing
                try {
                    Thread.sleep(STEP_DELAY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // Collect metrics more frequently
            if (step % METRICS_INTERVAL == 0) {
                collectMetrics(simulationId, step, agents, result);
            }
            
            // Check for self-recognition
            for (Agent agent : agents) {
                if (agent.getSelfModel().getSelfRecognition() >= RECOGNITION_THRESHOLD && 
                    result.getStepsToSelfRecognition() == -1) {
                    result.setStepsToSelfRecognition(step);
                    System.out.println("Agent achieved self-recognition at step " + step);
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        long totalSeconds = (endTime - startTime) / 1000;
        System.out.printf("Simulation %d completed in %d seconds%n", 
            simulationId + 1, totalSeconds);
        
        results.add(result);
    }
    
    private void collectMetrics(int simulationId, int step, List<Agent> agents, ExperimentResult result) {
        double[] metrics = new double[10];
        
        for (Agent agent : agents) {
            SelfModel selfModel = agent.getSelfModel();
            EmotionalState emotions = agent.getEmotionalState();
            
            metrics[0] += selfModel.getSelfConsistency();
            metrics[1] += selfModel.getAgency();
            metrics[2] += selfModel.getSelfRecognition();
            metrics[3] += agent.getMemories().size();
            metrics[4] += emotions.getFear();
            metrics[5] += emotions.getJoy();
            metrics[6] += emotions.getPride();
            metrics[7] += emotions.getStress();
            metrics[8] += selfModel.getMirroring();
            metrics[9] += selfModel.getSuccessfulImitationCount();
        }
        
        // Average the metrics
        for (int i = 0; i < metrics.length; i++) {
            metrics[i] /= NUM_AGENTS;
        }
        
        result.addMetrics(metrics);
    }
    
    public void saveResults() {
        try (PrintWriter writer = new PrintWriter(new File(resultsPath))) {
            writer.println("simulation_id,step,self_consistency,agency,self_recognition,memory_count,fear,joy,pride,stress,mirroring,successful_imitations,steps_to_recognition");
            
            for (ExperimentResult result : results) {
                List<Double> selfConsistency = result.getSelfConsistency();
                List<Double> agency = result.getAgency();
                List<Double> selfRecognition = result.getSelfRecognition();
                List<Double> memoryCount = result.getMemoryCount();
                List<Double> fear = result.getFear();
                List<Double> joy = result.getJoy();
                List<Double> pride = result.getPride();
                List<Double> stress = result.getStress();
                List<Double> mirroring = result.getMirroring();
                List<Double> successfulImitations = result.getSuccessfulImitations();
                
                for (int i = 0; i < selfConsistency.size(); i++) {
                    writer.printf("%d,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%d%n",
                        result.getSimulationId(),
                        i * METRICS_INTERVAL,
                        selfConsistency.get(i),
                        agency.get(i),
                        selfRecognition.get(i),
                        memoryCount.get(i),
                        fear.get(i),
                        joy.get(i),
                        pride.get(i),
                        stress.get(i),
                        mirroring.get(i),
                        successfulImitations.get(i),
                        result.getStepsToSelfRecognition());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SelfRecognitionExperiment experiment = new SelfRecognitionExperiment();
        experiment.runExperiment();
    }
} 