package lmu.hradio.hradioshowcase.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.omri.radioservice.metadata.Visual;

import java.util.ArrayList;
import java.util.List;

import eu.hradio.httprequestwrapper.dtos.service_search.MediaDescription;
import lmu.hradio.hradioshowcase.manager.ImageDownloadTask;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.spotify.web.model.Image;

public final class ImageDataHelper {

    public static ImageData findBiggest(List<ImageData> images) {
        ImageData displayLogo = null;
        for (ImageData image : images) {
            if (displayLogo == null)
                displayLogo = image;
            else if (displayLogo.getHeight() < image.getHeight() && displayLogo.getWidth() < image.getWidth()) {
                displayLogo = image;
            }
        }
        return displayLogo;
    }

    public static Bitmap decodeToBitmap(ImageData visual) {
        if (visual == null || visual.getImageData() == null)
            return null;
        return BitmapFactory.decodeByteArray(visual.getImageData(), 0, visual.getImageData().length);
    }

    public static List<ImageData> imageListFromVisuals(List<Visual> images) {
        List<ImageData> transformedList = new ArrayList<>();
        for (Visual image : images) {
            transformedList.add(fromVisual(image));
        }
        return transformedList;
    }

    public static ImageData fromVisual(Visual visual) {
        return new ImageData(visual.getVisualData(), visual.getVisualWidth(), visual.getVisualHeight());
    }

    public static ImageData fromVisuals(List<Visual> images) {
        Visual displayLogo = null;
        for (Visual image : images) {
            if (displayLogo == null)
                displayLogo = image;
            else if (displayLogo.getVisualHeight() < image.getVisualHeight() && displayLogo.getVisualWidth() < image.getVisualWidth()) {
                displayLogo = image;
            }
        }
        return (displayLogo== null)? null : new ImageData(displayLogo.getVisualData(), displayLogo.getVisualWidth(), displayLogo.getVisualHeight());
    }

    public static String fromMediaDescription(MediaDescription[] mediaDescriptions, ImageDownloadTask.ImageDownloadListener listener) {
        List<ImageDownloadTask.UrlHolder> imageUrls = new ArrayList<>();
        for (MediaDescription multimedia : mediaDescriptions) {
            if (multimedia.getType() != null && multimedia.getType().equals("MULTIMEDIA")) {
                imageUrls.add(new ImageDownloadTask.UrlHolder(multimedia.getWidth(), multimedia.getHeight(), multimedia.getUrl()));
            }
        }

        ImageDownloadTask.UrlHolder displayLogo = null;
        for (ImageDownloadTask.UrlHolder image : imageUrls) {
            if (displayLogo == null)
                displayLogo = image;
            else if (displayLogo.getHeight() < image.getHeight() && displayLogo.getWidth() < image.getWidth()) {
                displayLogo = image;
            }
        }
        if (displayLogo != null) {
            new ImageDownloadTask(listener).execute(displayLogo);
            return displayLogo.getUrl();
        }
        return "";
    }

    public static  String fromSpotifyImages(Image[] images, ImageDownloadTask.ImageDownloadListener listener) {
        Image biggest = null;
        for (Image image : images){
            if (biggest == null)
                biggest = image;
            else if (biggest.getHeight() < image.getHeight() && biggest.getWidth() < image.getWidth()) {
                biggest = image;
            }
        }
        if (biggest != null) {
            new ImageDownloadTask(listener).execute(new ImageDownloadTask.UrlHolder(biggest.getWidth(), biggest.getHeight(), biggest.getUrl()));
            return biggest.getUrl();
        }
        return "";
    }


}
