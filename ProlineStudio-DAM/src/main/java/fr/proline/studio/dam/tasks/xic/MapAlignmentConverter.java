/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.MapTime;
import java.util.List;

/**
 * to convert elution time between maps depending of maps alignments
 * cf fr.proline.core.om.model.lcms.MapSet.convertElutionTime
 * @author MB243701
 */
public class MapAlignmentConverter {
    /**
     * convert the elutionTime in the sourceMapId into the targetMapId, with the given list of mapAlignments
     * The alignmentReferenceMapId must be given
     * @param elutionTime
     * @param sourceMapId
     * @param targetMapId
     * @param listMapAlignment
     * @param alnRefMapId
     * @return 
     * @throws java.lang.Exception 
     */
    public static Double convertElutionTime(Double elutionTime, Long sourceMapId, Long targetMapId, List<MapAlignment> listMapAlignment, Long alnRefMapId) throws Exception{
        if (listMapAlignment == null || listMapAlignment.isEmpty() ){
            throw new Exception("can't convert elution time without a defined listMapAlignment");
        }
        if (elutionTime.isNaN()){
            throw new Exception("can't convert undefined elution time");
        }
        if (sourceMapId == null || sourceMapId < 1){
            throw new Exception("sourceMapId  must be defined");
        }
        if (targetMapId == null || targetMapId < 1){
            throw new Exception("targetMapId  must be defined");
        }
        if (alnRefMapId == null || alnRefMapId < 1){
            throw new Exception("alnRefMapId  must be defined");
        }
        
        // If the reference is the target map => returns the provided time
        if( sourceMapId.equals(targetMapId) ) {
            return elutionTime ;
        }
        
        // If we have an alignment between the reference and the target
        MapAlignment mapAln = getMapAlgn(sourceMapId, targetMapId, listMapAlignment);
        if(mapAln != null) {
            return calcTargetMapElutionTime(mapAln, elutionTime);
        } else {// Else we need to make to consecutive time conversions
            // Convert time into the reference map scale
            MapAlignment refMapAln = getMapAlgn(sourceMapId, alnRefMapId, listMapAlignment);
            Double refTime = calcTargetMapElutionTime(refMapAln, elutionTime);
            
            MapAlignment targetMapAln = getMapAlgn(alnRefMapId, targetMapId, listMapAlignment);
            return calcTargetMapElutionTime(targetMapAln, refTime);
        }
        
    }
    
    
    /**
     * return the mapAlignment between the sourceMapId and the targetMapId in the given list if exits. null otherwise
     * @param sourceMapId
     * @param targetMapId
     * @param listMapAlignment
     * @return 
     */
    public static  MapAlignment getMapAlgn(Long sourceMapId, Long targetMapId, List<MapAlignment> listMapAlignment){
        for(MapAlignment mapAlignment : listMapAlignment){
            if (mapAlignment.getSourceMap().getId().equals(sourceMapId) && mapAlignment.getDestinationMap().getId().equals(targetMapId)){
                return mapAlignment;
            }
        }
        return null;
    }
    
    /***
     * calculate the predicted time into the given mapAlignment. Use of a linear interpolation
     * @param mapAlignment
     * @param refTime
     * @return 
     */
    public static Double calcTargetMapElutionTime(MapAlignment mapAlignment, Double refTime)throws Exception {
        List<MapTime> mapTimeList = mapAlignment.getMapTimeList();
        int nb = mapTimeList.size();
        double[] times = new double[nb];
        double[] deltaTimes= new double[nb];
        int i = 0;
        for(MapTime mapTime: mapTimeList){
            times[i] = mapTime.getTime();
            deltaTimes[i] = mapTime.getDeltaTime();
            i++;
        }
        return refTime + linearInterpolation(refTime, times, deltaTimes);
    }
    
    public static Double linearInterpolation(double refTime, double[] x, double[] y  )throws Exception {
        int index = -1;
        for (int i=0; i<x.length; i++){
            if (x[i] >= refTime){
                index = i;
                break;
            }
        }
        if( index == -1 ) {
            if (refTime < x[0]){
                index = 0;
            }
        }
        // If we are looking at the left-side of the vector boundaries
        // then we take the Y value of the first element
        if( index == 0 ){
            return y[0];
        }
        // Else if we are looking at the right-side of the vector boundaries
        // then we take the Y of the last element
        else if (index == -1){
            return y[y.length-1];
        }
        // Else we are inside the vector boundaries
        // We then compute the linear interpolation
        else{
            double x1 = x[index -1];
            double y1 = y[index-1];
            double x2 = x[index];
            double y2 = y[index];
            // If the vector contains two consecutive values with a same X coordinate
            // Then we take the mean of the corresponding Y values
            if (x1 == x2){
                return (y1 + y2)/2;
            }else{
                double[] lineP = calcLineParams( x1, y1, x2, y2 );
                return lineP[0] * refTime + lineP[1];
            }
        }
        
    }
    
    /** 
     * compute slope and intercept of a line using two data points coordinates 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return 
     */
    private static double[] calcLineParams (double x1, double y1, double x2, double y2) throws Exception {
        double[] lineP = new double[2];
        double deltaX = x2 - x1;
        
        if (deltaX == 0){
            throw new Exception("can't solve line parameters with two identical x values (" + x1 + ")");
        }
        double slope = (y2 - y1) / deltaX;
        double intercept = y1 - (slope * x1);
        lineP[0] = slope;
        lineP[1] = intercept;
        return lineP;
    }
    
    }
