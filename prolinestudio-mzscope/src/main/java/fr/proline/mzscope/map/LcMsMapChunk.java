/* 
 * Copyright (C) 2019 VD225637
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
package fr.proline.mzscope.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import fr.profi.mzdb.MzDbReaderHelper;
import fr.profi.mzdb.model.SpectrumSlice;
import fr.profi.mzdb.util.concurrent.CallableCallback;
import fr.profi.mzdb.util.concurrent.Callback;
import fr.proline.mzscope.map.color.IntensityPainter;
import fr.proline.mzscope.math.Function1D;

/**
 * @author JeT
 *
 */
public class LcMsMapChunk {

    private static class DoubleImageData {
	// public List<Pair<Integer, Integer>> data = new ArrayList<Pair<Integer, Integer>>();
	public double intensitySum = 0.;
    }

    private Date creationTime = null;
    private LcMsMap map = null;
    private SpectrumSlice[] slices = null; // all slices containing data
    private BufferedImage image = null;
    private LcMsViewport viewport = null;
    private byte[] pixels = null; // pixel image
    private DoubleImageData values[] = null; // double image
    private double maxValue = 0.;
    private int nbData = 0; // sum of all data slices

    // final ThreadFactory threadFactory = new
    // ThreadFactoryBuilder().setNameFormat("Orders-%d").setDaemon(true)
    // .build();
    // final ExecutorService executorService = Executors.newFixedThreadPool(10, this.threadFactory);

    /**
     * Constructor
     *
     * @param map
     *            map parent LC/MS map
     * @param viewport
     *            chunk viewport
     */
    public LcMsMapChunk(LcMsMap map, LcMsViewport viewport) {
	super();
	this.creationTime = new Date();
	this.setMap(map);
	this.setViewport(viewport);
    }

    /**
     * @return the creationTime
     */
    public Date getCreationTime() {
	return this.creationTime;
    }

    /**
     * @return the map
     */
    public LcMsMap getMap() {
	return this.map;
    }

    /**
     * @param map
     *            the parent map to set
     */
    public final void setMap(LcMsMap map) {
	this.map = map;
    }

    /**
     * @return the viewport
     */
    public LcMsViewport getViewport() {
	return this.viewport;
    }

    /**
     * @param viewport
     *            the viewport used to compute chunk
     */
    public final void setViewport(LcMsViewport viewport) {
	this.viewport = new LcMsViewport(viewport);
    }

    /**
     * get a process computing Slices using the current Viewport
     *
     * @return
     */
    public Callable<SpectrumSlice[]> getSlicesProcess(Callback<SpectrumSlice[]> callbackWhenDone) {
	return MzDbReaderHelper.getSpectrumSlicesInRanges(LcMsMapChunk.this.viewport.minMz,
		LcMsMapChunk.this.viewport.maxMz, (float) LcMsMapChunk.this.viewport.minRt,
		(float) LcMsMapChunk.this.viewport.maxRt, this.getMap().getFile(), this.getMap().getCache(),
		callbackWhenDone);
    }

    /**
     * Synchronous call. It calls the asynchronous task and wait for completion
     *
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public SpectrumSlice[] getSlices() {
	try {
	    return Executors.newSingleThreadExecutor().submit(LcMsMapChunk.this.getSlicesProcess(null)).get();
	} catch (InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * get a process computing an Image using current viewport
     *
     * @return
     */
    public Callable<LcMsMapChunk> getImageProcess(final int imageWidth, final int imageHeight,
	    final Function1D intensityFactor, Callback<LcMsMapChunk> callbackWhenDone,
	    final IntensityPainter painter) {

	Callable<LcMsMapChunk> generateImage = new Callable<LcMsMapChunk>() {

	    @Override
	    public LcMsMapChunk call() throws Exception {
		LcMsMapChunk.this.doSQLiteRequest();
		LcMsMapChunk.this.generateDoubleImage(imageWidth, imageHeight);
		LcMsMapChunk.this.generateImageFromDouble(imageWidth, imageHeight, intensityFactor, painter);

		return LcMsMapChunk.this;
	    }
	};

	return new CallableCallback<LcMsMapChunk>(generateImage, callbackWhenDone);

    }

