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
package com.thierry.filtering;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import com.thierry.filtering.report.HTMLReportBuilder;
import com.thierry.filtering.report.TableReportGenerator;
import com.thierry.filtering.report.TextReportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableSelection implements Transferable, ClipboardOwner {

    public static void installCopyAction(final JTable table) {
        KeyStroke copy = KeyStroke.getKeyStroke(
                KeyEvent.VK_C,
                ActionEvent.CTRL_MASK,
                false);

        Action copyAction = new AbstractAction("Copy") {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("Table selection change");
                TableSelection transfer = new TableSelection(table);
                logger.debug("Clipboard transfertHandler will change after ");
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transfer, transfer);
                logger.debug("Clipboard transfertHandler changed ");
            }
        };
        table.registerKeyboardAction(
                copyAction,
                "Copy",
                copy,
                JComponent.WHEN_FOCUSED);
    }
    private static ArrayList<DataFlavor> htmlFlavors =
            new ArrayList<DataFlavor>();
    private static ArrayList<DataFlavor> textFlavors =
            new ArrayList<DataFlavor>();
    private static ArrayList<DataFlavor> stringFlavors =
            new ArrayList<DataFlavor>();
    private static ArrayList<DataFlavor> supporterFlavors =
            new ArrayList<DataFlavor>();

    static {
        String encoding = "unicode";
        htmlFlavors.add(new DataFlavor("text/html;charset="
                + encoding + ";class=java.lang.String", "HTML Text"));
        htmlFlavors.add(new DataFlavor("text/html;charset="
                + encoding + ";class=java.io.Reader", "HTML Text"));
        htmlFlavors.add(new DataFlavor("text/html;charset="
                + encoding + ";class=java.io.InputStream", "HTML Text"));

        textFlavors.add(new DataFlavor("text/plain;charset="
                + encoding + ";class=java.lang.String", "Plain Text"));
        textFlavors.add(new DataFlavor("text/plain;charset="
                + encoding + ";class=java.io.Reader", "Plain Text"));
        textFlavors.add(new DataFlavor("text/plain;charset="
                + encoding + ";class=java.io.InputStream", "Plain Text"));

        stringFlavors.add(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.String",
                "String Flavor"));
        stringFlavors.add(DataFlavor.stringFlavor);
        supporterFlavors.addAll(htmlFlavors);
        supporterFlavors.addAll(textFlavors);
        supporterFlavors.addAll(stringFlavors);
    }

    private static boolean isHTMLDataFlavor(DataFlavor flavor) {
        return htmlFlavors.contains(flavor);
    }

    private static boolean isTextDataFlavor(DataFlavor flavor) {
        return textFlavors.contains(flavor);
    }

    private static boolean isStringDataFlavor(DataFlavor flavor) {
        return stringFlavors.contains(flavor);
    }

    protected static final Logger logger = LoggerFactory.getLogger("ProlineStudio.Commons");
    private JTable m_table;
    private String m_textReport;
    private String m_htmlReport;
    
    public TableSelection(JTable table) {
        m_table = table;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[]) supporterFlavors.toArray(
                new DataFlavor[supporterFlavors.size()]);
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return supporterFlavors.contains(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        
        //long start = System.currentTimeMillis();
        String transferedData = null;
        if (isHTMLDataFlavor(flavor)) {
            //System.out.println("HTML");
            transferedData = getHTMLStringSelection();
        }
        if (isTextDataFlavor(flavor)) {
            //System.out.println("TEXT");
            transferedData = getTextStringSelection();
        }
        if (isStringDataFlavor(flavor)) {
            //System.out.println("STRING");
            transferedData = getTextStringSelection();
        }
        
        /*long stop = System.currentTimeMillis();
        
        double delta = ((double)(stop-start))/1000.0;
        System.out.println(delta);*/
        
        if (transferedData != null) {
            return getTransferData(flavor, transferedData);
        }
        throw new UnsupportedFlavorException(flavor);

    }

    private String getHTMLStringSelection() {
        logger.debug("HTMLReport requested");
        if (m_htmlReport == null)
            m_htmlReport = new TableReportGenerator(m_table).getSelection(new HTMLReportBuilder());
        return m_htmlReport;
    }

    private Object getTransferData(DataFlavor flavor, String transfered) throws UnsupportedFlavorException {
        if (String.class.equals(flavor.getRepresentationClass())) {
            return transfered;
        } else if (Reader.class.equals(flavor.getRepresentationClass())) {
            return new StringReader(transfered);
        } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
            return new ByteArrayInputStream(transfered.getBytes());
        }
        throw new UnsupportedFlavorException(flavor);
    }

    private String getTextStringSelection() {
        logger.debug("Text Report Requested ... ");
        if (m_textReport == null)
            m_textReport = new TableReportGenerator(m_table).getSelection(new TextReportBuilder());
        return m_textReport;
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // TODO Auto-generated method stub
    }
}
