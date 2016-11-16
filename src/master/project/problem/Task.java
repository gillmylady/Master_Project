/*
 
task class, contains variables and methods of task

in this project, schedule = task

 */
package master.project.problem;

import java.util.Comparator;

/**
 *
 * @author gillmylady
 */
public class Task{
    private int taskID;             //task unique ID for hashtable
    private int priority;
    private int processTime;         //how long it needs to process
    private int startTime;           //time window, start time
    private int endTime;             //time window, end time
    
    // constructer
    public Task(int taskID, int priority, int processTime, int startTime, int endTime){
        this.taskID = taskID;
        this.priority = priority;
        this.processTime = processTime;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public int getTaskID(){
        return taskID;
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
    
    //print all parameters of this task
    public String parameterToString(){
        return "scheduleID = " + taskID + ", priority = " + priority + ", processTime = " + processTime 
                + ", startTime = " + startTime + ", endTime = " + endTime;
    }
   
    //Comparator for sorting the list by priority/processtime 
    public static Comparator<Task> PriorityWithProcessTime = new Comparator<Task>() {
        @Override
        public int compare(Task o1, Task o2) {
            double a = (double)o1.getPriority() / (double)o1.getProcessTime();
            double b = (double)o2.getPriority() / (double)o2.getProcessTime();
            return -(int)(1000 * (a-b));
        }
    };
    
    //Comparator for sorting the list by priority/processtime 
    public static Comparator<Task> Priority = new Comparator<Task>() {
        @Override
        public int compare(Task o1, Task o2) {
            return o2.getPriority() - o1.getPriority();
        }
    };
    
    //Comparator for sorting the list by priority/processtime 
    public static Comparator<Task> ProcessTime = new Comparator<Task>() {
        @Override
        public int compare(Task o1, Task o2) {
            return +(o1.getProcessTime()- o2.getProcessTime());
        }
    };

}
