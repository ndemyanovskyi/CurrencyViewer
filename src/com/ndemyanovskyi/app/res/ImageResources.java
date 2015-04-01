/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.res;

import com.ndemyanovskyi.app.localization.Language;
import com.ndemyanovskyi.util.BiConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Predicate;
import javafx.scene.image.Image;


public final class ImageResources extends FileResources<Image> {
    
    private static final String EXTENSION = "png";
    
    private static final Predicate<String> PREDICATE = key -> key.toLowerCase().endsWith("." + EXTENSION);
    
    private static final BiConverter<String, String> KEY_CONVERTER = new BiConverter<String, String>() {

        @Override
        public String to(String key) {
            if(PREDICATE.test(key)) {
                return key.substring(0, key.length() - EXTENSION.length() - 1);
            }
            return key;
        }

        @Override
        public String from(String key) {
            if(!PREDICATE.test(key)) {
                return key + "." + EXTENSION;
            }
            return key;
        }
    };
    
    ImageResources(Language language) {
	super(language, "images", PREDICATE, KEY_CONVERTER);
    }

    @Override
    protected Image readFile(File file) throws IOException {
	return new Image(new FileInputStream(file), 0, 0, false, true);
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
