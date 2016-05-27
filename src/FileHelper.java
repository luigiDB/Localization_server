/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giulio
 */
public class FileHelper {
    private File file;
    private String FileName;
    private String FolderPath;
    private FileOutputStream fbOut;
    private FileWriter fb;

    public FileHelper(String FileName) {
        if(FileName == null){
            System.err.println("ARRFParser: bad parameter");
            return;
        }
        this.FileName = FileName;
        this.FolderPath = "";
        this.file = null;
        this.fb = null;
        //Create the file
        createFile();
    }
    public FileHelper(String FileName, String FolderPath) {
        if(FileName == null && FolderPath == null){
            System.err.println("ARRFParser: bad parameters");
            return;
        }
            
        this.FileName = FileName;
        this.FolderPath = FolderPath;
        this.file = null;
        this.fb = null;
        //Create the file
        createFile();
    }

    public boolean saveFile(byte[] fileByte, int dim){
        try {
            fbOut.write(fileByte, 0, dim);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void createFile(){
        try {
            file = new File(FolderPath, FileName);
            // creates the file
            if(file.exists()){
                Integer appendix = 1;
                while(true){
                    String[] splitted = FileName.split("\\.");
                    String FileNameTemp = splitted[0] + appendix.toString() + "." + splitted[1];
                    file = new File(FolderPath, FileNameTemp);
                    if(!file.exists()){
                        FileName = FileNameTemp;
                        break;
                    }
                    appendix++;
                }                
            }
            boolean res = file.createNewFile();
            if(!res){
                file = null;
                System.err.println("ARRFParser: error in creating the file");
            }
 
            fbOut = new FileOutputStream(file);
            fb = new FileWriter(file);
        } catch (IOException ex) {
            Logger.getLogger(FileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //L'ultimo attributo dovrà essere la classe messo in questa forma: "class", "{c1, c2, c3}"
    public boolean setAttributes(LinkedHashMap<String,String> attributes){
        if(attributes == null)
                return false;
        try {
            String relationName = FileName.split("\\.")[0]; 
            fb.write("@RELATION " + relationName + "\n");
            Set<String> key = attributes.keySet();
            for(String str : key){
                String attrType = attributes.get(str);
                fb.write("@ATTRIBUTE " + str + " " + attrType + "\n");
            }
            fb.write("@DATA\n");
        } catch (IOException ex) {
            Logger.getLogger(FileWriter.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return true;
    }
    
    public boolean writeDataRow(String[] attrValues){
        if(attrValues == null)
                return false;
        try {
            fb.write("\n");
            for(int i = 0; i < attrValues.length; i++){   
                fb.write(attrValues[i]);
                if(i < attrValues.length - 1)
                    fb.write(",");
            }
        } catch (IOException ex) {
            Logger.getLogger(FileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    public boolean closeFile(){
        try {
            fbOut.flush();
            fb.flush();
            fbOut.close();
            fb.close();
        } catch (IOException ex) {
            Logger.getLogger(FileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    /**
     * @param args the command line arguments
     */
    /*
    public static void main(String[] args) {
        FileWriter prova = new FileWriter("prova.txt", ".");
        LinkedHashMap<String,String> attributes = new LinkedHashMap<>();
        attributes.put("ap1", "REAL");
        attributes.put("ap3", "REAL");
        attributes.put("ap2", "REAL");
        prova.setAttributes(attributes);
        String[] values = {"-82","-54","-62"};
        prova.writeDataRow(values);
        prova.closeFile();
    }*/
    
}
