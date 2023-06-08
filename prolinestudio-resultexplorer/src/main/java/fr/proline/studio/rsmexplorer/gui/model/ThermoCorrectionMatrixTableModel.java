package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.utils.StringUtils;

import java.util.ArrayList;

public class ThermoCorrectionMatrixTableModel extends AbstractCorrectionMatrixTaleModel  {

    //Known TMT methods
    public final static String TMT_6PLEX_METHOD = "TMT 6plex";
    public final static String TMT_10PLEX_METHOD = "TMT 10plex";
    public final static String TMT_11PLEX_METHOD = "TMT 11plex";
    public final static String TMT_16PLEX_METHOD = "TMT 16plex";
    public final static String TMT_18PLEX_METHOD = "TMT 18plex";
    final String m_quantMethod;

    private final String[] MASS_REPORTER_NAME = {"126","127N","127C","128N","128C","129N","129C","130N","130C","131N","131C", "132N","132C", "133N", "133C", "134N", "134C", "135N"};

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
                }
                break;
            }
            case 4:
            case 6:
            case 8:
            case 10:
            case 12:
            case 14:
            case 16:
            case 18: {
                switch (tmtMethod) {
                    case TMT_16PLEX_METHOD, TMT_18PLEX_METHOD -> {
                        naIndexes = new int[2];
                        naIndexes[0] = 1;
                        naIndexes[1] = 3;
                    }
                }
                break;
            }
            case 5,7,9,11,13,15,17: {
                switch (tmtMethod) {
                    case TMT_16PLEX_METHOD, TMT_18PLEX_METHOD -> {
                        naIndexes = new int[2];
                        naIndexes[0] = 4;
                        naIndexes[1] = 6;
                    }
                }
                break;
            }
        }

        return naIndexes;
    }

    public String getPurityMatrixAsString() {
        int mReporterCount = massReporters.size();
        StringBuilder sb  = new StringBuilder();
        Float[] row = new Float[mReporterCount];
        sb.append("[");

        for(int index = 0;index<mReporterCount; index++){
            MassReporter mr =  massReporters.get(index);
            Float coefSum = mr.getCoeffSum();
            if(index!=0)
                sb.append(",");//separate reporters
            for(int rowIndex = 0;rowIndex<mReporterCount; rowIndex++){
                row[rowIndex] = 0.0f;
                if(rowIndex == index) {
                    row[rowIndex] = ( (100 - coefSum)/100);
                } else  if (m_quantMethod.equals(TMT_6PLEX_METHOD)) {
                    if(rowIndex == (index-2)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(0))/100);
                    }
                    if(rowIndex == (index-1)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(1))/100);
                    }
                    if(rowIndex == (index+1)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(2))/100);
                    }
                    if(rowIndex == (index+2)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(3))/100);
                    }
                } else if(m_quantMethod.equals(TMT_10PLEX_METHOD) || m_quantMethod.equals(TMT_11PLEX_METHOD)) {
                    if(rowIndex == (index-4)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(0))/100);
                    }
                    if(rowIndex == (index-2)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(1))/100);
                    }
                    if(rowIndex == (index+2)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(2))/100);
                    }
                    if(rowIndex == (index+4)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(3))/100);
                    }
                } else if(m_quantMethod.equals(TMT_16PLEX_METHOD) || m_quantMethod.equals(TMT_18PLEX_METHOD)) {
                    boolean isCreporter = (index%2==0);
                    if(rowIndex == (index-1) && !isCreporter){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(3))/100);
                    }
                    if(rowIndex == (index-2)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(2))/100);
                    }
                    if(rowIndex == (index-3)&&!isCreporter){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(1))/100);
                    }
                    if(rowIndex == (index-4)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(0))/100);
                    }
                    if(rowIndex == (index+1)&&isCreporter){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(4))/100);
                    }
                    if(rowIndex == (index+2)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(5))/100);
                    }
                    if(rowIndex == (index+3)&&isCreporter){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(6))/100);
                    }
                    if(rowIndex == (index+4)){
                        row[rowIndex] =( (mr.getCoefWithoutNan().get(7))/100);
                    }
                }
            }
            sb.append(StringUtils.formatFloatArray(row, 4));
        }

         sb.append("]");
        return sb.toString();
    }


}
