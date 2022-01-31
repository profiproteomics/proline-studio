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
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.xic.DataboxMapAlignment;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.IonsRTScatterPlot;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.IonsRTTableModel;
import fr.proline.studio.utils.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

/**
 * Common class for Map (delta time vs time) alignment and moz Map (delta moz vs time) alignement
 */
public abstract  class AbstractMapAlignmentPanel extends HourglassPanel implements DataBoxPanelInterface  {


  protected static final Logger logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
  protected static final String CLOUD_VIEW_BEST_FIT_TEXT = "Zoom to fit RT tolerance range";
  protected static final String CLOUD_VIEW_All_TEXT = "Zoom to fit all ions";
  protected static final String CLOUD_VIEW_NOT_ENABLED_TEXT = "Zoom disabled";
  protected static final int CLOUD_VIEW_ALL = 1;
  protected static final int CLOUD_VIEW_BEST_FIT = 0;//default show mode
  protected static final String CLOUD_LOAD_TEXT = "Load matched peptide ions";
  protected static final String CLOUD_REMOVE_TEXT = "Remove peptide ions plot";
  protected static final String CLOUD_IS_LOADING = "Loading ions ...";
  protected final static NumberFormat format2 = new DecimalFormat("#0.0000");

  final double THRESHOLD = .0001;

  // databox using this Panel
  protected AbstractDataBox m_dataBox;

  // quanti information
  protected QuantChannelInfo m_quantChannelInfo;

  // processed map to display by Index
  protected Map<Integer, ProcessedMap> m_pMapByIndex;

  String m_typeAlignement;//specify what is aligned with time
  //
  protected JLabel m_labelTitle;
  protected JTextField m_srcTimeValueTF;
  protected JTextField m_destValueTF;
  protected JComboBox m_sourceMapsCB;

  //common buttons
  private JToggleButton m_loadIonsBtn;
  protected boolean m_isIonsCloudLoaded;
  protected JToggleButton m_zoomModeBtn;
  int m_zoomMode;
  protected JButton m_showHideCrossAssigned;

  //displayed plot
  protected BasePlotPanel m_alignmentGraphicPanel;
  protected IonsRTScatterPlot m_ionsScatterPlot;
  protected JSplitPane m_splitPane;


  // for alignment iterative mode, sometimes, we should show 2 graphic
  protected BasePlotPanel m_alignmentGraphicPanel_2;
  protected PlotPanel m_plotPanel2;

  protected boolean m_isInitialized = false;


  public AbstractMapAlignmentPanel(AbstractDataBox dataBox, String typeAlignement) {
    this.m_dataBox = dataBox;
    m_typeAlignement = typeAlignement;

    m_isIonsCloudLoaded = false;

    JPanel mainPanel = initComponents();

    this.setLayout(new BorderLayout());
    this.add(mainPanel, BorderLayout.CENTER);
  }

  protected abstract String getTitleLabel();
  protected abstract JPanel createConvertPanel();
  protected abstract List<JButton> getMoreCloudButtons();
  public abstract void setData(QuantChannelInfo quantChannelInfo, List<ExtendedTableModelInterface> compareDataInterfaceList);
  protected abstract Double getCorrespondingData(Double time, Long mapId);
  protected abstract void setDataGraphic();



  protected JPanel initComponents() {
    JPanel mainPanel;
    mainPanel = new JPanel();

    JPanel headerPanel = initToolbarPanel();
    PlotPanel plotPanel1 = new PlotPanel();
    m_alignmentGraphicPanel = plotPanel1.getBasePlotPanel();

    mainPanel.setLayout(new BorderLayout());
    TitledBorder titleB = new TitledBorder(null, "LC-MS Map "+m_typeAlignement+"Alignments", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
    mainPanel.setBorder(titleB);
    mainPanel.add(headerPanel, BorderLayout.PAGE_START);

    m_plotPanel2 = new PlotPanel();
    m_alignmentGraphicPanel_2 = m_plotPanel2.getBasePlotPanel();
    // the second graphic panel has not data in exhaustive mode and in iterative mode, when one selected map is reference map
    m_plotPanel2.setVisible(false);

    m_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, plotPanel1, m_plotPanel2);
    m_splitPane.setResizeWeight(0.5); //half half for each graphic panel
    m_splitPane.setBorder(BorderFactory.createRaisedBevelBorder());

    mainPanel.add(m_alignmentGraphicPanel, BorderLayout.CENTER);
    return mainPanel;
  }

