/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demokritos.iit.hyperCommunity;

/**
 *
 * @author Dimitrios
 */
public class CommunityStats {

    private double modularity;
    private double density;
    private double percentageNoise;
    private int numCommunities;
    private int eta;

    public void setEta (int newVal) {
        this.eta = newVal;
        
    }
    
    public void setModularity(double newVal) {
        this.modularity = newVal;
    }

    public void setDensity(double newVal) {
        this.density= newVal;
    }

    public void setNumCommunities(int newVal) {
        this.numCommunities = newVal;
    }

    public void setPercentageNoise(double newVal) {
        this.percentageNoise=newVal;
    }

    public double getModularity() {
        return modularity;

    }

    public double getDensity() {
        return density;
    }

    public int getNumCommunities() {
        return numCommunities;
    }

    public double getPercentageNoise() {
        return percentageNoise;
    }   

    public int getEta () {
        return eta;
    }
    
}
