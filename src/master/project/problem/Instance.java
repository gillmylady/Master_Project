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

/**
 *
 * @author gillmylady
 * in here, we parse all instances (data set) and use them.
 */
public class Instance {
    private static final int indexLimit = 13;                //maximum 13 kinds of data to be read
    
    private int taskNumber;
    private int technicianNumber;
    private int[] priority;
    private int[][] skill;
    private int[] taskProcessTime;
    private int[] taskStartTime;
    private int[] taskEndTime;
    private int[] technicianStartTime;
    private int[] technicianEndTime;
    private int mValue;
    private int mwValue;
    private int mzValue;
    private double[][] taskPosition;
    
    
    public Instance(String fileName) throws FileNotFoundException, UnsupportedEncodingException{
        int dataIndex = 0;
        int count = 0;
        int validCount = 0;
        String[] data;
        File file = new File(fileName);
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(file));
            String str = null;
            while(dataIndex < indexLimit){
                str = reader.readLine();
                if(str.isEmpty())
                    continue;
                switch(dataIndex){
                    case 0:
                        taskNumber = Integer.parseInt(str.trim());
                        dataIndex++;
                        break;
                    case 1:
                        technicianNumber = Integer.parseInt(str.trim());
                        dataIndex++;
                        priority = new int[taskNumber+1];
                        skill = new int[taskNumber+1][technicianNumber];
                        taskProcessTime = new int[taskNumber+1];
                        taskStartTime = new int[taskNumber+1];
                        taskEndTime = new int [taskNumber+1];
                        technicianStartTime = new int[technicianNumber];
                        technicianEndTime = new int[technicianNumber];
                        taskPosition = new double[taskNumber+1][2];
                        
                        break;
                    case 2:
                        priority[count++] = Integer.parseInt(str.trim());
                        if(count > taskNumber){
                            count = 0;
                            dataIndex++;
                        }
                        break;
                    case 3:
                        str = str.replace('\t', ' ');
                        str = str.replaceAll("    ", " ");
                        data = str.split(" ");
                        validCount = 0;
                        for(int i = 0; i < data.length; i++){
                            if(data[i].trim().isEmpty())
                                continue;
                            skill[count][validCount++] = Integer.parseInt(data[i].trim());
                        }
                        count++;
                        if(count > taskNumber){
                            count = 0;
                            dataIndex++;
                        }
                        break;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        str = str.replace('\t', ' ');
                        data = str.split(" ");
                        validCount = 0;
                        for(int i = 0; i < data.length; i++){
                            if(data[i].trim().isEmpty())
                                continue;
                            switch (dataIndex) {
                                case 4:
                                    taskProcessTime[validCount++] = Integer.parseInt(data[i].trim());
                                    break;
                                case 5:
                                    taskStartTime[validCount++] = Integer.parseInt(data[i].trim());
                                    break;
                                case 6:
                                    taskEndTime[validCount++] = Integer.parseInt(data[i].trim());
                                    break;
                                case 7:
                                    technicianStartTime[validCount++] = Integer.parseInt(data[i].trim());
                                    break;
                                case 8:
                                    technicianEndTime[validCount++] = Integer.parseInt(data[i].trim());
                                    break;
                                default:
                                    break;
                            }
                        }
                        dataIndex++;
                        break;
                    
                    case 9:
                        mValue = Integer.parseInt(str.trim());
                        dataIndex++;
                        break;
                    case 10:
                        mwValue = Integer.parseInt(str.trim());
                        dataIndex++;
                        break;
                    case 11:
                        mzValue = Integer.parseInt(str.trim());
                        dataIndex++;
                        break;
                    case 12:
                        str = str.replace('\t', ' ');
                        data = str.split(" ");
                        validCount = 0;
                        for(int i = 0; i < data.length; i++){
                            if(data[i].trim().isEmpty())
                                continue;
                            taskPosition[count][validCount++] = Double.parseDouble(data[i].trim());
                        }
                        count++;
                        if(count > taskNumber){
                            count = 0;
                            dataIndex++;
                        }
                        break;
                    default:
                        dataIndex++;
                        break;
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
    
    
    public int getTaskNumber() {return taskNumber;}
    public int getTechnicianNumber() {return technicianNumber;}
    public int getPriority(int k){
        if(k <= taskNumber) 
            return priority[k];
        else return 0;
    }
    public int[] getArrayPriority() {return priority;}
    public double[][] getPositions() {return taskPosition;}
    public int[][] getSkills() {return skill;}
    
    //techNumber, from 1 to n which is technician's number
    public boolean getSkill(int taskNum, int techNum){
        if(taskNum > taskNumber || techNum >= technicianNumber)
            return false;
        if(skill[taskNum][techNum] == 0)
            return false;
        else return true;
    }
    
    public int getProcessTime(int taskNumber) { return taskProcessTime[taskNumber]; }
    public int getTaskStartTime(int taskNumber) { return taskStartTime[taskNumber]; }
    public int getTaskEndTime(int taskNumber) { return taskEndTime[taskNumber]; }
    public int getTechStartTime(int techNumber) { return technicianStartTime[techNumber]; }
    public int getTechEndTime(int techNumber) { return technicianEndTime[techNumber]; }
    
    public int getmValue() {return mValue;}
    public int getmwValue() {return mwValue;}
    public int getmzValue() {return mzValue;}
    
    //Euclidean distance
    public int getDistance(int taskn1, int taskn2){
        double euclideanDist;
        double x = taskPosition[taskn1][0] - taskPosition[taskn2][0];
        double y = taskPosition[taskn1][1] - taskPosition[taskn2][1];
        euclideanDist = Math.sqrt(x * x + y * y);
        
        return ((int) euclideanDist + 1);   //ceiling 
    }
    
}
