package com.linhnguyen.rccar.core;

import android.graphics.Bitmap;

/**
 * Created by linhn on 3/30/17.
 */

public class ImageItem {
    private Bitmap image;
    private String title;
    private String item_id;

    public ImageItem(Bitmap image, String title) {
        super();
        this.image = image;
        this.title = title;
        this.item_id = "default_icon";
    }

    public ImageItem(Bitmap image, String title, String item_id) {
        super();
        this.image = image;
        this.title = title;
        this.item_id = item_id;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItemId() { return item_id; }

    public void setItemId(String item_id) { this.item_id = item_id; }
}