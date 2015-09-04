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

    private final DrawVisualization m_drawVisualization;
    private HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap = new HashMap<>();

    public ClusterAComponent(DrawVisualization drawVisualization) {

        m_drawVisualization = drawVisualization;
        ArrayList<Component> compList = m_drawVisualization.get_ComponentList();
        m_peptideToProteinMap = m_drawVisualization.get_peptideToProteinMap();
 	 	//m_proteinToPeptideMap = tempObject.get_proteinToPeptideMap() ;

        for (Component tempComp : compList) {
            int[][] peptProtMap = getPeptProtMatrix(tempComp);
            ArrayList<Integer> proteinOrder = clusterDataArray(tempComp.proteinSet.size(), peptProtMap);

            int[][] transpose = new int[peptProtMap[0].length][peptProtMap.length];
            for (int i = 0; i < peptProtMap.length; i++) {
                for (int j = 0; j < peptProtMap[i].length; j++) {
                    transpose[j][i] = peptProtMap[i][j];
                }
            }

            ArrayList<Integer> peptideOrder = clusterDataArray(tempComp.peptideSet.size(), transpose);

            ArrayList<LightProteinMatch> proteinSetTemp = new ArrayList<>();
            ArrayList<LightPeptideMatch> peptideSetTemp = new ArrayList<>();

            for (int index : proteinOrder) {
                proteinSetTemp.add(tempComp.proteinSet.get(index));
            }

            tempComp.proteinSet = proteinSetTemp;

            for (int index : peptideOrder) {
                peptideSetTemp.add(tempComp.peptideSet.get(index));
            }

            tempComp.peptideSet = peptideSetTemp;
        }

    }

    public final ArrayList<Integer> clusterDataArray(int lengthData, int[][] peptProtMap) {
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

        double[][] m = new double[colCount][colCount];
        double[][] bt1 = new double[colCount][colCount];
        double[][] bt2 = new double[colCount][colCount];
		//DendrogramNode BJDendogram = barJoseph(dendrogram.getRoot(),similarityMatrix , m, bt1,bt2 );

		//depth first Order component using tree
        ArrayList<Integer> clusterOrder = new ArrayList<>();
        clusterOrder = getDendogramOrder(dendrogram.getRoot(), clusterOrder);
	//	clusterOrder =	getDendogramOrder(BJDendogram,clusterOrder);

        return clusterOrder;

    }

    private DendrogramNode barJoseph(DendrogramNode node, double[][] similarityMatrix, double[][] memoise,
            double[][] bt1, double[][] bt2) {

        if (node instanceof ObservationNode) {
            int index = ((ObservationNode) node).getObservation();
            memoise[index][index] = 0.0d;
            bt1[index][index] = -1;
            bt2[index][index] = -1;
            return node;
        } else if (node instanceof MergeNode) {
            DendrogramNode leftTree = barJoseph(node.getLeft(), similarityMatrix, memoise, bt1, bt2);
            DendrogramNode rightTree = barJoseph(node.getRight(), similarityMatrix, memoise, bt1, bt2);

            ArrayList<Integer> LLleafnodes = new ArrayList<Integer>();
            ArrayList<Integer> LRleafnodes = new ArrayList<Integer>();
            ArrayList<Integer> RLleafnodes = new ArrayList<Integer>();
            ArrayList<Integer> RRleafnodes = new ArrayList<Integer>();

            if (leftTree != null) {
                if (leftTree.getLeft() != null) {
                    DendrogramNode LLTree = leftTree.getLeft();
                    LLleafnodes = getDendogramOrder(LLTree, LLleafnodes);
                }
            }

            if (leftTree != null) {
                if (leftTree.getRight() != null) {
                    DendrogramNode LRTree = leftTree.getRight();
                    LRleafnodes = getDendogramOrder(LRTree, LRleafnodes);
                }
            }

            if (rightTree != null) {
                if (rightTree.getLeft() != null) {
                    DendrogramNode RLTree = rightTree.getLeft();
                    RLleafnodes = getDendogramOrder(RLTree, RLleafnodes);
                }
            }

            if (rightTree != null) {
                if (rightTree.getRight() != null) {
                    DendrogramNode RRTree = rightTree.getRight();
                    RRleafnodes = getDendogramOrder(RRTree, RRleafnodes);
                }
            }

            ArrayList<Integer> Lleafnodes = new ArrayList<>();
            Lleafnodes.addAll(LLleafnodes);
            Lleafnodes.addAll(LRleafnodes);

            ArrayList<Integer> Rleafnodes = new ArrayList<>();
            Rleafnodes.addAll(RLleafnodes);
            Rleafnodes.addAll(RRleafnodes);

            double c = 0.0d;

            /*		for(int v1Leaf : Lleafnodes)
             {
             for(int v2Leaf : Rleafnodes)
             {
             if(similarityMatrix[v1Leaf][v2Leaf] > c)
             c = similarityMatrix[v1Leaf][v2Leaf] ;
             }
    				
             } */
            for (int lNode : Lleafnodes) {
                for (int rNode : Rleafnodes) {
                    ArrayList<Integer> mLeafnodes = new ArrayList<>();
                    ArrayList<Integer> kLeafnodes = new ArrayList<>();

                    if (LLleafnodes.contains(lNode)) {
                        mLeafnodes = LRleafnodes;
                    } else if (LRleafnodes.contains(lNode)) {
                        mLeafnodes = LLleafnodes;
                    }

                    if (RLleafnodes.contains(rNode)) {
                        kLeafnodes = RRleafnodes;
                    } else if (RRleafnodes.contains(rNode)) {
                        kLeafnodes = RLleafnodes;
                    }

                    double MValue = 0.0d;
                    int maxM = -1, maxK = -1;

                    for (int m : mLeafnodes) {
                        for (int k : kLeafnodes) {
                            double temp = memoise[lNode][m] + memoise[rNode][k] + similarityMatrix[m][k];

                            if (temp > MValue) {
                                MValue = temp;
                                maxM = m;
                                maxK = k;
                            }
                        }
                    }

                    memoise[lNode][rNode] = MValue;
                    bt1[lNode][rNode] = maxM;
                    bt2[lNode][rNode] = maxK;

                }
            }

        }

        return node;
    }

    private ArrayList<Integer> getDendogramOrder(DendrogramNode d, ArrayList<Integer> order) {

        DendrogramNode root = d;

        if (root instanceof ObservationNode) {
            order.add(((ObservationNode) root).getObservation());
        } else if (root instanceof MergeNode) {
            order = getDendogramOrder(d.getLeft(), order);
            order = getDendogramOrder(d.getRight(), order);
        }

        return order;
    }

    private int[][] getPeptProtMatrix(Component compTemp) {
        if (compTemp == null) {
            return null;
        }

        int[][] tempMatch = new int[compTemp.peptideSet.size()][compTemp.proteinSet.size()];
        int i = 0, j = 0;
        for (LightPeptideMatch tempPept : compTemp.peptideSet) {
            ArrayList<LightProteinMatch> proteinList = m_peptideToProteinMap.get(tempPept);
            j = 0;

            for (LightProteinMatch tempProt : compTemp.proteinSet) {
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
