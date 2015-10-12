/**
 *
 */
package fr.proline.mzscope.map.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import fr.proline.mzscope.map.LcMsMapSpectrum;

/**
 * @author JeT
 *
 */
public class LcMsMapSpectrumPanel extends JPanel implements MouseMotionListener {

    private static final long serialVersionUID = 5320119933983533560L; // generated UID
    private LcMsMapSpectrum spectrum = null;
    private int spectrumLeftMargin = 40;
    private int spectrumTopMargin = 20;
    private int spectrumRightMargin = 10;
    private int spectrumBottomMargin = 30;
    private Color axisColor = new Color(0.7f, 0.7f, 0.7f);
    private Color valueColor = new Color(0.3f, 0.3f, 0.5f);
    private Color valueBgColor = new Color(0.95f, 0.95f, 0.6f, 0.25f);
    private Stroke valueStroke = null;
    private int displayValueMargin = 5;
    private Color spectrumColor = Color.red;
    private Color backgroundColor = Color.white;
    private int majorTickSize = 4;
    private int minorTickSize = 2;
    private Double displayIntensity = null;
    private Double displayXValue = null;
    private int displayPositionX = 0;
    private int displayPositionY = 0;

    private static final DecimalFormat df = new DecimalFormat("#.###");

    /**
     * @param lcMsViewerController
     * @param viewport
     */
    public LcMsMapSpectrumPanel(LcMsMapSpectrum spectrum) {
	this.spectrum = spectrum;
	this.valueStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f,
		new float[] { 3.0f }, 0.0f);
	this.addMouseMotionListener(this);
    }

    /**
     * getter of Spectrum
     *
     * @return the existing LcMsMapSpectrum
     */
    private LcMsMapSpectrum getSpectrum() {
	return this.spectrum;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D) g.create();
	try {
	    int width = this.getWidth();
	    int height = this.getHeight();
	    // draw background
	    g2.setColor(this.backgroundColor);
	    g2.fillRect(0, 0, width, height);

	    if (this.getSpectrum() == null) {
		return;
	    }

	    // draw axis
	    Rectangle r = new Rectangle(this.spectrumLeftMargin, this.spectrumTopMargin,
		    width - this.spectrumLeftMargin - this.spectrumRightMargin,
		    height - this.spectrumBottomMargin - this.spectrumTopMargin);
	    g2.setColor(this.axisColor);
	    g2.drawLine(r.x, r.y + r.height, r.x + r.width, r.y + r.height);
	    g2.drawLine(r.x, r.y, r.x, r.y + r.height);
	    g2.setColor(this.spectrumColor);

	    // draw axis values and ticks
	    // g2.setFont(g2.getFont().deriveFont(0.5f));
	    // max intensity value
	    String str = df.format(this.getSpectrum().getMaxIntensity());
	    Rectangle2D strB = g2.getFont().getStringBounds(str, g2.getFontRenderContext());
	    g2.drawString(str, (int) (this.spectrumLeftMargin - strB.getWidth()), this.spectrumTopMargin);
	    // min x Value
	    str = df.format(this.getSpectrum().getMinX());
	    strB = g2.getFont().getStringBounds(str, g2.getFontRenderContext());
	    g2.drawString(str, (int) (this.spectrumLeftMargin - (strB.getWidth() / 2.)),
		    (this.getHeight() - this.spectrumBottomMargin) + this.majorTickSize);
	    // max x Value
	    str = df.format(this.getSpectrum().getMaxX());
	    strB = g2.getFont().getStringBounds(str, g2.getFontRenderContext());
	    g2.drawString(str, (int) (this.getWidth() - this.spectrumRightMargin - (strB.getWidth() / 2.)),
		    (this.getHeight() - this.spectrumBottomMargin) + this.majorTickSize);

	    // draw spectrum
	    if ((this.getSpectrum().getIntensities() != null)
		    && (this.getSpectrum().getIntensities().length > 0)) {
		GeneralPath polyline = new GeneralPath(Path2D.WIND_EVEN_ODD,
			this.getSpectrum().getIntensities().length);
		double intensity = this.spectrum.getIntensity(0);
		double x = r.x;
		double y = this.value2pixelY(intensity);
		polyline.moveTo(x, y);
		for (int index = 0; index < this.spectrum.getIntensities().length; index++) {
		    intensity = this.spectrum.getIntensity(index);
		    x = this.value2pixelX(this.getSpectrum().getMinX()
			    + ((index / (double) this.spectrum.getIntensities().length)
				    * (this.getSpectrum().getMaxX() - this.getSpectrum().getMinX())));
		    y = this.value2pixelY(intensity);
		    polyline.lineTo(x, y);
		}

		g2.draw(polyline);
	    }
	    if ((this.displayXValue != null) && (this.displayIntensity != null)) {
		g2.setColor(this.valueColor);

		g2.setFont(g2.getFont().deriveFont(Font.BOLD, g.getFont().getSize() * 0.8f));
		String str1 = "x=" + df.format(this.displayXValue);
		String str2 = "y=" + df.format(this.displayIntensity);
		Rectangle2D strB1 = g2.getFont().getStringBounds(str1, g2.getFontRenderContext());
		Rectangle2D strB2 = g2.getFont().getStringBounds(str2, g2.getFontRenderContext());
		g2.setColor(this.valueBgColor);
		int tooltipX = this.displayPositionX + 20;
		int tooltipY = this.displayPositionY - 20;
		int tooltipWidth = (int) Math.max(strB1.getWidth(), strB2.getWidth());
		int tooltipHeight = (int) (strB1.getHeight() + strB2.getHeight() + this.displayValueMargin);
		tooltipX = Math.min(tooltipX, this.getWidth() - (tooltipWidth + this.displayValueMargin));
		tooltipY = Math.max(tooltipY, tooltipHeight + (2 * this.displayValueMargin));
		g2.fillRect(tooltipX - this.displayValueMargin,
			(tooltipY - tooltipHeight) - (3 * this.displayValueMargin),
			tooltipWidth + (2 * this.displayValueMargin),
			tooltipHeight + (2 * this.displayValueMargin));
		g2.setColor(this.valueColor);
		g2.drawRect(tooltipX - this.displayValueMargin,
			(tooltipY - tooltipHeight) - (3 * this.displayValueMargin),
			tooltipWidth + (2 * this.displayValueMargin),
			tooltipHeight + (2 * this.displayValueMargin));

		g2.drawString(str1, tooltipX,
			(int) (tooltipY - strB2.getHeight() - strB1.getHeight() - (this.displayValueMargin)));
		g2.drawString(str2, tooltipX, (int) (tooltipY - strB2.getHeight()));

		g2.drawLine(this.displayPositionX - this.minorTickSize,
			this.displayPositionY - this.minorTickSize,
			this.displayPositionX + this.minorTickSize,
			this.displayPositionY + this.minorTickSize);
		g2.setStroke(this.valueStroke);
		g2.drawLine(this.displayPositionX - this.minorTickSize,
			this.displayPositionY + this.minorTickSize,
			this.displayPositionX + this.minorTickSize,
			this.displayPositionY - this.minorTickSize);
		g2.drawLine(this.displayPositionX, this.displayPositionY, this.displayPositionX,
			this.getHeight() - this.spectrumBottomMargin);
	    }
	} finally {
	    g2.dispose();
	}

    }

    private double value2pixelX(double xValue) {
	return this.spectrumLeftMargin
		+ (((this.getWidth() - this.spectrumLeftMargin - this.spectrumRightMargin)
			* (xValue - this.getSpectrum().getMinX()))
			/ (this.getSpectrum().getMaxX() - this.getSpectrum().getMinX()));
    }

    private double value2pixelY(double intensity) {
	return (this.getHeight() - this.spectrumBottomMargin) - ((intensity / this.spectrum.getMaxIntensity())
		* (this.getHeight() - this.spectrumBottomMargin - this.spectrumTopMargin));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e) {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent e) {
	int x = e.getX();
	int y = e.getY();
	this.displayXValue = null;
	this.displayIntensity = null;

	double xValue = (((x - this.spectrumLeftMargin)
		/ (double) (this.getWidth() - this.spectrumLeftMargin - this.spectrumRightMargin))
		* (this.getSpectrum().getMaxX() - this.getSpectrum().getMinX()))
		+ this.getSpectrum().getMinX();

	int index = this.getSpectrum().getIndex(xValue);
	if ((index >= 0) && (index < this.getSpectrum().getIntensities().length)) {
	    xValue = (((index + 0.5) / this.getSpectrum().getIntensities().length)
		    * (this.getSpectrum().getMaxX() - this.getSpectrum().getMinX()))
		    + this.getSpectrum().getMinX();
	    double intensity = this.getSpectrum().getIntensity(index);
	    this.displayXValue = xValue;
	    this.displayIntensity = intensity;
	    this.displayPositionX = (int) this.value2pixelX(xValue);
	    this.displayPositionY = (int) this.value2pixelY(intensity);
	    this.repaint();

	}

    }

}
