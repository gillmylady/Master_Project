/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.main;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author gillmylady
 */
public class PublicData {

    public static int origin = 0;                       // original location
    
    public static int workerBeeNotBackUp = 100;         //10 to 100
    public static int resetBeeCount = 50;              //20 to 50
    
    public static int totalBeeNumber20 = 20;
    public static int totalBeeNumber46 = 46;
    public static int totalBeeNumber100 = 100;
    
    public static int exaParameter = 1000;
    public static int exaLargestParameter = 5; 
    public static int exaSecondLargestParameter = 3;
    
    public static boolean AmIAtSublab = false;
    public static boolean AmIAtOldMachine = false;
    
    //path of my files
    public static String homeInstancePath = "/Users/gillmylady/NetBeansProjects/Master_Project/instances/FTSP_";
    public static String sunlabInstancePath = "/home/hfw5079/NetBeansProjects/Master_Project/instances/FTSP_";
    public static String homeResultPath = "/Users/gillmylady/Desktop/master project C212/FTSP_Instances and Results/Results.txt";
    public static String sunlabResultPath = "/home/hfw5079/NetBeansProjects/Master_Project/Results.txt";
    
    public static String homeInstancePathOldMachine = "C:\\Users\\gillmylady\\Documents\\NetBeansProjects\\Master_Project\\instances\\FTSP_";
    public static String homeResultPathOldMachine = "C:\\Users\\gillmylady\\Documents\\NetBeansProjects\\Master_Project\\Results.txt";
    
    public static String printTime(){
         Calendar ca = Calendar.getInstance();
         return ca.getTime().toString();
    }
    
    public static String printSimpleTime(){
        
        GregorianCalendar gcalendar = new GregorianCalendar();
        String ret = "";
        ret += (gcalendar.get(Calendar.MONTH)+1) + "_" + gcalendar.get(Calendar.DATE) + "_" 
                + gcalendar.get(Calendar.DATE) + "_" + gcalendar.get(Calendar.MINUTE) + "_" + gcalendar.get(Calendar.SECOND);
        
        return ret;
    }
    
    //each case's running time according to referred paper
    public static int[] runLimitTime = {0, 3, 5, 10, 50, 75, 80, 180, 250, 480, 600, 900, 1800, 3600};
    
}
