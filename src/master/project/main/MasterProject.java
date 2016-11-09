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
        
        //剩下可以做的: shrink tasks and see if solutions can be improved
        
        //before test, please make sure if i'm at sun-lab or own computer
        
        //RunAllInstancesInSameCaseWithLimitedTime();
        
        //RunAllInstancesInLimitedRounds();
        
        for(int i = 1; i <= 20; i++){
            String key = "R_12_" + i;
            runOneInstanceInLimitedRounds(key);
        }
        //RunAllInstancesInLimitedRounds();
        
        return;
        
        
    }
    
    //run ABC algorithm, each instance is given limited time
    public static void RunAllInstancesInSameCaseWithLimitedTime() throws FileNotFoundException, UnsupportedEncodingException {
        String[] instanceType = {"R", "C", "RC", "RAD"};
        int caseNumber = 13;
        int instanceNumber = 20;
        ReferredResult result = new ReferredResult();
        
        LogFile log = new LogFile("log_ReferedTimeout.txt");
        
        for(int caseN = 1; caseN <= caseNumber; caseN++){
            
            ResultAnalysis analysis = new ResultAnalysis("analysis_" + caseN + ".txt");
        
            for(int instType = 0; instType < instanceType.length; instType++){
                for(int instN = 1; instN <= instanceNumber; instN++){
                    String key = instanceType[instType] + "_" + caseN + "_" + instN;
                    if(key.equalsIgnoreCase("R_13_1") || key.equalsIgnoreCase("RC_13_7"))    //these two instances error, something in the instance incorrect
                        continue;
                    
                    String fileName = null;
                    if(PublicData.AmIAtSublab){
                        fileName = PublicData.sunlabInstancePath + key + ".txt";
                    }else{
                        fileName = PublicData.homeInstancePath + key + ".txt";
                    }
                    
                    Instance ss = new Instance(fileName);
                    
                    log.writeFile(PublicData.printTime() + "\n");
                    
                    AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber46, ss);
                    
                    //run the rounds in limited time
                    abc.RunBasicABCAlgorithm(-1, PublicData.runLimitTime[caseN], true, true, true, false);
                    
                    String logBuf = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue()
                            + ", referredResult=" + result.valueOfKey(key) + ", improveABC=" + (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                            + ", gap=" + (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()) + "\n";
                    log.writeFile(logBuf);
                    analysis.insertOneResultAnalysis(key, (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                                                        , (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()));
                    
                    //after all, we check again if improved solutions are invalid
                    if(abc.getSoFarBestSolutionValue() > abc.getInitialBestSolutionValue()){
                        for(Solution s: abc.getSolutions()){
                            ConflictTest ct = new ConflictTest(s);
                            if(ct.testIfConflict() == true){
                                System.out.println("some schedules conflict!!!");
                                return;
                            }
                        }
                    }  
                   
                }
            }
            analysis.endResultAnalysis();
        }
        
        log.closeFile();
        
    }
    
    //run all instances in limited rounds, run all different distributions
    public static void RunAllInstancesInLimitedRounds(){
        try {
            RunAllInstancesInLimitedRounds("R");
            RunAllInstancesInLimitedRounds("C");
            RunAllInstancesInLimitedRounds("RC");
            RunAllInstancesInLimitedRounds("RAD");
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        
    }
    
    //run ABC algorithm, each instance is given limited rounds
    //argument, String key_distribution, start from {"R", "C", "RC", "RAD"}, (each round takes long time)
    public static void RunAllInstancesInLimitedRounds(String key_distribution) throws FileNotFoundException, UnsupportedEncodingException {
        
        int caseNumber = 13;
        int instanceNumber = 20;
        ReferredResult result = new ReferredResult();
        
        LogFile log = new LogFile("log_ReferedTimeout.txt");
        ResultAnalysis totalAnalysis = new ResultAnalysis("analysis_" + key_distribution + ".txt");
        
        for(int caseN = 1; caseN <= caseNumber; caseN++){
            
            ResultAnalysis analysis = new ResultAnalysis("analysis1109_" + key_distribution + "_" + caseN + ".txt");
        
            for(int instN = 1; instN <= instanceNumber; instN++){
                String key = key_distribution + "_" + caseN + "_" + instN;
                if(key.equalsIgnoreCase("R_13_1") || key.equalsIgnoreCase("RC_13_7"))    //these two instances error, something in the instance incorrect
                    continue;

                String fileName = null;
                if(PublicData.AmIAtSublab){
                    fileName = PublicData.sunlabInstancePath + key + ".txt";
                }else{
                    fileName = PublicData.homeInstancePath + key + ".txt";
                }

                Instance ss = new Instance(fileName);

                log.writeFile(PublicData.printTime() + "\n");

                AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber46, ss);

                if(caseN < 13)
                    abc.RunBasicABCAlgorithm(caseN * 1000, -1, true, true, true, true);
                else
                    abc.RunBasicABCAlgorithm(caseN * 600, -1, true, true, true, false);
                
                String logBuf = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue()
                        + ", referredResult=" + result.valueOfKey(key) + ", improveABC=" + (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                        + ", gap=" + (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()) + "\n";
                log.writeFile(logBuf);
                analysis.insertOneResultAnalysis(key, (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                                                    , (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()));

                totalAnalysis.insertOneResultAnalysis(key, (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                                                    , (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()));

                //after all, we check again if improved solutions are invalid
                if(abc.getSoFarBestSolutionValue() > abc.getInitialBestSolutionValue()){
                    for(Solution s: abc.getSolutions()){
                        ConflictTest ct = new ConflictTest(s);
                        if(ct.testIfConflict() == true){
                            System.out.println("some schedules conflict!!!");
                            return;
                        }
                    }
                }  
            }
            analysis.endResultAnalysis();
        }
        totalAnalysis.endResultAnalysis();
        log.closeFile();
    }
    
    //test one instance
    public static void runOneInstanceInLimitedRounds(String key) throws FileNotFoundException, UnsupportedEncodingException {
        
        if(key.equalsIgnoreCase("R_13_1") || key.equalsIgnoreCase("RC_13_7"))    //these two instances error, something in the instance incorrect
            return;

        String fileName = null;
        if(PublicData.AmIAtSublab){
            fileName = PublicData.sunlabInstancePath + key + ".txt";
        }else{
            fileName = PublicData.homeInstancePath + key + ".txt";
        }

        Instance ss = new Instance(fileName);

        AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber46, ss);

        abc.RunBasicABCAlgorithm(3000, -1, true, true, true, true);
        
        String logBuf = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue() + "\n";
        System.out.print(logBuf);
        //abc.displayAllSolutionsSortedSchedule();
        abc.displayAllSolution(false);
        /*
        abc.solutionsTryShrink();
        
        abc.allSolutionsTryAdd();
        
        //abc.displayAllSolutionsSortedSchedule();
        abc.displayAllSolution(false);
        String logBuf2 = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue() + "\n";
        System.out.print(logBuf2);
*/
    }
}
