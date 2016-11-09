/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.problem;

import java.util.ArrayList;
import java.util.List;
import master.project.main.PublicData;

/**
 *
 * @author gillmylady
 * this is the ABC algorithm function
 */
public class AbcBasicAlgorithm {
    
    private int workerBeeNumber = 0;
    private int onlookerBeeNumber = 0;
    private int initialBestSolutionValue = 0;
    
    private List<Solution> solutions;
    private int soFarBestSolution;
    
    //constructor
    //we construct some initial solutions in here, according to the bees number
    public AbcBasicAlgorithm(int beeNumber, Instance instance){
        this.workerBeeNumber = beeNumber/2;
        this.onlookerBeeNumber = beeNumber - beeNumber/2 - 1;
        solutions = new ArrayList<>();
        soFarBestSolution = 0;
        for(int i = 0; i < this.workerBeeNumber; i++){
            Solution s = new Solution(instance, i);         //second argument is solution ID
            switch (i) {
                case 0:
                    s.greedySortSchedulesPriorityProcessTime(); //priority/processTime
                    s.naiveSolution();
                    break;
                case 1:
                    s.greedySortSchedulesPriority();            //priority
                    s.naiveSolution();
                    break;
                case 2:
                    s.greedySortSchedulesProcessTime();         //processTime
                    s.naiveSolution();
                    break;
                case 3:
                    s.greedySortSchedulesPriorityProcessTime(); //priority/processTime
                    s.constructiveShortDistanceSolution();//short distance
                    break;
                case 4:
                    s.greedySortSchedulesPriority(); //priority/processTime
                    s.constructiveShortDistanceSolution();//short distance
                    break;
                case 5:
                    s.greedySortSchedulesProcessTime(); //priority/processTime
                    s.constructiveShortDistanceSolution();//short distance
                    break;
                case 6:
                    s.greedySortSchedulesPriorityProcessTime(); //priority/processTime
                    s.constructiveMultiveHeuristicSolution();//short distance
                    break;
                case 7:
                    s.greedySortSchedulesPriority(); //priority/processTime
                    s.constructiveMultiveHeuristicSolution();//short distance
                    break;
                case 8:
                    s.greedySortSchedulesProcessTime(); //priority/processTime
                    s.constructiveMultiveHeuristicSolution();//short distance
                    break;
                default:
                    s.constructiveRandomSolution();     //random solution
                    break;
            }
            solutions.add(s);
        }
        
        for(int i = 0; i < this.onlookerBeeNumber; i++){
            Solution s = new Solution(instance, i + this.workerBeeNumber);      //the second argument is the solution ID
            s.constructiveRandomSolution();     //random solution
            solutions.add(s);
        }
        
        initialBestSolutionValue = getBestSolutionValue();                  //get initial best solution's total priority
    }
    
    /*
    totalRounds: how many rounds does this algorithm run? or later on we can add the feature, how much time does it run
    timeout, if totalRounds < 0, then we use timeout instead
    onlookerBeeExist: if true, onlookerBee exist, and it will be reset after specified rounds' failure to improve 
                        if false, it doesnt exist
    workerBeeAllowNotBackupWhenGetStucked, workerBee doesn't reset, but we allow the chance that if they add task with drop, leave some
                            probability that they dont recover dropped task
    allowExchange: if this algorithm allows exchange among each solution
    */
    public void RunBasicABCAlgorithm(int totalRounds, int timeout, boolean onlookerBeeExist, 
            boolean workerBeeAllowNotBackupWhenGetStucked,
            boolean allowExchange, boolean allowShrink){
        
        int currentRound = 0;
        int rdNum;
        int rdSchedule;
        int averagePrio;
        
        int[] eachPrio = new int[solutions.size()];
        
        //http://stackoverflow.com/questions/19727109/how-to-exit-a-while-loop-after-a-certain-time
        long startTime = System.currentTimeMillis();
        
        while((currentRound++) < totalRounds || (System.currentTimeMillis() - startTime) < timeout * 1000 ){
            
            //if need, we can print all solutions' value in each totalRounds, to see if it's improved
            //displayAllSolution(false);
            
            //this is the fitness function, probability choose, for neighbor selection
            eachPrio = PublicData.getSolutionFitness(solutions);
            RouletteWheel rw = new RouletteWheel(eachPrio);
            rdNum = rw.spin();                                              //this is the chosen solution (neighbor)
            rdSchedule = solutions.get(rdNum).getOneScheduledTask();        //this is the chosen task for neighbor selection 
            
            for(int sID = 0; sID < solutions.size(); sID++){
                if(sID == rdNum){   
                    //if this is who's chosen, don't do selection but do check if conflict (this feature can be removed 
                    //to make the whole test quicker, or we can add one boolean flag in the arguments)
                    ConflictTest ct = new ConflictTest(solutions.get(sID));
                    if(ct.testIfConflict() == true){
                        System.out.println("some schedules conflict!!!");
                        displayOneSolutionSortedSchedule(sID);
                        return;
                    }
                    continue;
                }
                
                //if this task is already scheduled, then skip it
                if(solutions.get(sID).isTaskScheduled(rdSchedule) == true)
                    continue;
                
                //if this task can be added without drop, perfect, reset the count
                if(solutions.get(sID).addOneTaskWithoutDrop(rdSchedule) == true){
                    solutions.get(sID).setCount(0);
                    continue;
                }
                
                //try if this task can be added with drop, leave some probability for not to recover the dropped task
                if(solutions.get(sID).addOneTaskWithDrop(rdSchedule, 
                        workerBeeAllowNotBackupWhenGetStucked && 
                        sID < this.workerBeeNumber &&
                        solutions.get(sID).getCount() > PublicData.workerBeeNotBackUp ) != null){ //PublicData.resetBeeCount
                    solutions.get(sID).setCount(0);
                    continue;
                }
                
                //if all methods fail, add the count
                solutions.get(sID).addCount();
                
            }
            
            //every 25 rounds, check those solutions which didnt change for a while so need to reset them.
            if(currentRound % 25 == 0){
                allSolutionsTryAdd();
                averagePrio = rw.getTotal() / eachPrio.length;
                    
                //if dont allow onlooker bee, then we compare each solution with average priority
                if(onlookerBeeExist == false){
                    for(int j = 0; j < eachPrio.length; j++){
                        if(solutions.get(j).getCount() > PublicData.resetBeeCount && solutions.get(j).totalPriority() <= averagePrio){
                            solutions.get(j).resetSolution();
                        }
                    }
                }else{  //if allow reset onlooker bee, start judge from the first onlooker bee
                    for(int j = this.workerBeeNumber; j < eachPrio.length; j++){
                        if(solutions.get(j).getCount() > PublicData.resetBeeCount){
                            solutions.get(j).resetSolution();
                        }
                    }
                }
            }
            
            //add exchange (one local search heuristic) for the solutions:
            if(allowExchange){
                solutionsTryExchange();
            }
            
            if(allowShrink){
                solutionsTryShrink();
            }
            
            storeSoFarBestSolutionValue();
        }
    }
    
