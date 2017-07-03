/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import java.io.File;

/**
 *
 * @author AK249877
 */
public class LocalFileSystemFile extends File {
    
    public LocalFileSystemFile(File parent, String child){
        super(parent, child);
    }
    
    public LocalFileSystemFile(String pathname){
        super(pathname);
    }
    
    @Override
    public String toString(){
        return this.getName();
    }
    
}
