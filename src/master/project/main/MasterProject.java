/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.main;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import master.project.problem.*;
/**
 *
 * @author gillmylady
 */

/*
1. add test features, try:
    a. if one technician has conflict among scheduled tasks
    b. if one task is scheduled more than once
2. gap of technician, 压缩, to try schedule more tasks
3. exchange two schedules in two technicians
3. Now, set executeTime <= endTime - processTime
4. consider if allowing backup, since sometimes dont backup and we will jump out local optimal solution


Refer: Effective Heuristic Procedures for a FTSP

develop:
1. basic ABC, either allowing or not allowing onlooker bee
2. ABC + xxxxx, allow swap and exchange
3. ABC + yyyyy, allow shortest technician
4. ABC + zzzzz, allow shrink time of each technician (try move backward)

conclusion: increasing number of bees might not help, because more steps are not helpful. (random solution too bad)


10.26 update:
1.  leave some probablity for solution not restore back-up when trying add one task
        -> done for workerBee with some probability
2.  try another way of probability selection method, like, select only from limited/restricted pool where only a certain percentange of 
        the individuals are allowed based on fitness value
3.  see smaller/larger size's result, how good or bad are they respectively
        -> done, now we parse the result file and print the result and see how much ABC improve the solution
4.  try constructive method, 
        a.  use multiple heuristic methods and assign them different probability, randomly pick (roulette wheel)
            -> exchange is done
            -> swap = neighbor selection is done
            -> change, one task scheduled by t1 was changed to t2
                   This might not be used when initial solution is generated, it can be used for constructive solution's use
        b.  try add and replace and other local methods <---|
5.  some bees run using this method, some using another method
        -> run differents method parallelly.
6.  split big instance to smaller ones, to speed up calculation time
*/

public class MasterProject {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        
        
        String[] instanceType = {"R", "C", "RC", "RAD"};
        int caseNumber = 13;
        int instanceNumber = 20;
        ReferredResult result = new ReferredResult();
        for(int instType = 0; instType < instanceType.length; instType++){
            for(int caseN = 1; caseN <= caseNumber; caseN++){
                for(int instN = 1; instN <= instanceNumber; instN++){
                    String key = instanceType[instType] + "_" + caseN + "_" + instN;
                    if(key.equalsIgnoreCase("R_13_1") || key.equalsIgnoreCase("RC_13_7"))    //these two instances error, something in the instance incorrect
                        continue;
                    
        
                    //sun-lab "/home/hfw5079/NetBeansProjects/Master_Project/instances/FTSP_"
                    //my PC: "/Users/gillmylady/NetBeansProjects/Master_Project/instances/FTSP_"
                    //String fileName = "/home/hfw5079/NetBeansProjects/Master_Project/instances/FTSP_" + key + ".txt";
                    String fileName = null;
                    if(PublicData.AmIAtSublab){
                        fileName = PublicData.sunlabInstancePath + key + ".txt";
                    }else{
                        fileName = PublicData.homeInstancePath + key + ".txt";
                    }
                    
                    Instance ss = new Instance(fileName);
                    
                    if(caseN < 12)
                        continue;
                    
                    AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber20, ss);
                    if(caseN < 10)
                        abc.RunBasicABCAlgorithm(caseN * 2000, true, false, false);
                    else
                        abc.RunBasicABCAlgorithm(caseN * 500, true, true, true);
                    System.out.printf("%s: bestBeforeABC=%d, bestAfterABC=%d, referredResult=%d, improveABC=%d\n", key, abc.getInitialBestSolutionValue(), 
                            abc.getBestSolutionValue(), result.valueOfKey(key), abc.getBestSolutionValue() - abc.getInitialBestSolutionValue());
                    
                    if(abc.getBestSolutionValue() > abc.getInitialBestSolutionValue()){
                        for(Solution s: abc.getSolutions()){
                            ConflictTest ct = new ConflictTest(s);
                            if(ct.testIfConflict() == true){
                                System.out.println("some schedules conflict!!!");
                                return;
                            }
                        }
                        
                        //see sorted tasks if need
                        //abc.displayEachSolutionSortedSchedule();
                        //System.out.printf("\n\n");
                    }  
                    
                    //add test of exchange
                    /*
                    int testExchange = 0;
                    boolean exchangeSucc = false;
                    int printFail = 0;
                    int solutionID = 8;
                    abc.displayOneSolutionSortedSchedule(solutionID);
                    while((testExchange++) < 100){
                        Solution s = abc.getSolutions().get(solutionID);
                        exchangeSucc = s.exchangeTasksAmongTechnicians();
                        if(exchangeSucc == true){
                            System.out.println("true!!!!!!!!");
                            abc.displayOneSolutionSortedSchedule(solutionID);
                        }else if(printFail < 5){
                            System.out.println("fail!!!!!!!!");
                            abc.displayOneSolutionSortedSchedule(solutionID);
                            printFail++;
                        }
                        System.out.println();
                        
                    }
                    return;
                    */
                    
                }
            }
        }
        
        /*
        Instance ss = new Instance("/Users/gillmylady/NetBeansProjects/Master_Project/instances/FTSP_R_13_3.txt");
        System.out.println(ss.getTaskNumber());
        System.out.println();
        
        AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber20, ss);
        abc.RunBasicABCAlgorithm(500, false);
        abc.displayEachSolutionSortedSchedule();
        */
        
        /*
        ReferredResult r = new ReferredResult();
        
        System.out.println(r.valueOfKey("R_10_12"));
        System.out.println(r.valueOfKey("C_10_12"));
        System.out.println(r.valueOfKey("RAD_10_12"));
        System.out.println(r.valueOfKey("RC_10_12"));
        */
        
        //abc.getSolutions().get(6).getSolution().get(0).sortExecuteTime();
        
        //System.out.println(abc.getSolutions().get(6).getSolution().get(0).getSortedExecuteTimeSchedules());
        
        /*abc.displayAllSolution();
        
        abc.allSolutionsTryAdd(ss);
        abc.displayAllSolution();
        */
        
        
    }
    
}
