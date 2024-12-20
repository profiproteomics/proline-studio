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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.*;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.rsmexplorer.tree.xic.QuantExperimentalDesignTree;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * experimental design panel and quanti params
 *
 * @author MB243701
 */
public class ExperimentalDesignPanel extends HourglassPanel implements DataBoxPanelInterface {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private AbstractDataBox m_dataBox;

    private JPanel m_expDesignPanel;
    private QuantExperimentalDesignTree m_expDesignTree;
    private JTabbedPane m_tabbedPane;
    private JPanel m_confPanel;
    private JPanel m_lowlevelConfPanel;

    private JPanel m_refinedPanel;

    private DDataset m_dataset;
    private boolean m_displayPostProcessing = false;
    private boolean m_displayLowLevel = false;
    private boolean m_displayQuantParam = true;

    private final DDatasetType.QuantitationMethodInfo m_quantMethodInfo;

    private static String TAB_POST_PROCESSING_TITLE = "Compute Post Processing";
    private static String TAB_LOW_LEVEL_TITLE = "Low Level";

    public ExperimentalDesignPanel(DDatasetType.QuantitationMethodInfo quantMethodInfo) {
        super();
        m_quantMethodInfo = quantMethodInfo;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel expDesignPanel = createExperimentalDesignPanel();
        this.add(expDesignPanel, BorderLayout.CENTER);
    }

    private JPanel createExperimentalDesignPanel() {
        JPanel expDesignPanel = new JPanel();
        expDesignPanel.setBounds(0, 0, 500, 400);
        expDesignPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        expDesignPanel.add(toolbar, BorderLayout.WEST);
        expDesignPanel.add(internalPanel, BorderLayout.CENTER);
        return expDesignPanel;
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        ExportButton exportButton = new ExportButton("Exp. Design", m_expDesignPanel);
        toolbar.add(exportButton);
        return toolbar;
    }

    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_tabbedPane = new JTabbedPane();

        // create objects
        JScrollPane scrollPaneExpDesign = new JScrollPane();
        m_expDesignPanel = new JPanel();
        m_expDesignPanel.setLayout(new BorderLayout());
        m_expDesignTree = new QuantExperimentalDesignTree(QuantitationTree.getCurrentTree().copyCurrentNodeForSelection(), false, false);
        m_expDesignPanel.add(m_expDesignTree, BorderLayout.CENTER);
        scrollPaneExpDesign.setViewportView(m_expDesignPanel);

        m_confPanel = new JPanel();
        m_confPanel.setLayout(new BorderLayout());

        m_refinedPanel = new JPanel();
        m_refinedPanel.setLayout(new BorderLayout());

        m_lowlevelConfPanel = new JPanel();
        m_lowlevelConfPanel.setLayout(new BorderLayout());

