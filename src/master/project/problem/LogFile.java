/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master.project.problem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



/**
 *
 * @author gillmylady
 */
public class LogFile {
    
    File fileName;
    BufferedWriter out;
    
    public LogFile(String filePath) {
        fileName = new File(filePath);
        if(!fileName.exists()){
            try {
                fileName.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        try {
            //System.out.println(fileName.createNewFile());               //create new file
            
            out = new BufferedWriter(new FileWriter(fileName.getAbsoluteFile()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void writeFile(String data){
        
        try {
            out.write(data);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public void closeFile(){
        
        try {
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
           
}
