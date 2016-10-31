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
import java.util.Random;
import master.project.main.PublicData;

/**
 *
 * @author gillmylady
 */
public class Solution {
    private int ID;
    private int count;
    
    List<Technician> solution;              //solution, size = techNumber
    List<Schedule> allSchedules;            //all schedules
    private double[][] taskPosition;        
    private int[][] skill;
    
    static int bestTechID;
    static int bestTechTravelTime;
    static int bestTechExecuteTime;
    
    //List<Schedule> scheduledTasks;      //use only one hashmap, save space
    //HashMap<Integer, Integer> scheduledTasks;
    List<Integer> scheduledTasks;       //only record scheduled tasks' ID
    
    //generate an empty solution
    public Solution(Instance instance, int id){
        initialSolution(instance);
        this.ID = id;
    }
    
    private void initialSolution(Instance instance){
        solution = new ArrayList<>();
        for(int i = 0; i < instance.getTechnicianNumber(); i++){
            Technician t = new Technician(instance.getTechStartTime(i), instance.getTechEndTime(i));
            //System.out.println(t.parameterToString());
            solution.add(t);
        }
        allSchedules = new ArrayList<>();
        for(int i = 0; i < instance.getTaskNumber() + 1; i++){
            Schedule s = new Schedule(i, instance.getPriority(i), instance.getProcessTime(i), 
                    instance.getTaskStartTime(i), instance.getTaskEndTime(i));
            allSchedules.add(s);
        }
        scheduledTasks = new ArrayList<>();
        taskPosition = new double[allSchedules.size()][2];
        taskPosition = instance.getPositions();
        skill = new int[instance.getTaskNumber()+1][instance.getTechnicianNumber()];
        skill = instance.getSkills();
        
        this.count = 0;
        //this.instance = instance;
    }
    
    public boolean checkAddOneTask(Schedule s, int technicianID){
        //System.out.printf("scheID = %d, techID = %d\n", s.getScheduleID(), technicianID);
        if(getSkill(s.getScheduleID(), technicianID) == false){
           // System.out.println("fail skill");
            return false;
        }
        int executeTime = TechnicianConflictSchedule(technicianID, s);
        //System.out.printf("j=%d,executeTime=%d\n",j,executeTime);
        if(executeTime != 0){
            solution.get(technicianID).addSchedule(executeTime, s);
            solution.get(technicianID).calculateReturnTime(getDistance(
                    solution.get(technicianID).getLastSchedule().getScheduleID(), 0));
            scheduledTasks.add(s.getScheduleID());
          //  System.out.println("succeed");
            return true;
        }
        //System.out.println("fail conflict");
        return false;
    }
    
    public void naiveSolution(){
        for(Schedule s : allSchedules){
            if(s.getScheduleID() == 0)          //omit 0 task (all 0)
                continue;
            for(int j = 0; j < solution.size(); j++){
                if(checkAddOneTask(s, j) == true)
                    break;
            }
        }
    }
    
    public void constructiveShortDistanceSolution(){
        for(Schedule s : allSchedules){
            if(s.getScheduleID() == 0)          //omit 0 task (all 0)
                continue;
            
            bestTechID = -1;
            bestTechTravelTime = Integer.MAX_VALUE;
            //System.out.println(bestTechTravelTime);
            bestTechExecuteTime = -1;
            for(int j = 0; j < solution.size(); j++){
                //check if this technician has enough skill first
                if(getSkill(s.getScheduleID(), j) == false)
                    continue;
                
                TechnicianConflictSchedule(j, s);
            }
            //System.out.println(bestTechID);
            if(bestTechID >= 0)
                checkAddOneTask(s, bestTechID);
        }
    }
    
    public void constructiveRandomSolution(){
        Random rd = new Random();
        int scheduleID;
        int techID;
        List<Integer> candidateTasks = new ArrayList<>();
        for(Schedule s : allSchedules){
            if(s.getScheduleID() == 0)
                continue;
            candidateTasks.add(s.getScheduleID());
        }
        
        // some schedules are not tried because of rounds, random might duplicate
        //System.out.println(candidateTasks.size());
        for(int i = 0; i < allSchedules.size() - 1; i++){
          //  System.out.println(i);
            int rdNumber = 0;
            if(candidateTasks.size() > 0)
                rdNumber = rd.nextInt(candidateTasks.size());
            scheduleID = candidateTasks.get(rdNumber);
            Schedule s = getTaskFromID(scheduleID);
            
            techID = rd.nextInt(solution.size());
            for(int j = 0; j < solution.size(); j++){
                
                if(checkAddOneTask(s, techID) == false){
                    techID++;
                    if(techID >= solution.size())
                        techID = 0;
                }else{
                    break;
                }
            }
            candidateTasks.remove(rdNumber);    //if this task is scheduled, remove it from the candidate list
        }
    }
    