        m_tabbedPane.add("Exp.Design", scrollPaneExpDesign);
        m_tabbedPane.add("Exp. Parameters", m_confPanel);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_tabbedPane, c);
        return internalPanel;
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
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    public void setData(Long taskId, DDataset dataset, boolean finished) {
        m_dataset = dataset;
        updateData();
    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        updateData();
    }

    private void updateData() {
        QuantExperimentalDesignTree.displayExperimentalDesign(m_dataset, (AbstractNode) m_expDesignTree.getModel().getRoot(), m_expDesignTree, true, true);

        try {
            if (m_dataset.isQuantitation() && m_dataset.isAggregation()) {
                //if isQuantitation isAggregation, we don't show parameter tab                
                m_confPanel.setVisible(false);
                m_displayQuantParam = false;
                m_tabbedPane.remove(m_confPanel);
            } else {
                if (m_dataset.getQuantProcessingConfig() != null) {
                    switch (m_quantMethodInfo) {
                        case FEATURES_EXTRACTION -> {
                            Map<String,Object>  quantCfg = m_dataset.getQuantProcessingConfigAsMap();
                            String cfgVersion = quantCfg.containsKey("config_version") ? quantCfg.get("config_version").toString() : "1.0";
                            LabelFreeMSParamsCompletePanel xicParamPanel = new LabelFreeMSParamsCompletePanel(true, false, cfgVersion);
                            m_confPanel.removeAll();
                            xicParamPanel.resetScrollbar();
                            m_confPanel.add(xicParamPanel, BorderLayout.CENTER);
                            xicParamPanel.setQuantParams(quantCfg);
                        }
                        case ISOBARIC_TAGGING -> {
                            Map<String,Object> tmtParams = m_dataset.getQuantProcessingConfigAsMap();
                            if(tmtParams.containsKey("label_free_quant_config")) {
                                IsobaricMethodParamsPanel tmtParamPanel = new IsobaricMethodParamsPanel(m_dataset.getQuantitationMethod(), true);
                                m_confPanel.removeAll();
                                m_confPanel.add(tmtParamPanel, BorderLayout.NORTH);
                                tmtParamPanel.setQuantParams(tmtParams);

                                Map<String,Object>  quantCfg = (Map<String,Object>) tmtParams.get("label_free_quant_config");
                                String cfgVersion = quantCfg.containsKey("config_version") ? quantCfg.get("config_version").toString() : "1.0";
                                LabelFreeMSParamsCompletePanel xicParamPanel = new LabelFreeMSParamsCompletePanel(true, false, cfgVersion);
                                xicParamPanel.resetScrollbar();
                                m_confPanel.add(xicParamPanel, BorderLayout.CENTER);
                                xicParamPanel.setQuantParams(quantCfg);

                            } else {
                                IsobaricMethodParamsPanel tmtParamPanel = new IsobaricMethodParamsPanel(m_dataset.getQuantitationMethod(), true);
                                m_confPanel.removeAll();
                                m_confPanel.add(tmtParamPanel, BorderLayout.CENTER);
                                tmtParamPanel.setQuantParams(tmtParams);
                            }

                        }
                        default -> {
                            m_confPanel.removeAll();
                            m_confPanel.add(new JLabel("no configuration available"), BorderLayout.CENTER);
                        }
                    }
                } else {
                    m_confPanel.removeAll();
                    m_confPanel.add(new JLabel("no configuration available"), BorderLayout.CENTER);
                }
                if(!m_displayQuantParam){
                    m_displayQuantParam = true;
                    m_tabbedPane.add(m_confPanel);
                }
            }

            if (m_dataset.getPostQuantProcessingConfig() != null) {
                m_refinedPanel.removeAll();
                if (!m_displayPostProcessing) {
                    m_tabbedPane.add(TAB_POST_PROCESSING_TITLE, m_refinedPanel);
                    m_displayPostProcessing = true;
                }
                Map<Long, String> ptmName = getPtmSpecificityNameById();
                QuantSimplifiedPostProcessingPanel postProcessingParamPanel = new QuantSimplifiedPostProcessingPanel(true, m_dataset.getQuantitationMethod(), m_quantMethodInfo, ptmName, true);//read only
                m_refinedPanel.add(postProcessingParamPanel, BorderLayout.CENTER);
                postProcessingParamPanel.setRefinedParams(m_dataset.getPostQuantProcessingConfigAsMap());

            } else {
                if (m_displayPostProcessing) {
                    m_tabbedPane.remove(m_refinedPanel);
                    m_displayPostProcessing = false;                    
                }
                m_refinedPanel.removeAll();                
            }

            if (m_dataset.getQuantLowLevelConfig() != null) {
                m_lowlevelConfPanel.removeAll();
                if (!m_displayLowLevel) {
                    m_tabbedPane.add(TAB_LOW_LEVEL_TITLE, m_lowlevelConfPanel);
                    m_displayLowLevel = true;
                }

                JScrollPane sPane = new JScrollPane();
                sPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                JTextArea area = new JTextArea(100,20);

                Map<String, Object> llMap = m_dataset.getQuantLowLevelConfigAsMap();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String formattedString = gson.toJson(llMap);
                area.setText(formattedString);
                area.setLineWrap(true);
                sPane.setViewportView(area);
                m_lowlevelConfPanel.add(sPane, BorderLayout.CENTER);
            } else {
                if (m_displayLowLevel) {
                    m_tabbedPane.remove(m_lowlevelConfPanel);
                    m_displayLowLevel = false;
                }
                m_lowlevelConfPanel.removeAll();
            }

        } catch (Exception ex) {
            m_logger.error("error while settings quanti params " + ex);
        }
        m_tabbedPane.revalidate();
    }

    private Map<Long, String> getPtmSpecificityNameById() {
        final ArrayList<PtmSpecificity> ptms = new ArrayList<>();
        DatabasePTMsTask task = new DatabasePTMsTask(null);
        task.initLoadUsedPTMs(m_dataset.getProject().getId(), m_dataset.getResultSummaryId(), ptms);
        task.fetchData();
        return ptms.stream().collect(Collectors.toMap(PtmSpecificity::getId, PtmSpecificity::toString));
    }
}
