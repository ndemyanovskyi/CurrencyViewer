/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.list;

import com.ndemyanovskyi.app.localization.binding.ImageResourceBindings;
import com.ndemyanovskyi.backend.Currency;
import com.ndemyanovskyi.ui.pane.main.chart.Intent;
import com.ndemyanovskyi.util.beans.Properties;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Назарій
 */
public class IntentButton extends ToggleButton {

    private final ImageView flagView = new ImageView();
    private final ReadOnlyObjectProperty<Intent<?>> intent;

    private boolean attachedChanging = false;

    public IntentButton(Intent<?> intent) {
	this.intent = Properties.readOnlyNonNull(this, "intent", intent);
	
	getStyleClass().add("intent-button");
	
	Currency currency = intent.getCurrency();
	ImageResourceBindings bindings = ImageResourceBindings.images();
	ReadOnlyObjectProperty<Image> flag = bindings.get(
		bindings.containsKey("flag_" + currency)
			? "flag_" + currency
			: "flag_empty");
	flagView.imageProperty().bind(flag);
	setGraphic(flagView);

	intent.attachedProperty().addListener((p, oldAttached, attached) -> {
	    if(!attached) {
		attachedChanging = true;
		setSelected(attached);
		attachedChanging = false;
	    }
	});

	selectedProperty().addListener((property, oldSelected, selected) -> {
	    if(!attachedChanging) {
		Intention intention = new Intention(
			selected ? Intention.Action.ADD : Intention.Action.REMOVE, intent);
		fireEvent(new IntentionEvent(this, intention));
	    }
	});

	textProperty().bind(getIntent().currencyProperty().asString());
    }

    public Intent<?> getIntent() {
	return intentProperty().get();
    }

    public final ReadOnlyObjectProperty<Intent<?>> intentProperty() {
	return intent;
    }

}
