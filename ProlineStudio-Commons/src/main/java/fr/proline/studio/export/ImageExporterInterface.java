package fr.proline.studio.export;

/* Author AW
 * 
 */


public interface ImageExporterInterface {
	
	public void generateSvgImage(String file);
	public void generatePngImage(String file);
	public String getSupportedFormats();

}
