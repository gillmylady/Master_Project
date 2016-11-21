/*

This is Technician class, contains all variables and methods about technician

 */
package master.project.problem;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author gillmylady
 */
public class Technician {
    private int technicianID;       //ID, unique
    private int startTime;           // start work time
    private int endTime;             // end work time
    private int returnOriginTime;    // time for this technician return origin, so that idle time = endtime - returnOriginTime
    private HashMap<Integer, Task> tasks;   // schedule's exact startExecuteTime and itself
    private List<Map.Entry<Integer, Task>> sortedList;    //schedules are sorted by execute time
    private int recentAddTaskTime;      //for exchange method, recent add task might be deleted when fail
    
    //random construct an object
    public Technician(int technicianID, int startTime, int endTime){
        this.technicianID = technicianID;
        this.startTime = startTime;
        this.endTime = endTime;
        returnOriginTime = startTime;           //initially, returnOriginTime is the start time
        tasks = new HashMap<>();            //exact startExecuteTime and schedule
        recentAddTaskTime = 0;
    }
    
    //get technician ID
    public int getTechnicianID(){
        return technicianID;
    }
    
    //get technician start time of work
    public int getStartTime(){
        return startTime;
    }
    
    //get technician end time of work
    public int getEndTime() {
        return endTime;
    }
    
    //get technician the time of return origin
    public int getReturnOriginTime(){
        return returnOriginTime;
    }
    
    //set the time of return origin
    public void setReturnOriginTime(int time){
        returnOriginTime = time;
    }
    
    //print the parameter of this technician
    public String parameterToString(){
        return "startTime = " + startTime + ", endTime = " + endTime + ", returnTime = " + returnOriginTime;
    }
    
    //return all tasks' execute time and its parameters
    public String scheduledTasksToString(){
        String ret = "";
    
        for (Integer executeTime : tasks.keySet()) {
            ret += executeTime;
            ret += ":";
            ret += tasks.get(executeTime).parameterToString();
            ret += ";";
        }
        ret += "\n";
        
        return ret;
    }
    
    //sort all scheduled tasks by execute Time
    public void sortExecuteTime(){
        
        sortedList = new LinkedList<>(tasks.entrySet());    //initial sortedList
        
        Collections.sort(sortedList, new Comparator<Map.Entry<Integer, Task>>(){
            @Override
            public int compare(Map.Entry<Integer, Task> o1, Map.Entry<Integer, Task> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
    }
    
    //return this sortedList for use
    public List<Map.Entry<Integer, Task>> getSortExecuteTimeList(){
        if(tasks.isEmpty())           //if empty, then no need to sort
            return null;
        sortExecuteTime();
        return sortedList;
    }
    
    public String toStringSortedExecuteTimeTasks(){
        String ret = "";
        if(tasks.isEmpty())           //if empty, then no need to sort
            return ret;
        
        sortExecuteTime();
        
        ret += "{";
        ret += startTime;
        
        for(int i = 0; i < sortedList.size(); i++){
            ret += ", [";
            ret += sortedList.get(i).getKey();
            
            ret += "(";
            ret += sortedList.get(i).getValue().getStartTime();
            ret += ", ";
            ret += sortedList.get(i).getValue().getEndTime();
            ret += "),";
            
            ret += (sortedList.get(i).getKey() + sortedList.get(i).getValue().getProcessTime());
            ret += "]";
        }
        
        ret += ", ";
        ret += endTime;
        ret += "}";
        
        return ret;
    }
    
    
    //get last schedule's execute time
    public Integer getLastTaskExecuteTime(){
        Integer lastExecuteTime = null;              //initialize
        for (Integer next : tasks.keySet()) {
            if (lastExecuteTime == null || next > lastExecuteTime) {
                lastExecuteTime = next;
            }
        }
        return lastExecuteTime;
    }
    
    //get last Task, we can also use the sortedTasks to get the last one
    public Task getLastTask(){
        if(tasks.isEmpty()){
            return null;
        }
        
        Integer lastExecuteTime;
        lastExecuteTime = getLastTaskExecuteTime();
        if(lastExecuteTime == null)
            return null;
        return tasks.get(lastExecuteTime);
    }
    
    //calculate the time when this technician can return origin
    public void calculateReturnTime(int travelTime){
        Task lastTask = getLastTask();
        if(lastTask == null)
            returnOriginTime = this.startTime;
        else{
            returnOriginTime = getLastTaskExecuteTime() + lastTask.getProcessTime() + travelTime;
        }
    }
    
    //return scheduled tasks, for solution to decide if it can add one schedule into this technician
    public HashMap<Integer, Task> getScheduledTask(){
        return tasks;
    }
    
    //get a random task's execute time 
    public int getOneScheduledTaskExecuteTime(){
        if(tasks.isEmpty())                  //if there is no scheduled task yet, return -1
            return -1;
        
        Random rd = new Random();
        sortExecuteTime();
        int rdNumber = rd.nextInt(sortedList.size());
        return sortedList.get(rdNumber).getKey();
    }
    
    //get Task from the execute time
    public Task getTaskFromExecuteTime(int executeTime){
        return tasks.get(executeTime);
    }
    
    
    //leave if one technician conflict one schedule to outside's dealing
    // travelTime = -1 , then no need to update returnTime
    public boolean addOneTask(int executeTime, Task s){
        tasks.put(executeTime, s);  
        recentAddTaskTime = executeTime;
        return true;
    }
    
    //delete one task, s can be null; but if not, need to check
    public boolean deleteOneTask(int executeTime, Task s){
        if(s != null && tasks.containsValue(s) == false)   //if schedule exist, check if it in the table  
            return false;
        if(s != null && executeTime > 0 )                   //if both exists
            return tasks.remove(executeTime, s);
        if(executeTime > 0 && tasks.containsKey(executeTime) == false)  //only check if key exist
            return false;
        tasks.remove(executeTime);
        return true;
    }
    
    //delete one task using taskID
    public boolean deleteOneTask(int taskID){
        if(tasks.isEmpty())           //if empty, then no need to sort
            return false;
        sortExecuteTime();
        for(int i = 0; i < sortedList.size(); i++){
            if(sortedList.get(i).getValue().getTaskID() == taskID){
                tasks.remove(sortedList.get(i).getKey());
                return true;
            }
        }
        return false;
    }
    
    //this is for exchange, in exchange, remember the recent added task, when backup we have to delete it
    public boolean deleteRecentAddTask(){
        return deleteOneTask(recentAddTaskTime, null);
    }
    
    //for reset solution's use
    public void removeAllTasks(){
        tasks.clear();
    }
    
    // total priority, this is the main objective of FTSP problem
    public int getTotalPriority(){
        int totalValue = 0;
        for(Task s : tasks.values()){
            totalValue += s.getPriority();
        }
        return totalValue;
    }
    
    //get total process Time
    public int getTotalProcessTime(){
        if(tasks.isEmpty())           //if empty, then no need to sort
            return 0;
        int ret = 0;
        sortExecuteTime();
        for(int i = 0; i < sortedList.size(); i++){
            ret += sortedList.get(i).getValue().getProcessTime();
        }
        return ret;
    }
    
    // idle time for this technician return home
    public int getIdleTime(){
        return endTime - returnOriginTime;
    }
    
    
}