package com.ndemyanovskyi.ui.pane.main.chart;

import com.ndemyanovskyi.app.Application;
import static com.ndemyanovskyi.app.Constants.MINIMAL_PERIOD;
import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import com.ndemyanovskyi.app.res.Resources;
import com.ndemyanovskyi.backend.Bank;
import com.ndemyanovskyi.backend.Currency;
import com.ndemyanovskyi.backend.DataManager;
import com.ndemyanovskyi.backend.Rate;
import com.ndemyanovskyi.backend.Rate.Field;
import com.ndemyanovskyi.backend.RateList;
import com.ndemyanovskyi.backend.Task;
import com.ndemyanovskyi.collection.FilteredCollection;
import com.ndemyanovskyi.time.Period;
import com.ndemyanovskyi.time.Range;
import com.ndemyanovskyi.ui.anim.OpacityAnimator;
import com.ndemyanovskyi.ui.anim.TranslateAnimator;
import com.ndemyanovskyi.ui.pane.InitializableBorderPane;
import com.ndemyanovskyi.ui.pane.main.chart.Description.Item;
import com.ndemyanovskyi.ui.pane.main.chart.legend.Legend;
import com.ndemyanovskyi.ui.toast.Toast;
import com.ndemyanovskyi.util.BiConverter;
import com.ndemyanovskyi.util.Compare;
import static com.ndemyanovskyi.util.Compare.less;
import com.ndemyanovskyi.util.Convert;
import static com.ndemyanovskyi.util.Convert.toDate;
import static com.ndemyanovskyi.util.Convert.toLocalDate;
import com.ndemyanovskyi.util.beans.ConvertedBinding;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ChartPane extends InitializableBorderPane {
    
    @FXML private XYChart<Date, Float> chart;
    @FXML private DateAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private HBox legendPane;
    
    @FXML private Pane centerPane;
    
    @FXML private Line line;
    @FXML private Region chartContent;
    
    @FXML private Pane descriptionPane;
    @FXML private Pane eventedPane;
    @FXML private Description description;
    
    private Popup popup;

    private IntentsManager intentsManager;
    private Range range;

    private IntentsManager getIntentsManager() {
        return intentsManager != null ? intentsManager
                : (intentsManager = new IntentsManager());
    }

    public ReadOnlyObjectProperty<Intents> intentsProperty() {
        return getIntentsManager().intentsProperty();
    }

    public Intents getIntents() {
        return intentsProperty().get();
    }

    private void initChart() {
        chartContent = (Region) ((Parent) chart.
                getChildrenUnmodifiable().get(1)).
                getChildrenUnmodifiable().get(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ResourceBindings.register(this);
        
        try {
            System.out.println(DataManager.loadRate(
                    Bank.SBER_BANK, Currency.EUR, LocalDate.parse("2014-06-01")));
        } catch(IOException ex) {
            ex.printStackTrace();;
        }
        
        range = MINIMAL_PERIOD.getValue().toRange();
        MINIMAL_PERIOD.addListener(p -> updateRange());
        
        xAxis.setLowerBound(toDate(MINIMAL_PERIOD.getValue().first()));
        xAxis.setUpperBound(toDate(MINIMAL_PERIOD.getValue().last()));
        
        ConvertedBinding.bind(
                fromDatePicker.valueProperty(), xAxis.lowerBoundProperty(), 
                BiConverter.of(Convert::toDate, Convert::toLocalDate));
        
        ConvertedBinding.bind(
                toDatePicker.valueProperty(), xAxis.upperBoundProperty(), 
                BiConverter.of(Convert::toDate, Convert::toLocalDate));
          
        /*Timeline t = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            Settings.LANGUAGE.set(Language.values()[(int) (Math.random() * 3)]);
        }));
        t.setCycleCount(Animation.INDEFINITE);
        t.play();*/
        
        initChart();    
        
        chart.prefWidthProperty().bind(centerPane.widthProperty());
        chart.prefHeightProperty().bind(centerPane.heightProperty());
        
        eventedPane.layoutXProperty().bind(chart.layoutXProperty().add(yAxis.widthProperty()).add(15));
        eventedPane.layoutYProperty().bind(chart.layoutYProperty().add(15));
        eventedPane.prefWidthProperty().bind(chart.widthProperty().subtract(yAxis.widthProperty()).subtract(30));
        eventedPane.prefHeightProperty().bind(chart.heightProperty().subtract(xAxis.heightProperty()).subtract(30));
        
        description.layoutXProperty().bind(Bindings.createDoubleBinding(() -> {
            double margin = 20;
            double offset = 20;
            if(line.getStartX() + offset + margin > descriptionPane.getWidth()) {
                return descriptionPane.getWidth() - description.getWidth() - margin;
            }
            if(line.getStartX() - description.getWidth() + offset - margin < 0) {
                return margin;
            }
            return line.getStartX() - description.getWidth() + offset;
        }, line.startXProperty()));
                
        getIntents().add(Bank.NBU, Currency.EUR);
        getIntents().add(Bank.SBER_BANK, Currency.EUR, Rate.Field.BUY);
        getIntents().add(Bank.FIDOBANK, Currency.EUR, Rate.Field.BUY);
        getIntents().add(Bank.OSCHAD_BANK, Currency.EUR, Rate.Field.BUY);
        getIntents().add(Bank.OSCHAD_BANK, Currency.EUR, Rate.Field.SALE);
        getIntents().add(Bank.PUMB, Currency.EUR, Rate.Field.SALE);
    } 
    
    @FXML
    private void onKeyReleased(KeyEvent e) {
        if(e.getCode() == KeyCode.F11) {
            Stage stage = (Stage) getScene().getWindow();
            stage.setFullScreen(!stage.isFullScreen());
        }
        e.consume();
    }

    private void updateRange() {
        LocalDate first = LocalDate.MAX;
        LocalDate last = LocalDate.MIN;

        Collection<IntentsManager.SeriesController<?>> scs = new FilteredCollection<>(
                getIntentsManager().getSeriesControllers().values(), s -> s.getTask().getRateList() != null);
        if(scs.size() > 0) {
            for(IntentsManager.SeriesController<?> sc : scs) {
                Task<?> task = sc.getTask();
                Period p = task.getRateList().getPeriod();
                if(!p.isEmpty()) {
                    first = Compare.min(first, task.getRateList().getPeriod().first());
                    last = Compare.max(last, task.getRateList().getPeriod().last());
                }
            }
        }
        
        Period min = MINIMAL_PERIOD.getValue();
        
        if(first.isAfter(min.first())) {
            first = min.first();
        }
        if(last.isBefore(min.last())) {
            last = min.last();
        }
        range.set(first, last);
    }

    private class IntentsManager implements SetChangeListener<Intent<?>> {

        private final ReadOnlyObjectWrapper<Intents> intents = new ReadOnlyObjectWrapper<>(new Intents());

        private final Map<Intent<?>, SeriesController<?>> seriesControllers = new HashMap<>();

        private IntentsManager() {            
            intents.get().addListener(this);
        }

        public ReadOnlyObjectProperty<Intents> intentsProperty() {
            return intents.getReadOnlyProperty();
        }

        Map<Intent<?>, SeriesController<?>> getSeriesControllers() {
            return seriesControllers;
        }

        /**
         * Listen intent set changes
         * @param change non null.
         */
        @Override
        public void onChanged(Change<? extends Intent<?>> change) {
            Application.execute(() -> {
                if(change.wasAdded()) {
                    Intent<?> intent = change.getElementAdded();
                    seriesControllers.put(intent, new SeriesController<>(intent));
                }else if(change.wasRemoved()) {
                    seriesControllers.remove(
                            change.getElementRemoved()).dispose();
                }
                updateRange();
            });
        }

        private final class SeriesController<R extends Rate> implements ListChangeListener<R> {

            private final Item item;
            private final Intent<R> intent;
            private final Legend legend;
            private Task task;
            private final Series<Date, Float> series;
            private OpacityAnimator seriesAnimator;
            private final TranslateAnimator legendAnimator;

            private boolean disposed;

            public SeriesController(Intent<R> intent) {
                this.intent = Objects.requireNonNull(intent, "intent");
                this.legend = new Legend(intent, e -> dispose());
                this.series = new Series<>();
                this.task = new Task();
                this.item = new Item(intent);
                
                description.getData().add(item);
                legendPane.getChildren().add(legend);
                
                legendAnimator = new TranslateAnimator(Duration.millis(200), legend);
            }

            public TranslateAnimator getLegendAnimator() {
                return legendAnimator;
            }

            public OpacityAnimator getSeriesAnimator() {
                return seriesAnimator;
            }

            @Override
            public boolean equals(Object o) {
                return o == this || (o instanceof SeriesController 
                        && ((SeriesController<?>) o).getIntent().equals(getIntent()));
            }

            public Task getTask() {
                return task;
            }

            public Item getItem() {
                return item;
            }

            public Series<Date, Float> getSeries() {
                return series;
            }

            public Intent<R> getIntent() {
                return intent;
            }

            public Legend getLegend() {
                return legend;
            }

            public boolean isDisposed() {
                return disposed;
            }

            public void dispose() {
                if(!disposed) {
                    disposed = true;
                    
                    seriesControllers.remove(getIntent());
                    description.getData().remove(item);
                    chart.getData().remove(getSeries());
                    legendPane.getChildren().remove(getLegend());
                    if(task.isRunning()) task.cancel();
                    updateRange();
                }
            }
            
            private int indexFor(List<Data<Date, Float>> list, Rate rate) {
                Date date = toDate(rate.getDate());
                for(int j = 0; j < list.size(); j++) {
                    Data<Date, Float> data = list.get(j);
                    if(less(date, data.getXValue())) {
                        return j;
                    }
                }
                return list.size();
            }

            @Override
            public void onChanged(Change<? extends R> change) {
                while(change.next()) {
                    if(change.wasAdded()) {
                        for(int i = change.getFrom(); i < change.getTo(); i++) {
                            R rate = change.getList().get(i);
                            Float value = rate.get(intent.getField());
                            if(!value.isNaN()) {
                                List<Data<Date, Float>> list = series.getData();
                                Date date = toDate(rate.getDate());
                                       
                                int index = indexFor(list, rate);
                                //When animation enabled, weird NPE throws when new data adding to series
                                Application.execute(() -> {
                                    series.getData().add(index, new Data<>(date, value));
                                });
                            }
                        }
                    }else {
                        throw new IllegalStateException(
                                "Unupported change of RateList: " + change);
                    }
                }
                updateRange();
            }

            @Override
            public int hashCode() {
                int hash = 3;
                hash = 41 * hash + Objects.hashCode(this.intent);
                hash = 41 * hash + Objects.hashCode(this.legend);
                hash = 41 * hash + Objects.hashCode(this.task);
                hash = 41 * hash + Objects.hashCode(this.series);
                hash = 41 * hash + Objects.hashCode(this.seriesAnimator);
                hash = 41 * hash + (this.disposed ? 1 : 0);
                return hash;
            }

            //<editor-fold defaultstate="collapsed" desc="Task class">
            private class Task extends com.ndemyanovskyi.backend.Task<R> {
                
                public Task() {
                    super(intent.getBank(), intent.getCurrency());
                    start();
                }
                
                @Override
                protected void onLoadFromDatabaseSuccess(RateList<R> list) {
                    legend.setProgress(0.5);
                    for(int i = 0; i < list.size(); i++) {
                        R rate = list.get(i);
                        Float value = rate.get(intent.getField());
                        if(!value.isNaN()) {
                            System.out.println(rate);
                            series.getData().add(new Data<>(toDate(rate.getDate()), value));
                        }
                    }
                    chart.getData().add(series);
                    seriesAnimator = new OpacityAnimator(Duration.millis(200), series.getNode());
                    setSeriesColor(series, legend.getIntent().getColor());
                    legend.addEventHandler(MouseEvent.MOUSE_ENTERED, ChartPane.this::onLegendMouseEntered);
                    legend.addEventHandler(MouseEvent.MOUSE_EXITED, ChartPane.this::onLegendMouseExited);
                    
                    list.addListener(SeriesController.this);
                    updateRange();
                }
                
                private void setSeriesColor(Series<?, ?> series, Color color) {
                    Path fill = (Path) series.getNode().lookup(".chart-series-area-fill");
                    Path line = (Path) series.getNode().lookup(".chart-series-area-line");
                    
                    String rgb = String.format("%d, %d, %d",
                            (int) (color.getRed() * 255),
                            (int) (color.getGreen() * 255),
                            (int) (color.getBlue() * 255));
                    
                    if(fill != null) fill.setStyle("-fx-fill: rgba(" + rgb + ", 0.15);");
                    if(line != null) line.setStyle("-fx-stroke: rgba(" + rgb + ", 1.0);");
                }
                
                @Override
                protected void onLoadFromDatabaseProgress(double progress) {
                    legend.setProgress(progress * 0.5);
                }
                
                @Override
                protected void onLoadFromInternetProgress(double progress) {
                    legend.setProgress(0.5 + (progress * 0.5));
                }
                
                @Override
                protected void onFinish(Result result) {
                    legend.setProgress(1.0);
                    updateRange();                    
                }
                
                @Override
                protected void onError(Cause cause) {
                    switch(cause.getType()) {
                        
                        case ERROR_INTERNET_CONNECTION:
                            Toast.of(ChartPane.this, "::{error_internet_connection}").show();
                            break;
                            
                        case DATABASE_LOCK_FAIL:
                            task = new Task();
                            break;
                            
                    }
                }

            }
            //</editor-fold>

        }
        
    }

    //<editor-fold defaultstate="collapsed" desc="Chart sensitive methods">    
    private final OpacityAnimator descriptionPaneAnimator = 
            new OpacityAnimator(Duration.millis(200), description.getBackgroundPane()); 
    private final OpacityAnimator opacityAnimator = 
            new OpacityAnimator(Duration.millis(200), line, description);
    private double previousX;
    private boolean dragged = false;
    
    @FXML 
    private void onLegendMouseEntered(MouseEvent e) {
        Map<Intent<?>, IntentsManager.SeriesController<?>> scs = intentsManager.getSeriesControllers();
        IntentsManager.SeriesController<?> entered = scs.get(((Legend) e.getSource()).getIntent());
        
        OpacityAnimator enteredAnimator = entered.getSeriesAnimator();
        if(enteredAnimator != null) {
            for(IntentsManager.SeriesController<?> sc : scs.values()) {
                OpacityAnimator tempAnimator = sc.getSeriesAnimator();
                if(sc != entered) {
                    if(tempAnimator != null) tempAnimator.play(0.1d);
                }
            }
            entered.getLegendAnimator().playY(-5);
            enteredAnimator.play(1.0d);
        }
    }
    
    @FXML 
    private void onLegendMouseExited(MouseEvent e) {
        Map<Intent<?>, IntentsManager.SeriesController<?>> scs = intentsManager.seriesControllers;
        for(IntentsManager.SeriesController<?> sc : scs.values()) {
            sc.getLegendAnimator().playY(0);
            OpacityAnimator animator = sc.getSeriesAnimator();
            if(animator != null) animator.play(1.0d);
        }
    }
    
    @FXML
    private void onEventedPaneMouseExited(MouseEvent e) {
        if(!dragged) {
            opacityAnimator.play(0.0d);
        }
    }
    
    @FXML
    private void onEventedPaneMouseEntered(MouseEvent e) {
        if(!dragged) {
            opacityAnimator.play(1.0d);
        }
    }
    
    @FXML
    private void onEventedPaneMouseMoved(MouseEvent e) {
        line.setStartX(e.getX());
        line.setEndX(e.getX());
        
        updateDescription(e.getX());
    }
    
    PopupWindow w;
    
    @FXML
    private void onEventedPaneMousePressed(MouseEvent e) {
        previousX = e.getX();
        getScene().setCursor(Cursor.MOVE);
    }
    
    @FXML
    private void onEventedPaneMouseReleased(MouseEvent e) {
        dragged = false;
        setCursor(Cursor.DEFAULT);
        if(!contains(e.getX(), e.getY())) {
            opacityAnimator.play(0.0d);
        }
    }
    
    @FXML
    private void onEventedPaneMouseDragged(MouseEvent e) {
        dragged = true;
        
        long offset = (long) ((previousX - e.getX()) / (xAxis.getWidth()
                / (xAxis.getUpperBound().getTime() - xAxis.getLowerBound().getTime())));
        
        long min = toDate(range.first()).getTime();
        long max = toDate(range.last()).getTime();
        
        if(xAxis.getLowerBound().getTime() + offset < min) {
            xAxis.setUpperBound(new Date(xAxis.getUpperBound().getTime() - xAxis.getLowerBound().getTime() + min));
            xAxis.setLowerBound(new Date(min));
        } else if(xAxis.getUpperBound().getTime() + offset > max) {
            long temp = xAxis.getUpperBound().getTime() - max;
            xAxis.setUpperBound(new Date(max));
            xAxis.setLowerBound(new Date(xAxis.getLowerBound().getTime() - temp));
        } else {
            xAxis.setLowerBound(new Date(xAxis.getLowerBound().getTime() + offset));
            xAxis.setUpperBound(new Date(xAxis.getUpperBound().getTime() + offset));
        }        
        
        previousX = e.getX();
        updateDescription(line.getStartX());
    }
    
    private static Toast.Builder toastBuilder(DatePicker picker) {
        return Toast.builder().
                    setOwner(picker).
                    setAlignment(Toast.Alignment.TOP_CENTER).
                    setOffset(0, picker.getHeight());
    }
    
    private boolean pickerValueChanging = false;
    
    @FXML
    private void onFromDatePickerValueChange(
            ObjectProperty<LocalDate> property, LocalDate oldDate, LocalDate newDate) {        
        
        if(newDate == null) return;

        int minDifference = Resources.numbers().
                getAsInteger("min_series_date_difference");

        if(newDate.isAfter(range.last())) {
            toastBuilder(fromDatePicker).
                    setText("::{warning_date_greater_then_upper_date}").show();
            property.set(range.last().minusDays(minDifference));
        }

        if(newDate.isBefore(range.first())) {
            toastBuilder(fromDatePicker).
                    setText("::{warning_date_less_then_lower_date}").show();
            property.set(range.first());
        }

        LocalDate to = toDatePicker.getValue();

        if(to != null && DAYS.between(newDate, to) < minDifference) {
            long oldBetween = DAYS.between(oldDate, to);
            LocalDate newTo = newDate.plusDays(oldBetween);
            if(newTo.isAfter(range.last())) {
                newTo = range.last();
                newDate = range.last().minusDays(minDifference);
            }
            
            if(!pickerValueChanging) {
                pickerValueChanging = true;
                fromDatePicker.setValue(newDate);
                toDatePicker.setValue(newTo);
                pickerValueChanging = false;
            }
        }
    }  
    
    @FXML
    private void onToDatePickerValueChange(
            ObjectProperty<LocalDate> property, LocalDate oldDate, LocalDate newDate) {
        
        if(newDate == null) return;
        
        int minDifference = Resources.numbers().
                getAsInteger("min_series_date_difference");
              
        if(newDate.isAfter(range.last())) {
            toastBuilder(toDatePicker).
                    setText("::{warning_date_greater_then_upper_date}").show();
            property.set(range.last());
        } 
        
        if(newDate.isBefore(range.first())) {
            toastBuilder(toDatePicker).
                    setText("::{warning_date_less_then_lower_date}").show();
            property.set(range.first().plusDays(minDifference));
        } 
        
        LocalDate from = fromDatePicker.getValue();
        
        if(from != null && DAYS.between(from, newDate) < minDifference) {
            long oldBetween = DAYS.between(from, oldDate);
            LocalDate newFrom = newDate.minusDays(oldBetween);
            if(newFrom.isBefore(range.first())) {
                newFrom = range.first();
                newDate = range.first().plusDays(minDifference);
            } 
            if(!pickerValueChanging) {
                pickerValueChanging = true;
                toDatePicker.setValue(newDate);
                fromDatePicker.setValue(newFrom);
                pickerValueChanging = false;
            }
        }
    }  
    
    @FXML
    private void onEventedPaneScroll(ScrollEvent e) {
        double coef = (xAxis.getUpperBound().getTime() - xAxis.getLowerBound().getTime()) / 5;
                
        Point2D pos = chartContent.sceneToLocal(e.getSceneX(), e.getSceneY());
        double width = chartContent.getBoundsInLocal().getWidth();
        double x = pos.getX();
        
        if(chartContent.contains(pos)) {
            double lowerZoom = (x / width) * coef;
            double upperZoom = ((width - x) / width) * coef;
            double lower;
            double upper;
            
            if(e.getDeltaY() > 0) {
                lower = xAxis.getLowerBound().getTime() + (1 * lowerZoom);
                upper = xAxis.getUpperBound().getTime() - (1 * upperZoom);
            }else {
                lower = xAxis.getLowerBound().getTime() - (1 * lowerZoom);
                upper = xAxis.getUpperBound().getTime() + (1 * upperZoom);
            }
            
            int minDifference = Resources.numbers().
                    getAsInteger("min_series_date_difference");
            
            if(upper - lower >= TimeUnit.DAYS.toMillis(minDifference)) {
                updateDescription(e.getX());
                long min = toDate(range.first()).getTime();
                long max = toDate(range.last()).getTime();

                if(lower < min) lower = min;
                if(upper > max) upper = max;

                xAxis.setLowerBound(new Date((long) lower));
                xAxis.setUpperBound(new Date((long) upper));
            } 
            
        }
    }
    
    private static Date round(Date date) {
        return date.getHours() < 11 ? date 
                : new Date(date.getYear(), date.getMonth(), date.getDate() + 1);
    }
    
    private float getNormalizedValue(RateList<?> list, LocalDate date, Field field) {
        LocalDate offsetDate = list.getPeriod().floor(date);
        Float value = 0f;
        Rate rate = list.get(offsetDate);
        if(rate != null) {
            value = rate.get(field);

            while(value != null && value.isNaN()) {
                offsetDate = list.getPeriod().lower(offsetDate);
                rate = list.get(offsetDate);
                if(rate == null) break;
                value = rate.get(field);
            }
        }
        if(value == null || value.isNaN()) value = 0f;
        return value;
    }
    
    private float getMaxValue(LocalDate from, LocalDate to) {
        final Period period = Period.ofInclusive(from, to);
        float max = 0f;
        
        for(IntentsManager.SeriesController<?> sc 
                : getIntentsManager().getSeriesControllers().values()) {
            RateList<?> list = sc.getTask().getRateList();
            if(list != null && !list.isEmpty()) {
                float localMax = 0f;
                Field field = sc.getIntent().getField();
                for(Rate rate : list.subList(period)) {
                    float value = rate.get(field);
                    if(Float.isFinite(value)) {
                        localMax = Compare.max(value, localMax);
                    }
                }
                System.out.println(sc.getIntent() + ": " + localMax);
                max = Compare.max(localMax, max);
            }
        }
        return max;
    }
    
    private void updateDescription(double x) {        
        LocalDate date = toLocalDate(round(xAxis.getValueForDisplay(x)));
        
        description.setDate(date);
        for(IntentsManager.SeriesController<?> sc : getIntentsManager().getSeriesControllers().values()) {
            RateList<?> list = sc.getTask().getRateList();
            if(list != null && !list.isEmpty()) {
                float value = getNormalizedValue(list, date, sc.getIntent().getField());
                sc.getItem().setValue(value);
            }
        }
        
        //updating opacity
        
        
        boolean intersects = false;
        Bounds bounds = description.getBoundsInParent();
        for(IntentsManager.SeriesController<?> sc : getIntentsManager().getSeriesControllers().values()) {
            Node node = sc.getSeries().getNode();
            if(node.intersects(bounds)) {
                intersects = true;
                break;
            }
        }
        /*LocalDate from = toLocalDate(round(xAxis.getValueForDisplay(bounds.getMinX() + 30)));
        LocalDate to = toLocalDate(round(xAxis.getValueForDisplay(bounds.getMaxX() - 30)));
        double max = getMaxValue(from, to);
        
        double cross = yAxis.getValueForDisplay(bounds.getMaxY() - 38).doubleValue();*/
        
        if(!intersects) {
            descriptionPaneAnimator.play(1.0d);
        } else {
            descriptionPaneAnimator.play(0.4d);
        }
    }
    //</editor-fold>

}
