package fr.proline.studio.export;


/**
 * represents a sheet in the configuration file for export It contains a unique
 * id, a title, a presentation mode (row or column) and a list of fields
 *
 * @author
 */
public class ExportExcelSheet {
    public String id;
    public String title;
    public String presentation;
    public ExportExcelSheetField[] fields;

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
