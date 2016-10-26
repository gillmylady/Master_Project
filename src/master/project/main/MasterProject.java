/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.main;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import master.project.problem.*;
/**
 *
 * @author gillmylady
 */

/*
1. add test features, try:
    a. if one technician has conflict among scheduled tasks
    b. if one task is scheduled more than once
2. gap of technician, 压缩, to try schedule more tasks
3. exchange two schedules in two technicians
3. Now, set executeTime <= endTime - processTime
4. consider if allowing backup, since sometimes dont backup and we will jump out local optimal solution

develop:
1. basic ABC, either allowing or not allowing onlooker bee
2. ABC + xxxxx, allow swap and exchange
3. ABC + yyyyy, allow shortest technician
4. ABC + zzzzz, allow shrink time of each technician (try move backward)

conclusion: increasing number of bees might not help, because more steps are not helpful. (random solution too bad)

*/

public class MasterProject {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        // TODO code application logic here
        
        Instance ss = new Instance("/Users/gillmylady/NetBeansProjects/Master_Project/instances/FTSP_R_12_9.txt");
        System.out.println(ss.getTaskNumber());
        System.out.println();
        
        AbcBasicAlgorithm abc = new AbcBasicAlgorithm(PublicData.totalBeeNumber20, ss);
        abc.RunBasicABCAlgorithm(50000, false);
        
        
        /*abc.displayAllSolution();
        
        abc.allSolutionsTryAdd(ss);
        abc.displayAllSolution();
        */
        
        
    }
    
}
