/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    private int technicianID;       //ID
    private int startTime;           // start work time
    private int endTime;             // end work time
    private int returnOriginTime;    // time for this technician return origin, so that idle time = endtime - returnOriginTime
    private HashMap<Integer, Task> schedules;   // schedule's exact startExecuteTime and itself
    private List<Map.Entry<Integer, Task>> sortedList;    //schedules are sorted by execute time
    private int recentAddTaskTime;
    
    //random construct an object
    public Technician(int technicianID, int startTime, int endTime){
        this.technicianID = technicianID;
        this.startTime = startTime;
        this.endTime = endTime;
        returnOriginTime = startTime;           //initially, returnOriginTime is the start time
        schedules = new HashMap<>();            //exact startExecuteTime and schedule
        recentAddTaskTime = 0;
    }
    
    public int getTechnicianID(){
        return technicianID;
    }
    
    public int getStartTime(){
        return startTime;
    }
    public int getEndTime() {
        return endTime;
    }
    public int getReturnOriginTime(){
        return returnOriginTime;
    }
    
    public void setReturnOriginTime(int time){
        returnOriginTime = time;
    }
    
    public String parameterToString(){
        return "startTime = " + startTime + ", endTime = " + endTime + ", returnTime = " + returnOriginTime;
    }
    
    //return all schedules' execute time and its parameters
    public String scheduledTasksToString(){
        String ret = "";
    
        for (Integer executeTime : schedules.keySet()) {
            ret += executeTime;
            ret += ":";
            ret += schedules.get(executeTime).parameterToString();
            ret += ";";
        }
        ret += "\n";
        
        return ret;
    }
    
    public void sortExecuteTime(){
        
        if(schedules.isEmpty())           //if empty, then no need to sort
            return;
        
        sortedList = new LinkedList<>(schedules.entrySet());    //initial sortedList
        
        Collections.sort(sortedList, new Comparator<Map.Entry<Integer, Task>>(){
            @Override
            public int compare(Map.Entry<Integer, Task> o1, Map.Entry<Integer, Task> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
    }
    
    //return this sortedList for use
    public List<Map.Entry<Integer, Task>> getSortExecuteTimeList(){
        if(schedules.isEmpty())           //if empty, then no need to sort
            return null;
        sortExecuteTime();
        return sortedList;
    }
    
    public String getSortedExecuteTimeSchedules(){
        String ret = "";
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
    public Integer getLastScheduleExecuteTime(){
        Integer lastExecuteTime = null;              //initialize
        for (Integer next : schedules.keySet()) {
            if (lastExecuteTime == null || next > lastExecuteTime) {
                lastExecuteTime = next;
            }
        }
        return lastExecuteTime;
    }
    
    //get last Task, we can also use the sortedTasks to get the last one
    public Task getLastSchedule(){
        if(schedules.isEmpty()){
            return null;
        }
        
        Integer lastExecuteTime;
        lastExecuteTime = getLastScheduleExecuteTime();
        if(lastExecuteTime == null)
            return null;
        return schedules.get(lastExecuteTime);
    }
    
    //calculate the time when this technician can return origin
    public void calculateReturnTime(int travelTime){
        Task lastSchedule = getLastSchedule();
        if(lastSchedule == null)
            returnOriginTime = this.startTime;
        else{
            returnOriginTime = getLastScheduleExecuteTime() + lastSchedule.getProcessTime() + travelTime;
        }
    }
    
    //return scheduled tasks, for solution to decide if it can add one schedule into this technician
    public HashMap<Integer, Task> getScheduledTask(){
        return schedules;
    }
    
    //leave if one technician conflict one schedule to outside's dealing
    // travelTime = -1 , then no need to update returnTime
    public boolean addSchedule(int executeTime, Task s){
        schedules.put(executeTime, s);  
        recentAddTaskTime = executeTime;
        return true;
    }
    
    //four ways of local search:
    //add, change, exchange, swap
    //add, just add
    //change, one task reassigned to another technician
    //exchange, two technicians exchange tasks
    //swap, one technician drop one task and add another task
    //if executeTime < 0, doesnt judge it
    public boolean deleteSchedule(int executeTime, Task s){
        if(s != null && schedules.containsValue(s) == false)   //if schedule exist, check if it in the table  
            return false;
        if(s != null && executeTime > 0 )                   //if both exists
            return schedules.remove(executeTime, s);
        if(executeTime > 0 && schedules.containsKey(executeTime) == false)  //only check if key exist
            return false;
        schedules.remove(executeTime);
        return true;
    }
    
    //this is for exchange, in exchange, remember the recent added task, when backup we have to delete it
    public boolean deleteRecentAddTask(){
        return deleteSchedule(recentAddTaskTime, null);
    }
    
    //for reset solution's use
    public void removeAllSchedules(){
        schedules.clear();
    }
    
    // total priority, this is the main objective of FTSP problem
    public int getTotalPriority(){
        int totalValue = 0;
        for(Task s : schedules.values()){
            totalValue += s.getPriority();
        }
        return totalValue;
    }
    
    //get total process Time
    public int getTotalProcessTime(){
        if(schedules.isEmpty())           //if empty, then no need to sort
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
    
    //get a random task's execute time 
    public int getOneScheduledTaskExecuteTime(){
        if(schedules.isEmpty())                  //if there is no scheduled task yet, return -1
            return -1;
        
        Random rd = new Random();
        sortExecuteTime();
        int rdNumber = rd.nextInt(sortedList.size());
        return sortedList.get(rdNumber).getKey();
    }
    
    //get Task from the execute time
    public Task getTaskFromExecuteTime(int executeTime){
        return schedules.get(executeTime);
    }
    
}