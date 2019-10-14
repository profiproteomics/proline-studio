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

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.util.concurrent.Callback;
import fr.proline.mzscope.map.LcMsViewerEvent.LcMsViewerEventType;
import fr.proline.mzscope.map.color.IntensityPainter;
import fr.proline.mzscope.math.Function1D;

/**
 * @author JeT
 *
 */
public class LcMsViewer {
    private LcMsMap map = null;
    private LcMsViewport mapViewport = null;
    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(3);

    /**
     * @param map
     */
    public LcMsViewer(LcMsMap map) {
	super();
	this.setMap(map);
    }

    /**
     * @return the map
     */
    public LcMsMap getMap() {
	return this.map;
    }

    /**
     * Reinit viewport to fit the whole LcMs Map
     */
    private void initMapViewport() {
	if (this.map == null) {
	    return;
	}
	MzDbReader mzDb;
	try {
	    mzDb = this.map.createReader();
	    if (mzDb == null) {
		return;
	    }
	    try {
		double runDuration = mzDb.getLastTime();
		int[] mzRange = mzDb.getMzRange(1);
		int minMzRun = mzRange[0];
		int maxMzRun = mzRange[1];
		this.mapViewport = new LcMsViewport(minMzRun, maxMzRun, 0, runDuration);
	    } catch (SQLiteException e) {
		e.printStackTrace();
	    }
	} catch (ClassNotFoundException | FileNotFoundException | SQLiteException e1) {
	    e1.printStackTrace();
	}
    }

    /**
     * @return the viewport
     */
    public LcMsViewport getMapViewport() {
	return this.mapViewport;
    }

    /**
     * @param map
     *            the map to set
     */
    public final void setMap(LcMsMap map) {
	this.map = map;
	this.initMapViewport();
	this.fireLcMsViewerEvent(new LcMsViewerEvent(this, LcMsViewerEventType.MAP_CHANGED));
    }

    /**
     * get a a chunk of Lc MS map
     *
     * @param minMz
     * @param maxMz
     * @param minRt
     * @param maxRt
     */
    public LcMsMapChunk extractChunk(double minMz, double maxMz, double minRt, double maxRt) {
	LcMsMapChunk chunk = this.extractChunk(new LcMsViewport(minMz, maxMz, minRt, maxRt));
	return chunk;
    }

    /**
     * get a a chunk of Lc MS map
     *
     * @param viewport
     */
    public LcMsMapChunk extractChunk(final LcMsViewport viewport) {
	LcMsMapChunk chunk = new LcMsMapChunk(this.getMap(), viewport);
	return chunk;
    }

    /**********************************/
    /** LcMsViewerListener management */
    private Set<LcMsViewerListener> viewerListeners = new HashSet<LcMsViewerListener>();

    public void removeLcMsViewerListener(LcMsViewerListener listener) {
	this.viewerListeners.remove(listener);
    }

    public void addLcMsViewerListener(LcMsViewerListener listener) {
	this.viewerListeners.remove(listener);
    }

    public void fireLcMsViewerEvent(LcMsViewerEvent e) {
	for (LcMsViewerListener listener : this.viewerListeners) {
	    listener.onLcMsViewerEvent(e);
	}
    }

    public void fireLcMsViewerEvent(LcMsViewerEventType type) {
	LcMsViewerEvent e = new LcMsViewerEvent(this, type);
	for (LcMsViewerListener listener : this.viewerListeners) {
	    listener.onLcMsViewerEvent(e);
	}
    }

    /**
     * @param width
     * @param height
     * @param value
     * @param viewport
     * @param lcMsViewerUI
     */
    public void requestNewImage(int width, int height, Function1D intensity, LcMsViewport viewport,
	    Callback<LcMsMapChunk> callback, IntensityPainter painter) {
	LcMsMapChunk chunk = this.extractChunk(viewport);
	// clear all pending task and replace it by this new request
	synchronized (this.threadPool) {
	    Iterator<Runnable> runnableIterator = this.threadPool.getQueue().iterator();
	    while (runnableIterator.hasNext()) {
		this.threadPool.remove(runnableIterator.next());
	    }
	    if (this.threadPool
		    .submit(chunk.getImageProcess(width, height, intensity, callback, painter)) != null) {
		this.fireLcMsViewerEvent(LcMsViewerEventType.IMAGE_PROCESS_ADDED);
	    }
	}
    }

    /**
     * @param width
     * @param height
     * @param value
     * @param viewport
     * @param lcMsViewerUI
     */
    public void requestUpdateImageColor(int width, int height, Function1D intensity, LcMsViewport viewport,
	    Callback<LcMsMapChunk> callback, IntensityPainter painter) {
	LcMsMapChunk chunk = this.extractChunk(viewport);
	for (double v = 0.; v <= 1; v += 0.2) {
	    System.out.println(v + " => " + intensity.eval(v));
	}
	// clear all pending task and replace it by this new request
	synchronized (this.threadPool) {
	    Iterator<Runnable> runnableIterator = this.threadPool.getQueue().iterator();
	    while (runnableIterator.hasNext()) {
		this.threadPool.remove(runnableIterator.next());
	    }
	    if (this.threadPool
		    .submit(chunk.getImageProcess(width, height, intensity, callback, painter)) != null) {
		this.fireLcMsViewerEvent(LcMsViewerEventType.IMAGE_PROCESS_ADDED);
	    }
	}
    }

    /**
     * @return
     */
    public int getRunningTaskCount() {
	return this.threadPool.getActiveCount();
    }

}