    //contructive solution from zero, using multiple heuristic, applying RouletteWheel probablity selection
    //use multiple heuristic methods and assign them different probability, randomly pick (roulette wheel)
    //firstly, sort tasks by priority/processTime
    //add, exchange, change(one task from T1 to T2 seems no need because we apply nearest technician here) 
    //swap, one techinician, drop one task and schedule another one (makes change sense)
    //nearest technician, 
    public void constructiveMultiveHeuristicSolution(){
        Random rd = new Random();
        int scheduleID;
        int techID;
        List<Integer> candidateTasks = new ArrayList<>();
        for(Schedule s : allSchedules){
            if(s.getScheduleID() == 0)
                continue;
            candidateTasks.add(s.getScheduleID());
        }
        
        int probExchange = 10;  //10$ to exchange tasks among technicians
        int probChange = 20;    //10% to change one task from T1 to T2
        int probSwap = 30;      //10% for a technician to drop one task and re-schedule another one
        int probAdd = 100;      //70% to add one task
        
        //System.out.println(candidateTasks.size());
        for(int i = 0; i < allSchedules.size() - 1; i++){
            
            //System.out.println(totalPriority());
            
            int rdNumber = 0;
            int rdProb = rd.nextInt(probAdd);
            if(i > 0 && rdProb < probExchange){
                exchangeTasksAmongTechnicians();    //doesn't care the return value true of false
                i--;        //dont change i
                continue;
            }else if(i > 0 && rdProb < probChange){
                rdNumber = rd.nextInt(candidateTasks.size());
                scheduleID = candidateTasks.get(rdNumber);
                Schedule droppedTask = addOneTaskWithDrop(scheduleID, false);  //to return dropped task? so we can add it back to the candidate
                if(droppedTask != null){
                    candidateTasks.remove(rdNumber);            //remove changed one
                    candidateTasks.add(droppedTask.getScheduleID());    //add dropped one
                }
                i--;
                continue;
            }else if(i > 0 && rdProb < probSwap){
                //drop any task and re-schedule one
                //randomly delete one task and then re-schedule one
                i--;
                continue;
            }
            
            //probAdd 
            if(candidateTasks.size() > 0)
                rdNumber = rd.nextInt(candidateTasks.size());
            scheduleID = candidateTasks.get(rdNumber);
            Schedule s = getTaskFromID(scheduleID);
            
            techID = rd.nextInt(solution.size());
            for(int j = 0; j < solution.size(); j++){
                
                if(checkAddOneTask(s, techID) == false){
                    techID++;
                    if(techID >= solution.size())
                        techID = 0;
                }else{
                    break;
                }
            }
            candidateTasks.remove(rdNumber);    //if this task is scheduled, remove it from the candidate list
        }
    }
    
    
    
    
    //reset this solution
    public void resetSolution(){
        
        count = 0;
        for(Technician t : solution){
            t.removeAllSchedules();
        }
        
        scheduledTasks.clear();
        constructiveRandomSolution();
        
    }
    
    public boolean tryAddFromUnscheduled(){
        boolean add = false;
        for(int j = 0; j < solution.size(); j++){
            for(Schedule s : allSchedules){
                if(s.getScheduleID() == 0)
                    continue;
                if(scheduledTasks.contains(s.getScheduleID()) == true)
                    continue;
                
                if(checkAddOneTask(s, j) == true)
                    add = true;
            }
        }
        return add;
    }
    
    public void greedySortSchedulesPriorityProcessTime(){
        Collections.sort(allSchedules, Schedule.PriorityWithProcessTime);
    }
    
    public void greedySortSchedulesPriority(){
        Collections.sort(allSchedules, Schedule.Priority);
    }
    
    public void greedySortSchedulesProcessTime(){
        Collections.sort(allSchedules, Schedule.ProcessTime);
    }
    
