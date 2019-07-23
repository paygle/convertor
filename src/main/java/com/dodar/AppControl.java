package com.dodar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AppControl implements Initializable {

    @FXML
    protected TextField textpath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ResourceBundle" + resources);
    }

    @FXML
    protected void directorySelect(ActionEvent e) {
        System.out.println("you click me" + e);
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(new Stage());
        String path = file.getPath();
        textpath.setText(path);
    }

}
