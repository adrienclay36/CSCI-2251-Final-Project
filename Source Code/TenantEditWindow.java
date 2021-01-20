/**************************************************
 * Ian Webb
 * Gabe Prudencio
 * Adrien Clay
 * CSCI 2251 Final Project
 * 12/8/2020
 * Rent Management System
 **************************************************/


package sample;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TenantEditWindow {

    private static TextField nameInput, leaseLengthInput;
    private static ComboBox usnInput;
    private static ComboBox rentPaidInput;



    public static void display(String title, String message, String name, double leaseLength, boolean rentPaid, String propertyID, String email, String phone, ObservableList dropDown, TableView table) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL); // Block User interaction with other windows
        window.setTitle(title);
        window.setMinWidth(250);
        Label label = new Label();
        label.setText(message);

        // Create two buttons

        Button submit = new Button("Submit");
        Button cancel = new Button("Cancel");

        // NAME INPUT
        nameInput = new TextField();
        nameInput.setText(name);
        nameInput.setMinWidth(100);

        // LEASE LENGTH INPUT

        leaseLengthInput = new TextField();
        leaseLengthInput.setText(String.valueOf(leaseLength));

        // RENT PAID INPUT

        rentPaidInput = new ComboBox();
        rentPaidInput.setPromptText("Paid Rent?");
        rentPaidInput.getItems().addAll("" +
                "true",
                "false");
        if(rentPaid){
            rentPaidInput.getSelectionModel().selectFirst();
        } else{
            rentPaidInput.getSelectionModel().selectLast();
        }


        // PROPERTY ID INPUT:
        usnInput = new ComboBox();
        usnInput.setPromptText("Property ID");
        usnInput.setItems(dropDown);
        int indexItem = dropDown.indexOf(propertyID);
        usnInput.getSelectionModel().select(indexItem);


        TextField emailInput = new TextField();
        emailInput.setText(email);
        emailInput.setMinWidth(200);

        TextField phoneInput = new TextField();
        phoneInput.setText(phone);

        HBox tenantInputs = new HBox();
        tenantInputs.setPadding(new Insets(10, 10, 10, 10));
        tenantInputs.setSpacing(10);
        tenantInputs.getChildren().addAll(nameInput, leaseLengthInput, rentPaidInput, usnInput, emailInput, phoneInput);


        // RECIEVED INPUT


        submit.setOnAction(e -> {
            try{
                String oldID = propertyID;
                String nameReceived = nameInput.getText();
                double leaseLengthReceived = Double.parseDouble(leaseLengthInput.getText());
                boolean rentPaidReceived = Boolean.parseBoolean(String.valueOf(rentPaidInput.getSelectionModel().getSelectedItem()));
                String propertyIDReceived = String.valueOf(usnInput.getSelectionModel().getSelectedItem());
                String emailReceived = emailInput.getText();
                String phoneReceived = phoneInput.getText();
                Client.updateTenant("9", oldID, nameReceived, leaseLengthReceived, rentPaidReceived, propertyIDReceived, emailReceived, phoneReceived, table);
            } catch (NumberFormatException ex){
                AlertBox.display("Input Error", "Please Fill All Fields Correctly");
            }
            window.close();
        });
        cancel.setOnAction(e -> {
            window.close();
        });


        VBox layout = new VBox(10);
        layout.getChildren().addAll(label,tenantInputs, submit, cancel);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 1000, 300);
        window.setMinWidth(1000);
        window.setMinHeight(300);
        window.setScene(scene);
        window.showAndWait(); // Display window and wait for closure before returning

        window.setOnCloseRequest(e->{
            window.close();
        });
    }




}