    public String solutionToString(){
        String str = "\nsolution:\n";
        for(int i = 0; i < solution.size(); i++){
            str += "technician: ";
            str += solution.get(i).parameterToString();
            str += "\n";
            str += solution.get(i).scheduleToString();
        }
        return str;
    }
    
    public int getDistance(int taskn1, int taskn2){
        double euclideanDist;
        double x = taskPosition[taskn1][0] - taskPosition[taskn2][0];
        double y = taskPosition[taskn1][1] - taskPosition[taskn2][1];
        euclideanDist = Math.sqrt(x * x + y * y);
        
        return ((int) euclideanDist + 1);   //ceiling 
    }
    
    //[t1s - t1e], [t2s, t2e] , get the most earliest time to execute
    //s.start <= executeTime <= s.end-s.processTime
    //previousTaskEndTime + travelTime(i,j) <= executeTime <= nextTaskBeginTime - travelTime(j,k) - processTime
    public int getExecuteTime(int t1s, int t1e, int t2s, int t2e){
        //System.out.printf("t1s = %d, t1e = %d, t2s = %d, t2e = %d\r\n", t1s, t1e, t2s, t2e);
            
        if(t1s > t2e || t2s > t1e)
            return 0;
        if(t1s > t1e || t2s > t2e)
            return 0;
        if(t2s >= t1s && t2s <= t1e)
            return t2s;
        else
            return t1s;
    }
    
    //return execute time of this schedule s for this technician
    private int TechnicianConflictSchedule(int techNumber, Schedule s){
        int availExecuteTime = 0;
        int travelTime1 = 0;
        int travelTime2 = 0;
        Technician t = solution.get(techNumber);
        HashMap<Integer, Schedule> schedules = t.getScheduledTask();
        
        //System.out.print("TechnicianConflictSchedule, size=");
        //System.out.println(schedules.size());
        
        //if empty, t.startTime + travelTime <= executeTime <= t.endTime - travelTime
        if(schedules.isEmpty()){            //if there is no schedules for this technician
            
            travelTime1 = getDistance(0, s.getScheduleID());
            travelTime2 = travelTime1;
            availExecuteTime = getExecuteTime(s.getStartTime(), s.getEndTime()-s.getProcessTime(), 
                    t.getStartTime() + getDistance(0, s.getScheduleID()),
                    t.getEndTime() - getDistance(0, s.getScheduleID()) - s.getProcessTime());
        }
        else{
            //otherwise, check every gap and see if this sehedule can be scheduled
            //http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
            List<Map.Entry<Integer, Schedule>> list = new LinkedList<>(schedules.entrySet());
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
            travelTime1 = getDistance(0, s.getScheduleID());
            int previousEndTimePlusTravelTime = t.getStartTime() 
                   // + instance.getDistance(0, schedules.get(sortedKey.get(0)).getScheduleID());
                    + travelTime1;           //this new schedule and original distance
            int nextStartTimeMinusTravelTime;
            for(int i = 0; i <= sortedKey.size(); i++){
                if(i > 0){
                    //last executeTime + processTime + travelTime
                    travelTime1 = getDistance(schedules.get(sortedKey.get(i-1)).getScheduleID(), s.getScheduleID());
                    previousEndTimePlusTravelTime = sortedKey.get(i-1) + schedules.get(sortedKey.get(i-1)).getProcessTime()
                            + travelTime1;  
                    //System.out.printf("i>0, distance=%d\n",instance.getDistance(schedules.get(sortedKey.get(i-1)).getScheduleID(), s.getScheduleID()));
                }    
                if(i == sortedKey.size())  {              //last schedule, distance to origin point
                    travelTime2 = getDistance(s.getScheduleID(), 0);
                    nextStartTimeMinusTravelTime = t.getEndTime() 
                            - travelTime2 - s.getProcessTime();

                    //System.out.printf("i==sortedkey.size, distance=%d\n",instance.getDistance(s.getScheduleID(), 0));
                }
                else{
                    travelTime2 = getDistance(s.getScheduleID(), schedules.get(sortedKey.get(i)).getScheduleID());
                    nextStartTimeMinusTravelTime = sortedKey.get(i)        //distance to this point
                            - travelTime2 - s.getProcessTime();
                    //System.out.printf("0<i<size, distance=%d\n",instance.getDistance(s.getScheduleID(), schedules.get(sortedKey.get(i)).getScheduleID()));
                }
                availExecuteTime = getExecuteTime(s.getStartTime(), s.getEndTime()-s.getProcessTime(), 
                        previousEndTimePlusTravelTime, nextStartTimeMinusTravelTime);
                if(availExecuteTime != 0)
                    break;
            }
        }
        if(availExecuteTime > 0 && (travelTime1 + travelTime2) < bestTechTravelTime){
            bestTechID = techNumber;
            bestTechTravelTime = travelTime1 + travelTime2;
            bestTechExecuteTime = availExecuteTime;
        }
        return availExecuteTime;
    }
    
