/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import master.project.main.PublicData;

/**
 *
 * @author gillmylady
 */
public class AbcBasicAlgorithm {
    
    private int totalBeeNumber = 0;
    private int workerBeeNumber = 0;
    private int onlookerBeeNumber = 0;
    private List<Solution> solutions;
    
    public AbcBasicAlgorithm(int beeNumber, Instance instance){
        this.totalBeeNumber = beeNumber;
        this.workerBeeNumber = beeNumber/2;
        this.onlookerBeeNumber = beeNumber - beeNumber/2 - 1;
        solutions = new ArrayList<>();
        for(int i = 0; i < this.workerBeeNumber; i++){
            Solution s = new Solution(instance, i);
            if(i == 0){         
                s.greedySortSchedulesPriorityProcessTime(); //priority/processTime
                s.naiveSolution();
            }else if(i == 1){
                s.greedySortSchedulesPriority();            //priority
                s.naiveSolution();
            }else if(i == 2){
                s.greedySortSchedulesProcessTime();         //processTime
                s.naiveSolution();
            }else if(i == 3){
                s.greedySortSchedulesPriorityProcessTime(); //priority/processTime
                s.constructiveShortDistanceSolution();//short distance
            }else if(i == 4){
                s.greedySortSchedulesPriority(); //priority/processTime
                s.constructiveShortDistanceSolution();//short distance
            }else if(i == 5){
                s.greedySortSchedulesProcessTime(); //priority/processTime
                s.constructiveShortDistanceSolution();//short distance
            }else{
                s.constructiveRandomSolution();     //random solution
            }
            solutions.add(s);
            
        }
        
        RunABCAlgorithm(100);
        
    }
    
    public void RunABCAlgorithm(int round){
        int i = 0;
        Random rd = new Random();
        int rdNum;
        int rdSchedule;
        int averagePrio;
        int[] eachPrio = new int[workerBeeNumber];
        while((i++) < round){
            
            displayAllSolution();
            
            rdNum = rd.nextInt(solutions.size());
            rdSchedule = solutions.get(rdNum).getOneScheduledTask();
            
            for(int sID = 0; sID < solutions.size(); sID++){
                if(sID == rdNum){
                    ConflictTest ct = new ConflictTest(solutions.get(sID));
                    if(ct.testIfConflict() == true){
                        System.out.println("some schedules conflict!!!");
                        return;
                    }
                    continue;
                }
                
                if(solutions.get(sID).isTaskScheduled(rdSchedule) == true)
                    continue;
                
                if(solutions.get(sID).addOneTaskWithoutDrop(rdSchedule) == true){
                    solutions.get(sID).setCount(0);
                    continue;
                }
                if(solutions.get(sID).addOneTaskWithDrop(rdSchedule) == true){
                    //System.out.println("solution improved");
                    solutions.get(sID).setCount(0);
                    continue;
                }
                solutions.get(sID).addCount();
                
            }
            
            if(i % 25 == 0){
                allSolutionsTryAdd();
            }
            
            
            //every 50 rounds, check those solutions which didnt change for a while so need to reset them.
            if(round % PublicData.resetBeeCount == 0){
                averagePrio = 0;
                for(int j = 0; j < eachPrio.length; j++){
                    eachPrio[j] = solutions.get(j).totalPriority();
                    averagePrio += eachPrio[j];
                }
                averagePrio /= eachPrio.length;
                
                for(int j = 0; j < eachPrio.length; j++){
                    if(solutions.get(j).getCount() > PublicData.resetBeeCount && solutions.get(j).totalPriority() < averagePrio){
                        solutions.get(j).resetSolution();
                        System.out.println("reset one solution");
                    }
                }
                
            }
        }
    }
    
    public Solution rouletteWheelProb(){
        int total = 0;
        int[] fit = new int[solutions.size()];
        for(int i = 0; i < solutions.size(); i++){
            fit[i] = solutions.get(i).totalPriority();
            total += fit[i];
        }
        Random r = new Random();
        int rv = r.nextInt(total);
        for(int i = 0; i < fit.length; i++){
            if(rv <= fit[i]){
                return solutions.get(i);
            }else{
                rv -= fit[i];
            }
        }
        return null;
    }
    
    public void displayAllSolution(){
        for(Solution s : solutions){
            System.out.printf("%d ", s.totalPriority());
            //System.out.println(s.solutionToString());
        }
        System.out.println();
    }
    
    public void allSolutionsTryAdd(){
        for(Solution s : solutions){
            //System.out.println(s.tryAddFromUnscheduled());
            //s.tryAddFromUnscheduled();
            //System.out.println(s.solutionToString());
            if(s.tryAddFromUnscheduled() == true){
              //  System.out.println("add one");
              //  System.out.println(s.solutionToString());
            }
        }
    }
    
    public List<Solution> getSolutions(){
        return solutions;
    }
}
