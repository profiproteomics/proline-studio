/*
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.mzscope.ui;

import com.github.psambit9791.jdsp.filter.*;
import com.github.psambit9791.jdsp.signal.Convolution;
import com.github.psambit9791.jdsp.signal.Smooth;
import com.github.psambit9791.jdsp.signal.peaks.FindPeak;
import com.github.psambit9791.jdsp.signal.peaks.Peak;
import fr.profi.mzdb.algo.signal.detection.SmartPeakelFinder;
import fr.profi.mzdb.algo.signal.filtering.BaselineRemover;
import fr.profi.mzdb.algo.signal.filtering.ISignalSmoother;
import fr.profi.mzdb.algo.signal.filtering.PartialSavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoothingConfig;
import fr.profi.mzdb.util.math.DerivativeAnalysis;
import fr.proline.mzscope.model.Signal;
import fr.proline.mzscope.ui.dialog.SmoothingParamDialog;
import fr.proline.mzscope.ui.dialog.WaveletSmootherParamDialog;
import fr.proline.studio.WindowManager;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.marker.PointMarker;
import fr.proline.studio.graphics.marker.coordinates.DataCoordinates;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.CyclicColorPalette;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;


import jwave.exceptions.JWaveException;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import fr.proline.mzscope.utils.SmoothUtils;


/**
 * @author CB205360
 */
public class SignalEditorPanel extends SignalPanel {

    final private static Logger logger = LoggerFactory.getLogger(SignalEditorPanel.class);

    private Map<Signal, PlotLinear> m_smoothedSignals;
    private JButton minmaxBtn;
    private JButton maxBtn;
    private JButton maxJDSPButton;

    private JButton experimentalJDSPButton;


    private JButton baseLineBtn;

    private JPanel smoothStatsPanel;

    private JLabel initialSmoothnessLabel;
    private JLabel finalSmoothnessLabelValue;

    private JLabel displaySNRJLabel;
    private JLabel displayBiasValue;

    private JLabel displayQualityValueJLabel;

    private JLabel smoothMethodUsedLabel;

    private JLabel displayComputeTime;

    private JLabel displaySizeSignalJLabel;

    private JLabel displayRateVariationLengthJLabel;

    private JProgressBar progressBar;


    public SignalEditorPanel(Signal signal) {
        super(signal);
        this.setPreferredSize(new Dimension(700, 500));
        m_smoothedSignals = new HashMap<>();
        createStatsPanel();

    }

    @Override
    protected JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();

