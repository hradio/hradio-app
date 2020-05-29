package lmu.hradio.hradioshowcase.events;

/**
 * app broadcast player events
 */
public interface PlayerEvents {
    /**
     * app package name
     */
    String PACKAGE = "lmu.hradio.hradioshowcase";
    /**
     * progress event name
     */
    String PROGRESS = PACKAGE + ".PROGRESS";
    /**
     * STARTED event name
     */
    String STARTED = PACKAGE + ".STARTED";
    /**
     * STOPPED event name
     */
    String STOPPED = PACKAGE + ".STOPPED";
    /**
     * PAUSED event name
     */
    String PAUSED = PACKAGE + ".PAUSED";
    /**
     * TEXTUAL event name
     */
    String TEXTUAL = PACKAGE + ".TEXTUAL";
    /**
     * VISUAL event name
     */
    String VISUAL = PACKAGE + ".VISUAL";
    /**
     * SKIP_ITEM_ADDED event name
     */
    String SKIP_ITEM_ADDED = PACKAGE + ".SKIP_ITEM_ADDED";
    /**
     * SKIP_ITEM_STARTED event name
     */
    String SKIP_ITEM_STARTED = PACKAGE + ".SKIP_ITEM_STARTED";
    /**
     * SIGNAL_STRENGT_CHANGE event name
     */
    String SIGNAL_STRENGT_CHANGE = PACKAGE + ".SIGNAL_STRENGT_CHANGE";

    /**
     * SIGNAL_STRENGT_CHANGE event extra
     */
    String SIGNAL_STRENGT_CHANGE_EXTRA = "SIGNAL_STRENGT_CHANGE_EXTRA";
    /**
     * PROGRESS event extra current
     */
    String PROGRESS_EXTRAS_CURRENT = "CURRENT";
    /**
     * PROGRESS event extra TOTAL
     */
    String PROGRESS_EXTRAS_TOTAL = "TOTAL";
    /**
     * event extra SUBSTITUTION
     */
    String EXTRAS_SUBSTITUTION = "SUBSTITUTION";
    /**
     * TEXTUAL event extra
     */
    String TEXTUAL_EXTRA = "TEXTUAL";
    /**
     * VISUAL event extra
     */
    String VISUAL_EXTRA = "VISUAL";
    /**
     * SKIP_ITEM_ADDED event extra
     */
    String SKIP_ITEM_ADDED_EXTRA = "SKIP_ITEM_ADDED";
    /**
     * SKIP_ITEM_STARTED event extra
     */
    String SKIP_ITEM_STARTED_EXTRA = "SKIP_ITEM_STARTED";


}
