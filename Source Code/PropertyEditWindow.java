/**************************************************
 * Ian Webb
 * Gabe Prudencio
 * Adrien Clay
 * CSCI 2251 Final Project
 * 12/8/2020
 * Rent Management System
 **************************************************/

package sample;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PropertyEditWindow {
    private static TextField numBedroomsInput, propertyIDInput, rentPriceInput;
    private static ComboBox typeInput;




    public static void display(String title, String message, String type, int numBedrooms, String propertyID, double rentPrice, TableView table) {
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
        typeInput = new ComboBox<>();
        typeInput.setPromptText("Property Type");
        typeInput.getItems().addAll(
                "Apartment",
                "House",
                "Vacation Rental"
        );
        if(type.equals("Apartment")){
            typeInput.getSelectionModel().select(0);
        } else if(type.equals("House")){
            typeInput.getSelectionModel().select(1);
        } else{
            typeInput.getSelectionModel().select(2);
        }

        // LEASE LENGTH INPUT

        numBedroomsInput = new TextField();
        numBedroomsInput.setText(String.valueOf(numBedrooms));

        // RENT PAID INPUT

        propertyIDInput = new TextField();
        propertyIDInput.setText(propertyID);

        // PROPERTY ID INPUT:
        rentPriceInput = new TextField();
        rentPriceInput.setText(String.valueOf(rentPrice));

        HBox propertyInputs = new HBox();
        propertyInputs.setPadding(new Insets(10, 10, 10, 10));
        propertyInputs.setSpacing(10);
        propertyInputs.getChildren().addAll(typeInput, numBedroomsInput, propertyIDInput, rentPriceInput);


        // RECIEVED INPUT


        submit.setOnAction(e -> {
            try{
                String oldID = propertyID;
                String typeReceived = String.valueOf(typeInput.getSelectionModel().getSelectedItem());
                int numBedroomsReceived = Integer.parseInt(numBedroomsInput.getText());
                String propertyIDReceived = propertyIDInput.getText();
                double rentPriceReceived = Double.parseDouble(rentPriceInput.getText());
                Client.updateProperty("12", oldID, typeReceived, numBedroomsReceived, propertyIDReceived, rentPriceReceived, table);
            } catch (NumberFormatException ex){
                AlertBox.display("Input Error", "Please Fill All Fields Correctly");
            }
            window.close();
        });
        cancel.setOnAction(e -> {
            window.close();
        });


        VBox layout = new VBox(10);
        layout.getChildren().addAll(label,propertyInputs, submit, cancel);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 600, 500);
        window.setMinWidth(600);
        window.setMinHeight(500);
        window.setScene(scene);
        window.showAndWait(); // Display window and wait for closure before returning

        window.setOnCloseRequest(e->{
            window.close();
        });
    }


}

