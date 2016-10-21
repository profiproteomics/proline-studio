package fr.proline.studio.python.math;

import fr.proline.studio.python.data.Col;
import fr.proline.studio.python.data.ColRef;
import fr.proline.studio.python.data.PythonImage;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.table.LazyData;
import java.io.File;
import java.io.FileWriter;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;

/**
 *
 * @author JM235353
 */
public class StatsUtil {
    
    public static final String MATRIX_VARIABLE = "matrixProlineValues";
    
    public static RServerManager startRSever() throws Exception {
        RServerManager serverR = RServerManager.getRServerManager();
        boolean RStarted = serverR.startRProcessWithRetry();
        if (!RStarted) {
            throw Py.RuntimeError("Server R not found");
        }

        serverR.connect();

        return serverR;
    }
    
    
    public static File columnsToMatrixTempFile(ColRef[] cols, boolean header, boolean log2, boolean keepNaNForR) throws Exception {
        
        int nbRow = cols[0].getRowCount();
        
        File tempFile = File.createTempFile("matrix", "csv");
        tempFile.deleteOnExit();
        FileWriter fw = new FileWriter(tempFile);
        
        if (header) {
            for (int j=0;j<cols.length;j++) {
                ColRef c = cols[j];
                fw.write(c.getExportColumnName());
                if (j<cols.length-1) {
                    fw.write(';');
                }
            }
            fw.write('\n');
        }
        
        final double LOG2 = StrictMath.log(2);
        
        for (int i = 0; i < nbRow; i++) {
            for (int j=0;j<cols.length;j++) {
                ColRef c = cols[j];
                Object o = c.getValueAt(i);
                if (o instanceof LazyData) {
                    o = ((LazyData) o).getData();
                }
                double d;
                if ((o != null) && (o instanceof Number)) {
                    d = ((Number) o).doubleValue();
                    
                    if (d != d) { // NaN values
                        if (!keepNaNForR) {
                            d = 0;
                        }
                    }
                } else {
                    d = 0;
                }
                if (log2) {
                    d = StrictMath.log(d)/LOG2;
                }

                if (d==d) { // not a NaN value
                    fw.write(String.valueOf(d));
                }  // for NaN value, we write nothing, it will become a NAN in R.
                
                if (j<cols.length-1) {
                    fw.write(';');
                }
            }
            fw.write('\n');

        }
        
        fw.close();
        
        return tempFile;
    }
    
    public static File columnToMatrixTempFile(Col col) throws Exception {
        
        int nbRow = col.getRowCount();
        
        File tempFile = File.createTempFile("matrix", "csv");
        tempFile.deleteOnExit();
        FileWriter fw = new FileWriter(tempFile);

        for (int i = 0; i < nbRow; i++) {
            Object o = col.getValueAt(i);
            if (o instanceof LazyData) {
                o = ((LazyData) o).getData();
            }
            double d;
            if ((o != null) && (o instanceof Number)) {
                d = ((Number) o).doubleValue();
                if (d != d) {
                    d = 0; // NaN values
                }
            } else {
                d = 0;
            }
            fw.write(String.valueOf(d));
            fw.write('\n');

        }
        
        fw.close();

        return tempFile;
    }
    
    public static File createImageTempFile() throws Exception {
        File imageFile = File.createTempFile("graphics", "jpg");
        imageFile.deleteOnExit();
        return imageFile;
    }
    
    public static PyTuple[] colTupleToTuplesArray(PyTuple p) {
        Object[] objArray1 = p.getArray();
        int nb = objArray1.length;

        PyTuple[] tuples = new PyTuple[nb];
        for (int i = 0; i < nb; i++) {
            tuples[i] = ((PyTuple) objArray1[i]);
        }

        return tuples;
    }
    
    public static ColRef[] colTupleToColArray(PyTuple[] pArray) {
        
        int size = pArray.length;
        int totalNb = 0;
        for (int i=0;i<size;i++) {
            PyTuple p = pArray[i];
            Object[] objArray = p.getArray();
            totalNb += objArray.length;
        }
        ColRef[] cols = new ColRef[totalNb];

        int index = 0;
        for (int i = 0; i < size; i++) {
            PyTuple p = pArray[i];
            Object[] objArray = p.getArray();
            int nb = objArray.length;
            for (int j = 0; j < nb; j++) {
                cols[index] = ((ColRef) objArray[j]);
                index++;
            }
        }

        return cols;
    }
    
