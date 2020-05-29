package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class PodcastContainer implements Serializable {

   private static final long serialVersionUID = -886125130258290052L;
   private PodcastList shows;

   public PodcastList getShows() {
      return shows;
   }

   public void setShows(PodcastList shows) {
      this.shows = shows;
   }

}
