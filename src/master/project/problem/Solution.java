/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.problem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import master.project.main.PublicData;


/**
 *
 * @author gillmylady
 */
public class Solution {
    private int ID;                     //solution ID for future use
    private int count;                  //count of this solution, after how many times try it hasnt been improved
    
    private double[][] taskPosition;    //tasks' position, from instance data   
    private int[][] skill;              //tasks' skill requirement
    
    static int bestTechID;              //these three remember the closest technician
    static int bestTechTravelTime;
    static int bestTechExecuteTime;
    
    List<Technician> solution;          //solution, size = techNumber
    List<Task> allSchedules;            //all schedules
    List<Integer> scheduledTasks;       //only record scheduled tasks' ID
    
    //generate an empty solution, initial solution
    public Solution(Instance instance, int id){
        initialSolution(instance);
        this.ID = id;
    }
    
    //initial solution from instance data
    private void initialSolution(Instance instance){
        solution = new ArrayList<>();
        for(int i = 0; i < instance.getTechnicianNumber(); i++){
            Technician t = new Technician(i, instance.getTechStartTime(i), instance.getTechEndTime(i));
            solution.add(t);                //one solution is composed by all technicians, we schedule tasks for each technician
        }
        allSchedules = new ArrayList<>();
        for(int i = 0; i < instance.getTaskNumber() + 1; i++){
            Task s = new Task(i, instance.getPriority(i), instance.getProcessTime(i), 
                    instance.getTaskStartTime(i), instance.getTaskEndTime(i));
            allSchedules.add(s);
        }
        scheduledTasks = new ArrayList<>();                 //initially, empty list
        taskPosition = new double[allSchedules.size()][2];
        taskPosition = instance.getPositions();
        skill = new int[instance.getTaskNumber()+1][instance.getTechnicianNumber()];
        skill = instance.getSkills();
        
        this.count = 0;
    }
    
    //check if one technician can add this task or not
    //argument: task s, the task to be added
    //argument: techinicianID, the techinician to add this task
    public boolean checkAddOneTask(Task s, int technicianID){
        if(getSkill(s.getTaskID(), technicianID) == false){         //if the skill doesnt meet the requirement
            return false;
        }
        
        int executeTime = TechnicianConflictSchedule(technicianID, s);  //to see if this task can be scheduled at this technician or not
        //System.out.printf("j=%d,executeTime=%d\n",j,executeTime);
        if(executeTime > 0){
            solution.get(technicianID).addOneTask(executeTime, s);         //add this task
            solution.get(technicianID).calculateReturnTime(getDistance(
                    solution.get(technicianID).getLastTask().getTaskID(), 0));      //re-caculate the time of technician go back to original
            //scheduledTasks.add(s.getTaskID());                      //scheduledTasks remember this task
            addTaskIntoScheduledList(s.getTaskID(), true);
            
            return true;
        }
        return false;
    }
    
    //generate a naive solution, check technician one by one to see if it can schedule ordered task
    public void naiveSolution(){
        for(Task s : allSchedules){
            if(s.getTaskID() == 0)          //omit 0 task (all 0)
                continue;
            for(int j = 0; j < solution.size(); j++){
                if(checkAddOneTask(s, j) == true)
                    break;
            }
        }
    }
    
    //construct shortest distance solution, before  that we need to sort all tasks first
    public void constructiveShortDistanceSolution(){
        for(Task s : allSchedules){
            if(s.getTaskID() == 0)          //omit 0 task (all 0)
                continue;
            
            //initialize theses three variables, and run all technicians and store the best one
            bestTechID = -1;
            bestTechTravelTime = Integer.MAX_VALUE;
            bestTechExecuteTime = -1;
            for(int j = 0; j < solution.size(); j++){
                //check if this technician has enough skill first
                if(getSkill(s.getTaskID(), j) == false)
                    continue;
                
                TechnicianConflictSchedule(j, s);               //run this function, the best will be stored inside
            }
            if(bestTechID >= 0)
                checkAddOneTask(s, bestTechID);
        }
    }
    
