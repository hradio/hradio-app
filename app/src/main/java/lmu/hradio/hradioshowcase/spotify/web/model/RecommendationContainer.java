package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class RecommendationContainer implements Serializable {

   private static final long serialVersionUID = 993498960555975551L;
   private Seed[] seeds;

   private Track[] tracks;

   public Seed[] getSeeds() {
      return seeds;
   }

   public void setSeeds(Seed[] seeds) {
      this.seeds = seeds;
   }

   public Track[] getTracks() {
      return tracks;
   }

   public void setTracks(Track[] tracks) {
      this.tracks = tracks;
   }
}
