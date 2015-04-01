/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.list;

import com.ndemyanovskyi.app.localization.binding.ImageResourceBindings;
import com.ndemyanovskyi.backend.Bank;
import com.ndemyanovskyi.backend.Rate.Field;
import com.ndemyanovskyi.ui.pane.InitializableVBox;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 *
 * @author Назарій
 */
public class BankItem extends InitializableVBox {

    @FXML private ImageView logoImageView;
    @FXML private HBox currencyBoxes;
    
    private ReadOnlyObjectWrapper<Bank<?>> bank;
    private ObjectProperty<EventHandler<? super IntentionEvent>> onIntention;

    public BankItem(Bank<?> bank) {
        setBank(bank);
	
        for(Field field : bank.getFields()) {
	    CurrencyBox box = new CurrencyBox(bank, field);
	    box.addEventHandler(IntentionEvent.INTENTION, e -> {
		fireIntentionEvent(e.getIntention());
	    });
            currencyBoxes.getChildren().add(box);
        }
	
	ImageResourceBindings bindings = ImageResourceBindings.images();
	ReadOnlyObjectProperty<Image> logo = bindings.get(
		bindings.containsKey("bank_logo_" + bank.getName())
			? "bank_logo_" + bank.getName()
			: "bank_logo_empty");
	logoImageView.imageProperty().bind(logo);
    }

    public void setOnIntention(EventHandler<? super IntentionEvent> onIntention) {
        onIntentionProperty().set(onIntention);
    }

    public EventHandler<? super IntentionEvent> getOnIntention() {
        return onIntentionProperty().get();
    }

    public ObjectProperty<EventHandler<? super IntentionEvent>> onIntentionProperty() {
        return onIntention != null ? onIntention
                : (onIntention = new SimpleObjectProperty<>(this, "onIntention"));
    }

    public Bank<?> getBank() {
        return bankPropertyImpl().get();
    }

    public void setBank(Bank<?> bank) {
        bankPropertyImpl().set(bank);
    }

    public ReadOnlyObjectProperty<Bank<?>> bankProperty() {
        return bankPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Bank<?>> bankPropertyImpl() {
        return bank != null ? bank
                : (bank = new ReadOnlyObjectWrapper<>(this, "bank"));
    }

    protected void fireIntentionEvent(Intention intention) {
        EventHandler<? super IntentionEvent> handler = getOnIntention();
        IntentionEvent event = new IntentionEvent(this, intention);
        if(handler != null) {
            handler.handle(event);
        }
        fireEvent(event);
    }
    
    
}
