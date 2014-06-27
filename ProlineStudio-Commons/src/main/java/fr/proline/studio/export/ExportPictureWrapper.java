package fr.proline.studio.export;


import java.io.File;


public class ExportPictureWrapper {

	File m_graphicFile = null; // jfreesvg
	File m_graphicFile2 = null; // "spectrum_tmp_3000x2000.png");
	File m_graphicFile3 = null; // batik svg

	
	public void setFile(File svgFile) {
		m_graphicFile = svgFile;
	}
	public void setFile2(File svgFile) {
		m_graphicFile2 = svgFile;
	}
	
	public void setFile3(File svgFile) {
		m_graphicFile2 = svgFile;
	}
	
	

	
	
}