    //construct random solution
    public void constructiveRandomSolution(){
        Random rd = new Random();
        int scheduleID;
        int techID;
        List<Integer> candidateTasks = new ArrayList<>();           //this list contains un-tried tasks ID
        for(Task s : allSchedules){
            if(s.getTaskID() == 0)
                continue;
            candidateTasks.add(s.getTaskID());      //initial all candidates (tasks) with their ID
        }
        
        //for(int i = 0; i < allSchedules.size() - 1; i++){
        while(candidateTasks.isEmpty() == false){
            int rdNumber = rd.nextInt(candidateTasks.size());
            scheduleID = candidateTasks.get(rdNumber);      //get Integer from list index
            Task s = getTaskFromID(scheduleID);             //get task from taskID
            
            techID = rd.nextInt(solution.size());           //start from a random technician
            for(int j = 0; j < solution.size(); j++){       //j is a counter, count the number of technician
                
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
        for(Task s : allSchedules){
            if(s.getTaskID() == 0)
                continue;
            candidateTasks.add(s.getTaskID());
        }
        
        int probExchange = 10;  //10$ to exchange tasks among technicians
        int probChange = 20;    //10% to change one task from T1 to T2
        int probSwap = 30;      //10% for a technician to drop one task and re-schedule another one
        int probAdd = 100;      //70% to add one task
        
        //System.out.println(candidateTasks.size());
        for(int i = 0; i < allSchedules.size() - 1; i++){
            
            int rdNumber;
            int rdProb = rd.nextInt(probAdd);
            if(i > 0 && rdProb < probExchange){
                exchangeTasksAmongTechnicians();    //doesn't care the return value true or false
                i--;        //dont change i
                continue;
            }else if(i > 0 && rdProb < probChange){
                rdNumber = rd.nextInt(candidateTasks.size());
                scheduleID = candidateTasks.get(rdNumber);
                Task droppedTask = addOneTaskWithDrop(scheduleID, false);  //to return dropped task? so we can add it back to the candidate
                if(droppedTask != null){
                    candidateTasks.remove(rdNumber);            //remove changed one
                    candidateTasks.add(droppedTask.getTaskID());    //add dropped one
                }
                i--;
                continue;
            }else if(i > 0 && rdProb < probSwap){
                //drop any task and re-schedule one
                //randomly delete one task and then re-schedule one 
                //this is implemented in neighbor selection
                i--;
                continue;
            }
            
            //probAdd 
            if(candidateTasks.size() > 0)
                rdNumber = rd.nextInt(candidateTasks.size());
            else
                break;
            scheduleID = candidateTasks.get(rdNumber);
            Task s = getTaskFromID(scheduleID);
            
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
    
    
    //constructive heuristic solution, new objective function
    //try add, swap = delete and add, exchange//, chang from one technician to another
    public void constructiveHeuristicSolution(){
        Random rd = new Random();
        int techID;
        List<Integer> candidateTasks = new ArrayList<>();
        for(Task s : allSchedules){
            if(s.getTaskID() == 0)
                continue;
            candidateTasks.add(s.getTaskID());
        }
        
        //System.out.println(candidateTasks.size());
        for(int i = 0; i < candidateTasks.size(); i++){
            
            int[] fitnessValue = new int[3];
            fitnessValue[0] = constructiveObjectFunction(0, getTaskFromID(candidateTasks.get(i)));
            Task dropTask = null;
            if(scheduledTasks.size() > 0){
                dropTask = getTaskFromID(scheduledTasks.get(rd.nextInt(scheduledTasks.size())));
                //System.out.println(dropTask.getTaskID());
                //System.out.println(Arrays.toString(scheduledTasks.toArray()));
            }
            fitnessValue[1] = constructiveObjectFunction(1, dropTask);
            fitnessValue[2] = constructiveObjectFunction(2, null);
            RouletteWheel rw = new RouletteWheel(fitnessValue);
            int probSelection = rw.spin();
            
            //exchange
            if(scheduledTasks.size() > 1 && probSelection == 2){
                exchangeTasksAmongTechnicians();    //doesn't care the return value true or false
                i--;        //dont change i
            }else if(scheduledTasks.size() > 0 && probSelection == 1){  //delete
                
                boolean findFlag = false;
                for(Technician t : solution){
                    List<Map.Entry<Integer, Task>> list = t.getSortExecuteTimeList();
                    if(list == null || list.isEmpty())
                        continue;
                    for(int taskNum = 0; taskNum < list.size(); taskNum++){
                        //System.out.println(list.get(taskNum).getValue().getTaskID());
                        if(list.get(taskNum).getValue().getTaskID() == dropTask.getTaskID()){
                            t.deleteOneTask(list.get(taskNum).getKey(), null);
                            removeTaskFromScheduedList(dropTask.getTaskID(), true);
                            findFlag = true;
                            break;
                        }
                    }
                    if(findFlag == true)
                        break;
                }
                if(findFlag == false){
                    System.out.println("??\n\n\n\n\n");
                    return;
                }
                i--;
                candidateTasks.add(dropTask.getTaskID());
            }else{
            
                Task s = getTaskFromID(candidateTasks.get(i));

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
                //candidateTasks.remove(i);    //if this task is scheduled, remove it from the candidate list
            }
        }
    }
    
    //constructive heuristic solution, new objective function
    //try add, swap = delete and add, exchange//, chang from one technician to another
    //argument: caseNum, determine when to break the loop (timeout)
    public void constructiveHeuristicSolution(int caseNum){
        Random rd = new Random();
        int techID;
        List<Integer> candidateTasks = new ArrayList<>();
        for(Task s : allSchedules){
            if(s.getTaskID() == 0)
                continue;
            candidateTasks.add(s.getTaskID());
        }
        
        //System.out.println(candidateTasks.size());
        //for(int i = 0; i < candidateTasks.size(); i++){
        long startTime = System.currentTimeMillis();
        //reach either rounds or specified time, loop will be over
        while((System.currentTimeMillis() - startTime) < PublicData.runLimitTime[caseNum] * 1000 ){    
            
            int[] fitnessValue = new int[3];
            int rdSchedule = rd.nextInt(candidateTasks.size());
            if(isTaskScheduled(candidateTasks.get(rdSchedule)) == true)
                continue;
            fitnessValue[0] = constructiveObjectFunction(0, getTaskFromID(candidateTasks.get(rdSchedule)));
            Task dropTask = null;
            if(scheduledTasks.size() > 0){
                dropTask = getTaskFromID(scheduledTasks.get(rd.nextInt(scheduledTasks.size())));
                //System.out.println(dropTask.getTaskID());
                //System.out.println(Arrays.toString(scheduledTasks.toArray()));
            }
            fitnessValue[1] = constructiveObjectFunction(1, dropTask);
            fitnessValue[2] = PublicData.exaLargestParameter * constructiveObjectFunction(2, null);
            RouletteWheel rw = new RouletteWheel(fitnessValue);
            int probSelection = rw.spin();
            
            //exchange
            if(scheduledTasks.size() > 1 && probSelection == 2){
                //exchangeTasksAmongTechnicians();    //doesn't care the return value true or false
                //i--;        //dont change i
                //continue;
                exchangeTasksAmongTechniciansUsingGreedy();
            }else if(scheduledTasks.size() > 0 && probSelection == 1){  //delete
                
                /*
                boolean findFlag = false;
                for(Technician t : solution){
                    List<Map.Entry<Integer, Task>> list = t.getSortExecuteTimeList();
                    if(list == null || list.isEmpty())
                        continue;
                    for(int taskNum = 0; taskNum < list.size(); taskNum++){
                        //System.out.println(list.get(taskNum).getValue().getTaskID());
                        if(list.get(taskNum).getValue().getTaskID() == dropTask.getTaskID()){
                            t.deleteOneTask(list.get(taskNum).getKey(), null);
                            removeTaskFromScheduedList(dropTask.getTaskID(), true);
                            findFlag = true;
                            break;
                        }
                    }
                    if(findFlag == true)
                        break;
                }
                if(findFlag == false){
                    System.out.println("??\n\n\n\n\n");
                    return;
                }
                //i--;
                //candidateTasks.add(dropTask.getTaskID());
                continue;
                */
                
                int techNum = rd.nextInt(solution.size());
                boolean loopflag = false;
                for(int i = techNum; loopflag == false || i < techNum; ){
                    //remove the old codes which define sorted tasks, if necessary, refer those back-up codes
                    List<Map.Entry<Integer, Task>> list = solution.get(i).getSortExecuteTimeList();
                    int worstTaskIndex = selectWorstTaskObjectiveFunctionValue(i, list);
                    if(worstTaskIndex < 0){
                        i++;                            //we try from the random start point, and when we reach the end we should go back to the very beginning
                        if(i >= solution.size()){
                            loopflag = true;
                            i = 0;
                        }
                        continue;
                    }
                    //delete t1Task
                    int taskExecuteTime = list.get(worstTaskIndex).getKey();
                    Task task = solution.get(i).getTaskFromExecuteTime(taskExecuteTime);
                    solution.get(i).deleteOneTask(taskExecuteTime, task);
                    removeTaskFromScheduedList(task.getTaskID(), false);          //remove this task 
                    break;
                }
                
            }else{
            
                Task s = getTaskFromID(candidateTasks.get(rdSchedule));

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
                //candidateTasks.remove(i);    //if this task is scheduled, remove it from the candidate list
            }
        }
    }
    
    //operationType, add, delete, exchange, change, 
    //taskID
    //return new total priority / total processtime
    public int constructiveObjectFunction(int operationType, Task s){
        int totalPrio = totalPriority();
        int totalProcessTime = totalProcessTime();
        double ret = 0.00;
        
        switch(operationType){
            case 0:     //add
                //System.out.printf("s.getPriority()=%d, s.getProcessTime()=%d\n",s.getPriority(), s.getProcessTime());
                if(s != null)
                    ret = PublicData.exaParameter * (double)(totalPrio + s.getPriority()) / (double) (totalProcessTime + s.getProcessTime());
                //System.out.print(ret);
                break;
            case 1:     //delete
                if(s != null && totalProcessTime > s.getProcessTime())
                    ret = PublicData.exaParameter * (double) (totalPrio - s.getPriority()) / (double) (totalProcessTime - s.getProcessTime());
                break;
            default:
                if(totalProcessTime > 0)
                    ret = PublicData.exaParameter * (double) totalPrio / (double) totalProcessTime;
                break;
        }
        return (int) (ret);
    }
    
    
    //reset this solution
    public void resetSolution(){
        
        count = 0;
        for(Technician t : solution){   //all technicians clear all tasks
            t.removeAllTasks();
        }
        
        scheduledTasks.clear();             //scheduledTasks clear
        constructiveRandomSolution();       //re-construct random solution
    }
    
    
    //try add un-scheduled tasks
    public boolean tryAddFromUnscheduled(){
        boolean add = false;
        for(int j = 0; j < solution.size(); j++){
            for(Task s : allSchedules){
                if(s.getTaskID() == 0)
                    continue;
                if(scheduledTasks.contains(s.getTaskID()) == true)
                    continue;
                
                if(checkAddOneTask(s, j) == true)
                    add = true;
            }
        }
        return add;
    }
    
    //sort all tasks in these three orders, which is used for greedy heuristic
    public void greedySortSchedulesPriorityProcessTime(){
        Collections.sort(allSchedules, Task.PriorityWithProcessTime);
    }
    
    public void greedySortSchedulesPriority(){
        Collections.sort(allSchedules, Task.Priority);
    }
    
    public void greedySortSchedulesProcessTime(){
        Collections.sort(allSchedules, Task.ProcessTime);
    }
    
    public String solutionToString(){
        String str = "\nsolution:\n";
        for(int i = 0; i < solution.size(); i++){
            str += "technician: ";
            str += solution.get(i).parameterToString();
            str += "\n";
            str += solution.get(i).scheduledTasksToString();
        }
        return str;
    }
    
    //argument 1; taskID to be added
    //argument 2: if we want to check if this taskID in the list or not
    public void addTaskIntoScheduledList(int taskID, boolean check){
        if(check == true){
            for(int id = 0; id < scheduledTasks.size(); id++){
                if(scheduledTasks.get(id) == taskID){
                    return;
                }
            }
        }
        scheduledTasks.add(taskID);
    }
    
    //remove one taskID from the list
    public void removeTaskFromScheduedList(int taskID, boolean checkAll){
        for(int id = 0; id < scheduledTasks.size(); id++){
            if(scheduledTasks.get(id) == taskID){
                scheduledTasks.remove(id);
                
                if(checkAll)
                    continue;
                
                break;
            }
        }
    }
    
    public int getDistance(int taskn1, int taskn2){
        double euclideanDist;
        double x = taskPosition[taskn1][0] - taskPosition[taskn2][0];
        double y = taskPosition[taskn1][1] - taskPosition[taskn2][1];
        euclideanDist = Math.sqrt(x * x + y * y);
        
        int roundV = (int) euclideanDist;
        double floorOrCeil = euclideanDist - (double) roundV;
        if(floorOrCeil < 0.5)
            return roundV;
        else 
            return roundV + 1;
        //return ((int) euclideanDist + 1);   //ceiling 
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
    private int TechnicianConflictSchedule(int techNumber, Task s){
        int availExecuteTime = 0;
        int travelTime1;
        int travelTime2 = 0;
        Technician t = solution.get(techNumber);
        
        //if empty, t.startTime + travelTime <= executeTime <= t.endTime - travelTime
        if(t.getScheduledTask().isEmpty()){            //if there is no schedules for this technician
            
            travelTime1 = getDistance(0, s.getTaskID());
            travelTime2 = travelTime1;
            availExecuteTime = getExecuteTime(s.getStartTime(), s.getEndTime()-s.getProcessTime(), 
                    t.getStartTime() + getDistance(0, s.getTaskID()),
                    t.getEndTime() - getDistance(0, s.getTaskID()) - s.getProcessTime());
        }
        else{
            //otherwise, check every gap and see if this sehedule can be scheduled
            //old codes are removed and optimized. if necessary, check the back-up codes.
            List<Map.Entry<Integer, Task>> list = solution.get(techNumber).getSortExecuteTimeList();
            if(list == null || list.isEmpty())
                return -1;
            
            travelTime1 = getDistance(0, s.getTaskID());
            int previousEndTimePlusTravelTime = t.getStartTime() 
                   // + instance.getDistance(0, schedules.get(sortedKey.get(0)).getTaskID());
                    + travelTime1;           //this new schedule and original distance
            int nextStartTimeMinusTravelTime;
            for(int i = 0; i <= list.size(); i++){
                if(i > 0){
                    //last executeTime + processTime + travelTime
                    travelTime1 = getDistance(list.get(i-1).getValue().getTaskID(), s.getTaskID());
                    previousEndTimePlusTravelTime = list.get(i-1).getKey() + list.get(i-1).getValue().getProcessTime()
                            + travelTime1;  
                    //System.out.printf("i>0, distance=%d\n",instance.getDistance(schedules.get(sortedKey.get(i-1)).getTaskID(), s.getTaskID()));
                }    
                if(i == list.size())  {              //last schedule, distance to origin point
                    travelTime2 = getDistance(s.getTaskID(), 0);
                    nextStartTimeMinusTravelTime = t.getEndTime() 
                            - travelTime2 - s.getProcessTime();

                    //System.out.printf("i==sortedkey.size, distance=%d\n",instance.getDistance(s.getTaskID(), 0));
                }
                else{
                    travelTime2 = getDistance(s.getTaskID(), list.get(i).getValue().getTaskID());
                    nextStartTimeMinusTravelTime = list.get(i).getKey()        //distance to this point
                            - travelTime2 - s.getProcessTime();
                    //System.out.printf("0<i<size, distance=%d\n",instance.getDistance(s.getTaskID(), schedules.get(sortedKey.get(i)).getTaskID()));
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
    
    //check if one task is scheduled in this solution or not
    public boolean isTaskScheduled(int scheduleID){
        for(Integer s : scheduledTasks){
            if(s == scheduleID)
                return true;
        }
        return false;
    }
    
    //get task from taskID
    public Task getTaskFromID(int scheduleID){
        for(Task s : allSchedules){
            if(s.getTaskID() == scheduleID){
                return s;
            }
        }
        return null;
    }
    
    //add one task without dropping any task
    public boolean addOneTaskWithoutDrop(int scheduleID){
        if(isTaskScheduled(scheduleID) == true)
            return false;
        if(scheduleID == 0)
            return false;
        Task task = getTaskFromID(scheduleID);
        if(task == null)
            return false;
        for(int j = 0; j < solution.size(); j++){   //try all technicians and see if one can add it
            if(checkAddOneTask(task, j) == true)
                return true;
        }
        return false;
    }
    
    //if succeed, return one schedule which is dropped, otherwise, return null
    public Task addOneTaskWithDrop(int scheduleID, boolean allowNotBackup){
        //drop one task whose execute time is in the window of this new task
        Random r = new Random();
        int rdNumber = r.nextInt(solution.size());  //start check from a random-number of technician
        boolean repeatFlag = true;                  //help start from a random technician and make sure it tries all technicians
        Task backupS;
        Task task = getTaskFromID(scheduleID);
        for(int i = rdNumber; repeatFlag == true || i < rdNumber; ){
            
            //remove the old codes which define sorted tasks, if necessary, refer those back-up codes
            List<Map.Entry<Integer, Task>> list = solution.get(i).getSortExecuteTimeList();
            
            for(int num = 0; list != null && num < list.size(); num++){         //try all tasks in one technician
                
                int executeTime = list.get(num).getKey();
                
                if(executeTime > task.getStartTime() + 50 && executeTime < task.getEndTime()){  //+50 for travel time
                    backupS = solution.get(i).getScheduledTask().get(executeTime);
                    
                    if(task.getPriority() <= backupS.getPriority())     //only like higher priority
                        continue;
                    
                    //delete the task first, and see if it can add new one
                    solution.get(i).deleteOneTask(executeTime, null);// getScheduledTask().remove(executeTime);
                    removeTaskFromScheduedList(backupS.getTaskID(), false);
                    
                    if(checkAddOneTask(task, i) == true){       //if it add successfully, return succeed
                        return backupS;
                    }else{
                        
                        //leave some probabality for solution not backup
                        Random rd = new Random();
                        int j = rd.nextInt(10);
                        if(allowNotBackup == false || (allowNotBackup && j < 5))     //50% percentage not backup
                        {
                            solution.get(i).addOneTask(executeTime, backupS);
                            //scheduledTasks.add(backupS.getTaskID());
                            addTaskIntoScheduledList(backupS.getTaskID(), true);
                        }else{                  //if we dont recover, then we return, which means the task is not added but we drop one task
                            return null;
                        }
                    }
                }
            }
            
            i++;                            //we try from the random start point, and when we reach the end we should go back to the very beginning
            if(i >= solution.size()){
                repeatFlag = false;
                i = 0;
            }
        }
        
        return null;
    }
    
    //exchange tasks among two random technicians
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
        if(t1TaskExecuteTime < 0)
            return false;
        Task t1Task = solution.get(t1).getTaskFromExecuteTime(t1TaskExecuteTime);
        solution.get(t1).deleteOneTask(t1TaskExecuteTime, t1Task);
        //System.out.printf("t1 ExeTime = %d, t1Task = %d\n", t1TaskExecuteTime, t1Task.getTaskID());
        
        //see if there is one task in the second technician so that they can exchange and shorten travel time
        Task t2Task = oneTechnicianAddWithDrop(t2, t1Task);
        if(t2Task == null){         //t2 fail
            //System.out.println("t2 cannot delete any task and add t1Task");
            solution.get(t1).addOneTask(t1TaskExecuteTime, t1Task);    //t1 add back, exchange fail
            return false;
        }
        
        //System.out.println(Arrays.toString(scheduledTasks.toArray()));
        
        //continue to see if t1 can add this schedule or not
        if(checkAddOneTask(t2Task, t1) == true){        //can add to t1
            //System.out.println("t1, t2 exchange correctly");
            //System.out.println(Arrays.toString(scheduledTasks.toArray()));
            return true;
        }else{
            //System.out.println("t1 cannot add t2's task and both recover");
            //System.out.println(Arrays.toString(scheduledTasks.toArray()));
            //t2 need to remove t1Task and add t2Task
            solution.get(t2).deleteRecentAddTask();
            checkAddOneTask(t2Task, t2);
            
            //t1 need to add t1task
            solution.get(t1).addOneTask(t1TaskExecuteTime, t1Task);
            return false;
        }
    }
    
    //add new task, if succeed, return the dropped task
    public Task oneTechnicianAddWithDrop(int techID, Task s){
        List<Map.Entry<Integer, Task>> list = solution.get(techID).getSortExecuteTimeList();
        if(list == null)
            return null;
        int executeTime;
        Task task;
        for(int i = 0; i < list.size(); i++){
            executeTime = list.get(i).getKey();
            task = list.get(i).getValue();
            
            solution.get(techID).deleteOneTask(executeTime, task);     //delete the backup task first
            removeTaskFromScheduedList(task.getTaskID(), false);        //delete from scheduled list
            
            if(checkAddOneTask(s, techID)){ //if succeed, then return
                return task;
            }else{                      //recover the deleted, and add it back to scheduled list
                solution.get(techID).addOneTask(executeTime, task);
                addTaskIntoScheduledList(task.getTaskID(), false);
            }
        }
        return null;
    }
    
    
    //shrink tasks scheduled in this technician
    //strategy: 1.move the last task to the end, 2.expand the largest gap by moving previous task and afterward task
    public void shrinkTasks(){
        
        for(int techNumber = 0; techNumber < solution.size(); techNumber++){
        
            List<Map.Entry<Integer, Task>> sortedList = solution.get(techNumber).getSortExecuteTimeList();
            if(sortedList == null || sortedList.isEmpty())
                continue;
      
            //first, check if we can move last task to the end, if yes, we succeed and return
            int lastExecuteTime = sortedList.get(sortedList.size() - 1).getKey();
            Task lastTask = sortedList.get(sortedList.size() - 1).getValue();
            
            int distanceFromLastTaskToOrigin = getDistance(lastTask.getTaskID(), 0);
            
            int latestGoBackToOriginTime = solution.get(techNumber).getEndTime() - distanceFromLastTaskToOrigin;
            int latestOriginTime = latestGoBackToOriginTime - lastTask.getProcessTime();
            int latestFinishTime = lastTask.getEndTime() - lastTask.getProcessTime();
                    
            //if last task have time to goback to origin later, and this task can be finished later
            if(latestOriginTime > lastExecuteTime && latestFinishTime > lastExecuteTime){
                int minValue = Math.min(latestOriginTime, latestFinishTime);
                
                //System.out.printf("techNumber=%d, lastExecuteTime=%d. latestOriginTime=%d, latestFinishTime=%d, minValue=%d\n", 
                //        techNumber, lastExecuteTime, latestOriginTime, latestFinishTime, minValue);
                solution.get(techNumber).deleteOneTask(lastExecuteTime, null);
                solution.get(techNumber).addOneTask(minValue, lastTask);
                
            }
            
            //only move the last task to the end is good enought for this case
            
            /*
            //gap of each two tasks
            int[] gap = new int[sortedList.size()];
            int lastScheduleID = 0;       //ID
            int lastScheduleEndTime = solution.get(techNumber).getStartTime();
            int thisScheduleID;        //last task's end time
            for(int i = 0; i < sortedList.size(); i++){
                thisScheduleID = sortedList.get(i).getValue().getTaskID();
                gap[i] = sortedList.get(i).getKey() - getDistance(lastScheduleID, thisScheduleID) - lastScheduleEndTime;
                //System.out.printf(" ,%d", gap[i]);
                lastScheduleID = thisScheduleID;
                lastScheduleEndTime = sortedList.get(i).getKey() + sortedList.get(i).getValue().getProcessTime();
            }
            System.out.println();
            
            //try to expand the largest gap, to help schedule one more task
            int largestGap = 0;
            int largestGapIndex = 0;
            for(int i = 0; i < gap.length; i++){
                if(gap[i] > largestGap){
                    largestGapIndex = i;
                    largestGap = gap[i];
                }
            }
            
            //try move previous task, if it's the first task, no need to check since the first one is scheduled as early as possible
            if(largestGapIndex > 0){
            }
            
            */
        }
        
        
    }
    
    //before call this method, should confirm if the target is better than this one
    //this solution, try drop all tasks at this technician, and schedule all tasks from specificed list
    public boolean tryExchnageWholeTechnician(int techID, List<Map.Entry<Integer, Task>> sortedList){
        
        if(sortedList == null || sortedList.isEmpty()){
            return false;
        }
        
        //first, check if this technician can schedule all tasks in this sortedList
        //any task in sortedList can not be scheduled by other technicians
        int currentTaskID;
        boolean findFlag;
        List<Map.Entry<Integer, Task>> thisTechSortedList = solution.get(techID).getSortExecuteTimeList();  //get this sortedList
        if(thisTechSortedList == null || thisTechSortedList.isEmpty()){                     //must be scheduled in another technician
            return false;
        }
        
        for(int i = 0; i < sortedList.size(); i++){
            
            currentTaskID = sortedList.get(i).getValue().getTaskID();
            
            if(scheduledTasks.contains(currentTaskID) == false){        //this task is not scheduled in the solution, ok
                continue;
            }
            findFlag = false;
            for(int j = 0; j < thisTechSortedList.size(); j++){
                if(thisTechSortedList.get(j).getValue().getTaskID() == currentTaskID){
                    findFlag = true;
                    break;
                }
            }
            if(findFlag == false)                                       //this task is not scheduled in the same technician, then conflict
                return false;
            
        }
        
        List<Integer> scheduledByThisTech = new ArrayList<>();          //back up all scheduled tasks list, to delete
        for(int j = 0; j < thisTechSortedList.size(); j++){
            scheduledByThisTech.add(thisTechSortedList.get(j).getValue().getTaskID());
        }
        
        solution.get(techID).removeAllTasks();                      //delete 
        scheduledTasks.removeAll(scheduledByThisTech);                  //delete
        
        for(int i = 0; i < sortedList.size(); i++){                     //add 
            solution.get(techID).addOneTask(sortedList.get(i).getKey(), sortedList.get(i).getValue());
        }
        
        return true;
    }
    
    //only drop worst tasks to try add a task
    public Task addOneTaskWithDropUsingGreedy(int scheduleID, boolean allowNotBackup){
        //drop one task whose execute time is in the window of this new task
        Random r = new Random();
        int rdNumber = r.nextInt(solution.size());  //start check from a random-number of technician
        boolean repeatFlag = true;                  //help start from a random technician and make sure it tries all technicians
        Task task = getTaskFromID(scheduleID);
        for(int i = rdNumber; repeatFlag == true || i < rdNumber; ){
            
            //remove the old codes which define sorted tasks, if necessary, refer those back-up codes
            List<Map.Entry<Integer, Task>> list = solution.get(i).getSortExecuteTimeList();
            int worstTaskIndex = selectWorstTaskObjectiveFunctionValue(i, list);
            if(worstTaskIndex < 0){
                i++;                            //we try from the random start point, and when we reach the end we should go back to the very beginning
                if(i >= solution.size()){
                    repeatFlag = false;
                    i = 0;
                }
                continue;
            }
            //delete t1Task
            int taskExecuteTime = list.get(worstTaskIndex).getKey();
            Task dropTask = solution.get(i).getTaskFromExecuteTime(taskExecuteTime);
            solution.get(i).deleteOneTask(taskExecuteTime, dropTask);
            removeTaskFromScheduedList(dropTask.getTaskID(), false);          //remove this task 

            if(checkAddOneTask(task, i) == true){       //if it add successfully, return succeed
                return dropTask;
            }else{

                //leave some probabality for solution not backup
                int j = r.nextInt(10);
                if(allowNotBackup == false || (allowNotBackup && j < 1))     //10% percentage not backup
                {
                    solution.get(i).addOneTask(taskExecuteTime, dropTask);
                    addTaskIntoScheduledList(dropTask.getTaskID(), true);
                }else{                  //if we dont recover, then we return, which means the task is not added but we drop one task
                    return null;
                }
            }
             
            i++;                            //we try from the random start point, and when we reach the end we should go back to the very beginning
            if(i >= solution.size()){
                repeatFlag = false;
                i = 0;
            }
        }
        
        return null;
    }
    
    //exchange tasks among two random technicians
    //each technician selection worst task to be exchanged
    public boolean exchangeTasksAmongTechniciansUsingGreedy(){
        
        //select two random technicians
        int t1, t2;
        Random rd = new Random();
        t1 = t2 = rd.nextInt(solution.size());
        while(t2 == t1){
            t2 = rd.nextInt(solution.size());
        }
        //System.out.printf("exchangeTasks\n t1 = %d, t2 = %d\n", t1, t2);
        
        //select worst task from the first technician
        List<Map.Entry<Integer, Task>> t1SortedList = solution.get(t1).getSortExecuteTimeList(); 
        int worstTaskIndex = selectWorstTaskObjectiveFunctionValue(t1, t1SortedList);
        if(worstTaskIndex < 0)
            return false;
        
        //select worst task from the second technician
        List<Map.Entry<Integer, Task>> t2SortedList = solution.get(t2).getSortExecuteTimeList(); 
        int worstTaskIndex2 = selectWorstTaskObjectiveFunctionValue(t2, t2SortedList);
        if(worstTaskIndex2 < 0){                        //cannot find one to exchange
            return false;
        }
        
        //delete t1Task
        int t1TaskExecuteTime = t1SortedList.get(worstTaskIndex).getKey();
        Task t1Task = solution.get(t1).getTaskFromExecuteTime(t1TaskExecuteTime);
        solution.get(t1).deleteOneTask(t1TaskExecuteTime, t1Task);
        removeTaskFromScheduedList(t1Task.getTaskID(), false);          //remove this task 
        //System.out.printf("\n\nt1 ExeTime = %d, t1Task = %d\n", t1TaskExecuteTime, t1Task.getTaskID());
        
        //delete t2Task
        int t2TaskExecuteTime = t2SortedList.get(worstTaskIndex2).getKey();
        Task t2Task = solution.get(t2).getTaskFromExecuteTime(t2TaskExecuteTime);
        solution.get(t2).deleteOneTask(t2TaskExecuteTime, t2Task);
        removeTaskFromScheduedList(t2Task.getTaskID(), false);          //remove this task 
        //System.out.printf("t2 ExeTime = %d, t2Task = %d\n", t2TaskExecuteTime, t2Task.getTaskID());
        
        //continue to see if t1 can add this schedule or not
        //if there is one technician can add successfully, we add it, the other one just delete, like drop
        if(checkAddOneTask(t2Task, t1) == true && checkAddOneTask(t1Task, t2) == true){        
            //System.out.println("t1, t2 exchange correctly");
            //System.out.println(Arrays.toString(scheduledTasks.toArray()));
            return true;
        }else{
            //System.out.println("recover all tasks");
            
            solution.get(t1).deleteOneTask(t2Task.getTaskID());
            solution.get(t2).deleteOneTask(t1Task.getTaskID());
            
            solution.get(t1).addOneTask(t1TaskExecuteTime, t1Task);
            addTaskIntoScheduledList(t1Task.getTaskID(), true);
            solution.get(t2).addOneTask(t2TaskExecuteTime, t2Task);
            addTaskIntoScheduledList(t2Task.getTaskID(), true);
            return false;
        }
    }
    
    //this is to make the probability larger for best candidate
    public void exaFunctionValues(int[] time){
        int largestIndex = -1;
        int largestValue = 0;
        int secondLargestIndex = -1;
        int secondLargestValue = 0;
        
        //first find the largest two tasks
        for(int i = 0; i < time.length; i++){
            if(time[i] > largestValue){                 //if maximum
                secondLargestValue = largestValue;
                secondLargestIndex = largestIndex;
                largestValue = time[i];
                largestIndex = i;
            }else if(time[i] > secondLargestValue){           //if second maximum
                secondLargestValue = time[i];
                secondLargestIndex = i;
            }
        }
        
        //exa the largest two values
        for(int i = 0; i < time.length; i++){
            if(i == largestIndex){
                time[i] *= PublicData.exaLargestParameter;
            }else if(i == secondLargestIndex){
                time[i] *= PublicData.exaSecondLargestParameter;
            }
        }
        
    }
    
    //when doing neighbor selection, alsway choose best task for other neighbors
    //return the index of selected task
    public int selectBestTaskObjectiveFunctionValue(int techID, List<Map.Entry<Integer, Task>> thisTechSortedList){
        
        //List<Map.Entry<Integer, Task>> thisTechSortedList = solution.get(techID).getSortExecuteTimeList();  //get this sortedList
        if(thisTechSortedList == null || thisTechSortedList.isEmpty()){                     //must be scheduled in another technician
            return -1;
        }
        
        //consider priority and travel time and wait time
        //we consider the time spend related with this task
        //eg., right after previous task is done, the technician can come to this task, 
        //tavel time and wait time may happen //and then process time may happen
        //and then the technician has to go to next task's place, 
        //right before next task execute, travel time and wait time may happen
        //we can count all these time as the time spent in this task; 
        //notice some time are calculated repeatedly because better task may have smaller wait time, which means more tasks can be scheduled
        
        int previousTaskEndTime;
        int nextTaskExecuteTime;
        int[] time = new int[thisTechSortedList.size()];
        
        //from previousTaskEndTime to nextTaskExecuteTIme
        for(int i = 0; i < thisTechSortedList.size(); i++){
            
            if(i == 0)
                previousTaskEndTime = solution.get(techID).getStartTime();
            else    //previous task's execute time + process time
                previousTaskEndTime = thisTechSortedList.get(i-1).getKey() + thisTechSortedList.get(i-1).getValue().getProcessTime();
            
            if(i == thisTechSortedList.size() - 1)
                nextTaskExecuteTime = solution.get(techID).getEndTime();
            else    //next task's execute time
                nextTaskExecuteTime = thisTechSortedList.get(i+1).getKey();
            
            time[i] = PublicData.exaParameter * thisTechSortedList.get(i).getValue().getPriority() 
                    / (nextTaskExecuteTime - previousTaskEndTime);
        }
        exaFunctionValues(time);
        
        //then apply roulette wheel selection
        RouletteWheel rw = new RouletteWheel(time);
        return rw.spin();
    }
    
    //when doing swap, exchange, alsway drop worst task, for better tasks
    //return the index of selected task
    public int selectWorstTaskObjectiveFunctionValue(int techID, List<Map.Entry<Integer, Task>> thisTechSortedList){
        
        //List<Map.Entry<Integer, Task>> thisTechSortedList = solution.get(techID).getSortExecuteTimeList();  //get this sortedList
        if(thisTechSortedList == null || thisTechSortedList.isEmpty()){                     //must be scheduled in another technician
            return -1;
        }
        
        //consider priority and travel time and wait time
        //we consider the time spend related with this task
        //eg., right after previous task is done, the technician can come to this task, 
        //tavel time and wait time may happen //and then process time may happen
        //and then the technician has to go to next task's place, 
        //right before next task execute, travel time and wait time may happen
        //we can count all these time as the time spent in this task; 
        //notice some time are calculated repeatedly because better task may have smaller wait time, which means more tasks can be scheduled
        
        int previousTaskEndTime;
        int nextTaskExecuteTime;
        int[] time = new int[thisTechSortedList.size()];
        
        //from previousTaskEndTime to nextTaskExecuteTIme
        for(int i = 0; i < thisTechSortedList.size(); i++){
            
            if(i == 0)
                previousTaskEndTime = solution.get(techID).getStartTime();
            else    //previous task's execute time + process time
                previousTaskEndTime = thisTechSortedList.get(i-1).getKey() + thisTechSortedList.get(i-1).getValue().getProcessTime();
            
            if(i == thisTechSortedList.size() - 1)
                nextTaskExecuteTime = solution.get(techID).getEndTime();
            else    //next task's execute time
                nextTaskExecuteTime = thisTechSortedList.get(i+1).getKey();
            
            time[i] = (nextTaskExecuteTime - previousTaskEndTime) / thisTechSortedList.get(i).getValue().getPriority();
        }
        
        exaFunctionValues(time);
        
        //then apply roulette wheel selection
        RouletteWheel rw = new RouletteWheel(time);
        return rw.spin();
    }
    
    //when doing neighbor selection, alsway choose best task for other neighbors
    //return the index of selected task
    //here, we select by priority/processTime
    public int selectBestTaskPriorityProcessTime(int techID, List<Map.Entry<Integer, Task>> thisTechSortedList){
        
        //List<Map.Entry<Integer, Task>> thisTechSortedList = solution.get(techID).getSortExecuteTimeList();  //get this sortedList
        if(thisTechSortedList == null || thisTechSortedList.isEmpty()){                     //must be scheduled in another technician
            return -1;
        }
        
        int[] time = new int[thisTechSortedList.size()];
        
        //from previousTaskEndTime to nextTaskExecuteTIme
        for(int i = 0; i < thisTechSortedList.size(); i++){
            time[i] = PublicData.exaParameter * thisTechSortedList.get(i).getValue().getPriority() 
                    / thisTechSortedList.get(i).getValue().getProcessTime();
        }
        
        exaFunctionValues(time);
        
        //then apply roulette wheel selection
        RouletteWheel rw = new RouletteWheel(time);
        return rw.spin();
    }
    
    
    //return solution list in case
    public List<Technician> getSolution(){
        return solution;
    }
    
    //return one random scheduled Task
    public int getOneScheduledTask(){
        if(scheduledTasks.isEmpty())
            return -1;
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
            Task s = getTaskFromID(rdNumber);
            if(s.getPriority() < prio || (acceptSame == true && s.getPriority() == prio)){
                return s.getTaskID();
            }
            i++;
            if(i >= scheduledTasks.size()){
                i = 0; 
                flag = false;
            }
        }
        return -1;
    }
    
    //return one scheduled Task from one techID, using greedy method
    //argument: objFunFlag, if true, use the object function value, otherwise, use priority/processTime
    public int getScheduledTaskUsingGreedy(boolean objFunFlag){
        Random rd = new Random();
        int techID = rd.nextInt(solution.size());
        
        //get sorted list of all tasks, select by probability of selectBestTaskObjectiveFunctionValue
        List<Map.Entry<Integer, Task>> techSortedList = solution.get(techID).getSortExecuteTimeList();  //get this sortedList
        
        int taskIndex;
        if(objFunFlag)
            taskIndex = selectBestTaskObjectiveFunctionValue(techID, techSortedList);
        else
            taskIndex = selectBestTaskPriorityProcessTime(techID, techSortedList);
            
        if(taskIndex < 0 || taskIndex >= techSortedList.size())
            return -1;
        
        return techSortedList.get(taskIndex).getValue().getTaskID();
    }
    
    public void setID(int id) { this.ID = id; }
    public int getID() { return this.ID; }
    public int getCount() { return this.count; }
    public void setCount(int count) { this.count = count; }
    public void addCount() { this.count++; }
    
    public boolean getSkill(int taskNum, int techNum){
        return skill[taskNum][techNum] != 0;
    }
    
    //judge solution's score by total priority
    public int totalPriority(){
        int totalValue = 0;
        for(Technician t : solution){
            totalValue += t.getTotalPriority();
        }
        return totalValue;
    }
    
    //get total process time of all technicians
    public int totalProcessTime(){
        int totalValue = 0;
        for(Technician t : solution){
            totalValue += t.getTotalProcessTime();
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
