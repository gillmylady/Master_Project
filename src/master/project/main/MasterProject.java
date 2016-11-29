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
        
11.3 update
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

11.15 update
1.  use more parameters to determine the object function value when generate constructive solution
        besides priority/processtime, add travel time, highest priority, smallest process time

2.  use more parameters to determine the object function value of all tasks scheduled by one technician
        priority/processtime, travel time, highest priority
     this is used to determine which tasks are worse than some better ones
     -> when swapped or exchanged, we always delete bad or even worst tasks 


11.28 update
1.  check abandoned solution and see if consecutive 3 abandoned solutions improve, if not, then stop the algorithm
        if it's getting better, the whole best solution might also be better then we continue
        -> Monday night's work
2.  draw some diagram for the paper
        -> Tuesday's work

3.  we should record the percentage that we improved from the initial best solution if the data is incorrect in the referred paper
        -> easy, the number of incorrect instances is limited, we can manually record them
4.  we should compare my result with best initial solution and see how much we improved
        -> if we want to record all data's improvement percentage from best initial, we have to run the program again
        -> if we want only record incorrect instances, we can do manually
5.  Write a read-me file about this project, and how to run this project
*/

public class MasterProject {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        
        //before test, please make sure if i'm at sun-lab or own computer
        
        runExperiment();
        
    }
    
    //run ABC algorithm, each instance is given limited time
    //this method requires multiple arguments
    public static void RunAllInstancesInSameCaseWithLimitedTime(
            boolean oddFlag,            //use multiple to run this program
            int minCaseNum,
            int maxCaseNum,
            boolean onlookerBeeExist, 
            boolean workerBeeAllowNotBackupWhenGetStucked,
            boolean allowExchange, boolean allowShrink, 
            boolean allowExchangeWholeTechnician,
            boolean allowGreedy,
            boolean greedySelectTask,
            boolean greedySelectTaskByObjectFunction,
            boolean constructiveFlag,
            boolean onlookerBeeCopyFromEmployeeBee) throws FileNotFoundException, UnsupportedEncodingException {
        String[] instanceType = {"R", "C", "RC", "RAD"};
        
        int instanceNumber = 20;
        
        ReferredResult result = new ReferredResult();
        
        String logFileName = PublicData.printSimpleTime();  //time format readable in windows
        
        LogFile log = new LogFile(logFileName + "_log.txt");
        
        for(int caseN = minCaseNum; caseN <= maxCaseNum; caseN++){
            
            if(oddFlag && caseN % 2 == 0)          //only run odd case number cases
                continue;
            if(oddFlag == false && caseN % 2 == 1)
                continue;
            
            ResultAnalysis analysis = new ResultAnalysis(logFileName + "analysis_" + caseN + ".txt");
        
            for(int instType = 0; instType < instanceType.length; instType++){
                
                if(caseN == 10 && instType > 1)
                    continue;       //run less test cases and speed up experiments
                if(caseN == 9 && instType > 2)
                    continue;       //run less test cases and speed up experiments
                
                for(int instN = 1; instN <= instanceNumber; instN++){
                    
                    if(instN % 2 == 0)          //only run odd instances number
                        continue;
                    
                    String key = instanceType[instType] + "_" + caseN + "_" + instN;
                    if(key.equalsIgnoreCase("R_13_1") || key.equalsIgnoreCase("RC_13_7"))    //these two instances error, something in the instance incorrect
                        continue;
                    
                    String fileName;
                    if(PublicData.AmIAtSublab){
                        fileName = PublicData.sunlabInstancePath + key + ".txt";
                    }else{
                        if(PublicData.AmIAtOldMachine)
                            fileName = PublicData.homeInstancePathOldMachine + key + ".txt";
                        else
                            fileName = PublicData.homeInstancePath + key + ".txt";
                    }
                    
                    Instance ss = new Instance(fileName);
                    
                    log.writeFile(PublicData.printTime() + "\r\n");
                    
                    AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber46, ss, constructiveFlag, onlookerBeeCopyFromEmployeeBee);
                    
                    //run the rounds in limited time
                    abc.RunBasicABCAlgorithm(-1, PublicData.runLimitTime[caseN], onlookerBeeExist, 
                            workerBeeAllowNotBackupWhenGetStucked, allowExchange, allowShrink, allowExchangeWholeTechnician, allowGreedy, 
                            greedySelectTask, greedySelectTaskByObjectFunction);
                    
                    String logBuf = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue()
                            + ", referredResult=" + result.valueOfKey(key) + ", improveABC=" + (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                            + ", gap=" + (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()) + "\r\n";
                    log.writeFile(logBuf);
                    analysis.insertOneResultAnalysis(key, (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                                                        , (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()));
                    
                    
                }
                analysis.recordSoFar();
            }
            //analysis.endResultAnalysis();
            analysis.close();
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
            
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
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

                String fileName;
                if(PublicData.AmIAtSublab){
                    fileName = PublicData.sunlabInstancePath + key + ".txt";
                }else{
                    if(PublicData.AmIAtOldMachine)
                            fileName = PublicData.homeInstancePathOldMachine + key + ".txt";
                        else
                            fileName = PublicData.homeInstancePath + key + ".txt";
                }

                Instance ss = new Instance(fileName);

                log.writeFile(PublicData.printTime() + "\r\n");

                AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber46, ss, false, false);

                if(caseN < 13)
                    abc.RunBasicABCAlgorithm(caseN * 1000, -1, true, true, true, true, true, false, false, false);
                else
                    abc.RunBasicABCAlgorithm(caseN * 600, -1, true, true, true, false, false, false, false, false);
                
                String logBuf = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue()
                        + ", referredResult=" + result.valueOfKey(key) + ", improveABC=" + (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                        + ", gap=" + (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()) + "\r\n";
                log.writeFile(logBuf);
                analysis.insertOneResultAnalysis(key, (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                                                    , (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()));

                totalAnalysis.insertOneResultAnalysis(key, (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                                                    , (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()));

            }
            analysis.endResultAnalysis();
        }
        totalAnalysis.endResultAnalysis();
        log.closeFile();
    }
    
    //test one instance
    public static void runOneInstanceInLimitedRounds(
            String key, 
            int caseNumber, 
            boolean onlookerBeeExist, 
            boolean workerBeeAllowNotBackupWhenGetStucked,
            boolean allowExchange, 
            boolean allowShrink, 
            boolean allowExchangeWholeTechnician,
            boolean exchangeGreedy, 
            boolean greedySelectTask) throws FileNotFoundException, UnsupportedEncodingException {
        
        if(key.equalsIgnoreCase("R_13_1") || key.equalsIgnoreCase("RC_13_7"))    //these two instances error, something in the instance incorrect
            return;

        ReferredResult result = new ReferredResult();
        
        String fileName;
        if(PublicData.AmIAtSublab){
            fileName = PublicData.sunlabInstancePath + key + ".txt";
        }else{
            if(PublicData.AmIAtOldMachine)
                fileName = PublicData.homeInstancePathOldMachine + key + ".txt";
            else
                fileName = PublicData.homeInstancePath + key + ".txt";
        }

        Instance ss = new Instance(fileName);

        AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber46, ss, false, false);

        abc.RunBasicABCAlgorithm(-1, PublicData.runLimitTime[caseNumber], onlookerBeeExist, 
            workerBeeAllowNotBackupWhenGetStucked,
            allowExchange, allowShrink, 
            allowExchangeWholeTechnician, exchangeGreedy, greedySelectTask, true);
        
        String logBuf = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue() 
                + " ,referedResult=" + result.valueOfKey(key) + "\r\n";
        System.out.print(logBuf);
        abc.displayAllSolution(false);
        
        for(Solution s : abc.getSolutions()){
            ConflictTest ct = new ConflictTest(s);
            if(ct.testIfConflict() == true){
                System.out.println("some schedules conflict!!!!!!!\n\n\n");
                return;
            }
        }
    }
    
    
    //run each instance, each instance is given limited round
    //if the argument round is -1, then run it within limited time
    //run different approach, and compare result
    public static void RunEachInstanceWithDifferentOption(int round) throws FileNotFoundException, UnsupportedEncodingException {
        String[] instanceType = {"R", "C", "RC", "RAD"};
        int caseNumber = 13;
        
        //int instanceNumber = 20;
        int instanceNumber = 10;
        
        ReferredResult result = new ReferredResult();
        
        LogFile log;
        if(PublicData.AmIAtOldMachine){
            log = new LogFile("oldMachine_whole_log.txt");
        }else{
            String fn = PublicData.printSimpleTime();
            log = new LogFile(fn + "_whole_log.txt");
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
        
        //from case N = 3, to compare each
        for(int caseN = 3; caseN <= caseNumber; caseN++){
            
            int totalRounds = caseN * round;
            for(int instType = 0; instType < instanceType.length; instType++){
                for(int instN = 1; instN <= instanceNumber; instN++){
                    
                    //on Nov 11, experiment in sun lab is interrupted, by R_5_9
                    //start from R_5_9
                    /*if(caseN < 5)
                        continue;
                    if(instType == 0 && caseN == 5 && instN <= 17)
                        continue;
                    */
                    String key = instanceType[instType] + "_" + caseN + "_" + instN;
                    if(key.equalsIgnoreCase("R_13_1") || key.equalsIgnoreCase("RC_13_7"))    //these two instances error, something in the instance incorrect
                        continue;
                    
                    String fileName;
                    if(PublicData.AmIAtSublab){
                        fileName = PublicData.sunlabInstancePath + key + ".txt";
                    }else{
                        if(PublicData.AmIAtOldMachine)
                            fileName = PublicData.homeInstancePathOldMachine + key + ".txt";
                        else
                            fileName = PublicData.homeInstancePath + key + ".txt";
                    }
                    
                    Instance ss = new Instance(fileName);
                    
                    String time = PublicData.printTime();
                    log.writeFile(time + "\r\n");
                    
                    //we try 8 different approaches together and see difference immediately
                    int diffApproachIndex = 0;
                    while(diffApproachIndex < analysis.size()){
                    
                        AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber46, ss, false, false);

                        switch(diffApproachIndex){
                            case 0:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], false, false, false , false, false, false, false, false);
                                break;
                                
                            case 1:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], false, false, false , false, true, false, false, false);
                                break;
                                
                            case 2:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, false , false, false, false, false, false);
                                break;
                                
                            case 3:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, false , false, true, false, false, false);
                                break;
                                
                            case 4:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, true, true , false, false, false, false, false);
                                break;
                                
                            case 5:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, true , false, false, false, false, false);
                                break;
                            
                            case 6:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, true, true , false, true, false, false, false);
                                break;
                                
                            case 7:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, true , false, true, false, false, false);
                                break;
                            
                            case 8:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, true, true , true, false, false, false, false);
                                break;
                                
                            case 9:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, true , true, false, false, false, false);
                                break;
                                
                            case 10:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, true, true , true, true, false, false, false);
                                break;
                                
                            case 11:
                                abc.RunBasicABCAlgorithm(totalRounds, PublicData.runLimitTime[caseN], true, false, true , true, true, false, false, false);
                                break;   
                            default:
                                break;
                        }
                        
                        String logBuf = key + ": bestBeforeABC=" + abc.getInitialBestSolutionValue() + ", bestAfterABC=" + abc.getSoFarBestSolutionValue()
                                + ", referredResult=" + result.valueOfKey(key) + ", improveABC=" + (abc.getSoFarBestSolutionValue() - abc.getInitialBestSolutionValue())
                                + ", gap=" + (result.valueOfKey(key) - abc.getSoFarBestSolutionValue()) + "\r\n";
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
    
    //test exchange feature
    public static void testExchange() throws FileNotFoundException, UnsupportedEncodingException{
        String key = "R_5_3";
        String fileName;
        if(PublicData.AmIAtSublab){
            fileName = PublicData.sunlabInstancePath + key + ".txt";
        }else{
            if(PublicData.AmIAtOldMachine)
                fileName = PublicData.homeInstancePathOldMachine + key + ".txt";
            else
                fileName = PublicData.homeInstancePath + key + ".txt";
        }

        Instance ss = new Instance(fileName);

        AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber46, ss, false, false);

        abc.RunBasicABCAlgorithm(2000, -1, false, false, true , false, false, true, false, false);
        
        for(Solution s: abc.getSolutions()){
            ConflictTest ct = new ConflictTest(s);
            if(ct.testIfConflict() == true){
                System.out.println("some schedules conflict!!!");
                return;
            }
        }                        
    }
    
    
    //just generate initial solutions, and compare constructive way and greedy heuristic
    //we find this comparison is not fair, and we decide making some empty initial solutions to 
    //test the combination of constructive and local search
    public static void compareConstructiveSolution() throws FileNotFoundException, UnsupportedEncodingException {
        String[] instanceType = {"R", "C", "RC", "RAD"};
        
        int instanceNumber = 20;
        int caseNumber = 13;
        
        String logFileName = PublicData.printTime();
        
        LogFile log = new LogFile(logFileName + "_log.txt");
        
        int totalGreedy;
        int totalConstructive;
        int constructiveGreaterThanGreedyCount;
        int constructiveEqualGreedyCount;
        int constructiveLessThanGreedyCount;
        
        int bestG;
        int bestC;
        
        for(int caseN = 1; caseN <= caseNumber; caseN++){
            
            totalGreedy = 0;
            totalConstructive = 0;
            constructiveGreaterThanGreedyCount = 0;
            constructiveEqualGreedyCount = 0;
            constructiveLessThanGreedyCount = 0;
            
            for(int instType = 0; instType < instanceType.length; instType++){
                for(int instN = 1; instN <= instanceNumber; instN++){
                    String key = instanceType[instType] + "_" + caseN + "_" + instN;
                    if(key.equalsIgnoreCase("R_13_1") || key.equalsIgnoreCase("RC_13_7"))    //these two instances error, something in the instance incorrect
                        continue;
                    
                    String fileName;
                    if(PublicData.AmIAtSublab){
                        fileName = PublicData.sunlabInstancePath + key + ".txt";
                    }else{
                        if(PublicData.AmIAtOldMachine)
                            fileName = PublicData.homeInstancePathOldMachine + key + ".txt";
                        else
                            fileName = PublicData.homeInstancePath + key + ".txt";
                    }
                    
                    Instance ss = new Instance(fileName);
                    
                    log.writeFile(PublicData.printTime() + "\r\n");
                    
                    AbcBasicAlgorithm abc = new AbcBasicAlgorithm(ss, caseN);      //only generate initial solutions and compare result
                    
                    bestG = abc.getGreedyBestInitial();
                    bestC = abc.getConstructiveHeuristic();
                    
                    String logBuf = key + ": bestGreedy=" + bestG + ", bestConstructive=" + bestC + "\r\n";
                    log.writeFile(logBuf);
                    
                    totalGreedy += bestG;
                    totalConstructive += bestC;
                    if(bestC > bestG)
                        constructiveGreaterThanGreedyCount++;
                    else if(bestC == bestG)
                        constructiveEqualGreedyCount++;    
                    else
                        constructiveLessThanGreedyCount++;
                }
            }
            
            log.writeFile("caseNum" + caseN + " ,totalG=" + totalGreedy + " ,totalC=" + totalConstructive
                            + ",betterConst=" + constructiveGreaterThanGreedyCount + " ,equal=" + constructiveEqualGreedyCount
                            + " ,betterGreedy=" + constructiveLessThanGreedyCount + "\r\n");
            
        }
        
        log.closeFile();
        
    }
    
    
    // run the whole experiments for this project
    public static void runExperiment() throws FileNotFoundException, UnsupportedEncodingException{
        
        //runABCWithConstructiveWithGreedySelectByObjFun(true);
        //runABCWithConstructiveWithGreedySelectByObjFun(false);
        runABCLocalSearchWithAbandonedCount(true);
    }
    
    // run no onlooker bee
    // argument, odd or even caseN
    public static void runNoOnlookerBee(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1 , 12, false, false, false, false, false, false, false, false, false, false);
    }
    
    // run onlooker bee but without local search
    public static void runNoLocalSearch(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 12, true, false, false, false, false, false, false, false, false, false);
    }
    
    // run onlooker bee but without local search
    public static void runABCNoLocalSearch(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, false, false, false, false, false, false, false, false, true);
    }
    
    // run onlooker bee but without local search
    public static void runLocalSearch(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, false, false, false, false, false);
    }
    
    // run initial onlooker bee selected from workerBee by probability
    public static void runABCLocalSearch(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, false, false, false, false, true);
    }
    
    // run onlooker bee but without local search
    public static void runDropWorst(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, true, false, false, false, false);
    }
    
    // run onlooker bee but without local search
    public static void runDropWorstSelectBestByObjFun(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, true, true, true, false, false);
    }
    
    // run onlooker bee but without local search
    public static void runABCDropWorstSelectBestByObjFun(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, true, true, true, false, true);
    }
    
    // run onlooker bee but without local search
    public static void runDropWorstSelectBestByPrioProcessTime(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, true, true, false, false, false);
    }
    
    // run onlooker bee but without local search
    public static void runABCDropWorstSelectBestByPrioProcessTime(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, true, true, false, false, true);
    }
    
    // run conbination of constructive and local search
    public static void runLocalSearchWithConstructive(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, false, false, false, true, false);
    }
    
    // run conbination of constructive and local search
    public static void runABCLocalSearchWithConstructive(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, false, false, false, true, true);
    }
    
    // run conbination of constructive and local search, with greedy select worst and best
    public static void runABCWithConstructiveWithGreedySelectByPrioProcessTime(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, true, true, false, true, true);
    }
    
    // run conbination of constructive and local search, with greedy select worst and best
    public static void runABCWithConstructiveWithGreedySelectByObjFun(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 1, 10, true, true, true, true, true, true, true, true, true, true);
    }
    
    // run onlooker bee but without local search, allow abandon count in PublicData must be true to test it.
    public static void runABCLocalSearchWithAbandonedCount(boolean oddFlag) throws FileNotFoundException, UnsupportedEncodingException{
        RunAllInstancesInSameCaseWithLimitedTime(oddFlag, 5, 6, true, true, true, true, true, false, false, false, false, true);
    }
}
