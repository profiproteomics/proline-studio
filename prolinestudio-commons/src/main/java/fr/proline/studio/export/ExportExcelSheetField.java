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
 * represents a field with an id and a title
 *
 * @author
 */
public class ExportExcelSheetField {

    public String id;
    public String title;
    public boolean default_displayed; // only used in default export.

    public ExportExcelSheetField() {
    }

    public ExportExcelSheetField(String id, String title, boolean default_displayed) {
        this.id = id;
        this.title = title;
        this.default_displayed = default_displayed;
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

    public boolean isDefault_displayed() {
        return default_displayed;
    }

    public void setDefault_displayed(boolean default_displayed) {
        this.default_displayed = default_displayed;
    }

}
