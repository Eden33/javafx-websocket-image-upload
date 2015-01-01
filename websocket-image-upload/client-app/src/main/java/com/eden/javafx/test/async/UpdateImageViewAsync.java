package com.eden.javafx.test.async;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author edi
 */
public class UpdateImageViewAsync implements Runnable {
    
    private Image img = null;
    private ImageView view = null;
    
    public UpdateImageViewAsync(ImageView view, Image img) {
        if(null == img) {
            throw new IllegalArgumentException("Image must not be null!");
        }
        if(null == view) {
            throw new IllegalArgumentException("ImageView must not be null!");    
        }
        this.img = img;
        this.view = view;
    }
    
    @Override
    public void run() {
        view.setImage(img);
    }
}
