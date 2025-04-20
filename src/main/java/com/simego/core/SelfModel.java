package com.simego.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelfModel {
    private static final int MAX_MEMORIES = 1000;
    private static final int MAX_BELIEFS = 500;
    private static final long MEMORY_LIFETIME_MS = 30000; // 30 seconds
    
    private List<Belief> beliefs;
    private List<Goal> currentGoals;
    private double selfConsistency; // 0.0 to 1.0
    private double agency; // 0.0 to 1.0
    private double selfRecognition; // 0.0 to 1.0
    private List<Memory> selfMemories;
    private Random random;
    private double learningRate;
    private int inconsistentActions; // Track inconsistent actions
    private int totalActions; // Track total actions
    private double mirroring; // Add mirroring field
    private int stepsToRecognition; // Add steps to recognition field
    private double recognitionThreshold; // Add recognition threshold
    private final List<MirroringMemory> mirroringMemories;
    private int successfulImitationCount; // Track successful imitations
    private double socialFeedback; // Track social feedback
    private static final double MIRRORING_LEARNING_RATE = 0.3;
    private static final double SOCIAL_FEEDBACK_DECAY = 0.95;

    public SelfModel() {
        this.beliefs = new ArrayList<>();
        this.currentGoals = new ArrayList<>();
        this.selfConsistency = 0.5; // Start at 0.5
        this.agency = 0.5;
        this.selfRecognition = 0.0;
        this.selfMemories = new ArrayList<>();
        this.random = new Random();
        this.learningRate = 0.1;
        this.inconsistentActions = 0;
        this.totalActions = 0;
        this.mirroring = 0.0;
        this.stepsToRecognition = 0;
        this.recognitionThreshold = 0.7;
        this.mirroringMemories = new ArrayList<>();
        this.successfulImitationCount = 0;
        this.socialFeedback = 0.0;
        
        // Add initial goals
        addInitialGoals();
    }

    private void addInitialGoals() {
        currentGoals.add(new Goal(Goal.GoalType.EXPLORE, null, 0.8));
        currentGoals.add(new Goal(Goal.GoalType.FIND_FOOD, null, 0.7));
        currentGoals.add(new Goal(Goal.GoalType.AVOID_DANGER, null, 0.9));
    }

    public void update(List<Memory> memories) {
        // Clean up old memories and beliefs
        cleanupOldMemories();
        cleanupOldBeliefs();
        
        // Update beliefs based on recent memories
        for (Memory memory : memories) {
            if (selfMemories.size() < MAX_MEMORIES) {
                updateAgency(memory);
                if (memory.isSelfGenerated()) {
                    selfMemories.add(memory);
                }
                if (beliefs.size() < MAX_BELIEFS) {
                    updateBeliefs(memory);
                }
                updateMirroring(memory);
            }
        }
        
        // Update self-consistency based on belief coherence
        updateSelfConsistency();
        
        // Update self-recognition based on self-memories and agency
        updateSelfRecognition();
    }

    private void cleanupOldMemories() {
        long currentTime = System.currentTimeMillis();
        selfMemories.removeIf(memory -> 
            currentTime - memory.getTimestamp() > MEMORY_LIFETIME_MS
        );
    }
    
    private void cleanupOldBeliefs() {
        // Remove beliefs that are too old or have low confidence
        beliefs.removeIf(belief -> {
            long currentTime = System.currentTimeMillis();
            return currentTime - belief.getTimestamp() > MEMORY_LIFETIME_MS ||
                   belief.getConfidence() < 0.2;
        });
    }

    private void updateAgency(Memory memory) {
        // More dynamic agency updates based on memory valence
        double valence = memory.getValence();
        double update = learningRate * valence;
        
        if (valence > 0) {
            // Positive experiences increase agency more
            agency = Math.min(1.0, agency + update * 1.5);
        } else if (valence < 0) {
            // Negative experiences decrease agency less
            agency = Math.max(0.0, agency + update * 0.5);
        }
        
        // Add more randomness to make it more dynamic
        agency += (random.nextDouble() - 0.5) * 0.1;
        agency = Math.max(0.0, Math.min(1.0, agency));
    }

    private void updateBeliefs(Memory memory) {
        // Create new beliefs or update existing ones based on memory content
        Belief newBelief = new Belief(memory);
        if (beliefs.size() < MAX_BELIEFS) {
            beliefs.add(newBelief);
        }
        
        // Set initial valence based on the type of memory
        if (memory.getContent() instanceof Stimulus) {
            Stimulus stimulus = (Stimulus) memory.getContent();
            switch (stimulus.getType()) {
                case FOOD:
                    memory = new Memory(memory.getContent(), memory.getTimestamp(), memory.isSelfGenerated(), 0.8);
                    break;
                case DANGER:
                    memory = new Memory(memory.getContent(), memory.getTimestamp(), memory.isSelfGenerated(), -0.9);
                    break;
                case OTHER_AGENT:
                    memory = new Memory(memory.getContent(), memory.getTimestamp(), memory.isSelfGenerated(), 0.3);
                    break;
                default:
                    memory = new Memory(memory.getContent(), memory.getTimestamp(), memory.isSelfGenerated(), 0.0);
            }
        }
    }

    private void updateSelfConsistency() {
        if (beliefs.isEmpty()) {
            selfConsistency = 0.5;
            return;
        }
        
        // Calculate consistency based on multiple factors
        double actionConsistency = calculateActionConsistency();
        double goalConsistency = calculateGoalConsistency();
        double beliefConsistency = calculateBeliefConsistency();
        
        // Weighted combination of different consistency measures
        selfConsistency = (actionConsistency * 0.4 + 
                          goalConsistency * 0.3 + 
                          beliefConsistency * 0.3);
        
        // Add some randomness to make it more dynamic
        double noise = (random.nextDouble() - 0.5) * 0.1;
        selfConsistency = Math.max(0.0, Math.min(1.0, selfConsistency + noise));
    }

    private double calculateActionConsistency() {
        if (totalActions == 0) return 0.5;
        return 1.0 - ((double) inconsistentActions / totalActions);
    }

    private double calculateGoalConsistency() {
        if (currentGoals.isEmpty()) return 0.5;
        
        int consistentGoals = 0;
        for (Goal goal : currentGoals) {
            if (isGoalConsistent(goal)) {
                consistentGoals++;
            }
        }
        return (double) consistentGoals / currentGoals.size();
    }

    private double calculateBeliefConsistency() {
        if (beliefs.isEmpty()) return 0.5;
        
        int consistentBeliefs = 0;
        for (Belief belief : beliefs) {
            if (isBeliefConsistent(belief)) {
                consistentBeliefs++;
            }
        }
        return (double) consistentBeliefs / beliefs.size();
    }

    private boolean isGoalConsistent(Goal goal) {
        // A goal is consistent if it doesn't conflict with other goals
        for (Goal otherGoal : currentGoals) {
            if (goal != otherGoal && goalsConflict(goal, otherGoal)) {
                return false;
            }
        }
        return true;
    }

    private boolean goalsConflict(Goal goal1, Goal goal2) {
        // Define conflicts between different goal types
        if (goal1.getType() == Goal.GoalType.FIND_FOOD && 
            goal2.getType() == Goal.GoalType.AVOID_DANGER) {
            return true;
        }
        if (goal1.getType() == Goal.GoalType.SOCIALIZE && 
            goal2.getType() == Goal.GoalType.AVOID_DANGER) {
            return true;
        }
        return false;
    }

    private boolean isBeliefConsistent(Belief belief) {
        // A belief is consistent if it aligns with the agent's goals and previous experiences
        if (belief.source.getContent() instanceof Action) {
            Action action = (Action) belief.source.getContent();
            totalActions++;
            boolean consistent = isActionConsistentWithGoals(action);
            if (!consistent) {
                inconsistentActions++;
            }
            return consistent;
        }
        return true;
    }

    private boolean isActionConsistentWithGoals(Action action) {
        for (Goal goal : currentGoals) {
            switch (goal.getType()) {
                case FIND_FOOD:
                    if (action.getType() == Action.ActionType.EAT) return true;
                    break;
                case AVOID_DANGER:
                    if (action.getType() == Action.ActionType.AVOID) return true;
                    break;
                case EXPLORE:
                    if (action.getType() == Action.ActionType.MOVE_UP ||
                        action.getType() == Action.ActionType.MOVE_DOWN ||
                        action.getType() == Action.ActionType.MOVE_LEFT ||
                        action.getType() == Action.ActionType.MOVE_RIGHT) return true;
                    break;
                case SOCIALIZE:
                    if (action.getType() == Action.ActionType.INTERACT) return true;
                    break;
            }
        }
        return false;
    }

    private void updateSelfRecognition() {
        // More sophisticated self-recognition calculation
        double memoryFactor = Math.min(1.0, selfMemories.size() / 10.0);
        double agencyFactor = agency;
        double consistencyFactor = selfConsistency;
        double mirroringFactor = mirroring;
        
        // Add interaction factor based on social memories
        double socialFactor = calculateSocialFactor();
        
        // Weighted combination of factors
        selfRecognition = (memoryFactor * 0.25 + 
                          agencyFactor * 0.25 + 
                          consistencyFactor * 0.2 + 
                          socialFactor * 0.15 +
                          mirroringFactor * 0.15);
        
        // Add more randomness
        selfRecognition += (random.nextDouble() - 0.5) * 0.1;
        selfRecognition = Math.max(0.0, Math.min(1.0, selfRecognition));

        // Make recognition threshold dynamic based on agent's development
        recognitionThreshold = 0.5 + (0.3 * (1.0 - Math.min(1.0, totalActions / 1000.0)));

        // Update steps to recognition
        if (selfRecognition >= recognitionThreshold && stepsToRecognition == 0) {
            stepsToRecognition = totalActions;
        }
    }

    private double calculateSocialFactor() {
        if (mirroringMemories.isEmpty()) return 0.0;
        
        double totalValence = 0.0;
        for (MirroringMemory memory : mirroringMemories) {
            totalValence += memory.getOriginalMemory().getValence();
        }
        
        return Math.max(0.0, Math.min(1.0, totalValence / mirroringMemories.size()));
    }

    private double calculateDynamicLearningRate() {
        // Base learning rate increases with successful imitations
        double baseRate = 0.3 + (successfulImitationCount * 0.01);
        // Learning rate decreases with high mirroring (diminishing returns)
        double diminishingFactor = 1.0 - (mirroring * 0.3);
        // Add some randomness
        double randomFactor = (random.nextDouble() - 0.5) * 0.05;
        return Math.min(0.5, Math.max(0.1, baseRate * diminishingFactor + randomFactor));
    }

    private double calculateDynamicThreshold() {
        // Threshold becomes more lenient with more social experience
        double baseThreshold = 0.7;
        double socialExperienceFactor = Math.min(1.0, successfulImitationCount / 10.0);
        double thresholdAdjustment = socialExperienceFactor * 0.2;
        return Math.max(0.5, baseThreshold - thresholdAdjustment);
    }

    private void updateMirroring(Memory memory) {
        if (memory.getType() == Memory.MemoryType.SOCIAL) {
            double delta = memory.getValence() * 0.2;
            mirroring = Math.min(1.0, Math.max(0.0, mirroring + delta));
        }
    }

    private double calculateRecentSuccessRate() {
        if (mirroringMemories.isEmpty()) return 0.0;
        
        int recentSuccesses = 0;
        int totalRecent = 0;
        long currentTime = System.currentTimeMillis();
        
        for (MirroringMemory memory : mirroringMemories) {
            if (currentTime - memory.getTimestamp() < 5000) { // Last 5 seconds
                totalRecent++;
                if (memory.getUpdateValue() > 0) {
                    recentSuccesses++;
                }
            }
        }
        
        return totalRecent > 0 ? (double) recentSuccesses / totalRecent : 0.0;
    }

    private void storeMirroringMemory(Memory memory, double updateValue, double learningRate, double threshold) {
        MirroringMemory mirroringMemory = new MirroringMemory(
            memory,
            updateValue,
            System.currentTimeMillis(),
            learningRate,
            threshold
        );
        mirroringMemories.add(mirroringMemory);
        
        // Keep only recent memories (last 100)
        if (mirroringMemories.size() > MAX_MEMORIES) {
            mirroringMemories.remove(0);
        }
    }

    public void updateSocialFeedback(double feedback) {
        // Update social feedback with decay
        socialFeedback = socialFeedback * SOCIAL_FEEDBACK_DECAY + feedback * (1.0 - SOCIAL_FEEDBACK_DECAY);
    }

    public double getSelfConsistency() {
        return selfConsistency;
    }

    public double getAgency() {
        return agency;
    }

    public double getSelfRecognition() {
        return selfRecognition;
    }

    public List<Belief> getBeliefs() {
        return beliefs;
    }

    public List<Goal> getCurrentGoals() {
        return currentGoals;
    }

    public double getMirroring() {
        return mirroring;
    }

    public int getStepsToRecognition() {
        return stepsToRecognition;
    }

    public int getSuccessfulImitationCount() {
        return successfulImitationCount;
    }

    public double getSocialFeedback() {
        return socialFeedback;
    }

    public List<MirroringDataPoint> getMirroringVisualizationData() {
        List<MirroringDataPoint> data = new ArrayList<>();
        for (MirroringMemory memory : mirroringMemories) {
            data.add(new MirroringDataPoint(
                memory.getTimestamp(),
                memory.getUpdateValue(),
                memory.getLearningRate(),
                memory.getThreshold(),
                memory.getOriginalMemory().getValence()
            ));
        }
        return data;
    }

    public static class MirroringDataPoint {
        private final long timestamp;
        private final double updateValue;
        private final double learningRate;
        private final double threshold;
        private final double valence;

        public MirroringDataPoint(long timestamp, double updateValue, double learningRate, 
                                double threshold, double valence) {
            this.timestamp = timestamp;
            this.updateValue = updateValue;
            this.learningRate = learningRate;
            this.threshold = threshold;
            this.valence = valence;
        }

        public long getTimestamp() { return timestamp; }
        public double getUpdateValue() { return updateValue; }
        public double getLearningRate() { return learningRate; }
        public double getThreshold() { return threshold; }
        public double getValence() { return valence; }
    }

    private static class Belief {
        private final Memory source;
        private final String content;
        private final double confidence;

        public Belief(Memory source) {
            this.source = source;
            this.content = generateContent(source);
            this.confidence = 0.5; // Initial confidence
        }

        private String generateContent(Memory memory) {
            // Generate a belief statement based on the memory
            if (memory.getContent() instanceof Action) {
                return "I performed an action";
            } else if (memory.getContent() instanceof Stimulus) {
                return "I experienced a stimulus";
            }
            return "I have a memory";
        }

        public long getTimestamp() {
            return source.getTimestamp();
        }

        public double getConfidence() {
            return confidence;
        }
    }

    private static class MirroringMemory {
        private final Memory originalMemory;
        private final double updateValue;
        private final long timestamp;
        private final double learningRate;
        private final double threshold;

        public MirroringMemory(Memory originalMemory, double updateValue, long timestamp, 
                              double learningRate, double threshold) {
            this.originalMemory = originalMemory;
            this.updateValue = updateValue;
            this.timestamp = timestamp;
            this.learningRate = learningRate;
            this.threshold = threshold;
        }

        public Memory getOriginalMemory() {
            return originalMemory;
        }

        public double getUpdateValue() {
            return updateValue;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public double getLearningRate() {
            return learningRate;
        }

        public double getThreshold() {
            return threshold;
        }
    }

    public void setSelfConsistency(double selfConsistency) {
        this.selfConsistency = Math.max(0.0, Math.min(1.0, selfConsistency));
    }
    
    public void setAgency(double agency) {
        this.agency = Math.max(0.0, Math.min(1.0, agency));
    }
    
    public void setMirroring(double mirroring) {
        this.mirroring = Math.max(0.0, Math.min(1.0, mirroring));
    }

    public void updateFromMemory(Memory memory) {
        if (memory.getType() == Memory.MemoryType.SOCIAL) {
            // Update mirroring based on social memory valence
            double mirroringDelta = memory.getValence() * 0.2;
            mirroring = Math.min(1.0, Math.max(0.0, mirroring + mirroringDelta));
            
            // Successful social interactions increase self-recognition
            if (memory.getValence() > 0.5) {
                selfRecognition += 0.1;
                selfRecognition = Math.min(1.0, selfRecognition);
            }
        }
    }
} 