package lmu.hradio.hradioshowcase.events;

/**
 * General app broadcast events
 */
public interface AppEvents {
    /**
     * app package
     */
    String PACKAGE = "lmu.hradio.hradioshowcase";
    /**
     * user killed app task event
     */
    String APP_KILLED = PACKAGE + "app-killed";
}
