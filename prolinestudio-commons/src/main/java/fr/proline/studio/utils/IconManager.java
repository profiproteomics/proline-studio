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
package fr.proline.studio.utils;

import fr.proline.studio.ImageUtilities;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

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
        HOUR_GLASS_MINI11,
        HOUR_GLASS_MINI16,
        PROJECT,
        PROJECT_READ_ONLY,
        PROJECT_DELETED,
        USER,
        USERS,
        USER_ADMIN,
        MAXIMIZE,
        MINIMIZE,
        ASSOCIATE,
        DISSOCIATE,
        DELETE,
        QUESTION,
        ERASER,
        CLEAN_UP,
        OPEN_FILE,
        EXCLAMATION,
        INFORMATION,
        WARNING,
        EMPTY,
        PLUS,
        CROSS_SMALL7,
        CROSS_SMALL16,
        CROSS_SMALL12,
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
        QUANT_TMT,
        QUANT_AGGREGATION_TMT,
        QUANT_AGGREGATION_XIC,
        CHART,
        BOOK,
        BOOK_OPEN,
        BOOK_QUESTION,
        BIG_HELP,
        BIG_WARNING,
        BIG_INFO,
        HELP_30,
        WARNING_30,
        INFO_30,
        SERVER_ON,
        MSN_SET,
        WAVE,
        PENCIL_RULER,
        COLUMNS_VISIBILITY,
        PLUS_SMALL_10X10,
        PROPERTY_SMALL_10X10,
        EDIT,
        EDIT_SMALL_10X10,
        IDENTIFICATION,
        ARROW_8X7,
        ARROW,
        BACK,
        DRAG_AND_DROP,
        WAND_HAT,
        FILE,
        FOLDER,
        FOLDER_EXPANDED,
        COMPUTER_NETWORK,
        DRIVE,
        FRAGMENTATION,
        BIOLOGICAL_GROUP,
        BIOLOGICAL_SAMPLE,
        REFERENCE_RSM,
        REFERENCE_AGRRE,
        REFERENCE_RSM_ERR,
        DATASET,
        DATASET_RSET,
        DATASET_RSM,
        DATASET_RSET_MERGED,
        DATASET_RSET_MERGED_AGG,
        DATASET_RSET_MERGED_UNION,
        DATASET_RSM_RSET_MERGED,
        DATASET_RSM_RSET_MERGED_AGG,
        DATASET_RSM_RSET_MERGED_UNION,
        DATASET_RSM_MERGED,
        DATASET_RSM_MERGED_AGG,
        DATASET_RSM_MERGED_UNION,
        DATASET_RSET_DECOY,
        DATASET_RSM_DECOY,
        LOAD_SETTINGS,
        LOAD_ALIGNMENT_CLOUD,
        REMOVE_ALIGNMENT_CLOUD,
        SHOW_CROSS_ASSIGNED,
        HIDE_CROSS_ASSIGNED,
        ADD_LOESS_CURVE,
        REMOVE_LOESS_CURVE,
        SAVE_SETTINGS,
        SAVE_WND,
        ARROW_DOWN,
        ARROW_RIGHT,
        ARROW_UP,
        ARROW_MOVE_UP,
        ARROW_MOVE_DOWN,
        ARROW_INSERT_UP,
        ARROW_INSERT_DOWN,
        ADD_DATA_ANALYZER,
        DATA_ANALYZER,
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
        REFRESH11,
        SETTINGS,
        TABLE_IMPORT,
        FULL_SCREEN,
        PROGRESS,
        UPDATE,
        SPECTRUM,
        SPECTRUM_EMISSION,
        NAVIGATE,
        SELECT,
        ZOOM_FIT,
        ZOOM_IN,
        ZOOM_OUT,
        ZOOM_ALL,
        CONTROL_PLAY,
        CONTROL_PAUSE,
        GEAR,
        HAND_OPEN,
        TOOLBOX_PLUS,
        TOOLBOX_MINUS,
        REFINE,
        SELECTED_CHECKBOXES,
        MENU,
        DOCUMENT_LARGE,
        SELECT_ALL,
        CLEAR_ALL,
        UNDO,
        REDO,
        PLUS_16X16,
        DATABASES_RELATION,
        TARGET,
        CONTROL_SMALL,
        SAME_MS_LEVEL,
        SIGNAL,
        AUTO_ZOOM,
        OVERLAY,
        ISOTOPES_PREDICTION,
        TEST,
        OPTIONS_MORE,
        OPTIONS_LESS,
        PIN,
        MOUSE_POINTER,
        MOUSE_SELECT_SQUARE,
        MOUSE_SELECT_FREE,
        EXPAND,
        ARROW_MOVE_UP_BIG,
        ARROW_MOVE_DOWN_BIG,
        VIEW_ALL,
        INVALIDATED,
        VALIDATED,
        VALIDATED_AND_USED,
        VALIDATED_AND_NOTUSED,
        INVALIDATED_LOCALLY,
        VALIDATED_LOCALLY,
        CENTROID_SPECTRA,
        EXPORT_CENTROID,
        SPLASH,
        FRAME_ICON,
        MINIFY,
        TIC,
        TIC_MS1,
        BPC,
        MS2,
        MERGE_PTM,
        FITTED_2_CENTROID;
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
                    greyLevel += (int) 50;
                    if (greyLevel > 255) {
                        greyLevel = 255;
                    }
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
                    greyLevel += (int) 100;
                    if (greyLevel > 255) {
                        greyLevel = 255;
                    }
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
                File f = File.createTempFile("tmpicon", ".png");
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
                return "fr/proline/studio/images/add-or-remove.png";
            case HOUR_GLASS:
                return "fr/proline/studio/images/hourglass.png";
            case HOUR_GLASS_MINI9:
                return "fr/proline/studio/images/hourglass-mini.png";
            case HOUR_GLASS_MINI11:
                return "fr/proline/studio/images/hourglass-mini-11x11.png";
            case HOUR_GLASS_MINI16:
                return "fr/proline/studio/images/hourglass-mini-16x16.png";
            case PROJECT:
                return "fr/proline/studio/images/project.png";
            case PROJECT_READ_ONLY:
                return "fr/proline/studio/images/project-read-only.png";
            case PROJECT_DELETED:
                return "fr/proline/studio/images/project-deleted.png";
            case USER:
                return "fr/proline/studio/images/user.png";
            case USERS:
                return "fr/proline/studio/images/users.png";
            case USER_ADMIN:
                return "fr/proline/studio/images/user-admin.png";
            case MAXIMIZE:
                return "fr/proline/studio/images/maximize.png";
            case MINIMIZE:
                return "fr/proline/studio/images/minimize.png";
            case ASSOCIATE:
                return "fr/proline/studio/images/associate.png";
            case DISSOCIATE:
                return "fr/proline/studio/images/dissociate.png";
            case DELETE:
                return "fr/proline/studio/images/delete-blue.png";
            case QUESTION:
                return "fr/proline/studio/images/question.png";
            case ERASER:
                return "fr/proline/studio/images/eraser.png";
            case CLEAN_UP:
                return "fr/proline/studio/images/broom.png";
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
            case CROSS_SMALL12:
                return "fr/proline/studio/images/cross-small12x16.png";
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
                return "fr/proline/studio/images/funnel-activated.png";
            case EXPORT:
                return "fr/proline/studio/images/export-data.png";
            case EXPORT_IMAGE:
                return "fr/proline/studio/images/export-image.png";
            case CHART:
                return "fr/proline/studio/images/chart.png";
            case QUANT:
                return "fr/proline/studio/images/chart-quant.png";
            case QUANT_XIC:
                return "fr/proline/studio/images/dataset-quant-xic.png";
            case QUANT_TMT:
                return "fr/proline/studio/images/dataset-tmt.png";
            case QUANT_AGGREGATION_TMT:
                return "fr/proline/studio/images/dataset-tmtA.png";
            case QUANT_SC:
                return "fr/proline/studio/images/dataset-quant-sc.png";
            case QUANT_AGGREGATION_XIC:
                return "fr/proline/studio/images/dataset-quant-xicA.png";
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
            case HELP_30:
                return "fr/proline/studio/images/help-30.png";
            case WARNING_30:
                return "fr/proline/studio/images/warning-30.png";
            case INFO_30:
                return "fr/proline/studio/images/info-30.png";
            case SERVER_ON:
                return "fr/proline/studio/images/server-on.png";
            case MSN_SET:
                return "fr/proline/studio/images/block.png";
            case WAVE:
                return "fr/proline/studio/images/wave.png";
            case PENCIL_RULER:
                return "fr/proline/studio/images/pencil-ruler.png";
            case PLUS_SMALL_10X10:
                return "fr/proline/studio/images/plus-small10x10.png";
            case PROPERTY_SMALL_10X10:
                return "fr/proline/studio/images/property-small10x10.png";
            case EDIT:
                return "fr/proline/studio/images/edit.png";
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
                return "fr/proline/studio/images/drag-and-drop.png";
            case WAND_HAT:
                return "fr/proline/studio/images/wand-hat.png";
            case FILE:
                return "fr/proline/studio/images/document.png";
            case FOLDER:
                return "fr/proline/studio/images/folder-horizontal.png";
            case FOLDER_EXPANDED:
                return "fr/proline/studio/images/folder-horizontal-open.png";
            case DRIVE:
                return "fr/proline/studio/images/drive.png";
            case COMPUTER_NETWORK:
                return "fr/proline/studio/images/computer-network.png";
            case FRAGMENTATION:
                return "fr/proline/studio/images/fragmentation.png";
            case BIOLOGICAL_GROUP:
                return "fr/proline/studio/images/biological-group.png";
            case BIOLOGICAL_SAMPLE:
                return "fr/proline/studio/images/biological-sample.png";
            case DATASET:
                return "fr/proline/studio/images/dataset.png";
            case DATASET_RSET:
                return "fr/proline/studio/images/dataset-rset.png";
            case REFERENCE_RSM:
                return "fr/proline/studio/images/quant-rsm-ref.png";
            case REFERENCE_AGRRE:
                return "fr/proline/studio/images/quant-aggregation-ref.png";
            case REFERENCE_RSM_ERR:
                return "fr/proline/studio/images/quant-rsm-ref-err.png";
            case DATASET_RSM:
                return "fr/proline/studio/images/dataset-rsm.png";
            case DATASET_RSET_MERGED:
                return "fr/proline/studio/images/dataset-rset-merged.png";
            case DATASET_RSET_MERGED_AGG:
                return "fr/proline/studio/images/dataset-rset-mergedA.png";
            case DATASET_RSET_MERGED_UNION:
                return "fr/proline/studio/images/dataset-rset-mergedU.png";
            case DATASET_RSM_RSET_MERGED:
                return "fr/proline/studio/images/dataset-rsm-rset-merged.png";
            case DATASET_RSM_RSET_MERGED_AGG:
                return "fr/proline/studio/images/dataset-rsm-rset-mergedA.png";
            case DATASET_RSM_RSET_MERGED_UNION:
                return "fr/proline/studio/images/dataset-rsm-rset-mergedU.png";
            case DATASET_RSM_MERGED:
                return "fr/proline/studio/images/dataset-rsm-merged.png";
            case DATASET_RSM_MERGED_AGG:
                return "fr/proline/studio/images/dataset-rsm-mergedA.png";
            case DATASET_RSM_MERGED_UNION:
                return "fr/proline/studio/images/dataset-rsm-mergedU.png";
            case DATASET_RSET_DECOY:
                return "fr/proline/studio/images/dataset-rset-decoy.png";
            case DATASET_RSM_DECOY:
                return "fr/proline/studio/images/dataset-rsm-decoy.png";
            case LOAD_SETTINGS:
                return "fr/proline/studio/images/load-settings.png";
            case LOAD_ALIGNMENT_CLOUD:
                return "fr/proline/studio/images/scatterplot-add.png";
            case SHOW_CROSS_ASSIGNED:
                return "fr/proline/studio/images/show-crossassigned-ions.png";
            case HIDE_CROSS_ASSIGNED:
                return "fr/proline/studio/images/hide-crossassigned-ions.png";
            case ADD_LOESS_CURVE:
                return "fr/proline/studio/images/loess-fit-add.png";
            case REMOVE_LOESS_CURVE:
                return "fr/proline/studio/images/loess-fit-remove.png";
            case REMOVE_ALIGNMENT_CLOUD:
                return "fr/proline/studio/images/scatterplot-remove.png";
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
            case ARROW_MOVE_UP:
                return "fr/proline/studio/images/move-up.png";
            case ARROW_MOVE_DOWN:
                return "fr/proline/studio/images/move-down.png";
            case ARROW_INSERT_UP:
                return "fr/proline/studio/images/insert-up.png";
            case ARROW_INSERT_DOWN:
                return "fr/proline/studio/images/insert-down.png";
            case ADD_DATA_ANALYZER:
                return "fr/proline/studio/images/data-analyzer-add.png";
            case DATA_ANALYZER:
                return "fr/proline/studio/images/data-mixer.png";
            case GRID:
                return "fr/proline/studio/images/grid.png";
            case IMPORT_TABLE_SELECTION:
                return "fr/proline/studio/images/import-table-selection.png";
            case EXPORT_TABLE_SELECTION:
                return "fr/proline/studio/images/export-table-selection.png";
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
            case REFRESH11:
                return "fr/proline/studio/images/refresh11x11.png";
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
            case SPECTRUM_EMISSION:
                return "fr/proline/studio/images/spectrum-emission.png";
            case NAVIGATE:
                return "fr/proline/studio/images/navigate.png";
            case SELECT:
                return "fr/proline/studio/images/select.png";
            case ZOOM_FIT:
                return "fr/proline/studio/images/magnifier-zoom-fit.png";
            case ZOOM_OUT:
                return "fr/proline/studio/images/magnifier-zoom-out.png";
            case ZOOM_IN:
                return "fr/proline/studio/images/magnifier-zoom-in.png";
            case ZOOM_ALL:
                return "fr/proline/studio/images/magnifier-zoom-all.png";
            case CONTROL_PLAY:
                return "fr/proline/studio/images/control.png";
            case CONTROL_PAUSE:
                return "fr/proline/studio/images/control-pause.png";
            case GEAR:
                return "fr/proline/studio/images/gear.png";
            case HAND_OPEN:
                return "fr/proline/studio/images/hand-open.png";
            case TOOLBOX_PLUS:
                return "fr/proline/studio/images/toolbox-plus.png";
            case TOOLBOX_MINUS:
                return "fr/proline/studio/images/toolbox-minus.png";
            case REFINE:
                return "fr/proline/studio/images/refine.png";
            case SELECTED_CHECKBOXES:
                return "fr/proline/studio/images/selected-checkboxes.png";
            case DOCUMENT_LARGE:
                return "fr/proline/studio/images/document-large.png";
            case MENU:
                return "fr/proline/studio/images/menu-down.png";
            case SELECT_ALL:
                return "fr/proline/studio/images/selection-select.png";
            case CLEAR_ALL:
                return "fr/proline/studio/images/broom-16x16.png";
            case UNDO:
                return "fr/proline/studio/images/undo.png";
            case REDO:
                return "fr/proline/studio/images/redo.png";
            case PLUS_16X16:
                return "fr/proline/studio/images/plus.png";
            case DATABASES_RELATION:
                return "fr/proline/studio/images/databases-relation.png";
            case TARGET:
                return "fr/proline/studio/images/target.png";
            case CONTROL_SMALL:
                return "fr/proline/studio/images/control-000-small.png";
            case SAME_MS_LEVEL:
                return "fr/proline/studio/images/same_level.png";
            case SIGNAL:
                return "fr/proline/studio/images/signal.png";
            case AUTO_ZOOM:
                return "fr/proline/studio/images/auto-zoom.png";
            case OVERLAY:
                return "fr/proline/studio/images/overlay.png";
            case ISOTOPES_PREDICTION:
                return "fr/proline/studio/images/isotopes-prediction.png";
            case TEST:
                return "fr/proline/studio/images/test.png";
            case OPTIONS_MORE:
                return "fr/proline/studio/images/hammer-plus.png";
            case OPTIONS_LESS:
                return "fr/proline/studio/images/hammer-minus.png";
            case PIN:
                return "fr/proline/studio/images/pin.png";
            case MOUSE_POINTER:
                return "fr/proline/studio/images/mouse-pointer.png";
            case MOUSE_SELECT_SQUARE:
                return "fr/proline/studio/images/selection-select.png";
            case MOUSE_SELECT_FREE:
                return "fr/proline/studio/images/selection-select-byhand.png";
            case EXPAND:
                return "fr/proline/studio/images/expand2.png";
            case ARROW_MOVE_UP_BIG:
                return "fr/proline/studio/images/arrow-090.png";
            case ARROW_MOVE_DOWN_BIG:
                return "fr/proline/studio/images/arrow-270.png";
            case VIEW_ALL:
                return "fr/proline/studio/images/view-all.png";
            case INVALIDATED:
                return "fr/proline/studio/images/invalidated.png";
            case INVALIDATED_LOCALLY:
                return "fr/proline/studio/images/invalidatedLocally.png";
            case VALIDATED:
                return "fr/proline/studio/images/validated.png";
            case VALIDATED_LOCALLY:
                return "fr/proline/studio/images/validatedLocally.png";
            case VALIDATED_AND_USED:
                return "fr/proline/studio/images/validated_and_used.png";
            case VALIDATED_AND_NOTUSED:
                return "fr/proline/studio/images/validated_and_notused.png";
            case CENTROID_SPECTRA:
                return "fr/proline/studio/images/centroid.png";
            case EXPORT_CENTROID:
                return "fr/proline/studio/images/export-centroid.png";
            case SPLASH:
                return "fr/proline/studio/images/splash.gif";
            case FRAME_ICON:
                return "fr/proline/studio/images/frame48.gif";
            case MINIFY:
                return "fr/proline/studio/images/minify.png";
            case TIC:
                return "fr/proline/studio/images/tic.png";
            case TIC_MS1:
                return "fr/proline/studio/images/tic-ms1.png";
            case BPC:
                return "fr/proline/studio/images/bpc.png";
            case MS2:
                return "fr/proline/studio/images/ms2.png";
            case MERGE_PTM:
                return "fr/proline/studio/images/merge_ptm.png";
            case FITTED_2_CENTROID:
                return "fr/proline/studio/images/fittedTocentroid.png";
        }

        return null; // can not happen
    }
}
