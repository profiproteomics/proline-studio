/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.utils;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.MapAlignmentPK;
import fr.proline.core.orm.lcms.MapTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

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
        }else{// Else we need to make to consecutive time conversions
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
    public static Double calcTargetMapElutionTime(MapAlignment mapAlignment, Double refTime){
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
    
    public static Double linearInterpolation(double refTime, double[] x, double[] y  ){
        LinearInterpolator linearIn = new LinearInterpolator();
        PolynomialSplineFunction f = linearIn.interpolate(x, y);
        return f.value(refTime);
    }
    
    public static MapAlignment getRevertedMapAlignment(MapAlignment map){
        MapAlignment revertedMap = new MapAlignment();
        MapAlignmentPK mapKey = new MapAlignmentPK();
        mapKey.setFromMapId(map.getDestinationMap().getId());
        mapKey.setToMapId(map.getSourceMap().getId());
        mapKey.setMassStart(map.getId().getMassStart());
        mapKey.setMassEnd(map.getId().getMassEnd());
        revertedMap.setId(mapKey);
        revertedMap.setDestinationMap(map.getSourceMap());
        revertedMap.setSourceMap(map.getDestinationMap());
        revertedMap.setMapSet(map.getMapSet());
        
        int  nbLandmarks = map.getMapTimeList().size();
        Double[] revTimeList = new Double[nbLandmarks];
        Double[] revDeltaTimeList = new Double[nbLandmarks];
        List<MapTime> revMapTimeList = new ArrayList();
        for (int i=0; i<nbLandmarks; i++){
            MapTime mapTime = map.getMapTimeList().get(i);
            Double deltaTime = mapTime.getDeltaTime();
            Double targetMapTime = mapTime.getTime() + deltaTime ;
            revTimeList[i] = targetMapTime;
            revDeltaTimeList[i] = -deltaTime;
            MapTime rmp = new MapTime(revTimeList[i], revDeltaTimeList[i]);
            revMapTimeList.add(rmp);
        }
        String deltaS = org.apache.commons.lang3.StringUtils.join(revDeltaTimeList, " ");
        String timeS = org.apache.commons.lang3.StringUtils.join(revTimeList, " ");
        
        revertedMap.setMapTimeList(revMapTimeList);
        revertedMap.setDeltaTimeList(deltaS);
        revertedMap.setTimeList(timeS);
        
        return revertedMap;
    }
    
}
