package controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class MyController3 extends HBox {
	
	public MyController3() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MyView3.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	@FXML
	public void initialize() {
		for(int i=0; i<7; i++) {
			ResponseController rc = new ResponseController(
					"A"+i,0.5,"B"+i,0.5,0.5);
			//rc.setLayoutX(j * 100);
			HBox.setHgrow(rc, Priority.NEVER);
			this.getChildren().add(rc);
		}
	}
}
