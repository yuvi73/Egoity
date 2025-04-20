package com.simego.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Random;
import java.util.Collections;

public class Agent {
    private static final int MAX_MEMORIES = 1000;
    private static final long MEMORY_LIFETIME_MS = 30000; // 30 seconds
    
    private final UUID id;
    private final List<Memory> memories;
    private final List<Goal> goals;
    private final SelfModel selfModel;
    private final EmotionalState emotionalState;
    private Position position;
    private final Random random;
    private int successfulImitationCount;
    private double socialFeedback;

    public Agent(Position position) {
        this.id = UUID.randomUUID();
        this.memories = new ArrayList<>();
        this.goals = new ArrayList<>();
        this.selfModel = new SelfModel();
        this.emotionalState = new EmotionalState();
        this.position = position;
        this.random = new Random();
        this.successfulImitationCount = 0;
        this.socialFeedback = 0.0;
        
        // Add initial goals from self model
        this.goals.addAll(selfModel.getCurrentGoals());
    }

    public void perceive(List<Stimulus> stimuli) {
        // Clean up old memories first
        cleanupOldMemories();
        
        // Process environmental stimuli
        for (Stimulus stimulus : stimuli) {
            if (memories.size() < MAX_MEMORIES) {
                processStimulus(stimulus);
            }
        }
        
        // Update emotional state
        emotionalState.decay();
    }

    public void act(Environment environment) {
        // Choose action based on goals and self-model
        Action action = decideAction();
        if (action != null) {
            executeAction(action, environment);
        }
        
        // After taking action, check for social implications
        if (action.getType() == Action.ActionType.INTERACT) {
            // Check if this action was observed by others
            List<Agent> nearbyAgents = environment.getAgentsNear(position, 2);
            for (Agent agent : nearbyAgents) {
                if (agent != this) {
                    // Create a stimulus for the observing agent
                    Stimulus socialStimulus = new Stimulus(
                        agent.getPosition(),
                        Stimulus.StimulusType.OTHER_AGENT,
                        0.8, // High intensity for social actions
                        action
                    );
                    agent.perceive(Collections.singletonList(socialStimulus));
                }
            }
        }
    }

    public void reflect() {
        // Update self-model based on recent experiences
        selfModel.update(memories);
        
        // Update goals based on emotional state and memories
        updateGoals();
    }

    private void cleanupOldMemories() {
        long currentTime = System.currentTimeMillis();
        memories.removeIf(memory -> 
            currentTime - memory.getTimestamp() > MEMORY_LIFETIME_MS
        );
    }

    private void processStimulus(Stimulus stimulus) {
        // Create memory of the stimulus
        Memory memory = new Memory(stimulus, System.currentTimeMillis());
        if (memories.size() < MAX_MEMORIES) {
            memories.add(memory);
        }
        
        // Update emotional state based on stimulus
        emotionalState.update(stimulus);
        
        // Update goals based on stimulus
        updateGoalsFromStimulus(stimulus);
        
        // Process social interaction
        processSocialInteraction(stimulus);
    }

    private void processSocialInteraction(Stimulus stimulus) {
        if (stimulus.getType() == Stimulus.StimulusType.OTHER_AGENT) {
            // Check if this is a successful imitation
            boolean isSuccessful = checkImitationSuccess(stimulus);
            
            // Calculate social feedback
            double feedback = calculateSocialFeedback(stimulus, isSuccessful);
            selfModel.updateSocialFeedback(feedback);
            
            // If successful, reinforce the imitation
            if (isSuccessful) {
                reinforceSuccessfulImitation(stimulus);
            }
        }
    }

    private boolean checkImitationSuccess(Stimulus stimulus) {
        // Check if the observed action led to a positive outcome
        Action sourceAction = stimulus.getSourceAction();
        if (sourceAction == null) {
            return false;
        }
        
        // Success is more likely if the action led to a positive outcome
        double successProbability = 0.3; // Base probability
        if (sourceAction.getType() == Action.ActionType.EAT && stimulus.getIntensity() > 0.5) {
            successProbability += 0.3; // Eating with high intensity is more likely to succeed
        } else if (sourceAction.getType() == Action.ActionType.INTERACT) {
            successProbability += 0.2; // Social interactions have moderate success rate
        }
        
        return random.nextDouble() < successProbability;
    }

