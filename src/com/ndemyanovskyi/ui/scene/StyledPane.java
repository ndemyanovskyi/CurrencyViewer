/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.scene;

import com.ndemyanovskyi.ui.pane.InitializableBorderPane;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author Назарій
 */
public class StyledPane extends InitializableBorderPane {

    @FXML private BorderPane root;
    @FXML private BorderPane center;
    @FXML private BorderPane centerTop;
    @FXML private Pane topLeft;
    @FXML private Pane top;
    @FXML private Pane topRight;
    @FXML private Pane left;
    @FXML private Pane bottomLeft;
    @FXML private Pane bottom;
    @FXML private Pane bottomRight;
    @FXML private Pane right;
    @FXML private Pane move;
    
    private final Parent content;
    private final Parent actionBar;

    private MouseEvent pressedEvent = null;

    public StyledPane(Parent content, Parent actionBar) {
	this.content = Objects.requireNonNull(content, "content");
	this.actionBar = actionBar;
	center.setCenter(content);
	if(actionBar != null) centerTop.setLeft(actionBar);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public Parent getContent() {
	return content;
    }

    public Parent getActionBar() {
	return actionBar;
    }

    @FXML
    private void onMousePressed(MouseEvent e) {
	pressedEvent = e;
    }

    private void resizeRight(Point2D point) {
	double width = point.getX();
	double minWidth = content.minWidth(0);
	double maxWidth = content.maxWidth(0);
	if(width < minWidth) width = minWidth;
	if(width > maxWidth) width = maxWidth;
	root.setPrefWidth(width);
    }

    @FXML
    private void onMoveMouseDragged(MouseEvent e) {
	double x = e.getScreenX() - pressedEvent.getSceneX();
	double y = e.getScreenY() - pressedEvent.getSceneY();
	Point2D point = normalizeXY(x, y);
	
	getScene().getWindow().setX(point.getX());
	getScene().getWindow().setY(point.getY());
    }

    @FXML
    private void onTopMouseDragged(MouseEvent e) {
	Window window = getScene().getWindow();
	
	double y = e.getScreenY() - pressedEvent.getSceneY();
	double height = window.getHeight() - (y - window.getY());
	double minHeight = root.minHeight(0);
	double maxHeight = root.maxHeight(0);

	if(height > maxHeight) {
	    y -= (maxHeight - height);
	    height = maxHeight;
	}
	if(height < minHeight) {
	    y -= (minHeight - height);
	    height = minHeight;
	}
	
	window.setY(normalizeY(y));
	window.setHeight(normalizeHeight(height));
	e.consume();
    }

    @FXML
    private void onTopLeftMouseDragged(MouseEvent e) {
	Window window = getScene().getWindow();

	double y = e.getScreenY() - pressedEvent.getSceneY();
	double height = window.getHeight() - (y - window.getY());
	double minHeight = root.minHeight(0);
	double maxHeight = root.maxHeight(0);

	if(height > maxHeight) {
	    y -= (maxHeight - height);
	    height = maxHeight;
	}
	if(height < minHeight) {
	    y -= (minHeight - height);
	    height = minHeight;
	}

	double x = e.getScreenX() - pressedEvent.getSceneX();
	double width = window.getWidth() - (x - window.getX());
	double minWidth = root.minWidth(0);
	double maxWidth = root.maxWidth(0);

	if(width > maxWidth) {
	    x -= (maxWidth - width);
	    width = maxWidth;
	}
	if(width < minWidth) {
	    x -= (minWidth - width);
	    width = minWidth;
	}

	Point2D point = normalizeXY(x, y);
	window.setX(point.getX());
	window.setY(point.getY());
	window.setWidth(normalizeWidth(width));
	window.setHeight(normalizeHeight(height));
	e.consume();
    }

    @FXML
    private void onTopRightMouseDragged(MouseEvent e) {
	Window window = getScene().getWindow();
	
	double y = e.getScreenY() - pressedEvent.getSceneY();
	double height = window.getHeight() - (y - window.getY());
	double minHeight = root.minHeight(0);
	double maxHeight = root.maxHeight(0);

	if(height > maxHeight) {
	    y -= (maxHeight - height);
	    height = maxHeight;
	}
	if(height < minHeight) {
	    y -= (minHeight - height);
	    height = minHeight;
	}
	
	Point2D point = right.localToScene(
		e.getX() + pressedEvent.getX(),
		e.getY() + pressedEvent.getY());

	double width = point.getX();
	double minWidth = root.minWidth(0);
	double maxWidth = root.maxWidth(0);
	if(width < minWidth) width = minWidth;
	if(width > maxWidth) width = maxWidth;
	
	window.setY(normalizeY(y));
	window.setHeight(normalizeHeight(height));
	window.setWidth(normalizeWidth(width));
	e.consume();
    }
    
    private Point2D normalizeXY(double x, double y) {
	final Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
	final double offset = centerTop.localToScene(0, centerTop.getHeight()).getY();
	final Bounds moveBounds = move.localToScene(move.getBoundsInLocal());
	final double rightOffset = moveBounds.getMinX() + offset;
	final double leftOffset = moveBounds.getMaxX() - offset;
	
	if(y < bounds.getMinY()) y = bounds.getMinY();
	if(y + offset > bounds.getMaxY()) y = bounds.getMaxY() - offset;
	if(x + leftOffset < bounds.getMinX()) x = bounds.getMinX() - leftOffset;
	if(x + rightOffset > bounds.getMaxX()) x = bounds.getMaxX() - rightOffset;
	
	return new Point2D(x, y);
    }
    
    private double normalizeX(double x) {
	return normalizeXY(x, 0).getX();
    }
    
    private double normalizeY(double y) {
	return normalizeXY(0, y).getY();
    }
    
    private double normalizeWidth(double width) {
	Rectangle2D rect = Screen.getPrimary().getVisualBounds();
	if(width > rect.getWidth()) width = rect.getWidth();
	return width;
    }
    
    private double normalizeHeight(double height) {
	Rectangle2D rect = Screen.getPrimary().getVisualBounds();
	if(height > rect.getHeight()) height = rect.getHeight();
	return height;
    }

    @FXML
    private void onBottomLeftMouseDragged(MouseEvent e) {
	Window window = getScene().getWindow();

	double x = e.getScreenX() - pressedEvent.getSceneX();
	double width = window.getWidth() - (x - window.getX());
	double minWidth = root.minWidth(0);
	double maxWidth = root.maxWidth(0);

	if(width > maxWidth) {
	    x -= (maxWidth - width);
	    width = maxWidth;
	}
	if(width < minWidth) {
	    x -= (minWidth - width);
	    width = minWidth;
	}
	
	Point2D point = bottom.localToScene(
		e.getX() + pressedEvent.getX(),
		e.getY() + pressedEvent.getY());

	double height = point.getY();
	double minHeight = root.minHeight(0);
	double maxHeight = root.maxHeight(0);
	if(height < minHeight) height = minHeight;
	if(height > maxHeight) height = maxHeight;
	
	window.setX(normalizeX(x));
	window.setWidth(normalizeWidth(width));
	window.setHeight(normalizeHeight(height));
	e.consume();
    }

    @FXML
    private void onLeftMouseDragged(MouseEvent e) {
	Window window = getScene().getWindow();

	double x = e.getScreenX() - pressedEvent.getSceneX();
	double width = window.getWidth() - (x - window.getX());
	double minWidth = root.minWidth(0);
	double maxWidth = root.maxWidth(0);

	if(width > maxWidth) {
	    x -= (maxWidth - width);
	    width = maxWidth;
	}
	if(width < minWidth) {
	    x -= (minWidth - width);
	    width = minWidth;
	}

	window.setX(normalizeX(x));
	window.setWidth(normalizeWidth(width));
	e.consume();
    }

    @FXML
    private void onBottomMouseDragged(MouseEvent e) {
	Point2D point = bottom.localToScene(
		e.getX() + pressedEvent.getX(),
		e.getY() + pressedEvent.getY());

	double height = point.getY();
	double minHeight = root.minHeight(0);
	double maxHeight = root.maxHeight(0);
	if(height < minHeight) height = minHeight;
	if(height > maxHeight) height = maxHeight;
	getScene().getWindow().setHeight(normalizeHeight(height));
	e.consume();
    }

    @FXML
    private void onRightMouseDragged(MouseEvent e) {
	Point2D point = right.localToScene(
		e.getX() + pressedEvent.getX(),
		e.getY() + pressedEvent.getY());
	Window window = getScene().getWindow();

	double width = point.getX();
	double minWidth = root.minWidth(0);
	double maxWidth = root.maxWidth(0);
	if(width < minWidth) width = minWidth;
	if(width > maxWidth) width = maxWidth;
	window.setWidth(normalizeWidth(width));
	e.consume();
    }

    @FXML
    private void onBottomRightMouseDragged(MouseEvent e) {
	Point2D point = bottomRight.localToScene(
		e.getX() + pressedEvent.getX(),
		e.getY() + pressedEvent.getY());

	double width = point.getX();
	double minWidth = root.minWidth(0);
	double maxWidth = root.maxWidth(0);
	if(width < minWidth) width = minWidth;
	if(width > maxWidth) width = maxWidth;
	getScene().getWindow().setWidth(width);

	double height = point.getY();
	double minHeight = root.minHeight(0);
	double maxHeight = root.maxHeight(0);
	if(height < minHeight) height = minHeight;
	if(height > maxHeight) height = maxHeight;
	getScene().getWindow().setHeight(normalizeHeight(height));
	e.consume();
    }

    @FXML
    private void onMinimizeButtonAction(ActionEvent e) {
	((Stage) getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void onExitButtonAction(ActionEvent e) {
	root.setOpacity(0);
	((Stage) getScene().getWindow()).setIconified(true);
	new Thread(getApplication()::stop).start();
    }

}
