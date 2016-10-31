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
    private int startTime;           // start work time
    private int endTime;             // end work time
    private int returnOriginTime;    // time for this technician return origin, so that idle time = endtime - returnOriginTime
    private HashMap<Integer, Schedule> schedules;   // schedule's exact startExecuteTime and itself
    private List<Map.Entry<Integer, Schedule>> sortedList;    //schedules are sorted by execute time
    private int recentAddTaskTime;
    
    //random construct an object
    public Technician(int startTime, int endTime){
        this.startTime = startTime;
        this.endTime = endTime;
        returnOriginTime = startTime;           //initially, returnOriginTime is the start time
        schedules = new HashMap<>();            //exact startExecuteTime and schedule
        recentAddTaskTime = 0;
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
    
    public String scheduleToString(){
        //return schedules.toString();
        String ret = "";
    
        for (Integer procTime : schedules.keySet()) {
            ret += procTime;
            ret += ":";
            ret += schedules.get(procTime).parameterToString();
            ret += "\n";
        }
        
        return ret;
    }
    
    public void sortExecuteTime(){
        sortedList = new LinkedList<>(schedules.entrySet());
        Collections.sort(sortedList, new Comparator<Map.Entry<Integer, Schedule>>(){
            @Override
            public int compare(Map.Entry<Integer, Schedule> o1, Map.Entry<Integer, Schedule> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        /*
        for(int i = 0; i < list.size(); i++){
            System.out.println(list.get(i).getKey());
            System.out.println(list.get(i).getValue().parameterToString());
        }
        */
        
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
        if(schedules.isEmpty()){
            return null;
        }
        Integer lastExecuteTime = null;              //initialize
        for (Integer next : schedules.keySet()) {
            if (lastExecuteTime == null || next > lastExecuteTime) {
                lastExecuteTime = next;
            }
        }
        return lastExecuteTime;
    }
    
    //get last Schedule
    public Schedule getLastSchedule(){
        Integer lastExecuteTime;
        lastExecuteTime = getLastScheduleExecuteTime();
        if(lastExecuteTime == null)
            return null;
        return schedules.get(lastExecuteTime);
    }
    
    //calculate the time when this technician can return origin
    public void calculateReturnTime(int travelTime){
        Schedule lastSchedule = getLastSchedule();
        if(lastSchedule == null)
            returnOriginTime = this.startTime;
        else{
            returnOriginTime = getLastScheduleExecuteTime() + lastSchedule.getProcessTime() + travelTime;
        }
    }
    
    //return scheduled tasks, for solution to decide if it can add one schedule into this technician
    public HashMap<Integer, Schedule> getScheduledTask(){
        return schedules;
    }
    
    //leave if one technician conflict one schedule to outside's dealing
    // travelTime = -1 , then no need to update returnTime
    public boolean addSchedule(int executeTime, Schedule s){
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
    public boolean deleteSchedule(int executeTime, Schedule s){
        if(s != null && executeTime > 0 )
            return schedules.remove(executeTime, s);
        if(s != null && schedules.containsValue(s) == false)
            return false;
        if(executeTime > 0 && schedules.containsKey(executeTime) == false)
            return false;
        schedules.remove(executeTime);
        return true;
    }
    
    public boolean deleteRecentAddTask(){
        return deleteSchedule(recentAddTaskTime, null);
    }
    
    public void removeAllSchedules(){
        schedules.clear();
    }
    
    // total priority
    public int getTotalPriority(){
        int totalValue = 0;
        for(Schedule s : schedules.values()){
            totalValue += s.getPriority();
        }
        return totalValue;
    }
    
    // idle time for this technician return home
    public int getIdleTime(){
        return endTime - returnOriginTime;
    }
    
    public int getOneScheduledTaskExecuteTime(){
        Random rd = new Random();
        sortExecuteTime();
        if(sortedList.size() == 0)                  //if there is no scheduled task yet, return -1
            return -1;
        int rdNumber = rd.nextInt(sortedList.size());
        return sortedList.get(rdNumber).getKey();
    }
    
    public Schedule getTaskFromExecuteTime(int executeTime){
        return schedules.get(executeTime);
    }
    
}