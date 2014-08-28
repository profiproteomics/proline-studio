package fr.proline.studio.rsmexplorer.spectrum;

/**
 *
 * @author JM235353
 */
public class TheoreticalFragmentSeries_AW {

    public String frag_series;
    public double[] masses;
    public int charge = 1; // default to 1 because it is used to multiply
    // the m/z to obtain real mass values for aa
    // calculation

    public void computeCharge() {
        this.charge = 0;
        if (frag_series != null) {
            for (int i = 0; i < frag_series.length(); i++) {
                if (frag_series.charAt(i) == '+') {
                    this.charge++;
                }
            }
        }
        if (this.charge == 0) {
            this.charge = 1;
        }

    }
}