    /**
     * get a process computing an Image using current viewport
     *
     * @return
     */
    public Callable<LcMsMapChunk> updateImageColorProcess(final int imageWidth, final int imageHeight,
	    final Function1D intensityFactor, Callback<LcMsMapChunk> callbackWhenDone,
	    final IntensityPainter painter) {

	Callable<LcMsMapChunk> generateImage = new Callable<LcMsMapChunk>() {

	    @Override
	    public LcMsMapChunk call() throws Exception {
		LcMsMapChunk.this.generateImageFromDouble(imageWidth, imageHeight, intensityFactor, painter);

		return LcMsMapChunk.this;
	    }
	};

	return new CallableCallback<LcMsMapChunk>(generateImage, callbackWhenDone);

    }

    // /**
    // * Synchronous call. It calls the asynchronous task and wait for completion
    // *
    // * @return
    // */
    // public BufferedImage getImage(final int imageWidth, final int imageHeight, final double
    // intensityFactor) {
    // try {
    // return Executors.newSingleThreadExecutor()
    // .submit(LcMsMapChunk.this.getImageProcess(imageWidth, imageHeight, intensityFactor, null))
    // .get().image;
    // } catch (InterruptedException | ExecutionException e) {
    // e.printStackTrace();
    // return null;
    // }
    // }

    /**
     * this method just returns the content of stored data. Take care that this data is computed
     * asynchronously, it's result may be invalid in some cases
     *
     * @return
     */
    public BufferedImage getLastImage() {
	return this.image;
    }

    /**
     * this method just returns the content of stored data. Take care that this data is computed
     * asynchronously, it's result may be invalid in some cases
     *
     * @return
     */
    public SpectrumSlice[] getLastSlices() {
	return this.slices;
    }

    /**
     * add the given intensity in the 'values' table. Pixel position is computed using mz and rt values. set
     * the maxValue
     *
     * @param mz
     * @param rt
     * @param sliceIndex
     */
    private void addIntensity(double mz, double rt, int sliceIndex, int dataIndex, int imageWidth,
	    int imageHeight) {
	Point2D p = LcMsMapChunk.this.viewport.value2pixel(mz, rt, imageWidth, imageHeight);
	int x = (int) p.getX();
	int y = (int) p.getY();
	if ((x >= 0) && (x < imageWidth) && (y >= 0) && (y < imageHeight)) {
	    int l = x + (y * imageWidth);
	    double intensity = LcMsMapChunk.this.slices[sliceIndex].getData().getIntensityList()[dataIndex];
	    if (LcMsMapChunk.this.values[l].intensitySum <= 0) {
		LcMsMapChunk.this.nbData++;
	    }
	    LcMsMapChunk.this.values[l].intensitySum += intensity;
	    LcMsMapChunk.this.maxValue = Math.max(LcMsMapChunk.this.maxValue,
		    LcMsMapChunk.this.values[l].intensitySum);
	    // LcMsMapChunk.this.values[l].data.add(new Pair<Integer, Integer>(sliceIndex,
	    // dataIndex));
	} else {
	    // System.out.println("pixel " + x + "x" + y + " is out of image mz
	    // = " + mz + " rt = " + rt);
	}

    }

