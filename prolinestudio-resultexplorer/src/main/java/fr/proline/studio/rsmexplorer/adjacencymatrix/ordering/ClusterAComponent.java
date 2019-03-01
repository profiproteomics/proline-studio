package fr.proline.studio.rsmexplorer.adjacencymatrix.ordering;

import java.util.ArrayList;
import java.util.HashMap;

import ch.usi.inf.sape.hac.HierarchicalAgglomerativeClusterer;
import ch.usi.inf.sape.hac.agglomeration.AgglomerationMethod;
import ch.usi.inf.sape.hac.agglomeration.AverageLinkage;
import ch.usi.inf.sape.hac.dendrogram.Dendrogram;
import ch.usi.inf.sape.hac.dendrogram.DendrogramBuilder;
import ch.usi.inf.sape.hac.dendrogram.DendrogramNode;
import ch.usi.inf.sape.hac.dendrogram.MergeNode;
import ch.usi.inf.sape.hac.dendrogram.ObservationNode;
import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;

import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.Component;
import fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.DrawVisualization;

public class ClusterAComponent {


    public static void clusterComponent(DrawVisualization drawVisualization) {

        ArrayList<Component> compList = drawVisualization.getComponentList();
        HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> peptideToProteinMap = drawVisualization.getPeptideToProteinMap();


        for (Component tempComp : compList) {
            int[][] peptProtMap = tempComp.getPeptProtMatrix(peptideToProteinMap);
            ArrayList<Integer> proteinOrder = clusterDataArray(tempComp.getProteinArray(false).size(), peptProtMap);

            int[][] transpose = new int[peptProtMap[0].length][peptProtMap.length];
            for (int i = 0; i < peptProtMap.length; i++) {
                for (int j = 0; j < peptProtMap[i].length; j++) {
                    transpose[j][i] = peptProtMap[i][j];
                }
            }

            ArrayList<Integer> peptideOrder = clusterDataArray(tempComp.getPeptideArray().size(), transpose);

            ArrayList<LightProteinMatch> proteinSetTemp = new ArrayList<>();
            proteinSetTemp.clear();
            ArrayList<LightPeptideMatch> peptideSetTemp = new ArrayList<>();
            peptideSetTemp.clear();

            for (int index : proteinOrder) {
                proteinSetTemp.add(tempComp.getProteinArray(false).get(index));
            }
            tempComp.setProteinArray(proteinSetTemp);

            for (int index : peptideOrder) {
                peptideSetTemp.add(tempComp.getPeptideArray().get(index));
            }
            tempComp.setPeptideArray(peptideSetTemp);
            
            
        }

    }

    private static final ArrayList<Integer> clusterDataArray(int lengthData, int[][] peptProtMap) {
        int rowCount = peptProtMap.length, colCount = peptProtMap[0].length;
        double[][] similarityMatrix = new double[colCount][colCount];
        for (int i = 0; i < colCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (i < j) {
                    similarityMatrix[i][j] = similarityMatrix[j][i];
                } else if (i == j) {
                    similarityMatrix[i][j] = 1;
                } else {
                    double similarityValue = 0;
                    for (int k = 0; k < rowCount; k++) {
                        similarityValue += peptProtMap[k][i] * peptProtMap[k][j];
                    }
                    similarityMatrix[i][j] = similarityValue;

                }
            }
        }

        Experiment experiment = new ExperimentData(lengthData);
        DissimilarityMeasure dissimilarityMeasure = new dissimilarityValue(similarityMatrix);
        AgglomerationMethod agglomerationMethod = new AverageLinkage();
        DendrogramBuilder dendrogramBuilder = new DendrogramBuilder(experiment.getNumberOfObservations());
        HierarchicalAgglomerativeClusterer clusterer = new HierarchicalAgglomerativeClusterer(experiment, dissimilarityMeasure, agglomerationMethod);
        clusterer.cluster(dendrogramBuilder);
        Dendrogram dendrogram = dendrogramBuilder.getDendrogram();


	//depth first Order component using tree
        ArrayList<Integer> clusterOrder = new ArrayList<>();
        clusterOrder = getDendogramOrder(dendrogram.getRoot(), clusterOrder);

        return clusterOrder;

    }


    private static ArrayList<Integer> getDendogramOrder(DendrogramNode d, ArrayList<Integer> order) {

        DendrogramNode root = d;

        if (root instanceof ObservationNode) {
            order.add(((ObservationNode) root).getObservation());
        } else if (root instanceof MergeNode) {
            order = getDendogramOrder(d.getLeft(), order);
            order = getDendogramOrder(d.getRight(), order);
        }

        return order;
    }

    private static int[][] getPeptProtMatrix(Component compTemp, HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> peptideToProteinMap) {
        if (compTemp == null) {
            return null;
        }

        int[][] tempMatch = new int[compTemp.getPeptideArray().size()][compTemp.getProteinArray(true).size()];
        int i = 0, j = 0;
        for (LightPeptideMatch tempPept : compTemp.getPeptideArray()) {
            ArrayList<LightProteinMatch> proteinList = peptideToProteinMap.get(tempPept);
            j = 0;

            for (LightProteinMatch tempProt : compTemp.getProteinArray(true)) {
                if (proteinList.contains(tempProt)) {
                    tempMatch[i][j] = 1;
                } else {
                    tempMatch[i][j] = 0;
                }

                j++;
            }

            i++;
        }

        //row - Pept , col - Prot
        return tempMatch;
    }

}