    //solution try exchange, we dont care if it succeed here. If succeed, exchange is done.
    public void solutionsTryExchange(){
        for(Solution s : solutions){
            //if(s.getCount() > PublicData.resetBeeCount){      //if we want, we can add some constrints for exchange times
            //now, we just do exchange every time, since some exchange cannot succeed.
                s.exchangeTasksAmongTechnicians();
            //}
        }
    }
    
    //solution try shrink
    public void solutionsTryShrink(){
        for(Solution s : solutions){
            if(s.getCount() > PublicData.resetBeeCount){
                s.shrinkTasks();
                s.setCount(0);
            }
        }
    }
    
    //display all solutions' sorted tasks
    public void displayAllSolutionsSortedSchedule(){
        for(Solution s : solutions){
            System.out.printf("solution %d:\n", s.getID());
            for(Technician t : s.getSolution()){
                System.out.println(t.getSortedExecuteTimeSchedules());
            }
        }
    }
    
    //display specified solution's sorted tasks
    public void displayOneSolutionSortedSchedule(int solutionID){
        Solution s = solutions.get(solutionID);
        if(s == null)           //make sure this solutionID exist
            return;
        System.out.printf("solution %d:\n", s.getID());
        for(Technician t : s.getSolution()){
            System.out.println(t.getSortedExecuteTimeSchedules());
        }
    }
    
    //display all solution's total priority (also print solution detail in case)
    //argument: ifPringDetail, if true, then print solution's detail
    public void displayAllSolution(boolean ifPrintDetail){
        for(Solution s : solutions){
            System.out.printf("%d ", s.totalPriority());
            if(ifPrintDetail)
                System.out.println(s.solutionToString());
        }
        System.out.println();
    }
    
    //all solution try add any task
    public void allSolutionsTryAdd(){
        for(Solution s : solutions){
            if(s.tryAddFromUnscheduled() == true){
            }
        }
        storeSoFarBestSolutionValue();
    }
    
    //return all solutions' list for use
    public List<Solution> getSolutions(){
        return solutions;
    }
    
    //store initial best solution and use it for compare -> to see how much ABC improve the initial best solution
    public int getInitialBestSolutionValue(){
        return initialBestSolutionValue;
    }
    
    //store the best solution when trying abc
    public void storeSoFarBestSolutionValue(){
        int[] eachPrio =  PublicData.getSolutionFitness(solutions);
        for(int value : eachPrio){
            if(value > soFarBestSolution)
                soFarBestSolution = value;
        }
    }
    
    //get sofar best solution
    public int getSoFarBestSolutionValue(){
        return soFarBestSolution;
    }
    
    //get so-far the best solution's total priority
    public int getBestSolutionValue(){
        int bestV = 0;
        for(Solution s : solutions){
            if(s.totalPriority() > bestV){
                bestV = s.totalPriority();
            }
        }
        return bestV;
    }
}