    private void generateImageFromDouble(final int imageWidth, final int imageHeight,
	    final Function1D intensityFactor, final IntensityPainter painter) {
	int squareSize = 1;
	int pixelCount = imageWidth * imageHeight;
	double imageFillRate = (double) LcMsMapChunk.this.nbData / (double) pixelCount;
	if (imageFillRate < (1 / 10.)) {
	    squareSize = 3;
	}
	if (imageFillRate < (1 / 100.)) {
	    squareSize = 5;
	}
	if (imageFillRate < (1 / 500.)) {
	    squareSize = 7;
	}
	if (imageFillRate < (1 / 1000.)) {
	    squareSize = 9;
	}

	// Convert Double image to pixel image
	for (int y = 0; y < imageHeight; y++) {
	    for (int x = 0; x < imageWidth; x++) {
		int l = (x + (y * imageWidth));
		double intensity = intensityFactor
			.eval(LcMsMapChunk.this.values[l].intensitySum / LcMsMapChunk.this.maxValue);
		if (intensity <= 0) {
		    continue;
		}
		Color c = painter.getColor(intensity);
		for (int dy = Math.max(0, y - (squareSize / 2)); dy <= Math.min(imageHeight - 1,
			(y + (squareSize / 2))); dy++) {
		    for (int dx = Math.max(0, x - (squareSize / 2)); dx <= Math.min(imageWidth - 1,
			    (x + (squareSize / 2))); dx++) {
			if ((dx >= 0) && (dx < imageWidth) && (dy >= 0) && (dy < imageHeight)) {
			    l = (dx + (dy * imageWidth)) * 4;
			    LcMsMapChunk.this.pixels[l] = (byte) 255;
			    LcMsMapChunk.this.pixels[l + 1] = (byte) c.getBlue();
			    LcMsMapChunk.this.pixels[l + 2] = (byte) c.getGreen();
			    LcMsMapChunk.this.pixels[l + 3] = (byte) c.getRed();
			}
		    }
		}
	    }
	}
    }

    private void doSQLiteRequest() {
	if (LcMsMapChunk.this.map == null) {
	    throw new IllegalStateException("map cannot be null");
	}

	try {
	    // create, launch and wait a slices process
	    LcMsMapChunk.this.slices = Executors.newSingleThreadExecutor()
		    .submit(LcMsMapChunk.this.getSlicesProcess(null)).get();
	} catch (InterruptedException | ExecutionException e) {
	    throw new IllegalStateException("exception getting slices", e);
	}
    }

    private void generateDoubleImage(final int imageWidth, final int imageHeight) {
	if (LcMsMapChunk.this.slices == null) {
	    throw new IllegalStateException("slices");
	}

	LcMsMapChunk.this.image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
	LcMsMapChunk.this.values = new DoubleImageData[imageWidth * imageHeight];
	LcMsMapChunk.this.pixels = ((DataBufferByte) LcMsMapChunk.this.image.getRaster().getDataBuffer())
		.getData();

	// initialize double and pixel images
	Graphics2D graphics = LcMsMapChunk.this.image.createGraphics();
	graphics.setPaint(new Color(0, 0, 0));
	graphics.fillRect(0, 0, imageWidth, imageHeight);
	for (int i = 0; i < (imageWidth * imageHeight); i++) {
	    LcMsMapChunk.this.values[i] = new DoubleImageData();
	}
	LcMsMapChunk.this.maxValue = 0.;
	LcMsMapChunk.this.nbData = 0;

	// ScanSlice[] slices = mzDb.getMsScanSlices(410, 600, 1000, 1500);

	for (int sliceIndex = 0; sliceIndex < LcMsMapChunk.this.slices.length; sliceIndex++) {
		SpectrumSlice slice = LcMsMapChunk.this.slices[sliceIndex];
	    double mzs[] = slice.getData().getMzList();
	    float intensities[] = slice.getData().getIntensityList();
	    float rt = slice.getHeader().getTime();
	    if ((rt >= LcMsMapChunk.this.viewport.minRt) && (rt <= LcMsMapChunk.this.viewport.maxRt)) {
		for (int dataIndex = 0; dataIndex < mzs.length; dataIndex++) {
		    double mz = mzs[dataIndex];
		    if ((mz >= LcMsMapChunk.this.viewport.minMz)
			    && (mz <= LcMsMapChunk.this.viewport.maxMz)) {
			this.addIntensity(mz, rt, sliceIndex, dataIndex, imageWidth, imageHeight);
		    } else {
			System.out.println("mz = " + mz + " is out of mz range "
				+ LcMsMapChunk.this.viewport.minMz + "-" + LcMsMapChunk.this.viewport.maxMz);
		    }
		}
	    } else {
		System.out.println("rt = " + rt + " is out of rt range " + LcMsMapChunk.this.viewport.minRt
			+ "-" + LcMsMapChunk.this.viewport.maxRt);
	    }
	}
    }

}
