package com.simego.core;

import java.util.*;

public class Environment {
    private final int width;
    private final int height;
    private final Map<Position, List<Stimulus>> stimuli;
    private final List<Agent> agents;
    private final Random random;
    private static final double INTERACTION_RADIUS = 2.0;

    public Environment(int width, int height) {
        this.width = width;
        this.height = height;
        this.stimuli = new HashMap<>();
        this.agents = new ArrayList<>();
        this.random = new Random();
    }

    public void addAgent(Agent agent) {
        agents.add(agent);
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void addStimulus(Position position, Stimulus stimulus) {
        stimuli.computeIfAbsent(position, k -> new ArrayList<>()).add(stimulus);
    }

    public List<Agent> getAgentsNear(Position position, int radius) {
        List<Agent> nearbyAgents = new ArrayList<>();
        for (Agent agent : agents) {
            if (agent.getPosition().distanceTo(position) <= radius) {
                nearbyAgents.add(agent);
            }
        }
        return nearbyAgents;
    }

    public List<Stimulus> getStimuliAt(Position position) {
        List<Stimulus> result = new ArrayList<>();
        
        // Add existing stimuli at position
        List<Stimulus> existingStimuli = stimuli.get(position);
        if (existingStimuli != null) {
            result.addAll(existingStimuli);
        }
        
        // Random chance to generate new stimulus
        if (random.nextDouble() < 0.15) { // Increased to 15% chance
            Stimulus.StimulusType type = getRandomStimulusType();
            double intensity = 0.5 + (random.nextDouble() * 0.5); // Higher base intensity
            Stimulus newStimulus = new Stimulus(position, type, intensity);
            addStimulus(position, newStimulus);
            result.add(newStimulus);
        }
        
        // Add other agents as stimuli with their actions
        for (Agent agent : getAgentsNear(position, (int)INTERACTION_RADIUS)) {
            if (!agent.getPosition().equals(position)) {
                double distance = agent.getPosition().distanceTo(position);
                double intensity = 1.0 - (distance / INTERACTION_RADIUS); // Intensity decreases with distance
                Stimulus agentStimulus = new Stimulus(
                    agent.getPosition(),
                    Stimulus.StimulusType.OTHER_AGENT,
                    intensity
                );
                result.add(agentStimulus);
            }
        }
        
        return result;
    }

    public void applyAction(Action action) {
        Agent agent = action.getAgent();
        Position newPosition = agent.getPosition().move(action.getDirection());
        
        // Check if new position is within bounds
        if (isValidPosition(newPosition)) {
            agent.setPosition(newPosition);
            
            // Generate response stimuli
            generateResponseStimuli(action);
            
            // Propagate action effects to nearby agents
            propagateActionEffects(action);
        }
    }

    private void generateResponseStimuli(Action action) {
        Position targetPosition = action.getAgent().getPosition().move(action.getDirection());
        
        switch (action.getType()) {
            case EAT:
                if (random.nextDouble() < 0.7) { // 70% chance of finding food
                    Stimulus foodStimulus = new Stimulus(targetPosition, Stimulus.StimulusType.FOOD, 0.8);
                    addStimulus(targetPosition, foodStimulus);
                }
                break;
            case INTERACT:
                if (random.nextDouble() < 0.6) { // 60% chance of positive interaction
                    double intensity = 0.6 + (random.nextDouble() * 0.3); // 0.6 to 0.9
                    Stimulus interactionStimulus = new Stimulus(targetPosition, Stimulus.StimulusType.OTHER_AGENT, intensity, action);
                    addStimulus(targetPosition, interactionStimulus);
                    
                    // Create reciprocal interaction for other agents
                    List<Agent> nearbyAgents = getAgentsNear(targetPosition, 1);
                    for (Agent other : nearbyAgents) {
                        if (other != action.getAgent()) {
                            Stimulus reciprocalStimulus = new Stimulus(action.getAgent().getPosition(), 
                                                                     Stimulus.StimulusType.OTHER_AGENT, 
                                                                     intensity, 
                                                                     action);
                            other.perceive(Collections.singletonList(reciprocalStimulus));
                        }
                    }
                }
                break;
            case AVOID:
                if (random.nextDouble() < 0.4) { // 40% chance of danger presence
                    Stimulus dangerStimulus = new Stimulus(targetPosition, Stimulus.StimulusType.DANGER, 0.4);
                    addStimulus(targetPosition, dangerStimulus);
                }
                break;
        }
    }

    private void propagateActionEffects(Action action) {
        Position actorPosition = action.getAgent().getPosition();
        List<Agent> nearbyAgents = getAgentsNear(actorPosition, (int)INTERACTION_RADIUS);
        
        for (Agent observer : nearbyAgents) {
            if (observer != action.getAgent()) {
                double distance = observer.getPosition().distanceTo(actorPosition);
                double intensity = 1.0 - (distance / INTERACTION_RADIUS);
                
                // Create observation stimulus
                Stimulus observationStimulus = new Stimulus(
                    actorPosition,
                    Stimulus.StimulusType.OTHER_AGENT,
                    intensity,
                    action
                );
                
                // Let the observer perceive the action
                observer.perceive(Collections.singletonList(observationStimulus));
            }
        }
    }

    private Stimulus.StimulusType getRandomStimulusType() {
        double rand = random.nextDouble();
        if (rand < 0.4) return Stimulus.StimulusType.FOOD;
        if (rand < 0.7) return Stimulus.StimulusType.OTHER_AGENT;
        if (rand < 0.9) return Stimulus.StimulusType.DANGER;
        return Stimulus.StimulusType.NEUTRAL;
    }

    private boolean isValidPosition(Position position) {
        return position.getX() >= 0 && position.getX() < width &&
               position.getY() >= 0 && position.getY() < height;
    }
} 