package lmu.hradio.hradioshowcase.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Helper class to save images in local device storage
 */
public final class ImageStorageManager {

    /**
     * Save image to local storage
     *
     * @param context - context object
     * @param image   - the image to be stored
     * @param name    - the images name
     */
    public static void saveImage(Context context, byte[] image, String name) {

        Function<String, Void> fun = (n)->{
            FileOutputStream fileOutputStream;
            try {
                String[] namePaths = n.split("/");
                String nameFin = namePaths[namePaths.length - 1];
                fileOutputStream = context.openFileOutput(nameFin + ".png", Context.MODE_PRIVATE);
                fileOutputStream.write(image);
                fileOutputStream.close();
            } catch (Exception ignored) {
            }
            return null;
        };

        new StorageTask<>(fun).execute(name);
    }

    /**
     * load image from local storage
     *
     * @param context - context object
     * @param name    - the images name
     * @return the stored image
     */
    public static void loadImage(Context context, String name, ResultListener<byte[]> resultListener) {

        Function<String, byte[]> callable = (n) -> {
            FileInputStream fileInputStream;
            byte[] image = null;
            try {
                String[] namePaths = n.split("/");
                String nameFin = namePaths[namePaths.length - 1];
                fileInputStream = context.openFileInput(nameFin + ".png");
                Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                image = stream.toByteArray();
                bitmap.recycle();
                fileInputStream.close();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            return image;
        };
        new StorageTask<>(resultListener, callable).execute(name);

    }

    /**
     * Delete image from local storage
     *
     * @param context - context object
     * @param name    - the images name
     * @return true if successfully
     */
    public static void deleteImage(Context context, String name) {
        if(name != null) {
            Function<String, Boolean> fun = (n) -> new File(context.getFilesDir(), n).delete();
            new StorageTask<>(fun).execute(name);
        }
    }

    private static class StorageTask<T, P,R> extends AsyncTask<T,P,R >{

        private Function<T,R> function;
        private ResultListener<R> listener;

        StorageTask(Function<T,R> function){
            this.function = function;
        }

        StorageTask(ResultListener<R> listener, Function<T,R> function){
            this.listener = listener;
            this.function = function;

        }


        @Override
        protected R doInBackground(T... ts) {
            R r = null;
            for(T t: ts){
                r = function.call(t);
            }
            return r;
        }

        @Override
        protected void onPostExecute(R r) {
            super.onPostExecute(r);
            if(listener != null) listener.onResult(r);
        }
    }

    @FunctionalInterface
    private interface Function<A, T>{
        T call(A a);
    }

    @FunctionalInterface
    public interface ResultListener<T>{
        void onResult(T res);
    }

}
