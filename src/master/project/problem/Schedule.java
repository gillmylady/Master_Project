/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.problem;

import java.util.Comparator;

/**
 *
 * @author gillmylady
 */
public class Schedule{
    private int scheduleID;             //schedule unique ID for hashtable
    private int priority;
    private int processTime;         //how long it needs to process
    private int startTime;           //time window, start time
    private int endTime;             //time window, end time
    
    //random construct an object
    public Schedule(int scheduleID, int priority, int processTime, int startTime, int endTime){
        this.scheduleID = scheduleID;
        this.priority = priority;
        this.processTime = processTime;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public int getScheduleID(){
        return scheduleID;
    }
    
    public int getPriority(){
        return priority;
    }
    
    public int getProcessTime(){
        return processTime;
    }
    
    public int getStartTime(){
        return startTime;
    }
    
    public int getEndTime(){
        return endTime;
    }
    
    public String parameterToString(){
        return "scheduleID = " + scheduleID + ", priority = " + priority + ", processTime = " + processTime 
                + ", startTime = " + startTime + ", endTime = " + endTime;
    }
   
    //Comparator for sorting the list by priority/processtime 
    public static Comparator<Schedule> PriorityWithProcessTime = new Comparator<Schedule>() {
        @Override
        public int compare(Schedule o1, Schedule o2) {
            double a = (double)o1.getPriority() / (double)o1.getProcessTime();
            double b = (double)o2.getPriority() / (double)o2.getProcessTime();
            return -(int)(1000 * (a-b));
        }
    };
    
    //Comparator for sorting the list by priority/processtime 
    public static Comparator<Schedule> Priority = new Comparator<Schedule>() {
        @Override
        public int compare(Schedule o1, Schedule o2) {
            return o2.getPriority() - o1.getPriority();
        }
    };
    
    //Comparator for sorting the list by priority/processtime 
    public static Comparator<Schedule> ProcessTime = new Comparator<Schedule>() {
        @Override
        public int compare(Schedule o1, Schedule o2) {
            return +(o1.getProcessTime()- o2.getProcessTime());
        }
    };

}
