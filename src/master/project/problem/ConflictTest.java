/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.problem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gillmylady
 * in here, we make conflict test, to make sure each solution is correct (no conflict inside)
 */
public class ConflictTest {
    Solution solution;
    
    public ConflictTest(Solution s){
        this.solution = s;
    }
    
    //run two conflict tests
    public boolean testIfConflict(){
        if(ifAnyTaskScheduledTwice() == true)
            return true;
        return ifAnyTechnicianConflict();
    }
    
    //test if any technician has scheduled tasks conflict (like two tasks cannot be scheduled with the execute time)
    public boolean ifAnyTechnicianConflict(){
        int lastSchedule;
        int thisSchedule;
        int lastEndTime;
        int thisStartTime;
            
        for(Technician t : solution.getSolution()){
            
            //rewrite this feature, if there is any problem, please refer old back-up codes in thie part.
            List<Map.Entry<Integer, Task>> sortedList = t.getSortExecuteTimeList();
            if(sortedList.isEmpty())            //if no tasks inside, just skip it
                continue;
            
            for(int i = 0; i <= sortedList.size(); i++){
                if(i == 0){
                    lastSchedule = 0;
                    lastEndTime = t.getStartTime();
                }
                else{
                    lastSchedule = sortedList.get(i - 1).getValue().getTaskID();
                    lastEndTime = sortedList.get(i - 1).getKey() + sortedList.get(i - 1).getValue().getProcessTime();
                }
                if(i != sortedList.size()){
                    thisSchedule = sortedList.get(i).getValue().getTaskID();
                    thisStartTime = sortedList.get(i).getKey();
                }
                else{
                    thisSchedule = 0;
                    thisStartTime = t.getEndTime();
                }
                
                //System.out.printf("lastEndTime=%d, thisStartTime=%d, distance=%d\n", lastEndTime, thisStartTime, solution.getDistance(lastSchedule, thisSchedule));
                
                //check if this executeTime is before startTime or after endTime
                if(i != sortedList.size()){
                    if(sortedList.get(i).getValue().getStartTime() > sortedList.get(i).getKey() ||
                          sortedList.get(i).getValue().getEndTime() < sortedList.get(i).getKey() + sortedList.get(i).getValue().getProcessTime())
                        return true;            
                }
                
                //check if the travel time is enough
                if(thisStartTime - lastEndTime < solution.getDistance(lastSchedule, thisSchedule))
                    return true;
                
            }
            
            
        }   
        return false;
    }
    
    //check if there is any scheduled task that is scheduled twice
    public boolean ifAnyTaskScheduledTwice(){
        HashMap<Integer, Integer> count = new HashMap<>();  //<taskId, count>
        for(Technician t : solution.getSolution()){
            for(Task s : t.getScheduledTask().values()){
                if(count.containsKey(s.getTaskID()) == false){
                    count.put(s.getTaskID(), 1);
                }else{
                    count.replace(s.getTaskID(), count.get(s.getTaskID()) + 1);
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
