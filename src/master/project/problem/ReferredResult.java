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
    
    HashMap<String, Integer> R;         //different kinds, to make it quick for looking up//eg, "R_C_1, 66"
    HashMap<String, Integer> C;
    HashMap<String, Integer> RC;
    HashMap<String, Integer> RAD;
    
    //we are going to parse the Result.txt file, to get all instances' results by the referred paper
    //we store all these results in 4 hashMap, each with one different name of distribution type
    public ReferredResult() throws FileNotFoundException, UnsupportedEncodingException{
        R = new HashMap<>();
        C = new HashMap<>();
        RC = new HashMap<>();
        RAD = new HashMap<>();
        
        //if we take experiement in the sun lab, the file path is different with that in my own computer
        if(PublicData.AmIAtSublab){
            ParseReferredResult(PublicData.sunlabResultPath);
        }else
            ParseReferredResult(PublicData.homeResultPath);
    }
    
    //parse the file and store data into the 4 hashmap
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
                
                //key is in format of XX_I_J, XX is the type, I is the case number, from 1-13, J is the instance number, from 1-20
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
                //select correct table to insert the data
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
    
    //get value from the key
    //key is in format of XX_I_J, XX is the type, I is the case number, from 1-13, J is the instance number, from 1-20
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
