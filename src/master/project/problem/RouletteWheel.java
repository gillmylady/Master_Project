/*

Roulette wheel probability selection class

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
    
    //constructor
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
    
    //spin and get the random value's index
    public int spin(){
        
        if(this.total == 0)     //in case total = 0, to avoid error happen
            return -1;
        
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
