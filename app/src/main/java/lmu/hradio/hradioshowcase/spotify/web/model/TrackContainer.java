package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class TrackContainer implements Serializable {

   private static final long serialVersionUID = -7452459828804349294L;
   private TrackList tracks;

   public TrackList getTracks() {
      return tracks;
   }

   public void setTracks(TrackList tracks) {
      this.tracks = tracks;
   }

}
