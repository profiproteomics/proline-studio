package fr.proline.studio.export;

import java.io.IOException;

/**
 * An exporter must implements this interface (csv, xls, xlsx...)
 * @author JM235353
 */
public interface ExporterInterface {
    
    public void start(String filePath) throws IOException ;

    public void startSheet(String pageName) throws IOException;

    public void startRow() throws IOException;

    public void addCell(String t) throws IOException;

    public void end() throws IOException;

}