    public boolean isTaskScheduled(int scheduleID){
        for(Integer s : scheduledTasks){
            if(s == scheduleID)
                return true;
        }
        return false;
    }
    
    public Schedule getTaskFromID(int scheduleID){
        for(Schedule s : allSchedules){
            if(s.getScheduleID() == scheduleID){
                return s;
            }
        }
        return null;
    }
    
    public boolean addOneTaskWithoutDrop(int scheduleID){
        if(isTaskScheduled(scheduleID) == true)
            return false;
        if(scheduleID == 0)
            return false;
        Schedule task = getTaskFromID(scheduleID);
        for(int j = 0; j < solution.size(); j++){
            if(checkAddOneTask(task, j) == true)
                return true;
        }
        return false;
    }
    
    //if succeed, return one schedule which is dropped, otherwise, return null
    public Schedule addOneTaskWithDrop(int scheduleID, boolean allowNotBackup){
        //drop one task whose execute time is in the window of this new task
        Random r = new Random();
        int rdNumber = r.nextInt(solution.size());  //start check from a random-number of technician
        boolean flag = true;
        Schedule backupS = null;
        Schedule task = getTaskFromID(scheduleID);
        for(int i = rdNumber; flag == true || i < rdNumber; ){
            
            List<Map.Entry<Integer, Schedule>> list = new LinkedList<>(solution.get(i).getScheduledTask().entrySet());
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
            
            
            for(Integer executeTime : sortedKey){
                if(executeTime > task.getStartTime() + 50 && executeTime < task.getEndTime()){  //+50 for travel time
                    backupS = solution.get(i).getScheduledTask().get(executeTime);
                    
                    if(task.getPriority() <= backupS.getPriority())     //only like higher priority
                        continue;
                            
                    solution.get(i).deleteSchedule(executeTime, null);// getScheduledTask().remove(executeTime);
                    //System.out.println(backupS.getScheduleID());
                    //System.out.println(scheduledTasks.contains(backupS.getScheduleID()));
                    for(int id = 0; id < scheduledTasks.size(); id++){
                        if(scheduledTasks.get(id) == backupS.getScheduleID()){
                            scheduledTasks.remove(id);
                        }
                    }
                    //scheduledTasks.remove((Integer) backupS.getScheduleID());
                    
                    if(checkAddOneTask(task, i) == true){
                        return backupS;
                    }else{
                        //solution.get(i).getScheduledTask().put(executeTime, backupS);
                        
                        //leave some probabality for solution not backup
                        Random rd = new Random();
                        int j = rd.nextInt(10);
                        //if(PublicData.dontAllowBackup) //80% percentage to backup, 20% not backup for jumpming local optimal
                        if(allowNotBackup == false || (allowNotBackup && j < 5))     //50% percentage not backup
                        {
                            solution.get(i).addSchedule(executeTime, backupS);
                            scheduledTasks.add(backupS.getScheduleID());
                        }else{
                            return null;
                        }
                        /*if(PublicData.allowBackup && j < 8) //80% percentage to backup, 20% not backup for jumpming local optimal
                        {
                            solution.get(i).addSchedule(executeTime, backupS);
                            scheduledTasks.add(backupS.getScheduleID());
                        }*/
                    }
                }
            }
            
            i++;
            if(i >= solution.size()){
                flag = false;
                i = 0;
            }
        }
        
        return null;
    }
    