    private double calculateSocialFeedback(Stimulus stimulus, boolean isSuccessful) {
        Action sourceAction = stimulus.getSourceAction();
        if (sourceAction == null) {
            return 0.0;
        }
        
        double baseFeedback = isSuccessful ? 0.3 : -0.1;
        
        // Adjust feedback based on action type
        switch (sourceAction.getType()) {
            case EAT:
                return baseFeedback * 1.2; // Eating is important for survival
            case INTERACT:
                return baseFeedback * 1.5; // Social interactions are highly valued
            case AVOID:
                return baseFeedback * 0.8; // Avoidance is less socially significant
            default:
                return baseFeedback;
        }
    }

    private void reinforceSuccessfulImitation(Stimulus stimulus) {
        Action sourceAction = stimulus.getSourceAction();
        if (sourceAction == null) {
            return;
        }
        
        // Create a positive memory of successful imitation
        Memory memory = new Memory(
            "Successful imitation of " + sourceAction.getType(),
            System.currentTimeMillis(),
            true, // Self-generated memory
            0.8 // High valence for successful imitation
        );
        memories.add(memory);
        
        // Update emotional state
        emotionalState.updateFromAction(sourceAction);
        
        // Update self-model with the successful imitation memory
        selfModel.updateFromMemory(memory);
    }

    private void updateGoalsFromStimulus(Stimulus stimulus) {
        Position targetPosition = stimulus.getPosition();
        switch (stimulus.getType()) {
            case FOOD:
                goals.add(new Goal(Goal.GoalType.FIND_FOOD, targetPosition, 0.8));
                break;
            case DANGER:
                goals.add(new Goal(Goal.GoalType.AVOID_DANGER, targetPosition, 0.9));
                break;
            case OTHER_AGENT:
                if (emotionalState.getStress() < 0.5) {
                    goals.add(new Goal(Goal.GoalType.SOCIALIZE, targetPosition, 0.6));
                }
                break;
        }
    }

    public Action decideAction() {
        // If no goals, explore randomly
        if (goals.isEmpty()) {
            return new Action(getRandomMovement(), this, getRandomDirection());
        }

        // Sort goals by priority
        goals.sort((g1, g2) -> Double.compare(g2.getPriority(), g1.getPriority()));
        
        // Get highest priority goal
        Goal currentGoal = goals.get(0);
        
        // Choose action based on goal type and emotional state
        Action.ActionType actionType = chooseActionForGoal(currentGoal);
        return new Action(actionType, this, getRandomDirection());
    }

    private Action.ActionType chooseActionForGoal(Goal goal) {
        switch (goal.getType()) {
            case FIND_FOOD:
                return emotionalState.getFear() > 0.7 ? Action.ActionType.AVOID : Action.ActionType.EAT;
            case AVOID_DANGER:
                return Action.ActionType.AVOID;
            case EXPLORE:
                return getRandomMovement();
            case REST:
                return Action.ActionType.MOVE_DOWN;
            case SOCIALIZE:
                return Action.ActionType.INTERACT;
            default:
                return getRandomMovement();
        }
    }

    private Action.ActionType getRandomMovement() {
        Action.ActionType[] movements = {
            Action.ActionType.MOVE_UP,
            Action.ActionType.MOVE_DOWN,
            Action.ActionType.MOVE_LEFT,
            Action.ActionType.MOVE_RIGHT
        };
        return movements[random.nextInt(movements.length)];
    }

