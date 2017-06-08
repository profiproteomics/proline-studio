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
public interface WorkerInterface {

    public static final int UPLOADER_TYPE = 0;
    public static final int CONVERTER_TYPE = 1;
    
    public static final int ACTIVE_STATE = 0;
    public static final int KILLED_STATE = 1;
    public static final int FINISHED_STATE = 2;

    public void terminate();

    public boolean isAlive();

    public int getState();

    public File getFile();

    public int getWorkerType();

    public StringBuilder getLogs();

}
