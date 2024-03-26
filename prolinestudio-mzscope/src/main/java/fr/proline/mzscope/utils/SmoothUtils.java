package fr.proline.mzscope.utils;

import com.github.psambit9791.jdsp.misc.UtilMethods;
import com.github.psambit9791.jdsp.signal.Convolution;
import com.github.psambit9791.jdsp.signal.Smooth;
import com.github.psambit9791.jdsp.signal.peaks.FindPeak;
import com.github.psambit9791.jdsp.signal.peaks.Peak;
import fr.proline.mzscope.ui.dialog.WaveletSmootherParamDialog;
import jwave.Transform;
import jwave.compressions.Compressor;
import jwave.compressions.CompressorMagnitude;
import jwave.compressions.CompressorPeaksAverage;
import jwave.transforms.AncientEgyptianDecomposition;
import jwave.transforms.DiscreteFourierTransform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.Wavelet;
import jwave.transforms.wavelets.WaveletBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;

public class SmoothUtils {

    final private static Logger logger = LoggerFactory.getLogger(SmoothUtils.class);

    public static void peakRestorer(double[] initialSignal, double[] smoothedSignal, double[] x, double ratio) {

        FindPeak fp = new FindPeak(initialSignal);

        Peak out = fp.detectPeaks();
        int[] peaks = out.getPeaks();
        double[] heightOfPeaks = out.getHeights();

        double maxPeak = 0;
        for (double heightOfPeak : heightOfPeaks) {
            if (maxPeak < heightOfPeak) {
                maxPeak = heightOfPeak;
            }
        }


        double[] xValuesOfPeaks = new double[peaks.length];
        for (int i = 0; i < peaks.length; i++) {

            xValuesOfPeaks[i] = x[peaks[i]];

        }
        int countPeaksRestored = 0;
        for (int k = 0; k < smoothedSignal.length; k++) {
            for (int i = 0; i < xValuesOfPeaks.length; i++) {
                if (xValuesOfPeaks[i] == x[k] && heightOfPeaks[i] > ratio * maxPeak) {
                    smoothedSignal[k] = heightOfPeaks[i];
                    countPeaksRestored++;

                }
            }
        }
        logger.info("peaks restored " + countPeaksRestored + " times ");
        logger.info("peaks with a height superior to " + ratio * 100 + " percent of the highest peak have been restored");



    }

    public static double[] getSlopes(double[] signal) {
        double[] signalSlopes = new double[signal.length - 1];
        for (int k = 0; k < signal.length - 1; k++) {
            signalSlopes[k] = signal[k + 1] - signal[k];
        }
        return signalSlopes;
    }
    public static double[] getDerivatives(double[]x,double[]y){
        double [] signalDerivatives=new double[x.length-1];
        for (int k=0;k<x.length-1;k++){
            signalDerivatives[k]=(y[k+1]-y[k])/(x[k+1]-x[k]);
        }
        return signalDerivatives;

    }
    public static double[] getSlopeAngles(double[]x,double[]y){
        double [] signalSlopeAngles=new double[x.length-1];
        for (int k=0;k<x.length-1;k++){
            // values always close to pi/2.....😤
            signalSlopeAngles[k]=Math.atan((y[k+1]-y[k])/(x[k+1]-x[k]));
        }
        return signalSlopeAngles;

    }

    public static double getSmoothness(double[] signal) {

        double[] slopes = getSlopes(signal);

        double roughness = 0;
        for (int k = 0; k < slopes.length - 1; k++) {

            boolean notCloseToBoundaries = (k > 0 && k < slopes.length - 1);
            boolean curveHasAHat = false;
            if (notCloseToBoundaries) {
                curveHasAHat = slopes[k - 1] != 0 && slopes[k] == 0 && slopes[k + 1] != 0;
            }
            boolean slopeChanged=slopes[k]*slopes[k+1]<0;

            if (slopeChanged || curveHasAHat) {
                roughness++;

            }
        }
        return 1 - roughness / slopes.length;

    }

 /*
    public static double evaluateSignalQuality(double[] signal, double peakThreshold) {

        double[] derivatives = getSlopes(signal);


        double maxValueOfSignalThreshold = getMaxFromArray(signal) * peakThreshold;
        int numberOfHighPeak = 0;
        for (int k = 0; k < derivatives.length - 1; k++) {
            //TODO take into account value of slope?

            // boolean slopeChanged2=((derivatives[k]==0)&&(derivatives[k+1]!=0))||((derivatives[k]!=0)&&derivatives[k+1]==0);
            boolean slopeChanged = (derivatives[k] > 0 && derivatives[k + 1] < 0) || (derivatives[k] < 0 && derivatives[k + 1] > 0);
            boolean peakHasHighAmplitude = Math.abs(signal[k + 1] - signal[k]) > maxValueOfSignalThreshold;

            if ((slopeChanged)) {

                if (peakHasHighAmplitude) {
                    numberOfHighPeak++;
                }
            }
        }
        double density = numberOfHighPeak;

        return 1 - density / derivatives.length;

    }*/

