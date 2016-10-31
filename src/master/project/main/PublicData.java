/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.main;

import java.util.Calendar;
import java.util.List;
import master.project.problem.Solution;

/**
 *
 * @author gillmylady
 */
public class PublicData {

    public static int origin = 0;                       // original location
    
    public static double max(double a, double b){
        if(a >= b)
            return a;
        else return b;
    }
    
    public static boolean allowBackup = true;
    public static boolean dontAllowBackup = true;
    
    public static int workerBeeNotBackUp = 10;
    public static int resetBeeCount = 20;
    
    public static int totalBeeNumber20 = 20;
    public static int totalBeeNumber46 = 46;
    public static int totalBeeNumber100 = 100;
    public static int totalBeeNumber200 = 200;
    public static int totalBeeNumber500 = 500;
    public static int totalBeeNumber1000 = 1000;
    
    public static int[] getSolutionFitness(List<Solution> solutions){
        int[] ret = new int[solutions.size()];
        
        for(int i = 0; i < solutions.size(); i++){
            ret[i] = solutions.get(i).totalPriority();
        }
        return ret;
    }
    
    public static boolean AmIAtSublab = false;
    
    public static String homeInstancePath = "/Users/gillmylady/NetBeansProjects/Master_Project/instances/FTSP_";
    public static String sunlabInstancePath = "/home/hfw5079/NetBeansProjects/Master_Project/instances/FTSP_";
    public static String homeResultPath = "/Users/gillmylady/Desktop/master project C212/FTSP_Instances and Results/Results.txt";
    public static String sunlabResultPath = "/home/hfw5079/NetBeansProjects/Master_Project/Results.txt";
    
    public static String printTime(){
         Calendar ca = Calendar.getInstance();
         //System.out.println(ca.getTime());
         return ca.getTime().toString();
    }
    
}
