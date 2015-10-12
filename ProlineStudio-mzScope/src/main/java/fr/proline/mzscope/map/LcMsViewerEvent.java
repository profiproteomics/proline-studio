/**
 *
 */
package fr.proline.mzscope.map;

/**
 * LcMsViewer events
 *
 * @author JeT
 */
public class LcMsViewerEvent {

    private LcMsViewer viewer = null;
    private LcMsViewerEventType type = null;

    public static enum LcMsViewerEventType {
	MAP_CHANGED, VIEWPORT_CHANGED, IMAGE_PROCESS_ADDED,

    }

    /**
     * Quick Constructor
     *
     * @param viewer
     */
    public LcMsViewerEvent(LcMsViewer viewer, LcMsViewerEventType type) {
	super();
	this.viewer = viewer;
	this.type = type;
    }

    /**
     * @return the viewer
     */
    public LcMsViewer getViewer() {
	return this.viewer;
    }

    /**
     * @return the type
     */
    public LcMsViewerEventType getType() {
	return this.type;
    }

}
