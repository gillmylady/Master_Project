/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.main;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        

1.	try constructive heuristic
            a.	add
            b.	swap and add
            c.	exchange and add
            d.	shrink
        -> try some other object function, and compare result, do probabily selection
			
2.	try shrink and not shrink, the difference
        -> done we can compare now
3.	during neighbor selection,
            1.	try exchange one whole technician's tasks
            2.	try exchange or swap 3 or 5 tasks
        -> done 1st method, 2th method is no difference with 3 or 5 rounds, takes more time

*/

public class MasterProject {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        
        //剩下可以做的: shrink tasks and see if solutions can be improved
        
        //before test, please make sure if i'm at sun-lab or own computer
        
        
        //RunAllInstancesInLimitedRounds();
        
        /*
        for(int i = 1; i <= 20; i++){
            String key = "C_3_" + i;
            runOneInstanceInLimitedRounds(key, false);
            runOneInstanceInLimitedRounds(key, true);
            System.out.println();
        }
        */
        
        //RunAllInstancesInLimitedRounds();
        
        RunEachInstanceWithDifferentOption(-1);
        
        
    }
    
    //run ABC algorithm, each instance is given limited time
    public static void RunAllInstancesInSameCaseWithLimitedTime(
            int caseNumber,
            boolean onlookerBeeExist, 
            boolean workerBeeAllowNotBackupWhenGetStucked,
            boolean allowExchange, boolean allowShrink, 
            boolean allowExchangeWholeTechnician) throws FileNotFoundException, UnsupportedEncodingException {
        String[] instanceType = {"R", "C", "RC", "RAD"};
        int instanceNumber = 20;
        ReferredResult result = new ReferredResult();
        
        String logFileName = PublicData.printTime();
        
        LogFile log = new LogFile(logFileName + "_log.txt");
        
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
                    abc.RunBasicABCAlgorithm(-1, PublicData.runLimitTime[caseN], onlookerBeeExist, 
                            workerBeeAllowNotBackupWhenGetStucked, allowExchange, allowShrink, allowExchangeWholeTechnician);
                    
                    String logBuf = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue()
                            + ", referredResult=" + result.valueOfKey(key) + ", improveABC=" + (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                            + ", gap=" + (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()) + "\n";
                    log.writeFile(logBuf);
                    analysis.insertOneResultAnalysis(key, (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                                                        , (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()));
                    
                    
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
                    abc.RunBasicABCAlgorithm(caseN * 1000, -1, true, true, true, true, false);
                else
                    abc.RunBasicABCAlgorithm(caseN * 600, -1, true, true, true, false, false);
                
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
    public static void runOneInstanceInLimitedRounds(
            String key, 
            boolean onlookerBeeExist, 
            boolean workerBeeAllowNotBackupWhenGetStucked,
            boolean allowExchange, 
            boolean allowShrink, 
            boolean allowExchangeWholeTechnician) throws FileNotFoundException, UnsupportedEncodingException {
        
        if(key.equalsIgnoreCase("R_13_1") || key.equalsIgnoreCase("RC_13_7"))    //these two instances error, something in the instance incorrect
            return;

        ReferredResult result = new ReferredResult();
        
        String fileName = null;
        if(PublicData.AmIAtSublab){
            fileName = PublicData.sunlabInstancePath + key + ".txt";
        }else{
            fileName = PublicData.homeInstancePath + key + ".txt";
        }

        Instance ss = new Instance(fileName);

        AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber46, ss);

        abc.RunBasicABCAlgorithm(-1, PublicData.runLimitTime[13], onlookerBeeExist, 
            workerBeeAllowNotBackupWhenGetStucked,
            allowExchange, allowShrink, 
            allowExchangeWholeTechnician);
        
        String logBuf = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue() 
                + " ,referedResult=" + result.valueOfKey(key) + "\n";
        System.out.print(logBuf);
        abc.displayAllSolution(false);
        
    }
    
    
    
    //run each instance, each instance is given limited round
    //if the argument round is -1, then run it within limited time
    //run different approach, and compare result
    public static void RunEachInstanceWithDifferentOption(int round) throws FileNotFoundException, UnsupportedEncodingException {
        String[] instanceType = {"R", "C", "RC", "RAD"};
        int caseNumber = 13;
        int instanceNumber = 20;
        ReferredResult result = new ReferredResult();
        
        String fn = PublicData.printSimpleTime();
        LogFile log = new LogFile(fn + "_whole_log.txt");
        if(log == null){
            return;
        }
        
        List<ResultAnalysis> analysis = new ArrayList<>();
        analysis.add(new ResultAnalysis("analysis_NoOnlookerBee_NoWholeTech.txt"));
        analysis.add(new ResultAnalysis("analysis_NoOnlookerBee_WholeTech.txt"));
        analysis.add(new ResultAnalysis("analysis_OnlookerBee_NoWholeTech.txt"));
        analysis.add(new ResultAnalysis("analysis_OnlookerBee_WholeTech.txt"));
        
        analysis.add(new ResultAnalysis("analysis_Exch_Drop_NoShrink_NoWholeTech.txt"));
        analysis.add(new ResultAnalysis("analysis_Exch_NoDrop_NoShrink_NoWholeTech.txt"));
        analysis.add(new ResultAnalysis("analysis_Exch_Drop_NoShrink_WholeTech.txt"));
        analysis.add(new ResultAnalysis("analysis_Exch_NoDrop_NoShrink_WholeTech.txt"));
        
        analysis.add(new ResultAnalysis("analysis_Exch_Drop_Shrink_NoWholeTech.txt"));
        analysis.add(new ResultAnalysis("analysis_Exch_NoDrop_Shrink_NoWholeTech.txt"));
        analysis.add(new ResultAnalysis("analysis_Exch_Drop_Shrink_WholeTech.txt"));
        analysis.add(new ResultAnalysis("analysis_Exch_NoDrop_Shrink_WholeTech.txt"));
        
        for(int caseN = 1; caseN <= caseNumber; caseN++){
            
            int totalRounds = caseN * round;
            for(int instType = 0; instType < instanceType.length; instType++){
                for(int instN = 1; instN <= instanceNumber; instN++){
                    
                    //on Nov 11, experiment in sun lab is interrupted, by R_5_9
                    //start from R_5_9
                    if(caseN < 5)
                        continue;
                    if(instType == 0 && caseN == 5 && instN <= 9)
                        continue;
                    
                    
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
                    
                    String time = PublicData.printTime();
                    log.writeFile(time + "\n");
                    
                    //we try 8 different approaches together and see difference immediately
                    int diffApproachIndex = 0;
                    while(diffApproachIndex < analysis.size()){
                    
                        AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber46, ss);

                        switch(diffApproachIndex){
                            case 0:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], false, false, false , false, false);
                                break;
                                
                            case 1:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], false, false, false , false, true);
                                break;
                                
                            case 2:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, false , false, false);
                                break;
                                
                            case 3:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, false , false, true);
                                break;
                                
                            case 4:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, true, true , false, false);
                                break;
                                
                            case 5:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, true , false, false);
                                break;
                            
                            case 6:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, true, true , false, true);
                                break;
                                
                            case 7:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, true , false, true);
                                break;
                            
                            case 8:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, true, true , true, false);
                                break;
                                
                            case 9:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, true , true, false);
                                break;
                                
                            case 10:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, true, true , true, true);
                                break;
                                
                            case 11:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, true , true, true);
                                break;   
                            default:
                                break;
                        }
                        
                        String logBuf = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue()
                                + ", referredResult=" + result.valueOfKey(key) + ", improveABC=" + (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                                + ", gap=" + (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()) + "\n";
                        log.writeFile(logBuf);
                        analysis.get(diffApproachIndex).insertOneResultAnalysis(key, (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                                                            , (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()));
                        
                        diffApproachIndex++;
                    }
                }
            }
            for(ResultAnalysis ra : analysis){
                ra.recordSoFar();
            }
        }
        
        log.closeFile();
        for(ResultAnalysis ra : analysis){
            ra.endResultAnalysis();
        }
    }
}
