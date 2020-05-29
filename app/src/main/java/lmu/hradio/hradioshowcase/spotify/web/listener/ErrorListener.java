package lmu.hradio.hradioshowcase.spotify.web.listener;

public interface ErrorListener<T> {

    void onError(T error);
}