 /*   public static double[] evaluateSignalQualityV2(double[] signal, double peakThreshold) {
        double[] signalMetrics = new double[2];
        double[] derivatives = getSlopes(signal);

        int smoothness = 0;
        double maxValueOfSignalThreshold = getMaxFromArray(signal) * peakThreshold;
        int numberOfHighPeak = 0;
        for (int k = 0; k < derivatives.length - 1; k++) {
            //TODO detect hats

            // boolean slopeChanged2=((derivatives[k]==0)&&(derivatives[k+1]!=0))||((derivatives[k]!=0)&&derivatives[k+1]==0);

            boolean slopeChanged = (derivatives[k] > 0 && derivatives[k + 1] < 0) || (derivatives[k] < 0 && derivatives[k + 1] > 0);
            //  boolean peakHasHighAmplitude = Math.abs(signal[k + 1] - signal[k]) > maxValueOfSignalThreshold;

            if ((slopeChanged)) {
                smoothness++;
                if (Math.abs(signal[k + 1] - signal[k]) > maxValueOfSignalThreshold) {
                    numberOfHighPeak++;
                }

            }

        }
        double density = numberOfHighPeak;
        double continuity = smoothness;
        signalMetrics[0] = 1 - density / derivatives.length;
        signalMetrics[1] = 1 - continuity / derivatives.length;


        return signalMetrics;

    }*/

    /**
     * Method that counts crests in the signal,
     * number of crests is pondered by the amplitude of crests
     *
     * @param signal signal analysed
     *
     */
    public static double evaluateSignalRoughnessV3(double[] signal) {

        double[] slopes = getSlopes(signal);
        // could be modified
        double[] peakThreshold = {0.1,0.2, 0.3, 0.4, 0.5,0.6,0.7,0.8};
        double roughness = 0;
        double maxValueInSignal= Arrays.stream(signal).max().getAsDouble();

        for (double v : peakThreshold) {
            double maxValueOfSignalThreshold = maxValueInSignal * v;
            int numberOfHighPeaks = 0;

            for (int k = 0; k < slopes.length - 1; k++) {
                boolean slopeChanged = (slopes[k] > 0 && slopes[k + 1] < 0) || (slopes[k] < 0 && slopes[k + 1] > 0);
                if ((slopeChanged)) {
                    boolean peakHasHighAmplitude = Math.abs(signal[k + 1] - signal[k]) > maxValueOfSignalThreshold;
                    if (peakHasHighAmplitude) {
                        numberOfHighPeaks++;
                    }
                }
            }
            // number of peaks are weighted by the amplitude of the peak
            double density = numberOfHighPeaks * v * 10;
            roughness = roughness + density;
        }
        return (roughness)/signal.length;

    }


    /**
     * A small value shall represent both a high correlation and a good smoothing
     * Warning: a factor 100 is used to compensate correlation always close to 1
     *
     */
    public static double calculateDistanceToOptimalPoint(double correlation, double smoothness) {

        return Math.sqrt((1 - correlation) * (1 - correlation) * 100 + (1 - smoothness) * (1 - smoothness));
    }

    /**
     *
     *
     */
    public static int findBestWavelet(double[] correlations, double[] smoothness) {

        int bestWaveletIndex = 7;
        boolean valueFound = false;
        for (int k = 1; k < 1999; k++) {
            for (int i = 0; i < 51 && i != 7; i++) {
                double smooth = smoothness[i];
                double correlation = correlations[i];
                double threshold = (double) k / 1000;
                if (correlation > (-smooth + 2 - threshold)) {
                    bestWaveletIndex = i;
                    valueFound = true;
                    break;
                }

            }
            if (valueFound) {
                break;
            }
        }
        return bestWaveletIndex;

    }

    public static int findBestWaveletEuclidAndSmoothness(double[] euclideanDistance, double[] smoothness) {

        int bestWaveletIndex = 7; // Daubechies 8
        boolean valueFound = false;
        double euclidMax = Arrays.stream(euclideanDistance).max().getAsDouble();

        for (int k = 0; k < 50; k++) {
            for (int i = 1; i < 51 && i != 7; i++) {
                double smooth = smoothness[i];
                double euclidDist = euclideanDistance[i];
                double threshold = (double) k / 100;
                if (euclidDist < (euclidMax * (smooth - 1 + threshold))) {
                    bestWaveletIndex = i;
                    valueFound = true;
                    break;
                }
            }
            if (valueFound) {
                break;
            }
        }
        if (!valueFound) {
            logger.info("No wavelet filled conditions, will use Daubechies 8 wavelet");
        }

        return bestWaveletIndex;

    }

    public static void correctNegativeValues(double[] initialArray, double[] smoothedSignal) {

        for (int k = 0; k < initialArray.length; k++) {
            if (smoothedSignal[k] < 0) {
                smoothedSignal[k] = initialArray[k];
            }
        }
    }

    // found in scientific article but not very efficient
    public static double[] compressImprovedMethod(double[] arr, double threshold) {

        int arrLength = arr.length;
        double[] arrComp = new double[arrLength];
        double magnitude = 0;
        for (double v : arr) {
            magnitude = magnitude + Math.abs(v);
        }
        magnitude = (threshold * magnitude) / arrLength;
        for (int i = 0; i < arrLength; ++i) {
            if (arr[i] >= magnitude) {
                arrComp[i] = arr[i] - ((2 * magnitude) / (1 + Math.exp(magnitude - arr[i])));
            }
            if (Math.abs(arr[i]) < magnitude) {
                arrComp[i] = 0;
            }
            if (arr[i] <= -magnitude) {
                arrComp[i] = arr[i] + ((2 * magnitude) / (1 + Math.exp(magnitude + arr[i])));
            }
        }

        return arrComp;
    }
    // Second method used for wavelet smoothing known as soft thresholding
    public static double[] compressSoftMethod(double[] arr, double threshold) {

        int arrLength = arr.length;
        double[] arrComp = new double[arrLength];
        double magnitude = 0;
        for (int k = 0; k < arr.length; k++) {
            magnitude = magnitude + Math.abs(arr[k]);
        }
        magnitude = magnitude / arrLength;
        for (int i = 0; i < arrLength; ++i) {
            if (Math.abs(arr[i]) >= threshold * magnitude) {
                arrComp[i] = Math.signum(arr[i]) * (Math.abs(arr[i]) - magnitude);
            } else {
                arrComp[i] = 0;
            }
        }

        return arrComp;
    }

