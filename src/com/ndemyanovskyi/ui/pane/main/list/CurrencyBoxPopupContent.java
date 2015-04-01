/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.list;

import com.ndemyanovskyi.backend.Currency;
import com.ndemyanovskyi.ui.pane.InitializableStackPane;
import com.ndemyanovskyi.ui.pane.main.chart.Intent;
import java.util.Objects;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 *
 * @author Назарій
 */
public class CurrencyBoxPopupContent extends InitializableStackPane {
    
    private final CurrencyBox currencyBox;
    
    @FXML 
    private Circle indicator; 
    @FXML
    private GridPane intentButtonPane;
    @FXML
    private Label fieldLabel;
    @FXML
    private Button clearButton;
    @FXML
    private VBox innerContent;
    @FXML
    private Pane backgroundPane;

    public CurrencyBoxPopupContent(CurrencyBox currencyBox) {
	this.currencyBox = Objects.requireNonNull(currencyBox, "currencyBox");
	
	currencyBox.getIndicator().strokeProperty().bind(indicator.strokeProperty());
	
	fieldLabel.textProperty().bind(currencyBox.getField().displayNameProperty());

	long columns = Math.round(Math.sqrt(currencyBox.getBank().getCurrencies().size()));
	if(columns < 2)
	    columns = 1;
	int column = 0;
	int row = 0;
	for(Currency currency : currencyBox.getBank().getCurrencies()) {
	    if(column == columns) {
		column = 0;
		row++;
	    }

	    IntentButton button = new IntentButton(
		    new Intent<>(currencyBox.getBank(), currency, currencyBox.getField()));
	    button.selectedProperty().addListener((property, oldSelected, selected) -> {
		boolean anySelected = false;
		for(IntentButton ib : getIntentButtons()) {
		    if(ib.isSelected()) {
			anySelected = true;
			break;
		    }
		}
		indicator.setStroke(anySelected ? Color.rgb(70, 196, 104): Color.LIGHTGRAY);
	    });
	    button.addEventHandler(IntentionEvent.INTENTION,
		    e -> currencyBox.fireEvent(new IntentionEvent(this, e.getIntention())));
	    intentButtonPane.add(button, column, row);
	    column++;
	}
    }

    public Pane getBackgroundPane() {
	return backgroundPane;
    }
    
    public ObservableList<IntentButton> getIntentButtons() {
	return (ObservableList) intentButtonPane.getChildren();
    }

    public VBox getInnerContent() {
	return innerContent;
    }

    public CurrencyBox getCurrencyBox() {
	return currencyBox;
    }

    @FXML
    private void onClearButtonAction(ActionEvent e) {
	for(Node node : intentButtonPane.getChildren()) {
	    ((Toggle) node).setSelected(false);
	}
    }
    
}
