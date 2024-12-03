package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.utils.StringUtils;

import java.util.ArrayList;

public class ThermoCorrectionMatrixTableModel extends AbstractCorrectionMatrixTableModel {

    final String m_quantMethod;

    private final String[] MASS_REPORTER_NAME = {"126","127N","127C","128N","128C","129N","129C","130N","130C","131N","131C", "132N","132C", "133N", "133C", "134N", "134C", "135N"};
    private final String[] MASS_DEUTERATED_REPORTER_NAME = {"127D", "128ND", "128CD", "129ND", "129CD", "130ND", "130CD", "131ND", "131CD", "132ND", "132CD", "133ND", "133CD", "134ND", "134CD", "135ND"};
    private final String[] MASS_MIXED_REPORTER_NAME = {"126","127N","127C","127D", "128N","128ND", "128C", "128CD", "129N","129ND", "129C","129CD", "130N", "130ND", "130C","130CD", "131N","131ND", "131C","131CD", "132N","132ND", "132C", "132CD",  "133N","133ND","133C", "133CD","134N", "134ND", "134C","134CD", "135N", "135ND", "135CD"};
    public ThermoCorrectionMatrixTableModel(String quantMethod) {
        m_quantMethod = quantMethod;
        initData();
    }

    private void  initColName(int nbrCol){
        switch (nbrCol) {
            case 6 -> {
                columnNames.add("Mass Reporter");
                columnNames.add("-2");
                columnNames.add("-1");
                columnNames.add("M+");
                columnNames.add("+1");
                columnNames.add("+2");
            }
            case 10 -> {
                columnNames.add("Mass Reporter");

                columnNames.add("-2 (-2x13C)");//add("<html><center>-2</center><br/><center>(-2x13C)</center></html>");
                columnNames.add("-2 (-13C -15N)");
                columnNames.add("-1 (-13C)");
                columnNames.add("-1 (-15N)");
                columnNames.add("M+");
                columnNames.add("+1 (+15N)");
                columnNames.add("+1 (+13C)");
                columnNames.add("+2 (+15N +13C)");
                columnNames.add("+2 (+2x13C)");
                valueCoeffColumWidth = 90;
            }
            case 16 -> {
                columnNames.add("Mass Reporter");
                columnNames.add("-2 (-13C -2H)");//add("<html><center>-2</center><br/><center>(-2x13C)</center></html>");
                columnNames.add("-2 (-2x13C)");
                columnNames.add("-2 (-15N -2H)");
                columnNames.add("-2 (-13C -15N)");
                columnNames.add("-1 (-2H)");
                columnNames.add("-1 (-13C)");
                columnNames.add("-1 (-15N)");
                columnNames.add("M+");
                columnNames.add("+1 (+15N)");
                columnNames.add("+1 (+13C)");
                columnNames.add("+1 (+2H)");
                columnNames.add("+2 (+15N +13C)");
                columnNames.add("+2 (+15N +2H)");
                columnNames.add("+2 (+2x13C)");
                columnNames.add("+2 (+13C +2H)");
                valueCoeffColumWidth = 90;
            }
        }
    }