        JButton smoothBtn = new JButton("Smooth");
        smoothBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    runSmoothing();
                } catch (JWaveException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        toolbar.add(smoothBtn);

        minmaxBtn = new JButton("Min/Max");
        minmaxBtn.setEnabled(false);
        minmaxBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("detect significant min max");
                detectSignificantMinMax();
            }
        });

        toolbar.add(minmaxBtn);

        maxBtn = new JButton("Max");
        maxBtn.setEnabled(false);
        maxBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("detect max");
                detectMax();

            }
        });


        toolbar.add(maxBtn);
        maxJDSPButton = new JButton("Max JDSP");
        maxJDSPButton.setEnabled(false);
        maxJDSPButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                detectMaxJDSP();

            }
        });
        toolbar.add(maxJDSPButton);

        experimentalJDSPButton = new JButton("wavelet smoother");
        experimentalJDSPButton.setEnabled(true);
        experimentalJDSPButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            runWaveletSmoothing();
                        } catch (JWaveException ex) {
                            throw new RuntimeException(ex);
                        }

                    }
                }
        );
        toolbar.add(experimentalJDSPButton);

        baseLineBtn = new JButton("Baseline");
        baseLineBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<Tuple2> input = m_signal.toScalaArrayTuple(false);
                Tuple2[] rtIntPairs = input.toArray(new Tuple2[input.size()]);
                BaselineRemover baselineRemover = new BaselineRemover(1, 3);
                double threshold = baselineRemover.calcNoiseThreshold(rtIntPairs);
                logger.info("detected baseline at threshold " + threshold);
                LineMarker positionMarker = new LineMarker(m_plotPanel.getBasePlotPanel(), threshold, LineMarker.ORIENTATION_HORIZONTAL, Color.BLUE, true);
                m_linear.addMarker(positionMarker);
                SmartPeakelFinder peakelFinder = new SmartPeakelFinder(5, 3, (float) 0.66, false, 10, false, false, true);
                Tuple2[] indices = peakelFinder.findPeakelsIndices(rtIntPairs);
            }
        });

        toolbar.add(baseLineBtn);
        return toolbar;
    }

    private void runSmoothing() throws JWaveException {
        SmoothingParamDialog dialog = new SmoothingParamDialog(WindowManager.getDefault().getMainWindow());
        dialog.pack();
        dialog.setVisible(true);

        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            // peakel detection did not include peaks with intensity = 0, this seems also mandatory since PartialSG smoother
            // get strange behavior with 0 intensities

            List<Tuple2> input = m_signal.toScalaArrayTuple(false);

            int nbrPoints = dialog.getNbrPoint();
            boolean usePeakRestore = dialog.getUsePeakrestore();
            double ratio = 0;

            if (usePeakRestore) {
                ratio = dialog.getJSpinnerValue();
            }
            // CD patch to maintain previous code
            boolean initialMethods = dialog.getMethod().equals(SmoothingParamDialog.SG_SMOOTHER) || dialog.getMethod().equals(SmoothingParamDialog.PARTIAL_SG_SMOOTHER) || dialog.getMethod().equals(SmoothingParamDialog.BOTH_SMOOTHER);
            if (initialMethods) {
                if (nbrPoints == 0)
                    nbrPoints = Math.min(input.size() / 4, 9);
            }

            String smoothMethod = dialog.getMethod();
            switch (smoothMethod) {
                case SmoothingParamDialog.SG_SMOOTHER:
                    logger.info("display smoothed signal, SG nb smoothing points = " + nbrPoints);
                    SavitzkyGolaySmoother sgSmoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
                    smooth(input, sgSmoother, "SG");
                    break;
                case SmoothingParamDialog.PARTIAL_SG_SMOOTHER:
                    logger.info("display smoothed signal, Partial SG nb smoothing points = " + nbrPoints);
                    PartialSavitzkyGolaySmoother psgSmoother = new PartialSavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 4, 1));
                    smooth(input, psgSmoother, "Partial SG");
                    break;
                case SmoothingParamDialog.BOTH_SMOOTHER:
                    logger.info("display smoothed signal, SG nb smoothing points = " + nbrPoints);
                    sgSmoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
                    smooth(input, sgSmoother, "SG");
                    logger.info("display smoothed signal, Partial SG nb smoothing points = " + nbrPoints);
                    psgSmoother = new PartialSavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 4, 1));
                    smooth(input, psgSmoother, "Partial SG");
                    break;

                case SmoothingParamDialog.SMOOTHING_JDSP:
                    logger.info("JDSP smooth method with window size = " + nbrPoints);
                    smoothJDSPV2(input, "smooth method with window size: ", nbrPoints, usePeakRestore, ratio);
                    break;
                case SmoothingParamDialog.CONVOLUTION_METHOD:
                    logger.info("Convolution method kernel size = " + nbrPoints);
                    smoothByConvolution(input, "convolution", nbrPoints, usePeakRestore, ratio);
                    break;

                case SmoothingParamDialog.GAUSSIAN_FITTING_METHOD:
                    logger.info("Fitting of curves by gaussian regression, optimization = "+usePeakRestore);
                    GaussianFinder(input, "gaussian fitting", nbrPoints, usePeakRestore, ratio);
                    break;

                case SmoothingParamDialog.WIENER_METHOD:
                    logger.info("Wiener filter with window size= "+nbrPoints+" peak restore used= "+usePeakRestore);
                    filterWiener(input, "Wiener filter", nbrPoints, usePeakRestore, ratio);
                    break;

                case SmoothingParamDialog.SVGOLAY2_METHOD:
                    logger.info("Savitsky Golay JDSP smoothing, number of points= " + nbrPoints);
                    savitskyJDSP(input, "Savitsky JDSP Smoothing", nbrPoints, usePeakRestore, ratio);
                    break;
                case SmoothingParamDialog.EXPERIMENTAL_TEST:
                    logger.info("Experimental calculus for test");
                    polynomialFitter(input, "Experimental Smoothing", nbrPoints, usePeakRestore, ratio);
                    break;

                case SmoothingParamDialog.MEDIAN_FILTER_METHOD:
                    logger.info("Median filter with window size= " + nbrPoints+" peak restored= "+usePeakRestore);
                    medianFilterJDSP(input, "median filter method", nbrPoints, usePeakRestore, ratio);
                    break;

                case SmoothingParamDialog.SIGNAL_QUALITY_EVALUATION:
                    logger.info("Evaluation of smoothness and quality of the signal");
                    evaluateSignalQuality(input, nbrPoints, usePeakRestore, ratio);
                    break;


            }
        }

    }

    private void runWaveletSmoothing() throws JWaveException {
        //SmoothingParamDialog dialog = new SmoothingParamDialog(WindowManager.getDefault().getMainWindow());
        WaveletSmootherParamDialog dialog = new WaveletSmootherParamDialog(WindowManager.getDefault().getMainWindow());
        dialog.pack();
        dialog.setVisible(true);

        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            List<Tuple2> input = m_signal.toScalaArrayTuple(false);
            String smoothMethod = dialog.getMethod();
            double threshold = dialog.getJSpinnerValue();
            boolean userHasSelectedAWavelet = dialog.getUserChoice();

            if (userHasSelectedAWavelet) {
                String waveletName = dialog.getWaveletSelected();
                int waveletIndex = SmoothUtils.getIndexOfWaveletByName(waveletName);
                waveletPacketSmoother(input, threshold, smoothMethod, waveletIndex, true);

            } else {
                waveletPacketSmoother(input, threshold, smoothMethod, 0, false);
            }


        }

    }

    private void waveletPacketSmoother(List<Tuple2> input, double threshold, String smoothMethod, int waveletIndex, boolean userSelectedWavelet) {
        long startTime = System.nanoTime();
        Tuple2[] result = input.toArray(new Tuple2[input.size()]);

        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }

        double[] smoothedSignal = SmoothUtils.waveletSmootherV2(input, smoothMethod, threshold, waveletIndex, userSelectedWavelet);

        Signal convolutedSignal = new Signal(x, smoothedSignal);
        long computeTime = System.nanoTime() - startTime;

        addSmoothedSignal(convolutedSignal, "wavelet transform");

        int index = SmoothUtils.getWaveletSelected();
        String nameOfWavelet = SmoothUtils.getWaveletByIndex(index).getName();

        displaySmoothStats(x, y, smoothedSignal, " Wavelet transform " + nameOfWavelet, computeTime);
        revalidate();
        repaint();
    }


    private void addSmoothedSignal(Signal s, String title) {

        minmaxBtn.setEnabled(true);
        maxBtn.setEnabled(true);
        maxJDSPButton.setEnabled(true);
        experimentalJDSPButton.setEnabled(true);

        BasePlotPanel basePlot = m_plotPanel.getBasePlotPanel();
        SignalWrapper wrappedSignal = new SignalWrapper(s, "smoothed signal : " + title, CyclicColorPalette.getColor((m_smoothedSignals.size() + 1) * 2));
        PlotLinear linear = new PlotLinear(basePlot, wrappedSignal, null, 0, 1);
        linear.setPlotInformation(wrappedSignal.getPlotInformation());
        linear.setStrokeFixed(true);
        linear.setAntiAliasing(true);
        basePlot.addPlot(linear, true);
        basePlot.repaintUpdateDoubleBuffer();
        m_smoothedSignals.put(s, linear);
        revalidate();
        repaint();


    }

    private void detectMax() {
        for (Map.Entry<Signal, PlotLinear> e : m_smoothedSignals.entrySet()) {
            Signal s = e.getKey();
            DerivativeAnalysis.ILocalDerivativeChange[] mm = DerivativeAnalysis.findMiniMaxi(s.getYSeries());
            PlotLinear plot = e.getValue();
            for (int k = 0; k < mm.length; k++) {
                if (mm[k].isMaximum())
                    plot.addMarker(new PointMarker(m_plotPanel.getBasePlotPanel(), new DataCoordinates(s.getXSeries()[mm[k].index()], s.getYSeries()[mm[k].index()]), plot.getPlotInformation().getPlotColor()));
            }
        }
        m_plotPanel.getBasePlotPanel().repaintUpdateDoubleBuffer();
    }

    /**
     * Can detect both peaks and troughs
     */
    private void detectMaxJDSP() {
        for (Map.Entry<Signal, PlotLinear> e : m_smoothedSignals.entrySet()) {
            Signal s = e.getKey();
            //  DerivativeAnalysis.ILocalDerivativeChange[] mm = DerivativeAnalysis.findMiniMaxi(s.getYSeries());
            double[] ySignal = s.getYSeries();
            double[] xSignal = s.getXSeries();
            FindPeak fp = new FindPeak(ySignal);

            Peak out = fp.detectPeaks();
            int[] peaks = out.getPeaks();
            double[] heightOfPeaks = out.getHeights();

            Peak out2 = fp.detectTroughs();
            int[] troughs = out2.getPeaks();
            double[] heightOfTroughs = out2.getHeights();

            double[] xValuesOfPeaks = new double[peaks.length];
            for (int i = 0; i < peaks.length; i++) {
                int index = peaks[i];
                xValuesOfPeaks[i] = xSignal[index];

            }
            double[] xValuesOfTroughs = new double[troughs.length];
            for (int i = 0; i < troughs.length; i++) {
                int index = troughs[i];
                xValuesOfTroughs[i] = xSignal[index];

            }

            PlotLinear plot = e.getValue();

            for (int k = 0; k < heightOfTroughs.length; k++) {
                plot.addMarker(new PointMarker(m_plotPanel.getBasePlotPanel(), new DataCoordinates(xValuesOfTroughs[k], heightOfTroughs[k]), plot.getPlotInformation().getPlotColor()));
                plot.addMarker(new PointMarker(m_plotPanel.getBasePlotPanel(), new DataCoordinates(xValuesOfPeaks[k], heightOfPeaks[k]), plot.getPlotInformation().getPlotColor()));

            }
        }
        m_plotPanel.getBasePlotPanel().repaintUpdateDoubleBuffer();

    }


    private void detectSignificantMinMax() {
        for (Map.Entry<Signal, PlotLinear> e : m_smoothedSignals.entrySet()) {
            Signal s = e.getKey();
            DerivativeAnalysis.ILocalDerivativeChange[] mm = DerivativeAnalysis.findSignificantMiniMaxi(s.getYSeries(), 3, 0.75f);
            for (int k = 0; k < mm.length; k++) {
                PlotLinear plot = e.getValue();
                plot.addMarker(new PointMarker(m_plotPanel.getBasePlotPanel(), new DataCoordinates(s.getXSeries()[mm[k].index()], s.getYSeries()[mm[k].index()]), plot.getPlotInformation().getPlotColor()));
            }
        }
        m_plotPanel.getBasePlotPanel().repaintUpdateDoubleBuffer();
    }


    private void smooth(List<Tuple2> xyPairs, ISignalSmoother smoother, String title) {

        logger.info("signal length before smoothing = " + xyPairs.size());
        long startTime = System.nanoTime();
        Tuple2[] initialSetOfValues = xyPairs.toArray(new Tuple2[xyPairs.size()]);
        double[] initialValues = new double[xyPairs.size()];
        for (int k = 0; k < initialSetOfValues.length; k++) {
            initialValues[k] = (double) initialSetOfValues[k]._2;
        }

        Tuple2[] result = smoother.smoothTimeIntensityPairs(xyPairs.toArray(new Tuple2[xyPairs.size()]));
        logger.info("signal length after smoothing = " + result.length);
        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }

        Signal s = new Signal(x, y);

        long computeTime = System.nanoTime() - startTime;

        addSmoothedSignal(s, title);
        displaySmoothStats(x, initialValues, y, " Savitsky JDSP", computeTime);

    }


    /**
     * Smoothing by convolution with non-constant kernels
     */
    private void smoothJDSPV2(List<Tuple2> xyPairs, String title, int windowSize, boolean scanWindowSize, double ratio) {

        long startTime = System.nanoTime();
        logger.info("signal length before smoothing = " + xyPairs.size());

        Tuple2[] result = xyPairs.toArray(new Tuple2[xyPairs.size()]);

        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }

        String mode = "triangular";
        String mode2 = "rectangular";

        int optimalWindowSize = windowSize;
        if (scanWindowSize) {
            optimalWindowSize = SmoothUtils.getBestSmoothWindowSize(y);
        }
        System.out.println("window size selected: " + optimalWindowSize);
        Smooth smoother = new Smooth(y, optimalWindowSize, mode);
        double[] kernel = smoother.getKernel();
        System.out.println("kernel used for JDSP smoothing: " + Arrays.toString(kernel));
        double[] smoothedSignal = smoother.smoothSignal("same");

        Signal s = new Signal(x, smoothedSignal);

        long computeTime = System.nanoTime() - startTime;

        addSmoothedSignal(s, title);

        displaySmoothStats(x, y, smoothedSignal, "smooth method JDSP", computeTime);

    }


    /**
     * Simple averaging method by convolution with kernel composed of constant values
     *
     */

    private void smoothByConvolution(List<Tuple2> xyPairs, String title, int nbrPoints, boolean usePeakRestore, double ratio) {

        long startTime = System.nanoTime();
        Tuple2[] result = xyPairs.toArray(new Tuple2[xyPairs.size()]);

        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }

        double[] kernel = SmoothUtils.buildKernel(nbrPoints);

        System.out.println("kernel used for convolution: " + Arrays.toString(kernel));

        Convolution con = new Convolution(y, kernel);
        // mode "same" allows to maintain the length of the signal
        double[] convolvedSignal = con.convolve("same");

        SmoothUtils.correctNegativeValues(y, convolvedSignal);

        if (usePeakRestore) {
            SmoothUtils.peakRestorerV2(y, convolvedSignal, x, ratio);
        }

        long computeTime = System.nanoTime() - startTime;

        Signal s = new Signal(x, convolvedSignal);


        addSmoothedSignal(s, title);

        displaySmoothStats(x, y, convolvedSignal, "averaging by convolution", computeTime);


    }

    /**
     * Curve fitting
     * gaussian regression
     */

    private void GaussianFinder(List<Tuple2> xyPairs, String title, int nbrPoints, boolean correctionApplied, double ratio) {

        long startTime = System.nanoTime();

        Tuple2[] result = xyPairs.toArray(new Tuple2[xyPairs.size()]);
        System.out.println("experimental test called");
        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }

        GaussianCurveFitter fitter = GaussianCurveFitter.create();
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int index = 0; index < result.length; index++) {
            obs.add(x[index], y[index]);
        }

        double[] bestFit = fitter.fit(obs.toList());

        double[] fittedSignal;

        if (!correctionApplied) {

            fittedSignal = SmoothUtils.buildSymmetricGaussian(bestFit, x[0], x[result.length - 1], result.length);


        } else {
            // TODO test if  order is optimal ?

            double bestMean = SmoothUtils.getBestGaussianMean(bestFit, x[0], x[result.length - 1], result.length, y);
            bestFit[1] = bestMean;

            double[] sigmas = SmoothUtils.computeOptimalSigmas(bestFit, x[0], x[result.length - 1], result.length, y);

            double bestNorm = SmoothUtils.getBestGaussianNorm(bestFit, x[0], x[result.length - 1], result.length, y);

            bestFit[0] = bestNorm;
            fittedSignal = SmoothUtils.buildAsymmetricGaussian(bestFit[0], bestFit[1], sigmas[0], sigmas[1], x[0], x[result.length - 1], result.length);

            // --------second method not fast and not so efficient
           /* double[] optimalGaussianParameters=SmoothUtils.computeOptimalGaussianParameters(bestFit,x[0],x[result.length-1],result.length,y);
            double optimalNorm=optimalGaussianParameters[0];
            double optimalMean=optimalGaussianParameters[1];
            double optimalSigmaLeft=optimalGaussianParameters[2];
            double optimalSigamRight=optimalGaussianParameters[3];

            fittedSignal=SmoothUtils.buildAsymmetricGaussian(optimalNorm,optimalMean,optimalSigmaLeft,optimalSigamRight,x[0],x[result.length-1],result.length);*/
            //--------------------------

        }

        Signal finalSignal = new Signal(x, fittedSignal);
        long computeTime = System.nanoTime() - startTime;
        addSmoothedSignal(finalSignal, "Gaussian regression");

        displaySmoothStats(x, y, fittedSignal, "Asymmetric gaussian regression", computeTime);


    }

    private void filterWiener(List<Tuple2> xyPairs, String title, int windowSize, boolean usePeakRestore, double ratio) {
        long startTime = System.nanoTime();

        Tuple2[] result = xyPairs.toArray(new Tuple2[xyPairs.size()]);

        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }

        Wiener wienerFilter= new Wiener(windowSize);
        double[] filteredSignal = wienerFilter.filter(y);

        if (usePeakRestore) {
            SmoothUtils.peakRestorerV2(y, filteredSignal, x, ratio);
        }

        Signal wienerSignal = new Signal(x, filteredSignal);

        long computeTime = System.nanoTime() - startTime;
        addSmoothedSignal(wienerSignal, title);

        displaySmoothStats(x, y, filteredSignal, "Wiener Method", computeTime);

    }

    private void savitskyJDSP(List<Tuple2> xyPairs, String savitskyJdspTitle, int windowSize, boolean peakRestore, double ratio) {
        long startTime = System.nanoTime();
        Tuple2[] result = xyPairs.toArray(new Tuple2[xyPairs.size()]);

        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }

        // String mode = "wrap";
        String mode = "nearest";
        //String mode="constant";
        // String mode="mirror";

        int polyOrder = 3; //Order of the polynomial used to generate coefficients
        int deriv = 0; // Order of derivative
        int delta = 10; // Spacing of samples to which filter is applied
        Savgol savgolFilter = new Savgol(windowSize, polyOrder, deriv, delta);

        double[] filteredSignal = savgolFilter.filter(y, mode);

        SmoothUtils.correctNegativeValues(y, filteredSignal);
        if (peakRestore) {

            SmoothUtils.peakRestorerV2(y, filteredSignal, x, ratio);
        }

        logger.info("initial smoothness: " + SmoothUtils.getSmoothness(y) + "  ,final smoothness: " + SmoothUtils.getSmoothness(filteredSignal));
        Signal JDSPSavitskyGolaySignal = new Signal(x, filteredSignal);

        long computeTime = System.nanoTime() - startTime;

        addSmoothedSignal(JDSPSavitskyGolaySignal, savitskyJdspTitle);

        displaySmoothStats(x, y, filteredSignal, "JDSP Savitsky Golay", computeTime);
    }

    /**
     * curve fitting with polynomials as a test
     * Not adapted to be removed
     *
     */

    private void polynomialFitter(List<Tuple2> xyPairs, String title, int nbrPoints, boolean peakRestore, double ratio) {
        long startTime = System.nanoTime();

        Tuple2[] result = xyPairs.toArray(new Tuple2[xyPairs.size()]);
        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }


        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(nbrPoints);
        WeightedObservedPoints obs = new WeightedObservedPoints();

        for (int index = 0; index < result.length; index++) {
            obs.add(x[index], y[index]);
        }
        final double[] polynomialCoefficients = fitter.fit(obs.toList());
        double[] smoothedSignal = SmoothUtils.buildPolynomialFromCoefficients(polynomialCoefficients, x[0], x[result.length - 1], result.length);

        SmoothUtils.correctNegativeValues(y, smoothedSignal);

        Signal polySignal = new Signal(x, smoothedSignal);

        long computeTime = System.nanoTime() - startTime;
        addSmoothedSignal(polySignal, title);

        displaySmoothStats(x, y, smoothedSignal, "Polynomial fitting", computeTime);


    }

    private void medianFilterJDSP(List<Tuple2> xyPairs, String find_peakTitle, int windowSize, boolean peakRestore, double ratio) {

        long startTime = System.nanoTime();
        Tuple2[] result = xyPairs.toArray(new Tuple2[xyPairs.size()]);

        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }

        Median medianFilter = new Median(windowSize);
        double[] medianSignal = medianFilter.filter(y);

        SmoothUtils.correctNegativeValues(y, medianSignal);

        if (peakRestore) {
            SmoothUtils.peakRestorerV2(y, medianSignal, x, ratio);
        }


        Signal convolutedSignal = new Signal(x, medianSignal);

        long computeTime = System.nanoTime() - startTime;

        addSmoothedSignal(convolutedSignal, find_peakTitle);

        displaySmoothStats(x, y, medianSignal, "median filter", computeTime);


    }

    /**
     * Method that calculates three indicators of the quality of the signal
     *
     */

    private void evaluateSignalQuality(List<Tuple2> xyPairs, int nbrPoints, boolean usePeakRestore, double ratio) {
        Tuple2[] result = xyPairs.toArray(new Tuple2[xyPairs.size()]);

        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }


        double[] derivatives=SmoothUtils.getDerivatives(x,y);
        double qualitySignal = SmoothUtils.evaluateSignalRoughnessV3(y);
        System.out.println("Roughness of signal: " +qualitySignal);

        double smoothness = SmoothUtils.getSmoothness(y);
        System.out.println("overall smoothness of signal : " + smoothness);
        //------------------experimental calculus

        double maxHeight1 = Arrays.stream(y).max().getAsDouble();
        StandardDeviation std = new StandardDeviation();

        double derivativesDeviation=std.evaluate(derivatives)/maxHeight1;

        logger.info("Standard deviation of derivatives: "+derivativesDeviation);
        double initialLength = SmoothUtils.computeLengthOfSignal(x, y);
        // build of a smoothedSignal
        double[] kernel = SmoothUtils.buildKernel(nbrPoints);
        Convolution con = new Convolution(y, kernel);
        double[] convolvedSignal = con.convolve("same");
        double finalLength=SmoothUtils.computeLengthOfSignal(x,convolvedSignal);
        double variationRate=100*(finalLength-initialLength)/initialLength;
        logger.info("Evaluation of signal quality:"+variationRate+" %");



    }


    private void createStatsPanel() {

        smoothStatsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 0;

        smoothStatsPanel.setBackground(new Color(230, 232, 238));
        smoothStatsPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));


        smoothStatsPanel.setMinimumSize(new Dimension(this.getWidth(), 50));
        JLabel titleLabel = new JLabel("Smoothing statistics");
        titleLabel.setFont(new Font("Serif", Font.PLAIN, 20));
        titleLabel.setForeground(Color.DARK_GRAY);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = 2;

        smoothStatsPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        JLabel smoothnessLabel = new JLabel("initial smoothness: ");

        gbc.gridy++;
        smoothStatsPanel.add(smoothnessLabel, gbc);

        initialSmoothnessLabel = new JLabel();

        gbc.gridx++;
        smoothStatsPanel.add(initialSmoothnessLabel, gbc);

        JLabel finalSmoothnessLabel = new JLabel("final smoothness: ");

        gbc.gridx = 0;
        gbc.gridy++;
        smoothStatsPanel.add(finalSmoothnessLabel, gbc);
        finalSmoothnessLabelValue = new JLabel();

        gbc.gridx++;
        smoothStatsPanel.add(finalSmoothnessLabelValue, gbc);

        JLabel sizeOfSignalJLabel = new JLabel("signal length: ");
        gbc.gridx = 0;
        gbc.gridy++;
        smoothStatsPanel.add(sizeOfSignalJLabel, gbc);

        displaySizeSignalJLabel = new JLabel();
        gbc.gridx++;
        smoothStatsPanel.add(displaySizeSignalJLabel, gbc);

        JLabel estimatedSNRJLabel = new JLabel("Estimated signal to noise ratio: ");
        gbc.gridy++;
        gbc.gridx = 0;
        smoothStatsPanel.add(estimatedSNRJLabel, gbc);

        displaySNRJLabel = new JLabel();
        gbc.gridx++;
        smoothStatsPanel.add(displaySNRJLabel, gbc);

        JLabel displayBiasJLabel = new JLabel("estimated bias: ");
        gbc.gridy++;
        gbc.gridx = 0;
        smoothStatsPanel.add(displayBiasJLabel, gbc);
        displayBiasValue = new JLabel();
        gbc.gridx++;

        smoothStatsPanel.add(displayBiasValue, gbc);
        JLabel qualityDisplayLabel = new JLabel("Roughness of signal");
        qualityDisplayLabel.setToolTipText("Indicator of the roughness of the signal, low value will correspond to smooth signal");
        gbc.gridx = 0;
        gbc.gridy++;
        smoothStatsPanel.add(qualityDisplayLabel, gbc);

        displayQualityValueJLabel = new JLabel();

        gbc.gridx++;
        smoothStatsPanel.add(displayQualityValueJLabel, gbc);

        JLabel qualityLabel = new JLabel("Quality of signal: ");
        qualityLabel.setToolTipText("Value can go from 0 to 100, a large value shall represent a signal with low noise");
        gbc.gridx = 0;
        gbc.gridy++;
        smoothStatsPanel.add(qualityLabel, gbc);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        // displayRateVariationLengthJLabel = new JLabel();
        gbc.gridx++;
        smoothStatsPanel.add(progressBar, gbc);

        JLabel displayMethod = new JLabel("Method used for smoothing: ");
        gbc.gridy++;
        gbc.gridx = 0;
        smoothStatsPanel.add(displayMethod, gbc);

        smoothMethodUsedLabel = new JLabel();
        gbc.gridx++;
        smoothStatsPanel.add(smoothMethodUsedLabel, gbc);

        JLabel timeJLabel = new JLabel("calculation time: ");
        gbc.gridy++;
        gbc.gridx = 0;
        smoothStatsPanel.add(timeJLabel, gbc);

        displayComputeTime = new JLabel();
        gbc.gridx++;
        smoothStatsPanel.add(displayComputeTime, gbc);


        this.add(smoothStatsPanel, BorderLayout.SOUTH);

        this.revalidate();
        this.repaint();
        smoothStatsPanel.setVisible(false);

    }

    private void displaySmoothStats(double[] x, double[] initialSignal, double[] finalSignal, String smoothMethod, long computeTime) {


        int signalLength = x.length;
        displaySizeSignalJLabel.setText(signalLength + " points");

        double smoothness = SmoothUtils.getSmoothness(initialSignal);
        initialSmoothnessLabel.setText(Double.toString(smoothness));

        double finalSmoothness = SmoothUtils.getSmoothness(finalSignal);
        finalSmoothnessLabelValue.setText(Double.toString(finalSmoothness));

        Double estimatedSNRValue = SmoothUtils.computeSignalToNoiseRatio(initialSignal, finalSignal);
        displaySNRJLabel.setText(estimatedSNRValue + " decibels");

        double bias = SmoothUtils.computeAbsoluteBias(initialSignal, finalSignal);
        displayBiasValue.setText(Double.toString(bias));
        double estimatedQualityValue = SmoothUtils.evaluateSignalRoughnessV3(initialSignal);
        displayQualityValueJLabel.setText(String.valueOf(estimatedQualityValue));
        smoothMethodUsedLabel.setText(smoothMethod);
        displayComputeTime.setText(SmoothUtils.displayTime(computeTime));
        double initialLength=SmoothUtils.computeLengthOfSignal(x,initialSignal);
        //TODO finalLength should be computed with a particular fixed method to allow better comparisons?
        double finalLength=SmoothUtils.computeLengthOfSignal(x,finalSignal);
        double variationRateOfLength=100+100*(finalLength-initialLength)/initialLength;
       // displayRateVariationLengthJLabel.setText(String.valueOf(variationRateOfLength));
        progressBar.setValue((int) variationRateOfLength);
        if (variationRateOfLength>50)
        {progressBar.setForeground(new Color(85, 200, 90));}
        else {
            progressBar.setForeground(new Color(180,0,0));
        }
        smoothStatsPanel.setVisible(true);

    }


}

