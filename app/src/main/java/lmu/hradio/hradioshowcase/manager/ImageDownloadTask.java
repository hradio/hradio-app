package lmu.hradio.hradioshowcase.manager;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.model.view.ImageData;

public class ImageDownloadTask extends AsyncTask<ImageDownloadTask.UrlHolder, Integer, List<ImageData>> {

    private ImageDownloadListener listener;

    public ImageDownloadTask(ImageDownloadListener listener){
        this.listener = listener;
    }

    public List<ImageData> downloadBlocking(UrlHolder... urls){
        List<ImageData> images = new ArrayList<>();
        for(UrlHolder urlHolder : urls){
            ImageData data = downloadImageBlocking(urlHolder);
            if(data != null)
                images.add(data);
        }
        return images;
    }

    //May return null for corrupt urls
    public ImageData downloadImageBlocking(UrlHolder urlHolder){

            try {
                URL url = new URL(urlHolder.getUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] data = new byte[1024];

                while ((nRead = input.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                data = buffer.toByteArray();
                return new ImageData(data, urlHolder.getWidth(), urlHolder.getHeight());
            } catch (IOException e) {
                // Log exception
                if(BuildConfig.DEBUG)Log.d(ImageDownloadTask.class.getSimpleName(), e.fillInStackTrace().toString());
            }
        return null;
    }



    @Override
    protected List<ImageData> doInBackground(UrlHolder... urls) {
        return downloadBlocking(urls);
    }

    @Override
    protected void onPostExecute(List<ImageData> result) {
        super.onPostExecute(result);
        if(listener != null)
            listener.onImagesDownloaded(result);
    }


    public static class UrlHolder{
        private int width, height;
        private String url;

        public UrlHolder(int width, int height, String url) {
            this.width = width;
            this.height = height;
            this.url = url;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getUrl() {
            return url;
        }
    }

    @FunctionalInterface
    public interface ImageDownloadListener{
        void onImagesDownloaded(List<ImageData> images);
    }

}