    public static double computeSignalPower(double[] signal) {
        double energy = 0;
        for (int k = 0; k < signal.length; k++) {
            energy = energy + (signal[k] * signal[k]);
        }
        return energy / signal.length;
    }


    public static double computeAbsoluteBias(double[] initialValues, double[] smoothedValues) {
        double bias = 0;
        int numberOfValues = initialValues.length;
        for (int k = 0; k < numberOfValues; k++) {
            bias = bias + Math.abs(initialValues[k] - smoothedValues[k]);

        }
        bias = bias / numberOfValues;
        return bias;

    }

    public static double computeRelativeBias(double[] initialValues, double[] smoothedValues) {
        double bias = 0;
        int numberOfValues = initialValues.length;
        for (int k = 0; k < numberOfValues; k++) {
            bias = bias + (initialValues[k] - smoothedValues[k]);

        }
        bias = bias / numberOfValues;
        return bias;

    }



    public static double[] signalMixer(double[] signal1, double[] signal2, double weight1) {
        double[] averageSignal = new double[signal1.length];
        for (int k = 0; k < signal1.length; k++) {
            averageSignal[k] = (weight1 * signal1[k] + (1 - weight1) * signal2[k]) / 2;

        }
        return averageSignal;
    }



    public static int indexOf(double[] array, double value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static double computeEnergyDifference(double[] signal1, double[] signal2) {
        return Math.abs(computeSignalPower(signal1) - computeSignalPower(signal2));
    }


    // not adapted to smoothing of peakels
    public static double[] fourierCompressor(double[] signal, double threshold, boolean usePeakRestore, double[] x, double ratio) {

        Transform fourierTransform = new Transform(new AncientEgyptianDecomposition(new DiscreteFourierTransform()));
        double[] fourierSignal = fourierTransform.forward(signal);
        Compressor fourierCompressor = new CompressorMagnitude(threshold);
        double[] compressedSignal = fourierCompressor.compress(fourierSignal);
        double[] rebuildSignal = fourierTransform.reverse(compressedSignal);
        SmoothUtils.correctNegativeValues(signal,rebuildSignal);

        if (usePeakRestore) {
            SmoothUtils.peakRestorer(signal, rebuildSignal, x, ratio);
        }
        logger.info("initial smoothness:  " + SmoothUtils.getSmoothness(signal) + " final smoothness: " + SmoothUtils.getSmoothness(rebuildSignal));

        return rebuildSignal;
    }

    public static double computeSignalToNoiseRatio(double[] initialSignal, double[] finalSignal) {
        double[] noiseSignal = new double[initialSignal.length];
        for (int k = 0; k < initialSignal.length; k++) {
            noiseSignal[k] = initialSignal[k] - finalSignal[k];
        }
        double powerInitial = computeSignalPower(initialSignal);
        double powerNoise = computeSignalPower(noiseSignal);

        return 10 * Math.log10(powerInitial / powerNoise);


    }

    public static double[] filter(double[] signal, int windowSize, float percentile) {
        if (windowSize >= signal.length) {
            throw new IllegalArgumentException("Window size cannot be greater than or equal to signal length");
        } else {
            int paddingSize = (windowSize - 1) / 2;
            double[] cons = new double[paddingSize];
            double[] newSignal = new double[signal.length];
            Arrays.fill(cons, 0.0);
            double[] paddedSignal = new double[0];
            paddedSignal = UtilMethods.concatenateArray(paddedSignal, cons);
            paddedSignal = UtilMethods.concatenateArray(paddedSignal, signal);
            paddedSignal = UtilMethods.concatenateArray(paddedSignal, cons);

            for (int i = 0; i < signal.length; ++i) {
                newSignal[i] = StatUtils.percentile(paddedSignal, i, windowSize, percentile);
            }

            return newSignal;
        }
    }

    public static double[] buildKernel(int n) {
        if (n<=1) {
            n = 2;
        }
        double[] kernel = new double[n];
        double value = (double) 1 / n;
        for (int k = 0; k < n; k++) {
            kernel[k] = value;
        }
        return kernel;
    }



    public static double[] buildSymmetricGaussian(double[] params, double minX, double maxX, int nbrPoints) {
        double norm = params[0];
        double mean = params[1];
        double sigma = params[2];
        double[] gaussianSignal = new double[nbrPoints];
        Gaussian gaussian = new Gaussian(norm, mean, sigma);
        for (int k = 0; k < nbrPoints; k++) {
            double x = ((maxX - minX) / nbrPoints) * k;
            gaussianSignal[k] = gaussian.value(minX + x);
        }

        return gaussianSignal;

    }

    public static double[] buildAsymmetricGaussian(double norm, double mean, double sigmaLeft, double sigmaRight, double minX, double maxX, int nbrPoints) {

        double[] gaussianSignal = new double[nbrPoints];
        Gaussian gaussianLeft = new Gaussian(norm, mean, sigmaLeft);
        Gaussian gaussianRight = new Gaussian(norm, mean, sigmaRight);
        for (int k = 0; k < nbrPoints; k++) {
            double x = ((maxX - minX) / nbrPoints) * k;
            if (minX + x <= mean) {
                gaussianSignal[k] = gaussianLeft.value(minX + x);
            } else {
                gaussianSignal[k] = gaussianRight.value(minX + x);
            }

        }
        return gaussianSignal;

    }

    public static double getBestGaussianMean(double[] params, double xMin, double xMax, int nbrPoints, double[] initialSignal) {

        double delta = 0.45 * (xMax - xMin);
        double norm = params[0];
        double bestMean = params[1];
        double sigma = params[2];

        double[] firstSmoothedSignal = new double[nbrPoints];
        Gaussian initialGaussian = new Gaussian(norm, bestMean, sigma);
        for (int i = 0; i < nbrPoints; i++) {
            double x = ((xMax - xMin) / nbrPoints) * i;
            firstSmoothedSignal[i] = initialGaussian.value(xMin + x);

        }
      //  double bestCorrelation=new PearsonsCorrelation().correlation(firstSmoothedSignal,initialSignal);
        double bestCorrelation = SmoothUtils.computeEuclideanDistance(firstSmoothedSignal, initialSignal);
        for (int k = 0; k < 11; k++) {
            double testedMean = params[1] - delta + k * delta / 5;
            double[] testedSignal = new double[nbrPoints];
            Gaussian gaussian = new Gaussian(norm, testedMean, sigma);

            for (int i = 0; i < nbrPoints; i++) {
                double x = ((xMax - xMin) / nbrPoints) * i;
                testedSignal[i] = gaussian.value(xMin + x);
            }
            // double correlation=new PearsonsCorrelation().correlation(testedSignal, initialSignal);
            double correlation = SmoothUtils.computeEuclideanDistance(testedSignal, initialSignal);
            if (correlation < bestCorrelation) {
                bestMean = testedMean;
                bestCorrelation = correlation;
            }
        }
        return bestMean;
    }

    /**
     * Computes a symmetric sigma that fits better the curve.
     *
     */
    public static double getBestGaussianSigmaSymmetric(double[] params, double xMin, double xMax, int nbrPoints, double[] initialSignal) {

        double norm = params[0];
        double sigma = params[2];
        double mean = params[1];
        double[] firstSmoothedSignal = new double[nbrPoints];
        Gaussian initialGaussian = new Gaussian(norm, mean, sigma);
        for (int i = 0; i < nbrPoints; i++) {
            double x = ((xMax - xMin) / nbrPoints) * i;
            firstSmoothedSignal[i] = initialGaussian.value(xMin + x);

        }
        //  double bestCorrelation=new PearsonsCorrelation().correlation(firstSmoothedSignal, initialSignal);;
        double bestCorrelation = SmoothUtils.computeEuclideanDistance(firstSmoothedSignal, initialSignal);
        double deltaSigma = 0.3 * sigma;
        double bestSigma = sigma;

        for (int k = 0; k < 31; k++) {

            double testedSigma = sigma - deltaSigma + 2 * deltaSigma * k / 30;
            double[] testedSignal = new double[nbrPoints];
            Gaussian gaussian = new Gaussian(norm, mean, testedSigma);

            for (int i = 0; i < nbrPoints; i++) {
                double x = ((xMax - xMin) / nbrPoints) * i;
                testedSignal[i] = gaussian.value(xMin + x);

            }

            double correlation = SmoothUtils.computeEuclideanDistance(testedSignal, initialSignal);
            if (correlation < bestCorrelation) {
                bestSigma = testedSigma;
                bestCorrelation = correlation;

            }

        }
        return bestSigma;
    }


    public static double[] computeOptimalSigmas(double[] initialGaussianParameters, double xMin, double xMax, int nbrPoints, double[] initialSignal) {
        int steps = 30;
        double norm = initialGaussianParameters[0];
        double mean = initialGaussianParameters[1];
        double sigma = initialGaussianParameters[2];
        double bestLeftSigma = sigma;
        double bestRightSigma = sigma;
        double[] firstSmoothedSignal = new double[nbrPoints];
        Gaussian initialGaussian = new Gaussian(norm, mean, sigma);
        for (int i = 0; i < nbrPoints; i++) {
            double x = ((xMax - xMin) / nbrPoints) * i;
            firstSmoothedSignal[i] = initialGaussian.value(xMin + x);
        }

        double bestCorrelation = SmoothUtils.computeEuclideanDistance(firstSmoothedSignal, initialSignal);
        double deltaSigma = 90* sigma / 100;

        for (int k = 0; k < steps; k++) {
            double leftSigma = sigma - deltaSigma + deltaSigma * 2 * k / steps;

            for (int i = 0; i < steps; i++) {

                double rightSigma = sigma - deltaSigma + deltaSigma * 2 * i / steps;
                double[] theoreticalSignal = SmoothUtils.buildAsymmetricGaussian(norm, mean, leftSigma, rightSigma, xMin, xMax, nbrPoints);
                double correlation=SmoothUtils.computeEuclideanDistance(theoreticalSignal,initialSignal);

                if (correlation < bestCorrelation) {
                    bestCorrelation = correlation;
                    bestRightSigma = rightSigma;
                    bestLeftSigma = leftSigma;

                }
            }


        }
        double[] sigmas = new double[2];
        sigmas[0] = bestLeftSigma;
        sigmas[1] = bestRightSigma;
        return sigmas;
    }

    // not very fast and not so good results can be removed
    public static double[] computeOptimalGaussianParameters(double[] initialGaussianParameters, double xMin, double xMax, int nbrPoints, double[] initialSignal) {
        int steps = 30;

        double norm = initialGaussianParameters[0];

        double mean = initialGaussianParameters[1];
        double sigma = initialGaussianParameters[2];
        double bestLeftSigma = sigma;
        double bestRightSigma = sigma;
        double bestNorm = norm;
        double bestMean = mean;
        double[] firstSmoothedSignal = new double[nbrPoints];
        Gaussian initialGaussian = new Gaussian(norm, mean, sigma);
        for (int i = 0; i < nbrPoints; i++) {
            double x = ((xMax - xMin) / nbrPoints) * i;
            firstSmoothedSignal[i] = initialGaussian.value(xMin + x);

        }

        double bestCorrelation = SmoothUtils.computeEuclideanDistance(firstSmoothedSignal, initialSignal);

        double deltaSigma = 20 * sigma / 100;
        double deltaNorm = 20 * norm / 100;
        double deltaMean = 20 * mean / 100;

        for (int k = 0; k < steps; k++) {
            double leftSigma = sigma - deltaSigma + deltaSigma * 2 * k / steps;
            for (int i = 0; i < steps; i++) {
                double rightSigma = sigma - deltaSigma + deltaSigma * 2 * i / steps;
                for (int j = 0; j < steps; j++) {
                    double meanTested = mean - deltaMean + deltaMean * 2 * j / steps;
                    for (int l = 0; l < steps; l++) {
                        double normTested = norm - deltaNorm + deltaNorm * 2 * l / steps;
                        double[] theoreticalSignal = SmoothUtils.buildAsymmetricGaussian(normTested, meanTested, leftSigma, rightSigma, xMin, xMax, nbrPoints);
                        double correlation = SmoothUtils.computeEuclideanDistance(theoreticalSignal, initialSignal);
                        if (correlation < bestCorrelation) {
                            bestCorrelation = correlation;
                            bestNorm = normTested;
                            bestMean = meanTested;
                            bestLeftSigma = leftSigma;
                            bestRightSigma = rightSigma;


                        }
                    }
                }
            }

        }
        double[] optimalParameters = new double[4];
        optimalParameters[0] = bestNorm;
        optimalParameters[1] = bestMean;
        optimalParameters[2] = bestLeftSigma;
        optimalParameters[3] = bestRightSigma;
        return optimalParameters;

    }
    public static double ComputePearsonCorrelation(double[] initialSignal, double[] transformedSignal) {
        return new PearsonsCorrelation().correlation(transformedSignal, initialSignal);
    }

    public static double computeEuclideanDistance(double[] signal1, double[] signal2) {
        if (signal1.length != signal2.length) {
            throw new IllegalArgumentException("Signals must have same length");
        }

        double sumOfSquared = 0.0;
        for (int i = 0; i < signal1.length; i++) {
            double difference = signal1[i] - signal2[i];
            sumOfSquared += difference * difference;
        }

        return Math.sqrt(sumOfSquared) / signal1.length;
    }

     public static int getBestSmoothWindowSize(double[] initialSignal) {

        int bestWindowSize = 5;
        String mode = "triangular";

        Smooth s1=new Smooth(initialSignal,bestWindowSize,mode);
        double[] testSignal=s1.smoothSignal("same");
        double bestDistance=computeEuclideanDistance(initialSignal,testSignal);

        for (int k = 0; k <6; k++) {
            int windowTested=bestWindowSize-2+k;
            Smooth s=new Smooth(initialSignal,windowTested,mode);
            double[] smoothSignal = s.smoothSignal("same");
            double distance = computeEuclideanDistance(initialSignal,smoothSignal);

                    if (distance<bestDistance) {
                    bestWindowSize = windowTested;
                   bestDistance=distance;
                }
        }
        return bestWindowSize;
    }

    public static double gaussian(double norm, double mean, double sigma, double x) {
        double exponent = -0.5 * Math.pow((x - mean) / sigma, 2);
        return norm * Math.exp(exponent) / (sigma * Math.sqrt(2 * Math.PI));
    }

    public static double getMaxFromArray(double[] parsedArray) {
        double max = 0;
        for (int k = 0; k < parsedArray.length; k++) {
            if (max < parsedArray[k]) {
                max = parsedArray[k];
            }
        }
        return max;
    }



    public static Pair<double[], String> waveletSmoother(List<Tuple2> xyPairs, String smoothMethod, double threshold, int waveletIndex, boolean userSelectedAWavelet) {


        Tuple2[] result = xyPairs.toArray(new Tuple2[xyPairs.size()]);

        double[] x = new double[result.length];
        double[] y = new double[result.length];
        for (int k = 0; k < result.length; k++) {
            x[k] = (Double) result[k]._1;
            y[k] = (Double) result[k]._2;
        }


        Wavelet[] arrOfWaveletObjects = WaveletBuilder.create2arr();

        Compressor compressor = new CompressorMagnitude(threshold);
        // compressorPeaksAverage Average not efficient
        Compressor compressor2 = new CompressorPeaksAverage(threshold);

        if (userSelectedAWavelet) {

            Wavelet wavelet = arrOfWaveletObjects[waveletIndex];

            Transform fwt = new Transform(new AncientEgyptianDecomposition(new FastWaveletTransform(wavelet)));
            // Transform fwt = new Transform(new AncientEgyptianDecomposition( new WaveletPacketTransform( wavelet )) );

            double[] transformedSignal = fwt.forward(y);
            double[] compressedSignal = compressor.compress(transformedSignal);

            /**
             * Two other methods to compress signal
             */
            // double[] compressedSignal=SmoothUtils.compressSoftMethod(transformedSignal,threshold);
            //double[] compressedSignal=SmoothUtils.compressImprovedMethod(transformedSignal,threshold);

            double[] rebuiltSignal = fwt.reverse(compressedSignal);


            SmoothUtils.correctNegativeValues(y,rebuiltSignal);
            String waveletName= wavelet.getName();

            return Pair.of(rebuiltSignal, waveletName);


        } else {

            double[] valuesOfBias = new double[arrOfWaveletObjects.length];
            double[] energyDifference = new double[arrOfWaveletObjects.length];

            double[] valuesOfROC = new double[arrOfWaveletObjects.length];
            double[] valuesOfSNR = new double[arrOfWaveletObjects.length];


            double[] valuesOfSmoothness = new double[arrOfWaveletObjects.length];
            double[] correlations = new double[arrOfWaveletObjects.length];
            double[] distanceToOptimalPoint = new double[arrOfWaveletObjects.length];
            double[] distanceToInitialSignal = new double[arrOfWaveletObjects.length];
            /**
             * at first will run over all wavelets and compute different metrics to
             * evaluate performance of wavelets
             */
            for (int i = 0; i < arrOfWaveletObjects.length; i++) {
                Wavelet wavelet = arrOfWaveletObjects[i];

                Transform fwt = new Transform(new AncientEgyptianDecomposition(new FastWaveletTransform(wavelet)));
                // Transform fwt = new Transform(new AncientEgyptianDecomposition( new WaveletPacketTransform( wavelet )) );

                double[] transformedSignal = fwt.forward(y);

                double[] compressedSignal = compressor.compress(transformedSignal);
                /**
                 * Two other methods to compress signal
                 */
                // double[] compressedSignal=SmoothUtils.compressSoftMethod(transformedSignal,threshold);
                //  double[] compressedSignal=SmoothUtils.compressImprovedMethod(transformedSignal,threshold);

                double[] rebuiltSignal = fwt.reverse(compressedSignal);

                SmoothUtils.correctNegativeValues(y,rebuiltSignal);
                valuesOfBias[i]=SmoothUtils.computeAbsoluteBias(y,rebuiltSignal);
                valuesOfSNR[i] = SmoothUtils.computeSignalToNoiseRatio(y, rebuiltSignal);
                energyDifference[i] = SmoothUtils.computeEnergyDifference(rebuiltSignal, y);
                valuesOfSmoothness[i] = SmoothUtils.getSmoothness(rebuiltSignal);
                correlations[i] = Math.abs(SmoothUtils.ComputePearsonCorrelation(y, rebuiltSignal));
                distanceToOptimalPoint[i] = SmoothUtils.calculateDistanceToOptimalPoint(correlations[i], valuesOfSmoothness[i]);
                distanceToInitialSignal[i] = SmoothUtils.computeEuclideanDistance(y, rebuiltSignal);
            }


            int indexOfOptimalWavelet = SmoothUtils.findBestWavelet(correlations, valuesOfSmoothness);

            int indexOfEuclideanSmoothness = SmoothUtils.findBestWaveletEuclidAndSmoothness(distanceToInitialSignal, valuesOfSmoothness);

            double minDistance = distanceToInitialSignal[0];
            int indexOfMinDist = 0;
            for (int k = 0; k < arrOfWaveletObjects.length; k++) {
                if (minDistance > distanceToInitialSignal[k]) {
                    minDistance = distanceToInitialSignal[k];
                    indexOfMinDist = k;
                }
            }

            double minBias = valuesOfBias[0];
            int indexOfMin = 0;
            for (int k = 1; k < arrOfWaveletObjects.length; k++) {
                if (minBias > valuesOfBias[k]) {
                    minBias = valuesOfBias[k];
                    indexOfMin = k;
                }

            }

            double maxBias = valuesOfBias[0];
            int indexOfMax = 0;
            for (int k = 0; k < arrOfWaveletObjects.length; k++) {
                if (maxBias < valuesOfBias[k]) {
                    maxBias = valuesOfBias[k];
                    indexOfMax = k;
                }
            }
            double minEnergyDifference = energyDifference[0];
            int indexOfMinEnergy = 0;
            for (int k = 0; k < arrOfWaveletObjects.length; k++) {
                if (minEnergyDifference > energyDifference[k]) {
                    minEnergyDifference = energyDifference[k];
                    indexOfMinEnergy = k;
                }
            }
            double maxSmoothness = valuesOfSmoothness[0];
            int indexOfmaxSmoothness = 0;
            for (int k = 0; k < arrOfWaveletObjects.length; k++) {
                if (maxSmoothness < valuesOfSmoothness[k]) {
                    maxSmoothness = valuesOfSmoothness[k];
                    indexOfmaxSmoothness = k;
                }
            }
            double minDistanceToOptimalPoint = valuesOfROC[0];
            int indexOfMinDistanceToOptimalPoint = 0;
            for (int k = 0; k < arrOfWaveletObjects.length; k++) {
                if (minDistanceToOptimalPoint > valuesOfROC[k]) {
                    minDistanceToOptimalPoint = valuesOfROC[k];
                    indexOfMinDistanceToOptimalPoint = k;
                }
            }
            double maxSNR = valuesOfSNR[0];
            int indexOfWaveletWithMaxSNR = 0;
            for (int k = 0; k < arrOfWaveletObjects.length; k++) {
                if (maxSNR < valuesOfSNR[k]) {
                    maxSNR = valuesOfSNR[k];
                    indexOfWaveletWithMaxSNR = k;
                }
            }
            double minSNR = valuesOfSNR[0];
            int indexOfWaveletWithMinSNR = 0;
            for (int k = 0; k < arrOfWaveletObjects.length; k++) {
                if (minSNR > valuesOfSNR[k]) {
                    minSNR = valuesOfSNR[k];
                    indexOfWaveletWithMinSNR = k;
                }
            }
            double maxCorrelation = correlations[0];
            int indexOfWaveletWithMaxCorrelation = 0;
            for (int k = 0; k < arrOfWaveletObjects.length; k++) {
                if (maxCorrelation < correlations[k]) {
                    maxCorrelation = correlations[k];
                    indexOfWaveletWithMaxCorrelation = k;

                }
            }
            double minCorrelation = correlations[0];
            int indexOfWaveletWithMinCorrelation = 0;
            for (int k = 0; k < arrOfWaveletObjects.length; k++) {
                if (minCorrelation > correlations[k]) {
                    minCorrelation = correlations[k];
                    indexOfWaveletWithMinCorrelation = k;
                }
            }

            double optimalValue = distanceToOptimalPoint[0];
            int indexOfOptimalValue = 0;
            for (int k = 0; k < arrOfWaveletObjects.length; k++) {
                if (optimalValue > distanceToOptimalPoint[k]) {
                    optimalValue = distanceToOptimalPoint[k];
                    indexOfOptimalValue = k;

                }
            }
            Wavelet selectedWavelet;

            switch (smoothMethod) {
                case WaveletSmootherParamDialog.MAX_CORRELATION:
                    selectedWavelet = arrOfWaveletObjects[indexOfWaveletWithMaxCorrelation];
                    break;
                case WaveletSmootherParamDialog.MIN_CORRELATION:
                    selectedWavelet = arrOfWaveletObjects[indexOfWaveletWithMinCorrelation];
                    break;

                case WaveletSmootherParamDialog.BOTH_SMOOTHNESS_AND_CORRELATION:
                    selectedWavelet = arrOfWaveletObjects[indexOfOptimalWavelet];
                    break;
                case WaveletSmootherParamDialog.MAX_SNR:
                    selectedWavelet = arrOfWaveletObjects[indexOfWaveletWithMaxSNR];
                    break;
                case WaveletSmootherParamDialog.MIN_ENERGY_DIFF:
                    selectedWavelet = arrOfWaveletObjects[indexOfMinEnergy];
                    break;
                case WaveletSmootherParamDialog.MIN_EUCLIDEAN_DISTANCE:
                    selectedWavelet = arrOfWaveletObjects[indexOfEuclideanSmoothness];
                    break;

                default:
                    selectedWavelet = arrOfWaveletObjects[0];
                    break;
            }

            Transform fwtFast = new Transform(new AncientEgyptianDecomposition(new FastWaveletTransform(selectedWavelet)));
            double[] transformedSignal = fwtFast.forward(y);
            double[] denoisedSignal = compressor.compress(transformedSignal);

            /**
             *
             * Two other methods to compress signal
             */

            // double[] denoisedSignal=SmoothUtils.compressSoftMethod(transformedSignal,threshold);
            // double[] denoisedSignal=SmoothUtils.compressImprovedMethod(transformedSignal,threshold);

            double[] finalSignal = fwtFast.reverse(denoisedSignal);

            SmoothUtils.correctNegativeValues(y,finalSignal);

            return Pair.of(finalSignal, selectedWavelet.getName());


        }
    }
    public static int getIndexOfWaveletByName(String waveletName) {
        int index = 0;
        Wavelet[] listOfWavelets = WaveletBuilder.create2arr();
        for (int k = 0; k < listOfWavelets.length; k++) {
            if (listOfWavelets[k].getName().equals(waveletName)) {
                index = k;
            }
        }
        return index;
    }
    public static Wavelet getWaveletByIndex(int waveletIndex) {
        Wavelet[] listOfWavelets = WaveletBuilder.create2arr();
        return listOfWavelets[waveletIndex];
    }
    public static double getBestGaussianNorm(double[] bestFit, double xMin, double xMax, int length, double[] y) {
        int steps = 30;
        double initialNorm = bestFit[0];
        double[] initialGaussianSignal = SmoothUtils.buildSymmetricGaussian(bestFit, xMin, xMax, length);
        double bestCorrelation = SmoothUtils.computeEuclideanDistance(y, initialGaussianSignal);
        double optimalNorm = initialNorm;
        double deltaNorm = 0.2 * initialNorm;
        for (int k = 0; k < steps; k++) {
            double testedNorm = initialNorm - deltaNorm + deltaNorm * k * 2 / steps;
            bestFit[0] = testedNorm;
            double[] gaussianCandidate = SmoothUtils.buildSymmetricGaussian(bestFit, xMin, xMax, length);
            double correlation = SmoothUtils.computeEuclideanDistance(gaussianCandidate, y);
            if (correlation < bestCorrelation) {
                bestCorrelation = correlation;
                optimalNorm = testedNorm;

            }
        }

        return optimalNorm;
    }
    public static String displayTime(long displayedTime) {

        String timeAsAString = "";
        long seconds = (long) (displayedTime / 1E9);
        long remaining = (long) (displayedTime - 1E9 * seconds);
        long milliSeconds = (long) (remaining / 1E6);
        long remaining2 = (long) (remaining - 1E6 * milliSeconds);
        long microSeconds = (long) (remaining2 / 1E3);
        long nanoseconds = remaining2 - microSeconds * 1000;

        if (seconds != 0) {
            timeAsAString = seconds + " s ";

        }
        if (milliSeconds != 0) {
            timeAsAString = timeAsAString + milliSeconds + " ms ";

        }
        if (microSeconds != 0) {
            timeAsAString = timeAsAString + microSeconds + " \u00B5s ";

        }
        if (nanoseconds != 0) {
            timeAsAString = timeAsAString + nanoseconds + " ns";

        }

        return timeAsAString;
    }


    public static double[] buildPolynomialFromCoefficients(double[] coefficients, double minX, double maxX, int nbrPoints) {
        double[] polynomialSignal=new double[nbrPoints];
        PolynomialFunction polynom=new PolynomialFunction(coefficients);

        for (int k = 0; k < nbrPoints; k++) {
            double x = ((maxX - minX) / nbrPoints) * k;
            polynomialSignal[k]=polynom.value(minX+x);
        }
        return polynomialSignal;


    }
    public static double estimateGlobalDeviation(double[] signal, int windowSize){

        int nbrOfSamples=signal.length/windowSize;
        int remainingSample=signal.length%windowSize;

        StandardDeviation standardDeviation=new StandardDeviation();
        double globalStandardDeviation=0;
        if (nbrOfSamples!=0) {
            for (int i = 0; i < nbrOfSamples; i++) {
                int start = i * windowSize;
                int end = start + windowSize;
                double[] truncatedSignal = SmoothUtils.getTruncatedSignal(signal, start, end);

                double localDeviation = standardDeviation.evaluate(truncatedSignal);

                globalStandardDeviation = globalStandardDeviation + localDeviation;
            }

            if (remainingSample!=0){
            int startRemaining = (nbrOfSamples) * windowSize;
            int endRemaining = startRemaining + remainingSample;

            double[] lastRemainingSignal = getTruncatedSignal(signal, startRemaining, endRemaining);

            double meanSlope=lastRemainingSignal[remainingSample-1]-lastRemainingSignal[0];
            globalStandardDeviation = globalStandardDeviation + standardDeviation.evaluate(lastRemainingSignal);
            }

            logger.info("locally computed standard deviation: "+globalStandardDeviation/nbrOfSamples+" with a window size of: "+windowSize);
            logger.info("length of signal: "+signal.length);
            return (globalStandardDeviation/nbrOfSamples)/signal.length;

        }
        else {
            logger.info("computed standard deviation on 1 sample: "+standardDeviation.evaluate(signal));
            return standardDeviation.evaluate(signal)/signal.length;


        }


    }
    // will run over different sizes of window
    public static double[] multiScaleStandardDeviationAnalysis(double[] signal,int nbrOfTests){
        double[] results=new double[nbrOfTests];
        for (int k=0;k<nbrOfTests;k++){
            results[k]=estimateGlobalDeviation(signal,5+k);
        }
        return results;


    }
    public static double[] getTruncatedSignal(double[]signal,int start,int end){
        double[] truncatedSignal=new double[end-start];
        if (end - start >= 0) System.arraycopy(signal, start, truncatedSignal, 0, end - start);
        return truncatedSignal;

    }


    public static double computeLengthOfSignal(double[]x,double[]y){
        double length=0;
        for (int k=0;k<x.length-1;k++){
            length=length+Math.sqrt((x[k+1]-x[k])*(x[k+1]-x[k])+(y[k+1]-y[k])*(y[k+1]-y[k]));
        }
        return length;
    }

    // method that might be not adapted , used in geography to estimate roughness of landscapes 🤔
    public static Pair<Double, Double> computeStandardDeviationOfSlopes(double[]x, double[]y){
        StandardDeviation standardDeviation=new StandardDeviation();

        double[] slopeAngles=getSlopeAngles(x,y);
        double stdSlopes=standardDeviation.evaluate(slopeAngles);

        double[] kernel = SmoothUtils.buildKernel(5);
        Convolution con = new Convolution(y, kernel);
        double[] convolvedSignal = con.convolve("same");
        double[] smoothedSlopeAngles=getSlopeAngles(x,convolvedSignal);
        double stdSmoothedSlopes=standardDeviation.evaluate(smoothedSlopeAngles);

        return Pair.of(stdSmoothedSlopes,stdSlopes);


    }
    public static double calculateArea(double[] x,double[] y){

        double integral1=0;
        double integral2=0;

        for (int k=0;k<x.length-1;k++){
            integral1=integral1+(x[k+1]-x[k])*y[k];
            integral2=integral2+(x[k+1]-x[k])*y[k+1];


        }
        return (integral2+integral1)/2;
    }

}
