
package fr.proline.studio.rsmexplorer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.rsmexplorer.gui.MSDiagTable_GenericTable.MSdiagTable_GenericTableModel;
import fr.proline.studio.utils.IconManager;

/**
 * Panel used to display MSDiag content
 *
 * @author AW
 */
public class RsetMSDiagPanel extends HourglassPanel implements DataBoxPanelInterface {

	protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
	private static final long serialVersionUID = 1L;
	private AbstractDataBox m_dataBox;
	public JTabbedPane m_tabbedPane = null;
	public String m_displayType = "default"; // force display to be default or
												// Pie chart or table etc.
	private String m_lastUsedDataContent; // stores last used data in order to
											// be reused when changing display
											// mode
	private JXTable m_table; // table model for exporting table
	private ExportButton m_exportButton;
	public FlipButton m_flipModeButton;

	/**
	 * Creates new form RsetMSDiagPanel
	 */
	public RsetMSDiagPanel(String message) {

		setLayout(new BorderLayout());

		m_tabbedPane = createInternalPanel();

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new java.awt.Insets(5, 5, 5, 5);

		// JToolBar toolbar = initToolbar();
		add(m_tabbedPane, BorderLayout.CENTER);
		// add(toolbar, BorderLayout.WEST);

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 3;

	}

	public final JToolBar initToolbar() {

		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
		toolbar.setFloatable(false);
		// m_picWrapper = new ExportPictureWrapper();
		// m_picWrapper.setFile(m_svgFile);

		m_flipModeButton = new FlipButton("flip button text", this);
		toolbar.add(m_flipModeButton);
		// m_exportButton = new ExportButton((ProgressInterface) null, "Peptide
		// Match", m_table);
		// toolbar.add(m_exportButton);

		return toolbar;

	}

	public final JTabbedPane createInternalPanel() {

		JTabbedPane jtabbedPane = new JTabbedPane();

		JPanel internalPanel = new JPanel();

		internalPanel.setLayout(new GridLayout(0, 1));

		internalPanel.setBackground(Color.white);

		return jtabbedPane; // internalPanel;
	}

	public void setData(String jsonMessageHashMapJson) {

		launchMSDiag(jsonMessageHashMapJson);
		m_lastUsedDataContent = jsonMessageHashMapJson;

	}

	public void setDisplayType(String type) {
		m_displayType = type;
	}

