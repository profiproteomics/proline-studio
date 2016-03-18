package fr.proline.studio.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 * Manage all icons needed by the application. Each icon is loaded only one time
 * when it is first needed
 *
 * Each icon can be loaded greyed with an hourglass on it
 *
 * @author JM235353
 */
public class IconManager {

    public enum IconType {

        OK,
        CANCEL,
        DEFAULT,
        TICK_SMALL,
        SAME_SET,
        SUB_SET,
        TYPICAL,
        ADD_REMOVE,
        HOUR_GLASS,
        HOUR_GLASS_MINI9,
        HOUR_GLASS_MINI16,
        GEL,
        PROJECT,
        PROJECT_READ_ONLY,
        USER,
        USERS,
        MAXIMIZE,
        MINIMIZE,
        ASSOCIATE,
        DISSOCIATE,
        DELETE,
        QUESTION,
        ERASER,
        ERASER_SMALL11,
        OPEN_FILE,
        EXCLAMATION,
        INFORMATION,
        WARNING,
        EMPTY,
        PLUS,
        CROSS_SMALL7,
        CROSS_SMALL16,
        CROSS_BLUE_SMALL16,
        DOCUMENT_LIST,
        WEB_LINK,
        TRASH,
        ALL_IMPORTED,
        PROPERTIES,
        ARROW_RIGHT_SMALL,
        PLUS11,
        MINUS11,
        SEARCH_ARROW,
        SEARCH,
        FUNNEL,
        FUNNEL_ACTIVATED,
        EXPORT,
        EXPORT_IMAGE,
        QUANT,
        QUANT_SC,
        QUANT_XIC,
        CHART,
        BOOK,
        BOOK_OPEN,
        BOOK_QUESTION,
        BIG_HELP,
        BIG_WARNING,
        BIG_INFO,
        SERVER_ON,
        SERVER_OFF,
        MSN_SET,
        WAVE,
        COLUMNS_VISIBILITY,
        PLUS_SMALL_10X10,
        PROPERTY_SMALL_10X10,
        EDIT_SMALL_10X10,
        IDENTIFICATION,
        ARROW_8X7,
        ARROW,
        BACK,
        DRAG_AND_DROP,
        WAND_HAT,
        FILE,
        FOLDER,
        FRAGMENTATION,
        BIOLOGICAL_GROUP,
        BIOLOGICAL_SAMPLE,
        DATASET,
        DATASET_RSET,
        DATASET_RSM,
        DATASET_RSET_MERGED,
        DATASET_RSM_RSET_MERGED,
        DATASET_RSM_MERGED,
        DATASET_RSET_DECOY,
        DATASET_RSM_DECOY,
        DATASET_RSM_ERROR,
        LOAD_SETTINGS,
        SAVE_SETTINGS,
        SAVE_WND, 
        ARROW_DOWN, 
        ARROW_RIGHT,
        ARROW_UP,
        ADD_DATA_MIXER,
        DATA_MIXER,
        GRID,
        IMPORT_TABLE_SELECTION,
        EXPORT_TABLE_SELECTION,
        LOCK,
        UNLOCK,
        COLOR_PICKER,
        CHART_ARROW,
        CALCULATOR,
        CHART_PIE,
        EXECUTE,
        STOP,
        TABLES,
        TABLE,
        COLUMN,
        CHALKBOARD,
        FUNCTION,
        TICK_CIRCLE,
        REFRESH,
        SETTINGS,
        TABLE_IMPORT, 
        FULL_SCREEN, 
        PROGRESS, 
        UPDATE, 
        SPECTRUM, 
        NAVIGATE,
        SELECT, 
        ZOOM_FIT,
        CONTROL_PLAY,
        CONTROL_STOP,
        CONTROL_PAUSE,
        GEAR;
        
    }
    private final static HashMap<IconType, ImageIcon> m_iconMap = new HashMap<>();
    private final static HashMap<IconType, ImageIcon> m_iconHourGlassMap = new HashMap<>();
    private final static HashMap<IconType, ImageIcon> m_grayedIconMap = new HashMap<>();
    private final static HashMap<IconType, String> m_iconURLMap = new HashMap<>(); 
    
    
    public static Image getImage(IconType iconType) {
        ImageIcon icon = getIcon(iconType);
        return icon.getImage();
    }
    
