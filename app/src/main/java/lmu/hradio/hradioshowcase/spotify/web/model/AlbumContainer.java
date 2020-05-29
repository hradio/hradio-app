package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class AlbumContainer implements Serializable {

   private static final long serialVersionUID = -8599279628303254565L;
   private AlbumList albums;

   public AlbumList getAlbums() {
      return albums;
   }

   public void setAlbums(AlbumList albums) {
      this.albums = albums;
   }

}