	private void launchMSDiag(String messageHashMapJson) {

		// data is encoded in JSON string, subformed of other json strings!!!
		ImageIcon icon = IconManager.getIcon(IconManager.IconType.CHART_PIE);

		final String SERIES_NAME = "MSDiag data";
		if (messageHashMapJson != null) {

			if (messageHashMapJson.length() == 0) {
				// nothing to process
			} else {
				if (messageHashMapJson.startsWith("{")) { // JSON data is there
					Gson gson = new Gson();
					HashMap<String, String> msOutputHashMap = new HashMap<>();
					msOutputHashMap = gson.fromJson(messageHashMapJson, msOutputHashMap.getClass());

					if (msOutputHashMap != null) {
						// go through all msOutputs
						Iterator<String> msOutputHashMapIterator = msOutputHashMap.keySet().iterator();
						java.util.ArrayList<MSDiagOutput_AW> msdiags = new java.util.ArrayList<MSDiagOutput_AW>();
						while (msOutputHashMapIterator.hasNext()) {
							String msOutputItem = msOutputHashMapIterator.next();
							MSDiagOutput_AW msOutput = gson.fromJson(msOutputHashMap.get(msOutputItem),
									MSDiagOutput_AW.class);
							if (msOutput != null) {
								msdiags.add(msOutput);
							}
						}
						msdiags.sort((m1, m2) -> m1.getOrder().compareTo(m2.getOrder()));
						for (int i = 0; i < msdiags.size(); i++) {
							MSDiagOutput_AW msOutput = msdiags.get(i);
							JScrollPane scrollPane = new JScrollPane();
							if (m_displayType.equals("default")) {

								switch (msOutput.output_type.value) { // could be changed to use enum in MSDiagOutput_AW
								case "chromatogram":
									MSDiag_Chromatogram m_msdiagChromatogram = new MSDiag_Chromatogram(this);
									m_msdiagChromatogram.setData(msOutput);
									// scrollPane = new JScrollPane();
									scrollPane.setViewportView(m_msdiagChromatogram);
									m_tabbedPane.addTab(msOutput.description, icon, scrollPane);
									break;
								case "box":
									MSDiag_BoxChart m_msdiagBoxChart = new MSDiag_BoxChart(this);
									m_msdiagBoxChart.setData(msOutput);
									// scrollPane = new JScrollPane();
									scrollPane.setViewportView(m_msdiagBoxChart);
									m_tabbedPane.addTab(msOutput.description, icon, scrollPane);
									break;
								/*
								 * case "StackedXYAreaChart":
								 * MSDiag_StackedXYAreaChart
								 * m_msdiagBoxStackedXY = new
								 * MSDiag_StackedXYAreaChart();
								 * m_msdiagBoxStackedXY.setData(msOutput);
								 * //scrollPane = new JScrollPane();
								 * scrollPane.setViewportView(
								 * m_msdiagBoxStackedXY);
								 * m_tabbedPane.addTab(msOutput.description,
								 * icon, scrollPane); break;
								 */
								case "categoryplot":
									MSDiag_CategoryPlot m_msdiagCategoryPlot = new MSDiag_CategoryPlot(this);
									m_msdiagCategoryPlot.setData(msOutput);
									// scrollPane = new JScrollPane();
									scrollPane.setViewportView(m_msdiagCategoryPlot);
									m_tabbedPane.addTab(msOutput.description, icon, scrollPane);
									break;

								case "pie":
									MSDiag_PieChart m_msdiagPieChart = new MSDiag_PieChart(this);
									m_msdiagPieChart.setData(msOutput);
									// scrollPane = new JScrollPane();
									scrollPane.setViewportView(m_msdiagPieChart);
									m_tabbedPane.addTab(msOutput.description, icon, scrollPane);
									break;

								default:
									// use table as default ---

								case "table":
									MSDiagTable_GenericTable m_msdiagTable = new MSDiagTable_GenericTable(this);
									m_msdiagTable.setModel(new MSdiagTable_GenericTableModel());
									((MSdiagTable_GenericTableModel) m_msdiagTable.getModel()).setData(msOutput);
									// ---add it to the tabbed pane

									scrollPane.setViewportView(m_msdiagTable);
									// m_tabbedPane.addTab(msOutput.description,icon,
									// scrollPane);
									// m_table = m_msdiagTable;
									// m_exportButton = new
									// ExportButton((ProgressInterface) null,
									// "Peptide Match", m_table);

									JPanel localPanel = new JPanel();
									// JToolBar toolbar = new
									// JToolBar(JToolBar.VERTICAL);
									JToolBar toolbar = initToolbar();
									m_flipModeButton.setEnabled(false);
									// add(m_tabbedpane, borderlayout.center);
									// add(toolbar, BorderLayout.WEST);
									toolbar.setFloatable(false);
									m_table = (JXTable) m_msdiagTable;
									m_exportButton = new ExportButton((ProgressInterface) null, "Peptide Match",
											m_table);
									toolbar.add(m_exportButton);

									localPanel.setLayout(new BorderLayout());
									localPanel.add(toolbar, BorderLayout.WEST);
									localPanel.add(scrollPane, BorderLayout.CENTER);
									m_tabbedPane.addTab(msOutput.description, icon, localPanel);

									break;
								}
							} else {

								MSDiagTable_GenericTable m_msdiagTable = new MSDiagTable_GenericTable(this);
								m_msdiagTable.setModel(new MSdiagTable_GenericTableModel());
								((MSdiagTable_GenericTableModel) m_msdiagTable.getModel()).setData(msOutput);
								// ---add it to the tabbed pane

								scrollPane.setViewportView(m_msdiagTable);
								JPanel localPanel = new JPanel();
								JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
								toolbar.setFloatable(false);
								m_table = (JXTable) m_msdiagTable;
								FlipButton flipModeButton = new FlipButton("flip button text", this);
								toolbar.add(flipModeButton);
								m_exportButton = new ExportButton((ProgressInterface) null, "Peptide Match", m_table);
								toolbar.add(m_exportButton);
								localPanel.setLayout(new BorderLayout());
								localPanel.add(toolbar, BorderLayout.WEST);
								localPanel.add(scrollPane, BorderLayout.CENTER);
								m_tabbedPane.addTab(msOutput.description, icon, localPanel);

								// m_tabbedPane.addTab(msOutput.description,icon,
								// scrollPane);

							}
						}

						this.repaint();

					}
				}
			}
		}

	}

	@Override
	public void setDataBox(AbstractDataBox dataBox) {
		m_dataBox = dataBox;
	}

	@Override
	public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
		return m_dataBox.getRemoveAction(splittedPanel);
	}

	@Override
	public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
		return m_dataBox.getAddAction(splittedPanel);
	}

	public static byte[] floatsToBytes(float[] floats) {

		// Convert float to a byte buffer
		ByteBuffer byteBuf = ByteBuffer.allocate(4 * floats.length).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < floats.length; i++) {
			byteBuf.putFloat(floats[i]);
		}
		// Convert byte buffer into a byte array
		return byteBuf.array();
	}

	@Override
	public AbstractDataBox getDataBox() {
		return m_dataBox;
	}

	@Override
	public void addSingleValue(Object v) {
		// should not be called
	}

	@Override
	public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
		return m_dataBox.getSaveAction(splittedPanel);
	}

	public void displayData() { // re display data with new (or same) display
								// parameters
		launchMSDiag(m_lastUsedDataContent);
	}

}