    private void initData(){
        massReporters = new ArrayList<>();
        columnNames = new ArrayList<>();
        switch (m_quantMethod) {
            case TMT_6PLEX_METHOD -> {
                massReporters.add(new MassReporter("126", 4, getNoneApplicableCoef(TMT_6PLEX_METHOD, 0)));
                massReporters.add(new MassReporter("127", 4, getNoneApplicableCoef(TMT_6PLEX_METHOD, 1)));
                massReporters.add(new MassReporter("128", 4, getNoneApplicableCoef(TMT_6PLEX_METHOD, 2)));
                massReporters.add(new MassReporter("129", 4, getNoneApplicableCoef(TMT_6PLEX_METHOD, 3)));
                massReporters.add(new MassReporter("130", 4, getNoneApplicableCoef(TMT_6PLEX_METHOD, 4)));
                massReporters.add(new MassReporter("131", 4, getNoneApplicableCoef(TMT_6PLEX_METHOD, 5)));
                initColName(6);
            }
            case TMT_10PLEX_METHOD -> {
                for (int i = 0; i < 10; i++) {
                    massReporters.add(new MassReporter(MASS_REPORTER_NAME[i], 4, getNoneApplicableCoef(TMT_10PLEX_METHOD, i)));
                }
                initColName(6);
            }
            case TMT_11PLEX_METHOD -> {
                for (int i = 0; i < 11; i++) {
                    massReporters.add(new MassReporter(MASS_REPORTER_NAME[i], 4, getNoneApplicableCoef(TMT_11PLEX_METHOD, i)));
                }
                initColName(6);
            }
            case TMT_16PLEX_METHOD -> {
                for (int i = 0; i < 16; i++) {
                    massReporters.add(new MassReporter(MASS_REPORTER_NAME[i], 8, getNoneApplicableCoef(TMT_16PLEX_METHOD, i)));
                }
                initColName(10);
            }
            case TMT_18PLEX_METHOD -> {
                for (int i = 0; i < 18; i++) {
                    massReporters.add(new MassReporter(MASS_REPORTER_NAME[i], 8, getNoneApplicableCoef(TMT_18PLEX_METHOD, i)));
                }
                initColName(10);
            }
            case TMT_16PLEX_DEUT_METHOD -> {
                for (int i = 0; i < 16; i++) {
                    massReporters.add(new MassReporter(MASS_DEUTERATED_REPORTER_NAME[i], 14, getNoneApplicableCoef(TMT_16PLEX_DEUT_METHOD, i)));
                }
                initColName(16);
            }
            case TMT_35PLEX_METHOD ->  {
                for (int i = 0; i < 35; i++) {
                    massReporters.add(new MassReporter(MASS_MIXED_REPORTER_NAME[i], 14, getNoneApplicableCoef(TMT_35PLEX_METHOD, i)));
                }
                initColName(16);
            }
            default -> columnNames.add("Mass Reporter");
        }

    }

