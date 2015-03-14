/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.res;

import com.ndemyanovskyi.app.localization.Language;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javafx.scene.image.Image;


public final class ImageResources extends FileResources<Image> {
    
    ImageResources(Language language) {
	super(language, "images");
    }

    @Override
    protected Image read(File file) throws IOException {
	return new Image(new FileInputStream(file));
    }

    /*@Override
    protected void write(File file, Image resource) throws IOException {
	BufferedImage image = SwingFXUtils.fromFXImage(resource, null);
	ImageIO.write(image, getExtension(file), file);
    }
    
    private static String getExtension(File file) {
	String name = file.getName();
	int index = name.lastIndexOf('.');
	return index != -1 ? name.substring(index) : name;
    }*/

    @Override
    public FileResources<Image> getParentResources() {
	Language parent = getLanguage().parent();
	return parent != null ? Resources.images(parent) : null;
    }

    @Override
    public FileResources<Image> getDefaultResources() {
	return Resources.images();
    }

}
