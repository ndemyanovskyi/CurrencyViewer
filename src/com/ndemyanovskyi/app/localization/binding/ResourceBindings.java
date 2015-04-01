/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.app.localization.binding;

import com.ndemyanovskyi.app.Settings;
import com.ndemyanovskyi.app.localization.Language;
import com.ndemyanovskyi.app.localization.binding.annotation.BooleanResource;
import com.ndemyanovskyi.app.localization.binding.annotation.ImageResource;
import com.ndemyanovskyi.app.localization.binding.annotation.NumberResource;
import com.ndemyanovskyi.app.localization.binding.annotation.StringResource;
import com.ndemyanovskyi.app.res.Resources;
import com.ndemyanovskyi.map.unmodifiable.UnmodifiableMap;
import com.ndemyanovskyi.reflection.Types;
import com.ndemyanovskyi.throwable.Exceptions;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ResourceBindings<T> extends UnmodifiableMap<String, ReadOnlyObjectProperty<T>> {

    private final Map<String, WritableProperty<T>> beans = new HashMap<>();

    private final Function<Language, Resources<T>> resourcesFactory;
    private Resources<T> resources;

    public ResourceBindings(Function<Language, Resources<T>> resourcesFactory) {
        super(new HashMap<>());

        this.resourcesFactory = Objects.requireNonNull(resourcesFactory, "resourcesFactory");
        this.resources = resourcesFactory.apply(Settings.LANGUAGE.getValue());

        for(String key : resources.keySet()) {
            WritableProperty p = new WritableProperty<>(this, key);
            base().put(key, p.getReadOnlyProperty());
            beans.put(key, p);
        }

        Settings.LANGUAGE.addListener((a, b, c) -> {
            if(b != c) {
                this.resources = resourcesFactory.apply(c);

                for(WritableProperty<?> p : beans.values()) {
                    p.fireValueChangedEvent();
                }
            }
        });
    }

    protected ReadOnlyObjectWrapper<T> getWritableProperty(Object key) {
        return beans.get(key);
    }

    @Override
    public ReadOnlyObjectProperty<T> get(Object key) {
        ReadOnlyObjectProperty<T> p = super.get(key);
        if(p == null) {
            throw new IllegalArgumentException(
                    "Property with key '" + key + "' does not found.");
        }
        return p;
    }

    public Language getLanguage() {
        return Settings.LANGUAGE.getValue();
    }

    protected Function<Language, Resources<T>> getResourcesFactory() {
        return resourcesFactory;
    }

    public Resources<T> getResources() {
        return resources;
    }

    private static class WritableProperty<T> extends ReadOnlyObjectWrapper<T> {

        private final ResourceBindings<T> resourceBindings;

        protected WritableProperty(ResourceBindings<T> resourceBindings, String key) {
            super(null, key);
            this.resourceBindings = Objects.
                    requireNonNull(resourceBindings, "resourceBindings");
        }

        public ResourceBindings<T> getResourceBindings() {
            return resourceBindings;
        }

        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }

        @Override
        public T get() {
            return getResourceBindings().getResources().get(getName());
        }

    }

    public static StringResourceBindings strings() {
        return Translation.getInstance().getStringBindings();
    }

    public static BooleanResourceBindings booleans() {
        return Translation.getInstance().getBooleanBindings();
    }

    public static NumberResourceBindings numbers() {
        return Translation.getInstance().getNumberBindings();
    }

    public static ImageResourceBindings images() {
        return Translation.getInstance().getImageBindings();
    }

    public static void register(Object object) {
        Objects.requireNonNull(object, "object");
        Exceptions.execute(() -> {
            Object o = object;
            registerByAnnotations(o);
            if(o instanceof Window) {
                if(o instanceof Stage) {
                    registerProperty(((Stage) o).titleProperty());
                }
                o = ((Window) o).getScene();
            }
            if(o instanceof Scene) o = ((Scene) o).getRoot();
            if(o instanceof Node) registerNode((Node) o);
        });
    }

    private static void registerByAnnotations(Object o) throws Exception {
        Class<?> c = o.getClass();
        while(c != Object.class) {
            for(Field field : o.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                Object value = field.get(o);

                ImageResource ir = field.getAnnotation(ImageResource.class);
                StringResource sr = field.getAnnotation(StringResource.class);
                NumberResource nr = field.getAnnotation(NumberResource.class);
                BooleanResource br = field.getAnnotation(BooleanResource.class);

                if(checkAnnotations(ir, sr, nr, br) != null) {
                    if(ir != null) registerImageProperty(field, value, ir.value());
                    if(sr != null) registerStringProperty(field, value, sr.value());
                    if(nr != null) registerNumberProperty(field, value, nr.value());
                    if(br != null) registerBooleanProperty(field, value, br.value());
                }
            }
            c = c.getSuperclass();
        }
    }

    private static Annotation checkAnnotations(Annotation... annotations) {
        Annotation res = null;
        for(Annotation a : annotations) {
            if(a != null) {
                if(res == null) {
                    res = a;
                }else {
                    throw new IllegalStateException("Field can`t be contains annotations "
                            + res.annotationType().getSimpleName() + " and " + a.annotationType().getSimpleName() + "");
                }
            }
        }

        return res;
    }

    private static void registerImageProperty(Field field, Object value, String resourceName) {
        ReadOnlyProperty<Image> image = images().get(resourceName);
        Class<?> c = field.getClass();

        if(Property.class.isAssignableFrom(c)) {
            ((Property) value).bind(image);
        }else if(ImageView.class.isAssignableFrom(c)) {
            ((ImageView) value).imageProperty().bind(image);
        }else {
            throw new IllegalArgumentException(
                    "ImageResource annotation does not support field " + field);
        }
    }

    private static void registerStringProperty(Field field, Object value, String resourceName) {
        ReadOnlyProperty<String> string = strings().get(resourceName);
        Class<?> c = field.getType();
        if(Property.class.isAssignableFrom(c)) {
            ((Property) value).bind(string);
        }else if(Labeled.class.isAssignableFrom(c)) {
            ((Labeled) value).textProperty().bind(string);
        }else {
            throw new IllegalArgumentException(
                    "StringResource annotation does not support field " + field);
        }
    }

    private static void registerBooleanProperty(Field field, Object value, String resourceName) {
        ReadOnlyObjectWrapper<Boolean> bool = booleans().getWritableProperty(resourceName);
        Class<?> c = field.getType();
        if(Property.class.isAssignableFrom(c)) {
            ParameterizedType type = (ParameterizedType) Types.resolveGenericType(Property.class, c);
            if(type != null) {
                Type genericType = type.getActualTypeArguments()[0];
                if(genericType instanceof Class) {
                    Class genericClass = (Class) genericType;

                    if(String.class.isAssignableFrom(genericClass)) {
                        ((Property<String>) value).bind(bool.asString());
                    }else if(Boolean.class.isAssignableFrom(genericClass)) {
                        ((Property<Boolean>) value).bind(bool);
                    }else {
                        throw new IllegalArgumentException(
                                "BooleanResource annotation does not support property with generic type " + genericClass);
                    }
                }else {
                    ((Property) value).bind(bool);
                }
            }else {
                ((Property) value).bind(bool);
            }
        }else if(Labeled.class.isAssignableFrom(c)) {
            ((Labeled) value).textProperty().bind(bool.asString());
        }else {
            throw new IllegalArgumentException(
                    "BooleanResource annotation does not support field " + field);
        }
    }

    private static void registerNumberProperty(Field field, Object value, String resourceName) {
        ReadOnlyObjectWrapper<Number> number = numbers().getWritableProperty(resourceName);
        Class<?> c = field.getType();

        if(Property.class.isAssignableFrom(c)) {
            ParameterizedType type = (ParameterizedType) Types.resolveGenericType(Property.class, c);
            if(type != null) {
                Type genericType = type.getActualTypeArguments()[0];
                if(genericType instanceof Class) {
                    Class genericClass = (Class) genericType;

                    if(String.class.isAssignableFrom(genericClass)) {
                        ((Property<String>) value).bind(number.asString());
                    }else if(Boolean.class.isAssignableFrom(genericClass)) {
                        ((Property<Number>) value).bind(number);
                    }else {
                        throw new IllegalArgumentException(
                                "NumberResource annotation does not support property with generic type " + genericClass);
                    }
                }else {
                    ((Property) value).bind(number);
                }
            }else {
                ((Property) value).bind(number);
            }
        }else if(Labeled.class.isAssignableFrom(c)) {
            ((Labeled) value).textProperty().bind(number.asString());
        }else {
            throw new IllegalArgumentException(
                    "NumberResource annotation does not support field " + field);
        }
    }
    
    private static void registerProperty(Property<String> property) {
        String text = property.getValue();
        if(text != null) {
            StringExpression exp
                    = StringExpressionParser.parse(text);
            if(exp != null) {
                property.unbind();
                property.bind(exp);
            }
        }
    }
    
    private static void registerNode(Node node) {
        if(node instanceof Control) {
            Tooltip tooltip = ((Control) node).getTooltip();
            if(tooltip != null) {
                registerProperty(tooltip.textProperty());
            }
            if(node instanceof Labeled) {
                registerProperty(((Labeled) node).textProperty());
            }
        } else if(node instanceof Parent) {
            registerParent((Parent) node);
        }
    }

    private static void registerParent(Parent parent) {
        for(Node node : parent.getChildrenUnmodifiable()) {
            registerNode(node);
        }
    }

}
