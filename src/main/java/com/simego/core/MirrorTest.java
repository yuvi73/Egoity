package com.simego.core;

import java.util.ArrayList;
import java.util.List;

public class MirrorTest {
    private final Environment environment;
    private final List<Agent> testSubjects;
    private boolean isTestActive;
    private int testDuration;
    private int currentStep;
    private double recognitionThreshold;
    private List<Action> mirrorActions;

    public MirrorTest(Environment environment) {
        this.environment = environment;
        this.testSubjects = new ArrayList<>();
        this.isTestActive = false;
        this.testDuration = 50; // Number of steps for the test
        this.currentStep = 0;
        this.recognitionThreshold = 0.8;
        this.mirrorActions = new ArrayList<>();
    }

    public void startTest(Agent agent) {
        if (!isTestActive) {
            testSubjects.clear();
            testSubjects.add(agent);
            isTestActive = true;
            currentStep = 0;
            mirrorActions.clear();
            
            // Create a mirror reflection of the agent
            Position mirrorPosition = calculateMirrorPosition(agent.getPosition());
            Agent mirrorAgent = new Agent(mirrorPosition);
            testSubjects.add(mirrorAgent);
            
            // Add both agents to the environment
            environment.addAgent(agent);
            environment.addAgent(mirrorAgent);
            
            System.out.println("Mirror test started for agent at position: " + agent.getPosition());
        }
    }

    public void runTestStep() {
        if (!isTestActive) return;
        
        currentStep++;
        
        // Update both the real agent and its reflection
        for (Agent agent : testSubjects) {
            List<Stimulus> stimuli = environment.getStimuliAt(agent.getPosition());
            agent.perceive(stimuli);
            Action action = agent.decideAction();
            if (action != null) {
                mirrorActions.add(action);
                environment.applyAction(action);
            }
            agent.reflect();
        }
        
        // Check for self-recognition with enhanced criteria
        checkSelfRecognition();
        
        if (currentStep >= testDuration) {
            endTest();
        }
    }

    private void checkSelfRecognition() {
        Agent realAgent = testSubjects.get(0);
        Agent mirrorAgent = testSubjects.get(1);
        
        // Enhanced self-recognition criteria
        double selfConsistency = realAgent.getSelfModel().getSelfConsistency();
        double agency = realAgent.getSelfModel().getAgency();
        double selfRecognition = realAgent.getSelfModel().getSelfRecognition();
        
        // Calculate action mirroring score
        double mirroringScore = calculateMirroringScore();
        
        // Self-recognition is demonstrated when:
        // 1. High self-consistency
        // 2. High agency
        // 3. High self-recognition
        // 4. Low mirroring (agent doesn't blindly copy mirror's actions)
        boolean hasSelfRecognition = selfConsistency > recognitionThreshold &&
                                   agency > recognitionThreshold &&
                                   selfRecognition > recognitionThreshold &&
                                   mirroringScore < 0.3;
        
        if (hasSelfRecognition) {
            System.out.println("Agent has demonstrated self-recognition!");
            System.out.println("Self-Consistency: " + selfConsistency);
            System.out.println("Agency: " + agency);
            System.out.println("Self-Recognition: " + selfRecognition);
            System.out.println("Mirroring Score: " + mirroringScore);
        }
    }

    private double calculateMirroringScore() {
        if (mirrorActions.size() < 2) return 0.0;
        
        int matchingActions = 0;
        for (int i = 0; i < mirrorActions.size() - 1; i++) {
            Action action1 = mirrorActions.get(i);
            Action action2 = mirrorActions.get(i + 1);
            if (action1.getType() == action2.getType()) {
                matchingActions++;
            }
        }
        
        return (double) matchingActions / (mirrorActions.size() - 1);
    }

    private Position calculateMirrorPosition(Position originalPosition) {
        // Create a mirror position on the opposite side of the environment
        int mirrorX = environment.getWidth() - originalPosition.getX() - 1;
        int mirrorY = originalPosition.getY();
        return new Position(mirrorX, mirrorY);
    }

    private void endTest() {
        isTestActive = false;
        System.out.println("Mirror test completed.");
        
        // Remove test subjects from environment
        for (Agent agent : testSubjects) {
            environment.getAgents().remove(agent);
        }
    }

    public boolean isTestActive() {
        return isTestActive;
    }
} 