    private int[] getNoneApplicableCoef(String tmtMethod, int coeffIndex){
        int[] naIndexes = new int[0];
        switch (coeffIndex) {
            case 0: {
                switch (tmtMethod) {
                    case TMT_6PLEX_METHOD, TMT_10PLEX_METHOD, TMT_11PLEX_METHOD -> {
                        naIndexes = new int[2];
                        naIndexes[0] = 0;
                        naIndexes[1] = 1;
                    }
                    case TMT_16PLEX_METHOD, TMT_18PLEX_METHOD -> {
                        naIndexes = new int[4];
                        naIndexes[0] = 0;
                        naIndexes[1] = 1;
                        naIndexes[2] = 2;
                        naIndexes[3] = 3;
                    }
                    case TMT_16PLEX_DEUT_METHOD -> {
                        naIndexes = new int[6];
                        naIndexes[0] = 0;
                        naIndexes[1] = 1;
                        naIndexes[2] = 2;
                        naIndexes[3] = 3;
                        naIndexes[4] = 5;
                        naIndexes[5] = 6;
                    }
                    case TMT_35PLEX_METHOD -> {
                        naIndexes = new int[10];
                        naIndexes[0] = 0;
                        naIndexes[1] = 1;
                        naIndexes[2] = 2;
                        naIndexes[3] = 3;
                        naIndexes[4] = 4;
                        naIndexes[5] = 5;
                        naIndexes[6] = 6;
                        naIndexes[7] = 9;
                        naIndexes[8] = 11;
                        naIndexes[9] = 13;
                    }
                }
                break;
            }
            case 1: {
                switch (tmtMethod) {
                    case TMT_6PLEX_METHOD, TMT_10PLEX_METHOD, TMT_11PLEX_METHOD -> {
                        naIndexes = new int[1];
                        naIndexes[0] = 0;
                    }
                    case TMT_16PLEX_METHOD, TMT_18PLEX_METHOD -> {
                        naIndexes = new int[5];
                        naIndexes[0] = 0;
                        naIndexes[1] = 1;
                        naIndexes[2] = 2;
                        naIndexes[3] = 4;
                        naIndexes[4] = 6;
                    }
                    case TMT_16PLEX_DEUT_METHOD -> {
                        naIndexes = new int[7];
                        naIndexes[0] = 0;
                        naIndexes[1] = 1;
                        naIndexes[2] = 3;
                        naIndexes[3] = 5;
                        naIndexes[4] = 7;
                        naIndexes[5] = 10;
                        naIndexes[6] = 11;
                    }
                    case TMT_35PLEX_METHOD -> {
                        naIndexes = new int[11];
                        naIndexes[0] = 0;
                        naIndexes[1] = 1;
                        naIndexes[2] = 2;
                        naIndexes[3] = 3;
                        naIndexes[4] = 4;
                        naIndexes[5] = 5;
                        naIndexes[6] = 7;
                        naIndexes[7] = 9;
                        naIndexes[8] = 10;
                        naIndexes[9] = 11;
                        naIndexes[10] = 13;
                    }
                }
                break;
            }
            case 2: {
                switch (tmtMethod) {
                    case TMT_10PLEX_METHOD, TMT_11PLEX_METHOD -> {
                        naIndexes = new int[1];
                        naIndexes[0] = 0;
                    }
                    case TMT_16PLEX_METHOD, TMT_18PLEX_METHOD -> {
                        naIndexes = new int[3];
                        naIndexes[0] = 0;
                        naIndexes[1] = 1;
                        naIndexes[2] = 3;
                    }
                    case TMT_16PLEX_DEUT_METHOD -> {
                        naIndexes = new int[4];
                        naIndexes[0] = 1;
                        naIndexes[1] = 2;
                        naIndexes[2] = 3;
                        naIndexes[3] = 6;
                    }
                    case TMT_35PLEX_METHOD -> {
                        naIndexes = new int[9];
                        naIndexes[0] = 0;
                        naIndexes[1] = 1;
                        naIndexes[2] = 2;
                        naIndexes[3] = 3;
                        naIndexes[4] = 4;
                        naIndexes[5] = 6;
                        naIndexes[6] = 9;
                        naIndexes[7] = 11;
                        naIndexes[8] = 13;
                    }
                }
                break;
            }
            case 3: {
                switch (tmtMethod) {
                    case TMT_16PLEX_METHOD, TMT_18PLEX_METHOD -> {
                        naIndexes = new int[3];
                        naIndexes[0] = 0;
                        naIndexes[1] = 4;
                        naIndexes[2] = 6;
                    }
                    case TMT_16PLEX_DEUT_METHOD -> {
                        naIndexes = new int[4];
                        naIndexes[0] = 1;
                        naIndexes[1] = 7;
                        naIndexes[2] = 10;
                        naIndexes[3] = 11;
                    }
                    case TMT_35PLEX_METHOD -> {
                        naIndexes = new int[6];
                        naIndexes[0] = 0;
                        naIndexes[1] = 1;
                        naIndexes[2] = 2;
                        naIndexes[3] = 3;
                        naIndexes[4] = 5;
                        naIndexes[5] = 6;
                    }
                }
                break;
            }
            case 4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34:
            {
                switch (tmtMethod) {
                    case TMT_16PLEX_METHOD, TMT_18PLEX_METHOD -> {
                        naIndexes = new int[2];
                        naIndexes[0] = 1;
                        naIndexes[1] = 3;
                    }
                    case TMT_16PLEX_DEUT_METHOD -> {
                        if(coeffIndex == 14) {
                            naIndexes = new int[4];
                            naIndexes[0] = 2;
                            naIndexes[1] = 3;
                            naIndexes[2] = 6;
                            naIndexes[3] = 12;

                        }else {
                            naIndexes = new int[3];
                            naIndexes[0] = 2;
                            naIndexes[1] = 3;
                            naIndexes[2] = 6;
                        }
                    }
                    case TMT_35PLEX_METHOD ->  {
                        if(coeffIndex == 4) {
                            naIndexes = new int[9];
                            naIndexes[0] = 0;
                            naIndexes[1] = 1;
                            naIndexes[2] = 2;
                            naIndexes[3] = 4;
                            naIndexes[4] = 7;
                            naIndexes[5] = 9;
                            naIndexes[6] = 10;
                            naIndexes[7] = 11;
                            naIndexes[8] = 13;

                        } else if(coeffIndex == 6 ||coeffIndex == 10 ||coeffIndex == 14 ||coeffIndex == 18 || coeffIndex == 22  ) {
                            naIndexes = new int[8];
                            naIndexes[0] = 0;
                            naIndexes[1] = 2;
                            naIndexes[2] = 3;
                            naIndexes[3] = 4;
                            naIndexes[4] = 6;
                            naIndexes[5] = 9;
                            naIndexes[6] = 11;
                            naIndexes[7] = 13;
                        } else if(coeffIndex == 8 ||coeffIndex == 12 ||coeffIndex == 16 ||coeffIndex == 20 || coeffIndex == 24  ) {
                            naIndexes = new int[8];
                            naIndexes[0] = 0;
                            naIndexes[1] = 2;
                            naIndexes[2] = 4;
                            naIndexes[3] = 7;
                            naIndexes[4] = 9;
                            naIndexes[5] = 10;
                            naIndexes[6] = 11;
                            naIndexes[7] = 13;
                        } else if(coeffIndex == 26){
                            naIndexes = new int[9];
                            naIndexes[0] = 0;
                            naIndexes[1] = 2;
                            naIndexes[2] = 3;
                            naIndexes[3] = 4;
                            naIndexes[4] = 6;
                            naIndexes[5] = 9;
                            naIndexes[6] = 11;
                            naIndexes[7] = 12;
                            naIndexes[8] = 13;
                        } else if(coeffIndex == 28){
                            naIndexes = new int[9];
                            naIndexes[0] = 0;
                            naIndexes[1] = 2;
                            naIndexes[2] = 4;
                            naIndexes[3] = 7;
                            naIndexes[4] = 9;
                            naIndexes[5] = 10;
                            naIndexes[6] = 11;
                            naIndexes[7] = 12;
                            naIndexes[8] = 13;
                        }else if(coeffIndex ==30){
                            naIndexes = new int[11];
                            naIndexes[0] = 0;
                            naIndexes[1] = 2;
                            naIndexes[2] = 3;
                            naIndexes[3] = 4;
                            naIndexes[4] = 6;
                            naIndexes[5] = 8;
                            naIndexes[6] = 9;
                            naIndexes[7] = 10;
                            naIndexes[8] = 11;
                            naIndexes[9] = 12;
                            naIndexes[10] = 13;
                        }else if(coeffIndex ==32){
                            naIndexes = new int[10];
                            naIndexes[0] = 0;
                            naIndexes[1] = 2;
                            naIndexes[2] = 4;
                            naIndexes[3] = 7;
                            naIndexes[4] = 8;
                            naIndexes[5] = 9;
                            naIndexes[6] = 10;
                            naIndexes[7] = 11;
                            naIndexes[8] = 12;
                            naIndexes[9] = 13;

                        }else { // if(coeffIndex ==34)
                            naIndexes = new int[7];
                            naIndexes[0] = 2;
                            naIndexes[1] = 3;
                            naIndexes[2] = 6;
                            naIndexes[3] = 8;
                            naIndexes[4] = 10;
                            naIndexes[5] = 12;
                            naIndexes[6] = 13;
                        }
                    }
                }
                break;
            }
            case 5,7,9,11,13,15,17,19,21,23,25,27,29,31,33: {
                switch (tmtMethod) {
                    case TMT_16PLEX_METHOD, TMT_18PLEX_METHOD -> {
                        naIndexes = new int[2];
                        naIndexes[0] = 4;
                        naIndexes[1] = 6;
                    }
                    case TMT_16PLEX_DEUT_METHOD -> {
                        if (coeffIndex == 15) {
                            naIndexes = new int[4];
                            naIndexes[0] = 7;
                            naIndexes[1] = 10;
                            naIndexes[2] = 11;
                            naIndexes[3] = 12;
                        } else {
                            naIndexes = new int[3];
                            naIndexes[0] = 7;
                            naIndexes[1] = 10;
                            naIndexes[2] = 11;
                        }
                    }
                    case TMT_35PLEX_METHOD -> {
                        if (coeffIndex == 5) {
                            naIndexes = new int[7];
                            naIndexes[0] = 0;
                            naIndexes[1] = 1;
                            naIndexes[2] = 3;
                            naIndexes[3] = 5;
                            naIndexes[4] = 7;
                            naIndexes[5] = 10;
                            naIndexes[6] = 11;

                        } else if (coeffIndex == 7) {
                            naIndexes = new int[4];
                            naIndexes[0] = 1;
                            naIndexes[1] = 2;
                            naIndexes[2] = 3;
                            naIndexes[3] = 6;
                        } else if (coeffIndex == 9) {
                            naIndexes = new int[4];
                            naIndexes[0] = 1;
                            naIndexes[1] = 7;
                            naIndexes[2] = 10;
                            naIndexes[3] = 11;
                        } else if (coeffIndex == 11 || coeffIndex == 15 || coeffIndex == 19 || coeffIndex == 23 || coeffIndex == 27) {
                            naIndexes = new int[3];
                            naIndexes[0] = 2;
                            naIndexes[1] = 3;
                            naIndexes[2] = 6;
                        } else if (coeffIndex == 13 || coeffIndex == 17 || coeffIndex == 21 || coeffIndex == 25 || coeffIndex == 29) {
                            naIndexes = new int[3];
                            naIndexes[0] = 7;
                            naIndexes[1] = 10;
                            naIndexes[2] = 11;
                        } else if (coeffIndex == 31) {
                            naIndexes = new int[4];
                            naIndexes[0] = 2;
                            naIndexes[1] = 3;
                            naIndexes[2] = 6;
                            naIndexes[3] = 12;
                        } else { //if(coeffIndex == 33)
                            naIndexes = new int[4];
                            naIndexes[0] = 7;
                            naIndexes[1] = 10;
                            naIndexes[2] = 11;
                            naIndexes[3] = 12;
                        }
                    }
                }
                break;
            }
        }

        return naIndexes;
    }

