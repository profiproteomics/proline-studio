/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

/**
 *
 * @author MB243701
 */
public enum ScanHeaderType {
    MS1("MS1"), MS2("MS2");
    
    private String name ;  
      
    private ScanHeaderType(String name) {  
         this.name = name ;  
    }  
      
     public String getName() {  
         return  this.name ;  
    }  
}
