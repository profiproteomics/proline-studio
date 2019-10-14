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
package fr.proline.mzscope.map.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import fr.proline.mzscope.map.LcMsMapChunk;
import fr.proline.mzscope.map.LcMsViewport;

/**
 * @author JeT
 *
 */
public class JLcMsMapPanel extends JPanel {

    private LcMsMapChunk chunk = null;
    private LcMsViewport viewport = null;
    private LcMsViewerUI ui = null;
    private static final int CELLSIZE = 10;
    private static final Color CELLCOLOR1 = new Color(250, 250, 250);
    private static final Color CELLCOLOR2 = new Color(230, 230, 240);

    /**
     * @param ui
     */
    public JLcMsMapPanel(LcMsViewerUI ui) {
	super();
	this.ui = ui;
    }

    /**
     * @return the ui
     */
    public LcMsViewerUI getViewerUI() {
	return this.ui;
    }

    /**
     * @return the chunk
     */
    public LcMsMapChunk getChunk() {
	return this.chunk;
    }

    /**
     * @param chunk
     *            the chunk to set
     */
    public void setChunk(LcMsMapChunk chunk) {
	this.chunk = chunk;
    }

    /**
     * @return the viewport
     */
    public LcMsViewport getViewport() {
	return this.viewport;
    }

    /**
     * @param viewport
     *            the viewport to set
     */
    public void setViewport(LcMsViewport viewport) {
	this.viewport = this.getViewerUI().clampViewport(viewport);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	super.paintComponent(g);
	for (int y = 0; y < this.getHeight(); y += CELLSIZE) {
	    for (int x = 0; x < this.getWidth(); x += CELLSIZE) {
		g.setColor(((((x / CELLSIZE) + (y / CELLSIZE)) % 2) == 0) ? CELLCOLOR1 : CELLCOLOR2);
		g2.fillRect(x, y, CELLSIZE, CELLSIZE);
	    }
	}

	if (this.chunk == null) {
	    g2.drawString("no map to display", 10, 10);
	    return;
	}

	if (this.viewport == null) {
	    g2.drawString("no viewport set", 10, 10);
	    return;
	}

	BufferedImage img = this.chunk.getLastImage();
	LcMsViewport chunkViewport = this.getChunk().getViewport();
	LcMsViewport displayViewport = this.getViewport();

	Point2D pMin = displayViewport.value2pixel(chunkViewport.minMz, chunkViewport.minRt, this.getWidth(),
		this.getHeight());

	double sx = (chunkViewport.maxRt - chunkViewport.minRt)
		/ (displayViewport.maxRt - displayViewport.minRt);
	double sy = (chunkViewport.maxMz - chunkViewport.minMz)
		/ (displayViewport.maxMz - displayViewport.minMz);
	AffineTransform transform = new AffineTransform();
	transform.translate(pMin.getX(), pMin.getY());
	transform.scale(sx, sy);
	g2.drawImage(img, transform, null);
	if (this.getViewerUI().getCurrentManipulator() != null) {
	    this.getViewerUI().getCurrentManipulator().paintComponent(g2);
	}
    }

}