    private Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[random.nextInt(directions.length)];
    }

    private void updateGoals() {
        // Remove completed goals
        goals.removeIf(goal -> {
            if (goal.getTargetPosition() != null && goal.getTargetPosition().equals(position)) {
                return true;
            }
            // Remove goals that are no longer relevant due to emotional state
            if (goal.getType() == Goal.GoalType.SOCIALIZE && emotionalState.getStress() > 0.7) {
                return true;
            }
            return false;
        });
        
        // Add exploration goal if no other goals exist
        if (goals.isEmpty()) {
            goals.add(new Goal(Goal.GoalType.EXPLORE, null, 0.5));
        }
    }

    public void executeAction(Action action, Environment environment) {
        // Store the previous position for memory creation
        Position previousPosition = new Position(position.getX(), position.getY());
        
        // Execute the action
        switch (action.getType()) {
            case MOVE_UP:
                position = new Position(position.getX(), Math.max(0, position.getY() - 1));
                break;
            case MOVE_DOWN:
                position = new Position(position.getX(), Math.min(environment.getHeight() - 1, position.getY() + 1));
                break;
            case MOVE_LEFT:
                position = new Position(Math.max(0, position.getX() - 1), position.getY());
                break;
            case MOVE_RIGHT:
                position = new Position(Math.min(environment.getWidth() - 1, position.getX() + 1), position.getY());
                break;
            case EAT:
                // Try to find food at current position
                List<Stimulus> stimuli = environment.getStimuliAt(position);
                boolean foundFood = false;
                for (Stimulus stimulus : stimuli) {
                    if (stimulus.getType() == Stimulus.StimulusType.FOOD) {
                        foundFood = true;
                        // Create positive memory and update emotional state
                        Memory foodMemory = new Memory(stimulus, System.currentTimeMillis(), true, 0.9);
                        memories.add(foodMemory);
                        emotionalState.updateFromAction(action);
                        break;
                    }
                }
                if (!foundFood) {
                    // Create negative memory for failing to find food
                    Memory failureMemory = new Memory(action, System.currentTimeMillis(), true, -0.3);
                    memories.add(failureMemory);
                    emotionalState.updateFromAction(action);
                }
                break;
            case AVOID:
                // Move away from danger
                List<Stimulus> nearbyStimuli = environment.getStimuliAt(position);
                boolean avoidedDanger = false;
                for (Stimulus stimulus : nearbyStimuli) {
                    if (stimulus.getType() == Stimulus.StimulusType.DANGER) {
                        avoidedDanger = true;
                        // Create positive memory for successful avoidance
                        Memory avoidanceMemory = new Memory(action, System.currentTimeMillis(), true, 0.7);
                        memories.add(avoidanceMemory);
                        emotionalState.updateFromAction(action);
                        break;
                    }
                }
                if (!avoidedDanger) {
                    // Create neutral memory for unnecessary avoidance
                    Memory unnecessaryMemory = new Memory(action, System.currentTimeMillis(), true, 0.0);
                    memories.add(unnecessaryMemory);
                }
                break;
            case INTERACT:
                // Try to interact with other agents
                List<Agent> nearbyAgents = environment.getAgentsNear(position, 1);
                boolean interacted = false;
                for (Agent other : nearbyAgents) {
                    if (other != this) {
                        interacted = true;
                        // Create memory of interaction with random valence
                        double interactionValence = 0.3 + (random.nextDouble() * 0.5); // 0.3 to 0.8
                        Memory interactionMemory = new Memory(action, System.currentTimeMillis(), true, interactionValence);
                        memories.add(interactionMemory);
                        emotionalState.updateFromAction(action);
                        
                        // Update social feedback
                        socialFeedback = Math.min(1.0, socialFeedback + 0.2);
                        break;
                    }
                }
                if (!interacted) {
                    // Create slightly negative memory for failed interaction
                    Memory failedInteractionMemory = new Memory(action, System.currentTimeMillis(), true, -0.2);
                    memories.add(failedInteractionMemory);
                    emotionalState.updateFromAction(action);
                }
                break;
        }
        
        // Create memory of movement if position changed
        if (!position.equals(previousPosition)) {
            Memory movementMemory = new Memory(action, System.currentTimeMillis(), true, 0.1);
            memories.add(movementMemory);
            emotionalState.updateFromAction(action);
        }
        
        // Decay social feedback over time
        socialFeedback *= 0.95;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public List<Memory> getMemories() {
        return memories;
    }

    public SelfModel getSelfModel() {
        return selfModel;
    }

    public EmotionalState getEmotionalState() {
        return emotionalState;
    }
} 