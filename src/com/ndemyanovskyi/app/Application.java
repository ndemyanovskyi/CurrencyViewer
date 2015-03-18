/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.app;

import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import com.ndemyanovskyi.app.localization.binding.Translation;
import com.ndemyanovskyi.reflection.Types;
import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public final class Application extends javafx.application.Application {
    
    private static final Logger LOG = Logger.getLogger(Application.class.getName());

    private static Application instance;

    private final Settings settings = Settings.getInstance();
    private final Manifest manifest = Manifest.getInstance();
    private final Translation translation = Translation.getInstance();

    private Stage mainStage;
    private Parent content;

    private BooleanProperty trayIconEnabled;
    private OnErrorProperty onError;
    
    private ResourceRegistrator resourceRegistrator;

    public Application() {
        instance = this;
    }

    public static Application getInstance() {
        return instance;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public boolean isTrayIconEnabled() {
        return trayIconEnabled != null ? trayIconEnabled.get() : false;
    }

    public void setTrayIconEnabled(boolean trayIconEnabled) {
        trayIconEnabledProperty().set(trayIconEnabled);
    }

    public EventHandler<? super ErrorEvent> getOnError() {
        return onErrorProperty().get();
    }

    public void setOnError(EventHandler<? super ErrorEvent> onError) {
        onErrorProperty().set(onError);
    }

    public BooleanProperty trayIconEnabledProperty() {
        if(this.trayIconEnabled == null) {
            this.trayIconEnabled
                    = new SimpleBooleanProperty(this, "trayIconEnabled", false);
        }
        return trayIconEnabled;
    }

    public ObjectProperty<EventHandler<? super ErrorEvent>> onErrorProperty() {
        if(this.onError == null) {
            this.onError = new OnErrorProperty();
        }
        return onError;
    }

    @Override
    public void init() throws Exception {
        checkSecondInit(content);
        content = manifest.get(Manifest.MAIN_CONTENT_TYPE).newInstance();
        initErrorHandler();
    }

    private void initErrorHandler() throws Exception {
        Class<EventHandler<? super ErrorEvent>> type = manifest.get(Manifest.ERROR_HANDLER_TYPE);

        if(type == null) {
            if(content instanceof EventHandler) {
                Type t = Types.resolveGenericType(EventHandler.class, content.getClass());
                if(t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) t;
                    Type arg = pt.getActualTypeArguments()[0];
                    if(arg instanceof Class
                            && ((Class<?>) arg).isAssignableFrom(ErrorEvent.class)) {
                        setOnError((EventHandler<? super ErrorEvent>) content);
                    }
                }
            }
        }else if(type.equals(content.getClass())) {
            setOnError((EventHandler<? super ErrorEvent>) content);
        }else {
            setOnError(type.newInstance());
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        checkSecondInit(mainStage);
        try {            
            mainStage = stage;
            StageStyle style = manifest.get(Manifest.MAIN_STAGE_STYLE);
            if(style != null) stage.initStyle(style);
            Scene scene = new Scene(content);
            stage.setScene(scene);
            
            ReadOnlyProperty<Image> icon = manifest.get(Manifest.APP_ICON_RESOURCE);
            if(icon != null) {
                stage.getIcons().add(0, icon.getValue());
            }
            ReadOnlyProperty<String> name = manifest.get(Manifest.APP_NAME_RESOURCE);
            if(name != null) {
                stage.titleProperty().bind(name);
            }
            stage.show();
            
            resourceRegistrator = new ResourceRegistrator();
        } catch(Throwable ex) {
            LOG.log(Level.SEVERE, "Error in application start: ", ex);
        }
    }

    public Translation getTranslation() {
        return translation;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public Settings getSettings() {
        return settings;
    }

    @Override
    public void stop() throws Exception {
        settings.flush();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        checkSecondInit(instance);
        launch(args);
    }

    public static void execute(Runnable r) {
        if(Platform.isFxApplicationThread()) {
            r.run();
        }else {
            Platform.runLater(r);
        }
    }

    private static void checkSecondInit(Object checker) {
        if(checker != null) {
            throw new IllegalStateException(
                    "This method can`t be launched secondly.");
        }
    }

    private static void initTrayIcon(Stage stage, boolean enable) throws AWTException {
        SystemTray.getSystemTray().add(new TrayIcon(SwingFXUtils.fromFXImage(null, null), STYLESHEET_MODENA, null));
    }

    public static Window getFocusedWindow() {
        Iterator<Window> it = Window.impl_getWindows();
        return it.hasNext() ? it.next() : null;
    }

    private static class OnErrorProperty extends SimpleObjectProperty<EventHandler<? super ErrorEvent>> {

        private final UncaughtExceptionHandler exceptionHandler
                = (thread, throwable) -> {
                    ErrorEvent event = ErrorEvent.create(thread, throwable);
                    execute(() -> get().handle(event));
                };

        @Override
        public void set(EventHandler<? super ErrorEvent> newValue) {
            super.set(newValue);
            Thread.setDefaultUncaughtExceptionHandler(
                    newValue != null ? exceptionHandler : null);
        }

    }

    private class ResourceRegistrator implements ChangeListener<Boolean>, EventHandler<WindowEvent> {

        public ResourceRegistrator() {
            register(mainStage);
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> property, Boolean old, Boolean current) {
            if(!current) register(getFocusedWindow());
        }
        
        private void register(Window window) {
            if(window != null) {
                window.addEventHandler(WindowEvent.WINDOW_SHOWN, this);
                window.focusedProperty().addListener(this);
                ResourceBindings.register(window);
            }
        }

        @Override
        public void handle(WindowEvent event) {
            ResourceBindings.register(event.getSource());
        }

    }

}