    public static ImageIcon getIcon(IconType iconType) {

        ImageIcon icon = m_iconMap.get(iconType);
        if (icon == null) {
            String path = getIconFilePath(iconType);
            icon = ImageUtilities.loadImageIcon(path, false);
            m_iconMap.put(iconType, icon);
        }

        return icon;
    }

    public static ImageIcon getGrayedIcon(IconType iconType) {
        
        ImageIcon grayedIcon = m_grayedIconMap.get(iconType);
        if (grayedIcon == null) {
            String path = getIconFilePath(iconType);
            Image imSource = ImageUtilities.loadImage(path, false);
            int sourceWidth = imSource.getWidth(null);
            int sourceHeight = imSource.getHeight(null);

            BufferedImage im = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_ARGB);

            im.getGraphics().drawImage(imSource, 0, 0, null);

            // grey out the image
            for (int x = 0; x < sourceWidth; x++) {
                for (int y = 0; y < sourceHeight; y++) {
                    int color = im.getRGB(x, y);
                    int alpha = (color & 0xFF000000);
                    int red = (color & 0x00FF0000) >> 16;
                    int green = (color & 0x0000FF00) >> 8;
                    int blue = color & 0x000000FF;
                    int greyLevel = (int) Math.round(((double) (red + green + blue)) / 3.0);
                    // lighter grey
                    greyLevel  += (int) 100;
                    if (greyLevel>255) greyLevel = 255;
                    int grey = alpha + (greyLevel << 16) + (greyLevel << 8) + greyLevel;
                    im.setRGB(x, y, grey);
                }
            }

 

            grayedIcon = new ImageIcon(im);
            m_grayedIconMap.put(iconType, grayedIcon);
        }

