/**************************************************
 * Ian Webb
 * Gabe Prudencio
 * Adrien Clay
 * CSCI 2251 Final Project
 * 12/8/2020
 * Rent Management System
 **************************************************/

package sample;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmBox {

    static boolean answer;

    public static boolean display(String title, String message) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL); // Block User interaction with other windows
        window.setTitle(title);
        window.setMinWidth(250);
        Label label = new Label();
        label.setText(message);

        // Create two buttons

        Button yes = new Button("Yes");
        Button no = new Button("No");

        yes.setOnAction(e -> {
            answer = true;
            window.close();
        });
        no.setOnAction(e -> {
            answer = false;
            window.close();
        });


        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, yes, no);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 400, 400);
        window.setMinHeight(400);
        window.setMinWidth(400);
        window.setScene(scene);
        window.showAndWait(); // Display window and wait for closure before returning

        return answer;
    }
}