    public static ColRef[] colTupleToColArray(PyTuple p) {
        Object[] objArray1 = p.getArray();
        int nb1 = objArray1.length;

        ColRef[] cols = new ColRef[nb1];
        for (int i = 0; i < nb1; i++) {
            cols[i] = ((ColRef) objArray1[i]);
        }
        
        return cols;
    }
    
    /*public static ColRef[] colTupleToColArray(PyTuple p1, PyTuple p2) {
        Object[] objArray1 = p1.getArray();
        int nb1 = objArray1.length;
        Object[] objArray2 = p2.getArray();
        int nb2 = objArray2.length;

        ColRef[] cols = new ColRef[nb1 + nb2];
        for (int i = 0; i < nb1; i++) {
            cols[i] = ((ColRef) objArray1[i]);
        }
        for (int i = 0; i < nb2; i++) {
            cols[nb1 + i] = ((ColRef) objArray2[i]);
        }

        return cols;
    }
    public static ColRef[] colTupleToColArray(PyTuple p1, PyTuple p2, PyTuple p3) {
        Object[] objArray1 = p1.getArray();
        int nb1 = objArray1.length;
        Object[] objArray2 = p2.getArray();
        int nb2 = objArray2.length;
        Object[] objArray3 = p3.getArray();
        int nb3 = objArray3.length;

        ColRef[] cols = new ColRef[nb1 + nb2 + nb3];
        for (int i = 0; i < nb1; i++) {
            cols[i] = ((ColRef) objArray1[i]);
        }
        for (int i = 0; i < nb2; i++) {
            cols[nb1 + i] = ((ColRef) objArray2[i]);
        }
        for (int i = 0; i < nb3; i++) {
            cols[nb1 + nb2 + i] = ((ColRef) objArray3[i]);
        }

        return cols;
    }*/
    
     public static String stringTupleToRVector(PyTuple p) {
         StringBuilder sb = new StringBuilder();
         
         sb.append("c(");
         Object[] stringArray = p.getArray();
         int nb = stringArray.length;
         for (int i=0;i<nb;i++) {
             String s = stringArray[i].toString();
             if (i>0) {
                 sb.append(',');
             }
             sb.append('"').append(s).append('"');
         }
         
         sb.append(')');
         
         return sb.toString();
     }
     
     public static String RVectorToRFactor(String rVector) {
         StringBuilder sb = new StringBuilder();
         sb.append("factor(").append(rVector).append(')');
         return sb.toString();
     }
     
     
     public static PyTuple colNamesToTuple(ColRef[] cols) {
         
         int nb = cols.length;
         PyObject[] colNames = new PyObject[nb];
         for (int i=0;i<nb;i++) {
             colNames[i] = new PyString(cols[i].getExportColumnName());
         }
         
         return new PyTuple(colNames);

     }
    
    public static String getPath(File f) throws Exception {
        return f.getCanonicalPath().replaceAll("\\\\", "/");
    }
    
    public static void readMatrixData(File matrixTempFile, boolean header) throws Exception {
        RServerManager serverR = RServerManager.getRServerManager();
        
        char headerChar = (header) ? 'T' : 'F';
        String cmdReadCSV = MATRIX_VARIABLE+"<-read.delim('"+getPath(matrixTempFile)+"',header="+headerChar+", sep=';')";
        serverR.parseAndEval(cmdReadCSV);
    }
    
    public static PythonImage createImage(File imageTempFile, String cmd) throws Exception {
        
        RServerManager serverR = RServerManager.getRServerManager();
        
        String cmdBB1 = "png(\""+StatsUtil.getPath(imageTempFile)+"\",width=500, height=500)";
        String cmdBB2 = cmd;
        String cmdBB3 = "dev.off()";
        serverR.parseAndEval(cmdBB1);
        serverR.parseAndEval(cmdBB2);
        serverR.parseAndEval(cmdBB3);

        return new PythonImage(imageTempFile.getCanonicalPath());
    }
}
