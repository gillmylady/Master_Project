/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.problem;

//http://stackoverflow.com/questions/298301/roulette-wheel-selection-algorithm

import java.util.Random;

/**
 *
 * @author gillmylady
 */
public class RouletteWheel {
    int[] fitness;
    int total;
    Random random;
    
    public RouletteWheel(int[] fitness){
        random = new Random();
        total = 0;
        this.fitness = new int[fitness.length + 1];
        this.fitness[0] = 0;
        for(int i = 0; i < fitness.length; i++){
            this.fitness[i+1] = this.fitness[i] + fitness[i];
            total += fitness[i];
        }
    }
    
    public int spin(){
        
        int r = random.nextInt(this.total);
        int left = 0;
        int right = fitness.length - 1;
        int mid;
        while(right - left > 1){
            mid = (left + right) / 2;
            if(fitness[mid] > r)
                right = mid;
            else
                left = mid;
        }
        return left;
    }
    
    public int getTotal() { return total; }
}
