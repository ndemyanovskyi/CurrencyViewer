/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main;

import com.ndemyanovskyi.ui.pane.InitializableBorderPane;
import com.ndemyanovskyi.ui.pane.main.chart.ChartPane;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author Назарій
 */
public class MainPane extends InitializableBorderPane/* implements Initializable, EventHandler<ErrorEvent> */{
    
    @FXML private ChartPane chartPane;
    
    public MainPane() {
    }
    

    @FXML 
    private void onChartMouseDragged(MouseEvent e) {
	System.out.println("Mouse dragged: " + e.getX() + " " + e.getY());
    }
    
    @FXML 
    private void onChartMouseClicked(MouseEvent e) {
	System.out.println("Mouse clicked: " + e.getButton());
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
