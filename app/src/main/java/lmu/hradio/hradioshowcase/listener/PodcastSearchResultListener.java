package lmu.hradio.hradioshowcase.listener;

import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;

/**
 * Podcast search result listener
 */
public interface PodcastSearchResultListener {

    /**
     * podcast received callback
     *
     * @param sourceCover - the source cover
     * @param podcasts    - the found podcasts
     */
    void onPodcastsReceived(byte[] sourceCover, SubstitutionItem[] podcasts);
}
