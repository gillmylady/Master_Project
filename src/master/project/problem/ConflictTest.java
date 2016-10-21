/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.problem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gillmylady
 */
public class ConflictTest {
    Solution solution;
    
    public ConflictTest(Solution s){
        this.solution = s;
    }
    
    public boolean testIfConflict(){
        if(ifAnyTaskScheduledTwice() == true)
            return true;
        return ifAnyTechnicianConflict();
    }
    
    public boolean ifAnyTechnicianConflict(){
        int lastSchedule;
        int thisSchedule;
        int lastEndTime;
        int thisStartTime;
            
        for(Technician t : solution.getSolution()){
            
            List<Map.Entry<Integer, Schedule>> list = new LinkedList<>(t.getScheduledTask().entrySet());
            Collections.sort(list, new Comparator<Map.Entry<Integer, Schedule>>(){
                @Override
                public int compare(Map.Entry<Integer, Schedule> o1, Map.Entry<Integer, Schedule> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
            Map<Integer, Schedule> sortedSchedules = new LinkedHashMap<>();
            for(Map.Entry<Integer, Schedule> entry: list){
                sortedSchedules.put(entry.getKey(), entry.getValue());
            }
            ArrayList<Integer> sortedKey = new ArrayList<>(sortedSchedules.keySet());
            
            for(int i = 0; i <= sortedKey.size(); i++){
                if(i == 0){
                    lastSchedule = 0;
                    lastEndTime = t.getStartTime();
                }
                else{
                    lastSchedule = sortedSchedules.get(sortedKey.get(i - 1)).getScheduleID();
                    lastEndTime = sortedKey.get(i - 1) + sortedSchedules.get(sortedKey.get(i - 1)).getProcessTime();
                }
                if(i != sortedKey.size()){
                    thisSchedule = sortedSchedules.get(sortedKey.get(i)).getScheduleID();
                    thisStartTime = sortedKey.get(i);
                }
                else{
                    thisSchedule = 0;
                    thisStartTime = t.getEndTime();
                }
                
                //System.out.printf("lastEndTime=%d, thisStartTime=%d, distance=%d\n", lastEndTime, thisStartTime, solution.getDistance(lastSchedule, thisSchedule));
                
                //check if this executeTime is before startTime or after endTime
                if(i != sortedKey.size()){
                    if(sortedSchedules.get(sortedKey.get(i)).getStartTime() > sortedKey.get(i) ||
                          sortedSchedules.get(sortedKey.get(i)).getEndTime() < sortedKey.get(i) + sortedSchedules.get(sortedKey.get(i)).getProcessTime())
                        return true;            
                }
                
                //check if the travel time is enought
                if(thisStartTime - lastEndTime < solution.getDistance(lastSchedule, thisSchedule))
                    return true;
                
            }
        }   
        return false;
    }
    
    public boolean ifAnyTaskScheduledTwice(){
        HashMap<Integer, Integer> count = new HashMap<>();
        for(Technician t : solution.getSolution()){
            for(Schedule s : t.getScheduledTask().values()){
                if(count.containsKey(s.getScheduleID()) == false){
                    count.put(s.getScheduleID(), 1);
                }else{
                    count.replace(s.getScheduleID(), count.get(s.getScheduleID()) + 1);
                    System.out.println("\n\n\n\n\n\n ifAnyTaskScheduledTwice\n\n\n\n\n");
                }
            }
        }
        
        for(Integer i : count.values()){
            if(i > 1){
                return true;
            }
        }
        
        return false;
    }
}