  private JPanel initToolbarPanel() {
    ExportButton exportImageButton = new ExportButton("Graphic", this);
    exportImageButton.setMargin(new Insets(2, 2, 2, 2));
    initLoadIonsButton();
    initZoomButton();

    m_labelTitle = new JLabel(getTitleLabel());

    m_srcTimeValueTF = new JTextField(10);
    m_srcTimeValueTF.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
      }

      @Override
      public void focusLost(FocusEvent e) {
        convertTime();
      }
    });
    m_srcTimeValueTF.addActionListener(e -> convertTime());
    m_srcTimeValueTF.setName("tfSourceTime");
    m_srcTimeValueTF.setToolTipText("Enter retention time (in minutes) ");

    m_sourceMapsCB = new JComboBox();
    m_sourceMapsCB.addActionListener(e -> {
      convertTime();
      setDataGraphic();
    });

    JPanel cloudOptionPane = new JPanel();
    cloudOptionPane.setLayout(new BoxLayout(cloudOptionPane, BoxLayout.LINE_AXIS));

    m_showHideCrossAssigned = new JButton();
    m_showHideCrossAssigned.setIcon(IconManager.getIcon(IconManager.IconType.HIDE_CROSS_ASSIGNED));
    m_showHideCrossAssigned.setActionCommand("HIDE");
    m_showHideCrossAssigned.setMargin(new Insets(2, 2, 2, 2));
    m_showHideCrossAssigned.setToolTipText("Show/Hide cross assigned ions");
    m_showHideCrossAssigned.addActionListener(e -> {
      if (m_ionsScatterPlot != null) {
        if (e.getActionCommand().equals("HIDE")) {
          m_ionsScatterPlot.showCrossAssignedIons(false);
          m_showHideCrossAssigned.setActionCommand("SHOW");
          m_showHideCrossAssigned.setIcon(IconManager.getIcon(IconManager.IconType.SHOW_CROSS_ASSIGNED));
        } else {
          m_ionsScatterPlot.showCrossAssignedIons(true);
          m_showHideCrossAssigned.setActionCommand("HIDE");
          m_showHideCrossAssigned.setIcon(IconManager.getIcon(IconManager.IconType.HIDE_CROSS_ASSIGNED));
        }
        m_alignmentGraphicPanel.repaintUpdateDoubleBuffer();
      }
    });

    JSeparator separator = new JSeparator(JSeparator.VERTICAL);
    cloudOptionPane.add(exportImageButton);
    cloudOptionPane.add(m_loadIonsBtn);
    cloudOptionPane.add(m_zoomModeBtn);
    cloudOptionPane.add(m_showHideCrossAssigned);
    for(JButton bt : getMoreCloudButtons()){
      cloudOptionPane.add(bt);
    }
    cloudOptionPane.add(Box.createRigidArea(new Dimension(10, 0)));
    setEnabledCloudButtons(false);
    cloudOptionPane.add(separator);

    JPanel beginPane = new JPanel(new FlowLayout());
    beginPane.add(cloudOptionPane);
    beginPane.add(createConvertPanel());

    JPanel pane = new JPanel();
    pane.setLayout(new BorderLayout());
    pane.add(beginPane, BorderLayout.LINE_START);
    pane.add(m_labelTitle, BorderLayout.LINE_END);
    pane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
    return pane;
  }

  protected void setEnabledCloudButtons(boolean enable) {
    m_showHideCrossAssigned.setEnabled(enable);
    m_zoomModeBtn.setEnabled(enable);
  }

  private void initLoadIonsButton() {
    m_loadIonsBtn = new JToggleButton();
    m_loadIonsBtn.setIcon(IconManager.getIcon(IconManager.IconType.LOAD_ALIGNMENT_CLOUD));
    m_loadIonsBtn.setMargin(new Insets(2, 2, 2, 2));

    m_loadIonsBtn.setToolTipText(CLOUD_LOAD_TEXT);
    m_loadIonsBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!m_isIonsCloudLoaded) {//action = set cloud
          m_isIonsCloudLoaded = true;
          m_loadIonsBtn.setIcon(IconManager.getIconWithHourGlass(IconManager.IconType.LOAD_ALIGNMENT_CLOUD));
          m_loadIonsBtn.setToolTipText(CLOUD_IS_LOADING);
          ((DataboxMapAlignment) m_dataBox).loadCloud(); //will return in this class and call this.setAlignmentCloud
        } else if (m_dataBox.isLoaded()) {//cloud is been shown, action = remove
          m_isIonsCloudLoaded = false;//this boolean will affect cloud show
          m_loadIonsBtn.setIcon(IconManager.getIcon(IconManager.IconType.LOAD_ALIGNMENT_CLOUD));
          m_loadIonsBtn.setToolTipText(CLOUD_LOAD_TEXT);
          setEnabledCloudButtons(false);
          setDataGraphic();
        }
      }
    });
  }

  protected void  initZoomButton() {
    m_zoomModeBtn = new JToggleButton();
    m_zoomModeBtn.setIcon(IconManager.getIcon(IconManager.IconType.ZOOM_ALL));
    m_zoomModeBtn.setMargin(new Insets(2, 2, 2, 2));
    m_zoomModeBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (m_zoomMode == CLOUD_VIEW_BEST_FIT) {
          m_zoomMode = CLOUD_VIEW_ALL;
          m_zoomModeBtn.setIcon(IconManager.getIcon(IconManager.IconType.ZOOM_FIT));
          m_zoomModeBtn.setToolTipText(CLOUD_VIEW_BEST_FIT_TEXT);
          setDataGraphic();
        } else {
          m_zoomMode = CLOUD_VIEW_BEST_FIT;
          m_zoomModeBtn.setIcon(IconManager.getIcon(IconManager.IconType.ZOOM_ALL));
          m_zoomModeBtn.setToolTipText(CLOUD_VIEW_All_TEXT);
          setDataGraphic();
        }

      }

    });
    m_zoomModeBtn.setToolTipText(CLOUD_VIEW_NOT_ENABLED_TEXT);
  }

  /**
   * show in plotScatter, the cloud, usually be called when user change combo
   * box selection <br>
   * can be called by DataBoxMapAlignment
   */
  public void setAlignmentCloud() {
    this.m_loadIonsBtn.setToolTipText(CLOUD_REMOVE_TEXT);
    this.m_loadIonsBtn.setIcon(IconManager.getIcon(IconManager.IconType.REMOVE_ALIGNMENT_CLOUD));
    this.m_zoomModeBtn.setToolTipText(CLOUD_VIEW_All_TEXT);
    setEnabledCloudButtons(true);
    this.setDataGraphic();
  }

  @Override
  public void setDataBox(AbstractDataBox dataBox) {
    m_dataBox = dataBox;
  }

  @Override
  public AbstractDataBox getDataBox() {
    return m_dataBox;
  }

  @Override
  public void addSingleValue(Object v) {
    // not used for the moment JPM.TODO ?
  }

  @Override
  public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel ) {
    return m_dataBox.getRemoveAction(splittedPanel);
  }

  @Override
  public ActionListener getAddAction(SplittedPanelContainer splittedPanel ) {
    return m_dataBox.getAddAction(splittedPanel);
  }

  @Override
  public ActionListener getSaveAction(SplittedPanelContainer splittedPanel ) {
    return m_dataBox.getSaveAction(splittedPanel);
  }

  void convertTime() {
    try {
      Double time = Double.parseDouble(m_srcTimeValueTF.getText());
      Double calcMoz = getCorrespondingData(time, getSelectedMapId(m_sourceMapsCB));
      if (calcMoz.isNaN()) {
        m_destValueTF.setText("");
      } else {
        m_destValueTF.setText(format2.format(calcMoz));
      }
    } catch (NumberFormatException ex) {
    }
  }


  IonsRTTableModel getCloudData(long mapIdSrc) {
    if (this.m_dataBox.isLoaded()) {
      return ((DataboxMapAlignment) this.m_dataBox).getPeptideCloud(mapIdSrc);
    } else {
      return null;
    }
  }

  protected Long getSelectedMapId(JComboBox cb) {
    if (cb == null) {
      return (long) -1;
    } else {
      int selId = cb.getSelectedIndex();
      if (m_pMapByIndex.containsKey(selId)) {
        return m_pMapByIndex.get(selId).getId();
      } else {
        return (long) -1;//should not happen
      }
    }
  }

}