    /**
     * Convert Thermo matrix to generic matrix
     * This is hard codded depending on TMT method ...
     * @return a String representing labelCount X labelCount matrix
     */
    public String getPurityMatrixAsString() {
        int mReporterCount = massReporters.size();
        StringBuilder sb  = new StringBuilder();
        Float[] row = new Float[mReporterCount];
        sb.append("[");

        for(int massRepIndex = 0;massRepIndex<mReporterCount; massRepIndex++){
            MassReporter mr =  massReporters.get(massRepIndex);
            Float coefSum = mr.getCoeffSum();
            if(massRepIndex!=0)
                sb.append(",");//separate reporters
            for(int colIndex = 0;colIndex<mReporterCount; colIndex++){
                row[colIndex] = 0.0f;
                if(colIndex == massRepIndex) {
                    row[colIndex] = ( (100 - coefSum)/100);
                } else  if (m_quantMethod.equals(TMT_6PLEX_METHOD)) {
                    if(colIndex == (massRepIndex-2)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(0))/100);
                    }
                    if(colIndex == (massRepIndex-1)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(1))/100);
                    }
                    if(colIndex == (massRepIndex+1)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(2))/100);
                    }
                    if(colIndex == (massRepIndex+2)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(3))/100);
                    }
                } else if(m_quantMethod.equals(TMT_10PLEX_METHOD) || m_quantMethod.equals(TMT_11PLEX_METHOD)) {
                    if(colIndex == (massRepIndex-4)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(0))/100);
                    }
                    if(colIndex == (massRepIndex-2)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(1))/100);
                    }
                    if(colIndex == (massRepIndex+2)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(2))/100);
                    }
                    if(colIndex == (massRepIndex+4)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(3))/100);
                    }
                } else if(m_quantMethod.equals(TMT_16PLEX_METHOD) || m_quantMethod.equals(TMT_18PLEX_METHOD)) {
                    boolean isCreporter = (massRepIndex%2==0);
                    if(colIndex == (massRepIndex-1) && !isCreporter){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(3))/100);
                    }
                    if(colIndex == (massRepIndex-2)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(2))/100);
                    }
                    if(colIndex == (massRepIndex-3)&&!isCreporter){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(1))/100);
                    }
                    if(colIndex == (massRepIndex-4)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(0))/100);
                    }
                    if(colIndex == (massRepIndex+1)&&isCreporter){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(4))/100);
                    }
                    if(colIndex == (massRepIndex+2)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(5))/100);
                    }
                    if(colIndex == (massRepIndex+3)&&isCreporter){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(6))/100);
                    }
                    if(colIndex == (massRepIndex+4)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(7))/100);
                    }
                } else if(m_quantMethod.equals(TMT_16PLEX_DEUT_METHOD) ) {
                    boolean isCreporter = (massRepIndex%2==0);
                    if(colIndex == (massRepIndex-1) && !isCreporter){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(6))/100);
                    }
                    if(colIndex == (massRepIndex-2)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(5))/100);
                    }
                    if(colIndex == (massRepIndex-3)&&!isCreporter){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(3))/100);
                    }
                    if(colIndex == (massRepIndex-4)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(1))/100);
                    }
                    if(colIndex == (massRepIndex+1)&&isCreporter){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(7))/100);
                    }
                    if(colIndex == (massRepIndex+2)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(8))/100);
                    }
                    if(colIndex == (massRepIndex+3)&&isCreporter){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(10))/100);
                    }
                    if(colIndex == (massRepIndex+4)){
                        row[colIndex] =( (mr.getCoefWithoutNan().get(12))/100);
                    }
                }else if(m_quantMethod.equals(TMT_35PLEX_METHOD) ) {
                    boolean isDreporter = (( massRepIndex>2 && massRepIndex%2==1) || massRepIndex==34);
                    boolean isNreporter =mr.name.contains("N");
                    if(colIndex == (massRepIndex-1) && massRepIndex == 1){ //
                        row[colIndex] =( (mr.getCoefWithoutNan().get(6))/100);
                    }
                    if(colIndex == (massRepIndex-2) ){
                        if( massRepIndex == 2)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(5))/100);
                        else if(massRepIndex>2 && isNreporter)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(6))/100);
                    }
                    if(colIndex == (massRepIndex-3) ){
                        if( massRepIndex == 3)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(4))/100);
                        if( massRepIndex == 4 || massRepIndex == 34)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(5))/100);
                    }
                    if(colIndex == (massRepIndex-4) ){
                        if( massRepIndex == 4)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(3))/100);
                        if( massRepIndex == 5 || massRepIndex == 34)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(4))/100);
                        if(massRepIndex > 5 && massRepIndex < 34)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(5))/100);
                    }
                    if(colIndex == (massRepIndex-5) ){
                        if( massRepIndex == 5)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(2))/100);
                        if( massRepIndex > 5 && massRepIndex < 34 && isDreporter)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(4))/100);
                    }
                    if(colIndex == (massRepIndex-6) ){
                        if( massRepIndex == 6)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(1))/100);
                        if( massRepIndex > 6 && isNreporter)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(3))/100);
                    }
                    if(colIndex == (massRepIndex-7) ){
                        if( massRepIndex == 7)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(0))/100);
                        if( massRepIndex == 8 || massRepIndex == 34)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(1))/100);
                        if( massRepIndex > 8 && isDreporter && isNreporter)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(2))/100);
                    }
                    if(colIndex == (massRepIndex-8) ){
                        if( massRepIndex == 9 || massRepIndex == 34)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(0))/100);
                        if( massRepIndex > 9 && massRepIndex<34)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(1))/100);
                    }
                    if(colIndex == (massRepIndex-9) ){
                        if( massRepIndex >10 && isDreporter && massRepIndex<34)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(0))/100);
                    }
                    if(colIndex == (massRepIndex+1) ){
                        if( massRepIndex == 0)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(7))/100);
                    }
                    if(colIndex == (massRepIndex+2) ){
                        if( massRepIndex == 0)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(8))/100);
                        if( massRepIndex > 1 && !isNreporter && massRepIndex < 32)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(7))/100);
                    }
                    if(colIndex == (massRepIndex+3) ){
                        if( massRepIndex == 1 || massRepIndex == 31 )
                            row[colIndex] =( (mr.getCoefWithoutNan().get(8))/100);
                    }
                    if(colIndex == (massRepIndex+4) ){
                        if( massRepIndex == 0)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(10))/100);
                        if( massRepIndex > 1 && massRepIndex < 30 )
                            row[colIndex] =( (mr.getCoefWithoutNan().get(8))/100);
                    }
                    if(colIndex == (massRepIndex+6) ){
                        if( massRepIndex == 0)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(12))/100);
                        if( massRepIndex > 1 && !isNreporter &&  massRepIndex < 29 )
                            row[colIndex] =( (mr.getCoefWithoutNan().get(10))/100);
                    }
                    if(colIndex == (massRepIndex+7) ){
                        if( massRepIndex == 1 || massRepIndex == 27)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(12))/100);
                    }
                    if(colIndex == (massRepIndex+8) ){
                        if( massRepIndex > 1 && massRepIndex< 26)
                            row[colIndex] =( (mr.getCoefWithoutNan().get(12))/100);
                    }
                }
            }//for each column (generic matric LabelCount X LabelCount)

            sb.append(StringUtils.formatFloatArray(row, 4));
        } // end for each tag row

         sb.append("]");
        return sb.toString();
    }


}
