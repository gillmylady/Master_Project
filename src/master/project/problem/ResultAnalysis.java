/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.problem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gillmylady
 */
public class ResultAnalysis {
    
    LogFile log;
    
    int numOfNotAsGoodAsReferedPaper;
    int numOfAsGoodAsReferredPaper;
    int numOfBetterThanReferredPaper;
    int numOfErrorDataInReferredPaper;
    
    double notGoodTotalPercentage;             //to caculate average
    List<String> asGoodAsPaper;
    List<String> betterThanPaper;
    List<String> errorDataInPaper;
    
    public ResultAnalysis(String fileName){
        
        log = new LogFile(fileName);
        
        numOfNotAsGoodAsReferedPaper = 0;
        numOfAsGoodAsReferredPaper = 0;
        numOfBetterThanReferredPaper = 0;
        numOfErrorDataInReferredPaper = 0;
        notGoodTotalPercentage = 0.00;
        
        errorDataInPaper = new ArrayList<>();
        asGoodAsPaper = new ArrayList<>();
        betterThanPaper = new ArrayList<>();
    }
    
    //insert one item with key and improved data, and gap
    public void insertOneResultAnalysis(String key, int improveByABC, int gapFromReferredBest){
        
        if(gapFromReferredBest < 0 && Math.abs(gapFromReferredBest) > 2 * improveByABC){
            numOfErrorDataInReferredPaper++;
            errorDataInPaper.add(key + ", " + improveByABC + ", " + gapFromReferredBest);
        }else if(gapFromReferredBest < 0){
            numOfBetterThanReferredPaper++;
            betterThanPaper.add(key + ", " + improveByABC + ", " + gapFromReferredBest);
        }else if(gapFromReferredBest == 0){
            numOfAsGoodAsReferredPaper++;
            asGoodAsPaper.add(key + ", " + improveByABC);
        }else{
            numOfNotAsGoodAsReferedPaper++;
            notGoodTotalPercentage += (double) improveByABC / (double) (improveByABC + gapFromReferredBest);
        }
    }
    
    //when end, record all information into log file and quit
    public void endResultAnalysis(){
        
        if(numOfErrorDataInReferredPaper > 0){
            log.writeFile("#number of wrong referred paper: " + numOfErrorDataInReferredPaper + "\n");
            for(String s : errorDataInPaper){
                log.writeFile(s + "\n");
            }
        }
        
        if(numOfBetterThanReferredPaper > 0){
            log.writeFile("#number of Better than referred paper: " + numOfBetterThanReferredPaper + "\n");
            for(String s : betterThanPaper){
                log.writeFile(s + "\n");
            }
        }
        
        if(numOfAsGoodAsReferredPaper > 0){
            log.writeFile("#number of as good as referred paper: " + numOfAsGoodAsReferredPaper + "\n");
            for(String s : asGoodAsPaper){
                log.writeFile(s + "\n");
            }
        }
        
        //calculate worse result
        if(numOfNotAsGoodAsReferedPaper > 0){
            log.writeFile("#number of worse: " + numOfNotAsGoodAsReferedPaper + "\n");
            log.writeFile("#average percentage of worse results: " + notGoodTotalPercentage/numOfNotAsGoodAsReferedPaper + "\n");
        }
        
        log.closeFile();
    }
    
}
