package client;

import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegController {
    public TextField loginField;
    public PasswordField passwordField;
    public TextField nickField;
    Controller controller;

    public void clickOk(ActionEvent actionEvent) {
        controller.tryRegistration(loginField.getText().trim(), passwordField.getText().trim(), nickField.getText().trim());
        ((Stage) loginField.getScene().getWindow()).close();

    }
}
