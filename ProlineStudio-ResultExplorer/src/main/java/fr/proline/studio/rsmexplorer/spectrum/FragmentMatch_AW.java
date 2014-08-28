package fr.proline.studio.rsmexplorer.spectrum;

/**
 *
 * @author JM235353
 */
public class FragmentMatch_AW {

    public String label;
    public Double moz;
    public Double calculated_moz;
    public Float intensity;
    public int charge = 0; // the charge taken from the serie (++ means double charged)

    public void computeChargeFromLabel() {
        this.charge = 0;
        if (label != null) {
            for (int i = 0; i < label.length(); i++) {
                if (label.charAt(i) == '+') {
                    this.charge++;
                }
            }
        }

    }
}
