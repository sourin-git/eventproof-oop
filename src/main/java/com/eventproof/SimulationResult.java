package com.eventproof;

public class SimulationResult {
    private final int originalScore;
    private final int simulatedScore;
    private final RiskLevel riskLevel;
    private final String recommendation;

    public SimulationResult(int originalScore, int simulatedScore, RiskLevel riskLevel, String recommendation) {
        this.originalScore = originalScore;
        this.simulatedScore = simulatedScore;
        this.riskLevel = riskLevel;
        this.recommendation = recommendation;
    }

    public int getOriginalScore() { return originalScore; }
    public int getSimulatedScore() { return simulatedScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public String getRecommendation() { return recommendation; }
}