        return grayedIcon;
    }
    
    public static ImageIcon getIconWithHourGlass(IconType iconType) {

        ImageIcon iconWithHourGlass = m_iconHourGlassMap.get(iconType);
        if (iconWithHourGlass == null) {
            String path = getIconFilePath(iconType);
            Image imSource = ImageUtilities.loadImage(path, false);
            int sourceWidth = imSource.getWidth(null);
            int sourceHeight = imSource.getHeight(null);

            BufferedImage im = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_ARGB);

            im.getGraphics().drawImage(imSource, 0, 0, null);

            // grey out the image
            for (int x = 0; x < sourceWidth; x++) {
                for (int y = 0; y < sourceHeight; y++) {
                    int color = im.getRGB(x, y);
                    int alpha = (color & 0xFF000000);
                    int red = (color & 0x00FF0000) >> 16;
                    int green = (color & 0x0000FF00) >> 8;
                    int blue = color & 0x000000FF;
                    int greyLevel = (int) Math.round(((double) (red + green + blue)) / 3.0);
                    // lighter grey
                    greyLevel  += (int) 100;
                    if (greyLevel>255) greyLevel = 255;
                    int grey = alpha + (greyLevel << 16) + (greyLevel << 8) + greyLevel;
                    im.setRGB(x, y, grey);
                }
            }

            if (miniHourGlassImage == null) {
                miniHourGlassImage = ImageUtilities.loadImage(getIconFilePath(IconType.HOUR_GLASS_MINI9), false);
            }

            // add an hour glass at the right bottom corner
            im.getGraphics().drawImage(miniHourGlassImage, sourceWidth - miniHourGlassImage.getWidth(null), sourceHeight - miniHourGlassImage.getHeight(null), null);

            iconWithHourGlass = new ImageIcon(im);
            m_iconHourGlassMap.put(iconType, iconWithHourGlass);
        }

        return iconWithHourGlass;

    }
    private static Image miniHourGlassImage = null;

    public static String getURLForIcon(IconType iconType) {
        
        String imageURL = m_iconURLMap.get(iconType);
        if (imageURL == null) {

            Image i = IconManager.getImage(iconType);
            BufferedImage bimage = new BufferedImage(i.getWidth(null), i.getHeight(null), BufferedImage.TYPE_INT_ARGB);

            // Draw the image on to the buffered image
            Graphics2D bGr = bimage.createGraphics();
            bGr.drawImage(i, 0, 0, null);
            bGr.dispose();



            try {
                File f = File.createTempFile("tmpicon", ".png", new File("."));
                f.deleteOnExit();
                ImageIO.write(bimage, "png", f);

                imageURL = f.toURI().toURL().toString();
                m_iconURLMap.put(iconType, imageURL);

            } catch (Exception e) {
            }
        }
        
        return imageURL;
    }
    
    private static String getIconFilePath(IconType iconType) {
        switch (iconType) {
            case OK:
                return "fr/proline/studio/images/tick.png";
            case CANCEL:
                return "fr/proline/studio/images/cross.png";
            case DEFAULT:
                return "fr/proline/studio/images/arrow-circle.png";
            case TICK_SMALL:
                return "fr/proline/studio/images/tick-small.png"; 
            case SAME_SET:
                return "fr/proline/studio/images/sameset.png";
            case SUB_SET:
                return "fr/proline/studio/images/subset.png";
            case TYPICAL:
                return "fr/proline/studio/images/typical.png";
            case ADD_REMOVE:
                return "fr/proline/studio/images/addremove.png";
            case HOUR_GLASS:
                return "fr/proline/studio/images/hourGlass.png";
            case HOUR_GLASS_MINI9:
                return "fr/proline/studio/images/hourGlassMini.png";
            case HOUR_GLASS_MINI16:
                return "fr/proline/studio/images/hourGlassMini16x16.png";
            case GEL:
                return "fr/proline/studio/images/gel.png";
            case PROJECT:
                return "fr/proline/studio/images/project.png";
            case PROJECT_READ_ONLY:
                return "fr/proline/studio/images/projectReadOnly.png";
            case USER:
                return "fr/proline/studio/images/user.png";
            case USERS:
                return "fr/proline/studio/images/users.png";
            case MAXIMIZE:
                return "fr/proline/studio/images/maximize.png";
            case MINIMIZE:
                return "fr/proline/studio/images/minimize.png";
            case ASSOCIATE:
                return "fr/proline/studio/images/associate.png";
            case DISSOCIATE:
                return "fr/proline/studio/images/dissociate.png";
            case DELETE:
                return "fr/proline/studio/images/deleteBlue.png";
            case QUESTION:
                return "fr/proline/studio/images/question.png";
            case ERASER:
                return "fr/proline/studio/images/eraser.png";
            case ERASER_SMALL11:
                return "fr/proline/studio/images/eraser11x11.png";
            case OPEN_FILE:
                return "fr/proline/studio/images/folder-open-document.png";
            case EXCLAMATION:
                return "fr/proline/studio/images/exclamation-red.png";
            case INFORMATION:
                return "fr/proline/studio/images/information.png";
            case WARNING:
                return "fr/proline/studio/images/exclamation.png";
            case EMPTY:
                return "fr/proline/studio/images/empty.png";
            case PLUS:
                return "fr/proline/studio/images/plus-small7x7.png";
            case CROSS_SMALL7:
                return "fr/proline/studio/images/cross-small7x7.png";
            case CROSS_SMALL16:
                return "fr/proline/studio/images/cross-small16x16.png";
            case CROSS_BLUE_SMALL16:
                return "fr/proline/studio/images/cross-smallblue16x16.png";
            case DOCUMENT_LIST:
                return "fr/proline/studio/images/document-list.png";
            case WEB_LINK:
                return "fr/proline/studio/images/weblink.png";
            case TRASH:
                return "fr/proline/studio/images/trash.png";
            case ALL_IMPORTED:
                return "fr/proline/studio/images/all-imported.png"; 
            case PROPERTIES:
                return "fr/proline/studio/images/property.png"; 
            case ARROW_RIGHT_SMALL:
                return "fr/proline/studio/images/arrow-right-small.png"; 
            case PLUS11:
                return "fr/proline/studio/images/plus11x11.png";
            case MINUS11:
                return "fr/proline/studio/images/minus11x11.png";
            case SEARCH_ARROW:
                return "fr/proline/studio/images/search-arrow.png";
            case SEARCH:
                return "fr/proline/studio/images/search.png";
            case FUNNEL:
                return "fr/proline/studio/images/funnel.png";
            case FUNNEL_ACTIVATED:
                return "fr/proline/studio/images/funnelActivated.png";
            case EXPORT:
                return "fr/proline/studio/images/exportData.png";
            case EXPORT_IMAGE:
                return "fr/proline/studio/images/exportImage.png";
            case CHART:
                return "fr/proline/studio/images/chart.png";
            case QUANT:
                return "fr/proline/studio/images/chart-quant.png";
            case QUANT_XIC:
                return "fr/proline/studio/images/chart-quantXic2.png";
            case QUANT_SC:
                return "fr/proline/studio/images/chart-quantSC2.png";                
            case BOOK:
                return "fr/proline/studio/images/book.png";
            case BOOK_OPEN:
                return "fr/proline/studio/images/book-open.png";
            case BOOK_QUESTION:
                return "fr/proline/studio/images/book-question.png";
            case BIG_HELP:
                return "fr/proline/studio/images/big-help.png";
            case BIG_WARNING:
                return "fr/proline/studio/images/big-warning.png";
            case BIG_INFO:
                return "fr/proline/studio/images/big-info.png";
            case SERVER_ON:
                return "fr/proline/studio/images/server-on.png";
            case SERVER_OFF:
                return "fr/proline/studio/images/server-off.png";
            case MSN_SET:
                return "fr/proline/studio/images/block.png";
            case WAVE:
                return "fr/proline/studio/images/wave.png";
            case COLUMNS_VISIBILITY:
                return "fr/proline/studio/images/columnsVisibility11x11.png";      
            case PLUS_SMALL_10X10:
                return "fr/proline/studio/images/plus-small10x10.png";     
            case PROPERTY_SMALL_10X10:
                return "fr/proline/studio/images/property-small10x10.png";    
            case EDIT_SMALL_10X10:
                return "fr/proline/studio/images/edit-small10x10.png";   
            case IDENTIFICATION:
                return "fr/proline/studio/images/identification.png";  
            case ARROW_8X7:
                return "fr/proline/studio/images/arrow8x7.png";
            case ARROW:
                return "fr/proline/studio/images/arrow.png";
            case BACK:
                return "fr/proline/studio/images/arrow-180.png";
            case DRAG_AND_DROP:
                return "fr/proline/studio/images/dragAndDrop.png";
            case WAND_HAT:
                return "fr/proline/studio/images/wand-hat.png";
            case FILE:
                return "fr/proline/studio/images/document.png";
            case FOLDER:
                return "fr/proline/studio/images/folder-horizontal.png";
             case FRAGMENTATION:
                return "fr/proline/studio/images/fragmentation.png";
            case BIOLOGICAL_GROUP:
                return "fr/proline/studio/images/biologicalGroup.png";
            case BIOLOGICAL_SAMPLE:
                return "fr/proline/studio/images/biologicalSample.png";
            case DATASET:
                return "fr/proline/studio/images/dataset.png";
            case DATASET_RSET:
                return "fr/proline/studio/images/dataset_rset.png";
            case DATASET_RSM:
                return "fr/proline/studio/images/dataset_rsm.png";
            case DATASET_RSET_MERGED:
                return "fr/proline/studio/images/dataset_rset_merged.png";
            case DATASET_RSM_RSET_MERGED:
                return "fr/proline/studio/images/dataset_rsm_rset_merged.png";
            case DATASET_RSM_MERGED:
                return "fr/proline/studio/images/dataset_rsm_merged.png";
            case DATASET_RSET_DECOY:
                return "fr/proline/studio/images/dataset_rset_decoy.png";
            case DATASET_RSM_DECOY:
                return "fr/proline/studio/images/dataset_rsm_decoy.png";
            case DATASET_RSM_ERROR:
                return "fr/proline/studio/images/dataset_rsm_error.png";
            case LOAD_SETTINGS:
                return "fr/proline/studio/images/load-settings.png"; 
            case SAVE_SETTINGS:
                return "fr/proline/studio/images/save-settings.png"; 
            case SAVE_WND:
                return "fr/proline/studio/images/save11x11.png";
           case ARROW_DOWN:
               return "fr/proline/studio/images/arrow-down.png";
           case ARROW_RIGHT:
               return "fr/proline/studio/images/arrow-right.png";
            case ARROW_UP:
               return "fr/proline/studio/images/arrow-up.png";
           case ADD_DATA_MIXER:
               return "fr/proline/studio/images/addDataMixer.png";
           case DATA_MIXER:
               return "fr/proline/studio/images/data-mixer.png";
           case GRID:
               return "fr/proline/studio/images/grid.png";
           case IMPORT_TABLE_SELECTION:
               return "fr/proline/studio/images/importTableSelection.png";
           case EXPORT_TABLE_SELECTION:
               return "fr/proline/studio/images/exportTableSelection.png";
           case LOCK:
               return "fr/proline/studio/images/lock.png";
           case UNLOCK:
               return "fr/proline/studio/images/lock-unlock.png";
           case COLOR_PICKER:
               return "fr/proline/studio/images/color.png";
           case CHART_ARROW:
               return "fr/proline/studio/images/chart-arrow.png";
           case CALCULATOR:
               return "fr/proline/studio/images/calculator.png";
           case CHART_PIE:
               return "fr/proline/studio/images/chart-pie.png";
           case EXECUTE:
               return "fr/proline/studio/images/execute.png";
           case STOP:
               return "fr/proline/studio/images/stop.png";
           case TABLES:
               return "fr/proline/studio/images/tables.png";
           case TABLE:
               return "fr/proline/studio/images/table.png";
           case COLUMN:
               return "fr/proline/studio/images/table-column.png";
           case CHALKBOARD:
               return "fr/proline/studio/images/chalkboard-text.png";
           case FUNCTION:
               return "fr/proline/studio/images/function.png";
           case TICK_CIRCLE:
               return "fr/proline/studio/images/tick-circle.png";
           case REFRESH:
               return "fr/proline/studio/images/refresh.png";
           case SETTINGS:
               return "fr/proline/studio/images/settings.png";
           case TABLE_IMPORT:
               return "fr/proline/studio/images/table-import.png";
            case FULL_SCREEN: 
               return "fr/proline/studio/images/fullscreen.png";
            case PROGRESS: 
               return "fr/proline/studio/images/progress.gif";
            case UPDATE: 
               return "fr/proline/studio/images/update.png";
            case SPECTRUM: 
               return "fr/proline/studio/images/spectrum.png";
            case NAVIGATE: 
               return "fr/proline/studio/images/navigate.png";    
            case SELECT: 
               return "fr/proline/studio/images/select.png";    
            case ZOOM_FIT:
                return "fr/proline/studio/images/magnifier-zoom-fit.png";
            case CONTROL_PLAY:
                return "fr/proline/studio/images/control.png";
            case CONTROL_STOP:
                return "fr/proline/studio/images/control-stop.png";
            case CONTROL_PAUSE:
                return "fr/proline/studio/images/control-pause.png";
            case GEAR:
                return "fr/proline/studio/images/gear.png";
                
        }


        return null; // can not happen
    }
}