    public boolean exchangeTasksAmongTechnicians(){
        
        //select two random technicians
        int t1, t2;
        Random rd = new Random();
        t1 = t2 = rd.nextInt(solution.size());
        while(t2 == t1){
            t2 = rd.nextInt(solution.size());
        }
        //System.out.printf("exchangeTasks\n t1 = %d, t2 = %d\n", t1, t2);
        
        //select a random task from the first technician
        int t1TaskExecuteTime = solution.get(t1).getOneScheduledTaskExecuteTime();
        if(t1TaskExecuteTime == -1)
            return false;
        Schedule t1Task = solution.get(t1).getTaskFromExecuteTime(t1TaskExecuteTime);
        solution.get(t1).deleteSchedule(t1TaskExecuteTime, t1Task);
        //System.out.printf("t1 ExeTime = %d, t1Task = %d\n", t1TaskExecuteTime, t1Task.getScheduleID());
        
        //see if there is one task in the second technician so that they can exchange and shorten travel time
        Schedule t2Task = oneTechnicianAddWithDrop(t2, t1Task);
        if(t2Task == null){         //t2 fail
            //System.out.println("t2 cannot delete any task and add t1Task");
            solution.get(t1).addSchedule(t1TaskExecuteTime, t1Task);    //t1 add back, exchange fail
            return false;
        }
        
        //continue to see if t1 can add this schedule or not
        if(checkAddOneTask(t2Task, t1) == true){        //can add to t1
            //System.out.println("t1, t2 exchange correctly");
            return true;
        }else{
            //System.out.println("t1 cannot add t2's task and both recover");
            //t2 need to remove t1Task and add t2Task
            solution.get(t2).deleteRecentAddTask();
            checkAddOneTask(t2Task, t2);
            
            //t1 need to add t1task
            solution.get(t1).addSchedule(t1TaskExecuteTime, t1Task);
            return false;
        }
    }
    
    //add new task, if succeed, return the dropped task
    public Schedule oneTechnicianAddWithDrop(int techID, Schedule s){
        List<Map.Entry<Integer, Schedule>> list = new LinkedList<>(solution.get(techID).getScheduledTask().entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Schedule>>(){
            @Override
            public int compare(Map.Entry<Integer, Schedule> o1, Map.Entry<Integer, Schedule> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        
        int executeTime = 0;
        Schedule task = null;
        for(int i = 0; i < list.size(); i++){
            executeTime = list.get(i).getKey();
            task = list.get(i).getValue();
            
            solution.get(techID).deleteSchedule(executeTime, task);
            
            if(checkAddOneTask(s, techID)){
                return task;
            }else{
                solution.get(techID).addSchedule(executeTime, task);
            }
        }
        return null;
    }
    
    public List<Technician> getSolution(){
        return solution;
    }
    
    public int getOneScheduledTask(){
        Random r = new Random();
        int rdNumber = r.nextInt(scheduledTasks.size());
        return scheduledTasks.get(rdNumber);
    }
    
    // get one task from scheduled tasks, with lower priority than required
    public int getOneScheduledTask(int prio, boolean acceptSame){
        Random r = new Random();
        int rdNumber = r.nextInt(scheduledTasks.size());
        boolean flag = true;
        for(int i = rdNumber; flag == true || i < rdNumber; ){
            /*
            if(scheduledTasks.get(i).getPriority() > prio || (acceptSame == true && scheduledTasks.get(i).getPriority() == prio)){
                return scheduledTasks.get(i).getScheduleID();
            }*/
            Schedule s = getTaskFromID(rdNumber);
            if(s.getPriority() < prio || (acceptSame == true && s.getPriority() == prio)){
                return s.getScheduleID();
            }
            i++;
            if(i >= scheduledTasks.size()){
                i = 0; 
                flag = false;
            }
        }
        return -1;
    }
    
    public int getID() { return this.ID; }
    public int getCount() { return this.count; }
    public void setCount(int count) { this.count = count; }
    public void addCount() { this.count++; }
    
    public boolean getSkill(int taskNum, int techNum){
        if(skill[taskNum][techNum] == 0)
            return false;
        else return true;
    }
    
    //judge solution's score by total priority
    public int totalPriority(){
        int totalValue = 0;
        for(Technician t : solution){
            totalValue += t.getTotalPriority();
        }
        return totalValue;
    }
    
    //judge solution's total idle time 
    public int totalIdleTime(){
        int totalValue = 0;
        for(Technician t : solution){
            totalValue += t.getIdleTime();
        }
        return totalValue;
    }
}
