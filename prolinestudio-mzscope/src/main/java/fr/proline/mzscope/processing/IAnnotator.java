package fr.proline.mzscope.processing;

import fr.proline.mzscope.model.AnnotatedChromatogram;
import fr.proline.mzscope.model.IChromatogram;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MsnExtractionRequest;

public interface IAnnotator {

  AnnotatedChromatogram annotate(IRawFile rawFile, IChromatogram chromatogram, MsnExtractionRequest request, Integer expectedCharge);
}
