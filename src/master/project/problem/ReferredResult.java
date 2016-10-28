/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import master.project.main.PublicData;

/**
 *
 * @author gillmylady
 */
public class ReferredResult {
    
    HashMap<String, Integer> R;         //eg, "R_C_1, 66"
    HashMap<String, Integer> C;
    HashMap<String, Integer> RC;
    HashMap<String, Integer> RAD;
    
    public ReferredResult() throws FileNotFoundException, UnsupportedEncodingException{
        R = new HashMap<>();
        C = new HashMap<>();
        RC = new HashMap<>();
        RAD = new HashMap<>();
        
        //sun-lab "/home/hfw5079/NetBeansProjects/Master_Project/Results.txt"
        //home  "/Users/gillmylady/Desktop/master project C212/FTSP_Instances and Results/Results.txt"
        //ParseReferredResult("/home/hfw5079/NetBeansProjects/Master_Project/Results.txt");
        
        if(PublicData.AmIAtSublab){
            ParseReferredResult(PublicData.sunlabResultPath);
        }else
            ParseReferredResult(PublicData.homeResultPath);
    
    
    }
    
    public void ParseReferredResult(String fileName) throws FileNotFoundException, UnsupportedEncodingException{
        String[] data;
        File file = new File(fileName);
        String key = null;
        int largestValue = 0;               //the largest value might be the upper bound, which is not real value of result
        int secondLargestValue = 0;         //we want to keep the second largest value, which is a good result already
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(file));
            String str = null;
            while((str = reader.readLine()) != null){
                
                str = str.replace('\t', ' ');
                data = str.split(" ");
                
                if(data.length < 6){
                    System.out.println("result file error!!!");
                    return;
                }
                if(data[0].startsWith("R") == false && data[0].startsWith("C") == false)        //first two lines ignore
                    continue;
                
                key = data[0] + "_" + data[1] + "_" + data[2];
                largestValue = 0;
                secondLargestValue = 0;
                for(int i = 0; i < 5; i++){
                    double doubleV = Double.parseDouble(data[data.length - 1 - i]);
                    int v = (int) doubleV;
                    if(v > largestValue){
                        secondLargestValue = largestValue;
                        largestValue = v;
                    }else if(v > secondLargestValue){
                        secondLargestValue = v;
                    }
                }
                
                //System.out.printf("key=%s, value=%d\n",key, secondLargestValue);
                if(key.startsWith("RAD")){
                    RAD.put(key, secondLargestValue);
                }else if(key.startsWith("RC")){
                    RC.put(key, secondLargestValue);
                }else if(key.startsWith("C")){
                    C.put(key, secondLargestValue);
                }else if(key.startsWith("R")){
                    R.put(key, secondLargestValue);
                }
            }
            reader.close();
        }catch(IOException | NumberFormatException e){
        }finally{
            if(reader != null){
                try{
                    reader.close();
                }catch(Exception e){
                }
            }
        }
    }
    
    public int valueOfKey(String key){
        if(key.startsWith("RAD")){
            return RAD.get(key);
        }else if(key.startsWith("RC")){
            return RC.get(key);
        }else if(key.startsWith("C")){
            return C.get(key);
        }else if(key.startsWith("R")){
            return R.get(key);
        }else{
            System.out.println("key error!!!!!!!!");
            return -1;
        }
    }
}
