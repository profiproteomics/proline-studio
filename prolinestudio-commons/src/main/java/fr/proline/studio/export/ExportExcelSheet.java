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
package fr.proline.studio.export;


/**
 * represents a sheet in the configuration file for export It contains a unique
 * id, a title, a presentation mode (row or column) and a list of fields
 *
 * @author
 */
public class ExportExcelSheet {
    public String id;
   /**
    * title for the tabbed Panel in custom options, CustomExportDialog
    */
    public String title; 
    private String presentation; // column or row
    public ExportExcelSheetField[] fields;
    public boolean default_displayed;

    public ExportExcelSheet() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * 
     * @return String represente column or row
     */
    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public ExportExcelSheetField[] getFields() {
        return fields;
    }

    public void setFields(ExportExcelSheetField[] fields) {
        this.fields = fields;
    }
    
    /* return true if a field in this sheet contains the given fieldTitle, before the specified index */
    public boolean containsFieldTitle(String fieldTitle, int index){
        for (int f=0; f<index; f++){
            if (this.fields[f].title.equalsIgnoreCase(fieldTitle)){
                return true;
            }
        }
        return false;
    }
    
}
