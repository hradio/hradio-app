package lmu.hradio.hradioshowcase.model.view;

import android.graphics.Bitmap;

import java.io.Serializable;

import lmu.hradio.hradioshowcase.util.ImageDataHelper;

public class ImageData implements Serializable  {
    private static final long serialVersionUID = -7344157383936199865L;

    private int width;
    private int height;
    private byte[] imageData;

    public ImageData(byte[] imageData, int width, int height) {
        this.width = width;
        this.height = height;
        this.imageData = imageData;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public Bitmap decode(){
       return ImageDataHelper.decodeToBitmap(this);
    }

}
