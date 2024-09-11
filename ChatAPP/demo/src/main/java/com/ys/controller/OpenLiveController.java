package com.ys.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class OpenLiveController {

    public Button cancel;


    public void Hide(ActionEvent actionEvent) {
        cancel.getScene().getWindow().hide();

    }
}
