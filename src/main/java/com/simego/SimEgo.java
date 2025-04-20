package com.simego;

import com.simego.core.*;
import java.util.Random;

public class SimEgo {
    private final Environment environment;
    private final Random random;

    public SimEgo(int width, int height) {
        this.environment = new Environment(width, height);
        this.random = new Random();
        initializeEnvironment();
    }

    private void initializeEnvironment() {
        // Add initial food sources
        for (int i = 0; i < 5; i++) {
            Position foodPosition = new Position(
                random.nextInt(environment.getWidth()),
                random.nextInt(environment.getHeight())
            );
            environment.addStimulus(foodPosition, new Stimulus(foodPosition, Stimulus.StimulusType.FOOD, 0.8));
        }

        // Add initial danger zones
        for (int i = 0; i < 3; i++) {
            Position dangerPosition = new Position(
                random.nextInt(environment.getWidth()),
                random.nextInt(environment.getHeight())
            );
            environment.addStimulus(dangerPosition, new Stimulus(dangerPosition, Stimulus.StimulusType.DANGER, 0.9));
        }

        // Add initial agents
        for (int i = 0; i < 5; i++) {
            Position agentPosition = new Position(
                random.nextInt(environment.getWidth()),
                random.nextInt(environment.getHeight())
            );
            Agent agent = new Agent(agentPosition);
            environment.addAgent(agent);
        }
    }

    public void runSimulation(int steps) {
        for (int step = 0; step < steps; step++) {
            // Update all agents
            for (Agent agent : environment.getAgents()) {
                agent.act(environment);
                agent.reflect();
            }

            // Add random stimuli occasionally
            if (random.nextDouble() < 0.1) { // 10% chance each step
                Position randomPosition = new Position(
                    random.nextInt(environment.getWidth()),
                    random.nextInt(environment.getHeight())
                );
                Stimulus.StimulusType type = getRandomStimulusType();
                environment.addStimulus(randomPosition, new Stimulus(randomPosition, type, random.nextDouble()));
            }
        }
    }

    private Stimulus.StimulusType getRandomStimulusType() {
        Stimulus.StimulusType[] types = Stimulus.StimulusType.values();
        return types[random.nextInt(types.length)];
    }

    public Environment getEnvironment() {
        return environment;
    }
} 