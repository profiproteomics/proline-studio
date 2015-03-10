/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rserver;

import org.python.core.PyObject;

/**
 *
 * @author JM235353
 */
public class ColTest extends PyObject {
    
    private String m_colName;
    
    public ColTest(){
        m_colName = "";
    }
    
    public ColTest(String colName){
        m_colName = colName;
    }
    
    @Override
    public PyObject __add__(PyObject other) {
        return new ColTest(this.m_colName+((ColTest)other).m_colName );
    }
    
}
