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
