package fr.proline.mzscope.mzml;

import java.util.Collection;
import java.util.List;


import com.ximpleware.extended.AutoPilotHuge;
import com.ximpleware.extended.VTDGenHuge;
import com.ximpleware.extended.VTDNavHuge;

public class mzMLReader {

	public static void readPreScan(String filepath, Collection<PreScan> scans) {
		long start = System.currentTimeMillis();
		VTDGenHuge vg = new VTDGenHuge();
		int msCount = 0;
		if (vg.parseFile(filepath, true, VTDGenHuge.MEM_MAPPED)) {
			VTDNavHuge vnh = vg.getNav();
			AutoPilotHuge aph = new AutoPilotHuge(vnh);
			try {
				aph.selectXPath("//spectrum");
				int i = 0;
				while ((i = aph.evalXPath()) != -1) {
					int att = vnh.getAttrVal("id");
					String id = vnh.toRawString(att);
					att = vnh.getAttrVal("index");
					String index = vnh.toRawString(att);
					vnh.push();
					vnh.toElement(VTDNavHuge.FIRST_CHILD, "cvParam");
					att = vnh.getAttrVal("accession");
					while (!"MS:1000511".equals(vnh.toRawString(att))) {
						vnh.toElement(VTDNavHuge.NEXT_SIBLING, "cvParam");
						att = vnh.getAttrVal("accession");
					}
					att = vnh.getAttrVal("value");
                                        // only read MS level 1 spectrum
					if ("1".equals(vnh.toRawString(att))) {
						
						//TODO verifier que l'ordre masses / intensites est le bon  
						PreScan scan = new PreScan();
						scan.id = id;
						scan.index = Integer.parseInt(index);
						vnh.toElement(VTDNavHuge.NEXT_SIBLING, "scanList");
						vnh.push();
						if (vnh.toElement(VTDNavHuge.FIRST_CHILD, "scan")) {
							vnh.toElement(VTDNavHuge.FIRST_CHILD, "cvParam");
							do {
								att = vnh.getAttrVal("accession");
								if ("MS:1000016".equals(vnh.toRawString(att))) {
									att = vnh.getAttrVal("value");
									scan.rt = vnh.toRawString(att);
									break;
								}
							} while (vnh.toElement(VTDNavHuge.NEXT_SIBLING, "cvParam"));
						}
						vnh.pop();

						vnh.toElement(VTDNavHuge.NEXT_SIBLING, "binaryDataArrayList");
						if (vnh.toElement(VTDNavHuge.FIRST_CHILD, "binaryDataArray")) {
//							 System.out.println("first binaryDataArray ");
							vnh.push();
							vnh.toElement(VTDNavHuge.FIRST_CHILD, "cvParam");
							do {
								att = vnh.getAttrVal("accession");
								if ("MS:1000523".equals(vnh.toRawString(att)) || "MS:1000521".equals(vnh.toRawString(att))) {
									att = vnh.getAttrVal("name");
									scan.masses_encoding = vnh.toRawString(att);
									break;
								}
							} while (vnh.toElement(VTDNavHuge.NEXT_SIBLING, "cvParam"));
							vnh.toElement(VTDNavHuge.NEXT_SIBLING, "binary");
							att = vnh.getText();
							scan.encodedMasses = vnh.toRawString(att);

							vnh.pop();
							if (vnh.toElement(VTDNavHuge.NEXT_SIBLING, "binaryDataArray")) {
								vnh.push();
//							 System.out.println("second binaryDataArray");
								vnh.toElement(VTDNavHuge.FIRST_CHILD, "cvParam");
								do {
									att = vnh.getAttrVal("accession");
									if ("MS:1000523".equals(vnh.toRawString(att)) || "MS:1000521".equals(vnh.toRawString(att))) {
										att = vnh.getAttrVal("name");
										scan.intensities_encoding = vnh.toRawString(att);
										break;
									}
								} while (vnh.toElement(VTDNavHuge.NEXT_SIBLING, "cvParam"));
								vnh.toElement(VTDNavHuge.NEXT_SIBLING, "binary");
								att = vnh.getText();
								scan.encodedIntensities = vnh.toRawString(att);
								vnh.pop();
							}
							scans.add(scan);
							msCount++;
						}
					}
					vnh.pop();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	
	public static List<Scan> read(String filepath) {
		List<Scan> scans = ScanFactory.read(filepath);
		return scans;
	}
}
