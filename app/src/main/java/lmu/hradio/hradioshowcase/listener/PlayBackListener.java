package lmu.hradio.hradioshowcase.listener;

import eu.hradio.timeshiftplayer.SkipItem;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.TextData;

/**
 * Playback event listener
 */
public interface PlayBackListener extends OnManagerErrorListener {

    /**
     * handle playback started
     */
    void started();

    /**
     * handle playback stopped
     */
    void stopped();

    /**
     * handle playback paused
     */
    void paused();

    /**
     * handle playback progress
     *
     * @param current - current progress
     * @param total   - total duration
     */
    void playProgress(long current, long total);

    void playProgressRealtime(long realTimePosix, long streamTimePosix, long curPos, long totalDuration);

    void skipItemRemoved(SkipItem skipItem);

    /**
     * handle playback textual event
     *
     * @param content - text data
     */
    void textualContent(TextData content);

    /**
     * handle playback visual event
     *
     * @param visual - image data
     */
    void visualContent(ImageData visual);

    /**
     * handle playback started substitution event
     *
     * @param substitution - the started substitution
     */
    void started(SubstitutionItem substitution);

    /**
     * handle playback stopped substitution event
     *
     * @param substitution - the stopped substitution
     */
    void stopped(SubstitutionItem substitution);

    /**
     * handle playback progress substitution
     *
     * @param current      - current progress
     * @param total        - total duration
     * @param substitution - the substitution
     */
    void playProgress(SubstitutionItem substitution, long current, long total);

    void sbtSeeked();


    /**
     * Skip item added to timeshiftplayer
     *
     * @param skipItem the skip item
     */
    void skipItemAdded(SkipItem skipItem);

    /**
     * Listening progress reached new skip item
     *
     * @param skipItem the skip item
     */
    void itemStarted(SkipItem skipItem);
}
