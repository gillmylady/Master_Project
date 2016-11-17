/*

Referred paper's results, for our analysis

 */
package master.project.problem;

import java.util.ArrayList;
import java.util.List;
import master.project.main.PublicData;

/**
 *
 * @author gillmylady
 */
public class ResultAnalysis {
    
    LogFile log;                                //the log file
    
    int numOfNotAsGoodAsReferedPaper;           //all kinds of data
    int numOfAsGoodAsReferredPaper;
    int numOfBetterThanReferredPaper;
    int numOfErrorDataInReferredPaper;
    
    double notGoodTotalPercentage;             //to caculate average
    List<String> asGoodAsPaper;                 //save the keys for satisfied condition
    List<String> betterThanPaper;
    List<String> errorDataInPaper;
    
    public ResultAnalysis(String fileName){
        
        log = new LogFile(fileName);                //the log file
        
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
    //this function will make decision which kind it belongs to
    //and add the record into correct arraylist
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
    
    //record so far result, for analysis purpose
    public void recordSoFar(){
        
        log.writeFile("\r\n" + PublicData.printTime() + "\r\n");            //title, time
        
        if(numOfErrorDataInReferredPaper > 0){                          //how many error data exist
            log.writeFile("#number of wrong referred paper: " + numOfErrorDataInReferredPaper + "\r\n");
            for(String s : errorDataInPaper){
                log.writeFile(s + "\r\n");
            }
        }
        
        if(numOfBetterThanReferredPaper > 0){                           //how many better data we have
            log.writeFile("#number of Better than referred paper: " + numOfBetterThanReferredPaper + "\r\n");
            for(String s : betterThanPaper){
                log.writeFile(s + "\r\n");
            }
        }
        
        if(numOfAsGoodAsReferredPaper > 0){                             //as good as paper
            log.writeFile("#number of as good as referred paper: " + numOfAsGoodAsReferredPaper + "\r\n");
            for(String s : asGoodAsPaper){
                log.writeFile(s + "\r\n");
            }
        }
        
        //calculate worse result
        if(numOfNotAsGoodAsReferedPaper > 0){                           //worse results
            log.writeFile("#number of worse: " + numOfNotAsGoodAsReferedPaper + "\r\n");
            log.writeFile("#average percentage of worse results: " + notGoodTotalPercentage/numOfNotAsGoodAsReferedPaper + "\r\n");
        }
    }
    
    //only cloase file
    public void close(){
        log.closeFile();
    }
    
    //when end, record all information into log file and quit
    public void endResultAnalysis(){
        recordSoFar();                  //record log
        log.closeFile();                //close
    }
    
}
