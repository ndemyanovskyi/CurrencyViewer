
package com.ndemyanovskyi.app;

import java.util.List;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.stage.Screen;

/**
 * @author Nazariy Demyanovskyi
 */
public class Scaling {
    
    public static final Scaling INSTANCE = new Scaling();
    
    private final ReadOnlyDoubleWrapper scale = new ReadOnlyDoubleWrapper(this, "scale", 1);
    
    private Scaling() {
	Platform.runLater(() -> {
	    Screen.getScreens().addListener(
		    (Observable e) -> scale.set(calculateScale()));
	    scale.set(calculateScale());
	});
    }
    
    private double calculateScale() {
	List<Screen> screens = Screen.getScreens();
	int count = 0;
	double sum = 0;
	for(Screen screen : screens) {
	    sum += screen.getDpi();
	    count++;
	}
	return count > 0 ? sum / count / 96 : 1;
    }
    
    public double getScale() {
	return scale.get();
    }
    
    public ReadOnlyDoubleProperty scaleProperty() {
	return scale.getReadOnlyProperty();
    }

}
