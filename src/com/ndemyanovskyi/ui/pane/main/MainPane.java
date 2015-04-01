/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main;

import com.ndemyanovskyi.backend.Bank;
import com.ndemyanovskyi.ui.pane.DelayedResizePane;
import com.ndemyanovskyi.ui.pane.main.chart.ChartPane;
import com.ndemyanovskyi.ui.pane.main.list.BankItem;
import com.ndemyanovskyi.ui.pane.main.list.Intention;
import static com.ndemyanovskyi.ui.pane.main.list.Intention.Action.ADD;
import static com.ndemyanovskyi.ui.pane.main.list.Intention.Action.REMOVE;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Назарій
 */
public class MainPane extends DelayedResizePane {
    
    @FXML private ChartPane chartPane;
    @FXML private VBox bankItemBox;
            
    public MainPane() {	
        for(Bank<?> bank : Bank.values()) {
            BankItem item = new BankItem(bank);
            item.setOnIntention(e -> {
                Intention intention = e.getIntention();
                switch(intention.getAction()) {
                    case ADD: chartPane.getIntents().add(intention.getIntent()); break;
                    case REMOVE: chartPane.getIntents().remove(intention.getIntent()); break;
                }
            });
            bankItemBox.getChildren().add(item);
        }
    }

    public ChartPane getChartPane() {
        return chartPane;
    }

    @FXML 
    private void onMouseEntered(MouseEvent e) {
        /*rotateAnimator.play(0d);
        opacityAnimator.play(1d);
        scaleAnimator.playXYZ(1d);
        translateAnimator.playY(0);*/
    }
    
    @FXML 
    private void onMouseExited(MouseEvent e) {
        /*rotateAnimator.play(180d);
        opacityAnimator.play(0d);
        scaleAnimator.playXYZ(0.95d);
        translateAnimator.playY(-50);*/
    }

    /***
     * Handing all errors in app and showing error messages.
     * @param event error event
     */
    /*@Override
    public void handle(ErrorEvent event) {
        Throwable cause = event.getCause();
        
        Dialogs dialogs = Dialogs.create();
        if(cause instanceof SQLException) {
            SQLException sqlCause = (SQLException) cause;
            
            switch(sqlCause.getSQLState()) {
                case "XSDB6": dialogs.message("::{error_db_alredy_loaded}").showError(); return;
            }
        } 
        dialogs.actions(new Action("::{continue}", e -> ((Dialog) e.getSource()).hide()),
                new Action("::{close_application}", e -> getApplication().stop())).
                showExceptionInNewWindow(event.getCause());
    }
    */
    
}
