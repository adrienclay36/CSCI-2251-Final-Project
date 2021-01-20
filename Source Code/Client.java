/**************************************************
 * Ian Webb
 * Gabe Prudencio
 * Adrien Clay
 * CSCI 2251 Final Project
 * 12/8/2020
 * Rent Management System
 **************************************************/

package sample;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.sql.rowset.CachedRowSet;
import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Properties;
import java.util.Scanner;


public class Client extends Application {


    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static LocalDateTime now = LocalDateTime.now();

    private static ObservableList<ObservableList> data;
    private Stage window;
    private Scene tenantScene, availScene, occupiedScene, viewAllScene;
    private Button add, delete;

    private Button viewAllButton, viewAllButton2, viewAllButton3;

    private TextField nameInput, leaseLengthInput;
    private ComboBox usnInput;
    private Button viewAvailability, viewTenants, occupiedButton, occupiedButton2, viewAvailability2, viewTenants2;
    private Button viewAvailability3, viewTenants3, occupiedButton3;
    private Button viewAvailability4, viewTenants4, occupiedButton4, viewAllButton4;
    private TextField bedroomInput, propIDInput, rentAmountInput;
    private ComboBox typeInput;
    private Button addProperty, deleteProperty;
    private Button export1, export2, export3, export4;

    private TableView tenantTableView;
    private TableView availTableView;
    private TableView occupiedTable;
    private TableView allTable;

    private static Socket socket;


    /**********************************************************
     *ESTABLISH SOCKET CONNECTION AND INPUT AND OUTPUT STREAMS*
     **********************************************************/
    static {
        try {
            // GET IP ADDRESS INPUT FROM USER
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the IP Address of the Server Machine on Your Local Area Network\n");
            System.out.println("If this is a Local Session, simply enter localHost: ");
            String input = scanner.nextLine();
            socket = new Socket(input, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static ObjectOutputStream objectOutputStream;
    static{
        try{
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static DataOutputStream out;

    static {
        try {
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ObjectInputStream in;

    static {
        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Client() throws IOException {
    }


    public static void main(String[] args) throws IOException {
        launch(args);


    }

    /*********************
     * CREATE GUI ELEMENTS *
     *********************/

    @Override
    public void start(Stage primaryStage) throws Exception{
        window = primaryStage;

        // NAME INPUT
        nameInput = new TextField();
        nameInput.setPromptText("Name");
        nameInput.setMinWidth(100);

        // LEASE LENGTH INPUT

        leaseLengthInput = new TextField();
        leaseLengthInput.setPromptText("Lease Length");

        // RENT PAID INPUT

        ComboBox rentPaid = new ComboBox();
        rentPaid.setPromptText("Paid Rent?");
        rentPaid.getItems().addAll(
                "true",
                "false"
        );


        // PROPERTY ID INPUT:
        usnInput = new ComboBox<>();
        usnInput.setPromptText("Property ID");

        populateDropdown("5");

        // EMAIL INPUT

        TextField emailInput = new TextField();
        emailInput.setPromptText("Email");

        // PHONE NUMBER INPUT

        TextField phoneInput = new TextField();
        phoneInput.setPromptText("Phone Number");


        // ADD AND DELETE RECORD BUTTONS

        // ADD TENANT BUTTON
        add = new Button("Add Tenant");
        add.setOnAction(e -> {

            try{
                String name = nameInput.getText();
                double leaseLength = Double.parseDouble(leaseLengthInput.getText());
                String propertyID = String.valueOf(usnInput.getSelectionModel().getSelectedItem());
                String paidRent = String.valueOf(rentPaid.getSelectionModel().getSelectedItem());
                String email = emailInput.getText();
                String phone = phoneInput.getText();
                addTenant("6", name, leaseLength, Boolean.parseBoolean(paidRent), propertyID, email, phone, tenantTableView);
                nameInput.clear();
                leaseLengthInput.clear();
                usnInput.valueProperty().set(null);
                rentPaid.valueProperty().set(null);
                emailInput.clear();
                phoneInput.clear();
                usnInput.setPromptText("Property ID");
                rentPaid.setPromptText("Paid Rent?");
                updateData("8", "availTable", availTableView);
                updateData("8", "occupied", occupiedTable);
                updateData("8", "allQuery", allTable);
                populateDropdown("5");
            } catch (NumberFormatException ex){
                AlertBox.display("Input Error", "Please Fill Out All Fields Correctly");
            } catch (IOException | SQLException | ClassNotFoundException ioException) {
                ioException.printStackTrace();
            }
        });

        // DELETE TENANT BUTTON
        delete = new Button("Delete Tenant");
        delete.setOnAction(e-> {
            try{
                boolean answer = ConfirmBox.display("Delete Tenant?", "Are You Sure You Want to Delete This Tenant?");
                if(answer){
                    Object selectedItems = tenantTableView.getSelectionModel().getSelectedItems().get(0);
                    String name = selectedItems.toString().split(",")[0].substring(1);
                    String propID = selectedItems.toString().split(",")[2].substring(1);
                    deleteTenant("7", name, propID, tenantTableView);
                    updateData("8", "availTable", availTableView);
                    updateData("8", "occupied", occupiedTable);
                    updateData("8", "allQuery", allTable);
                    populateDropdown("5");
                }
            } catch (IndexOutOfBoundsException | NullPointerException ex) {
                AlertBox.display("Nothing Selected", "Please Select A Tenant");
            } catch (IOException | ClassNotFoundException | SQLException ioException) {
                ioException.printStackTrace();
            }

        });

        // EDIT TENANT BUTTON

        Button tenantEdit = new Button("Edit Tenant");
        tenantEdit.setOnAction(e->{
            try{
                Object selectedItems=  tenantTableView.getSelectionModel().getSelectedItems().get(0);
                String name = selectedItems.toString().split(",")[0].substring(1);
                double leaseLength = Double.parseDouble(selectedItems.toString().split(",")[1].substring(1));
                boolean paidRent = true;
                if(selectedItems.toString().split(",")[3].substring(1).equals("Not Paid")){
                    paidRent=false;
                }
//                boolean paidRent = Boolean.parseBoolean(selectedItems.toString().split(",")[3].substring(1));
                String propertyID = selectedItems.toString().split(",")[2].substring(1);
                String email = selectedItems.toString().split(",")[4].substring(1);
                String phone = selectedItems.toString().split(",")[5].substring(1).replaceAll("]", "");
                ObservableList dropDown = usnInput.getItems();
                dropDown.add(propertyID);
                dropDown.sort(Comparator.naturalOrder());
                TenantEditWindow.display("Edit Tenant", "Edit All Fields Desired", name, leaseLength, paidRent, propertyID, email, phone, dropDown, tenantTableView);

                // REPOPULATE TABLES
                updateData("8", "availTable", availTableView); // AVAILABLE TABLE
                updateData("8", "occupied", occupiedTable); // OCCUPIED TABLE
                updateData("8", "tenant", tenantTableView); // TENANT TABLE
                updateData("8", "allQuery", allTable); // ALL PROPERTIES TABLE
                populateDropdown("5");

            } catch (NumberFormatException ex){
                AlertBox.display("Input Error", "Please Fill All Fields Correctly");
            }catch (IndexOutOfBoundsException | NullPointerException IEX) {
                AlertBox.display("No Tenant Selected", "Please Select a Tenant");
            } catch (IOException | SQLException | ClassNotFoundException ioException) {
                ioException.printStackTrace();
            }
        });


        // ADD HBOX FOR BUTTONS AND INPUTS:

        HBox tenantInputs = new HBox();
        tenantInputs.setPadding(new Insets(10, 10, 10, 10));
        tenantInputs.setSpacing(10);
        tenantInputs.getChildren().addAll(nameInput, leaseLengthInput, rentPaid, usnInput, emailInput, phoneInput, add);




        // Change to availability
        viewAvailability = new Button("View Availability");
        viewAvailability.setOnAction(e-> {
            window.setMinHeight(500);
            window.setMinWidth(1200);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(availScene);
        });
        occupiedButton = new Button("View Occupied");
        occupiedButton.setOnAction(e-> {
            window.setMinWidth(1200);
            window.setMinHeight(500);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(occupiedScene);

        });
        viewAllButton = new Button("All Properties");
        viewAllButton.setOnAction(e-> {
            window.setMinHeight(500);
            window.setMinWidth(1200);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(viewAllScene);

        });
        export1=  new Button("Export Excel");
        export1.setOnAction(e-> {
            try {
                export("15");
                AlertBox.display("Excel Export", "Exported Excel File To Working Directory");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        Button sendEmail = new Button("Send Email");
        sendEmail.setOnAction(e -> {
            try{
                Object selectedItems = tenantTableView.getSelectionModel().getSelectedItems().get(0);
                String name = selectedItems.toString().split(",")[0].substring(1);
                String email = selectedItems.toString().split(",")[4].substring(1);
                String propertyID = selectedItems.toString().split(",")[2].substring(1);
                String subject = "Invoice for " + name + " regarding property " + propertyID;
                String body = "Hello " + name + ",\n" +
                        "\n" +
                        "Our records indicate you have not yet paid your rent. \n" +
                        "\n" +
                        "Please review the attached invoice document to avoid late fees associated with your property " + propertyID + ".\n" +
                        "\n" +
                        "We thank you for your cooperation in this matter, and wish you the best during these hard times.\n" +
                        "\n" +
                        "All the best,\n" +
                        "\n" +
                        "- Group 4 Management";
                deliverEmail(email, name, propertyID, subject, body);

            } catch(NullPointerException ex){
                AlertBox.display("Select Tenant", "Please Select A Tenant To Send An Email To");
            } catch(MessagingException ex1){
                AlertBox.display("Unable To Send", "Unable To Send Message, Please Ensure Email Is Correct");
            }


        });



        //Change to tenants
        viewTenants = new Button("View Tenants");
        viewTenants.setOnAction(e -> {
            window.setMinWidth(1600);
            window.setMinHeight(500);
            window.setMaxWidth(1600);
            window.setMaxHeight(500);
            window.centerOnScreen();
            window.setScene(tenantScene);
        });

        // SET MENU BUTTON WIDTH
        viewTenants.setMaxWidth(Double.MAX_VALUE);
        viewAvailability.setMaxWidth(Double.MAX_VALUE);
        occupiedButton.setMaxWidth(Double.MAX_VALUE);
        viewAllButton.setMaxWidth(Double.MAX_VALUE);
        export1.setMaxWidth(Double.MAX_VALUE);

        tenantTableView = new TableView();
        buildData("1", tenantTableView);
        ContextMenu lateFeeContext = new ContextMenu();
        MenuItem applyFees = new MenuItem("Apply Late Fees");
        lateFeeContext.getItems().addAll(applyFees);



        // ADD BUTTONS FOR LEFT MENU

        VBox menuOptions = new VBox(10);
        menuOptions.setPadding(new Insets(20, 20, 20, 20));
        menuOptions.setAlignment(Pos.TOP_CENTER);
        menuOptions.getChildren().addAll(viewAvailability, viewTenants, occupiedButton, viewAllButton, export1);

        // RIGHT MENU BUTTON WIDTH
        tenantEdit.setMaxWidth(Double.MAX_VALUE);
        delete.setMaxWidth(Double.MAX_VALUE);
        sendEmail.setMaxWidth(Double.MAX_VALUE);

        // ADD BUTTONS FOR RIGHT MENU
        VBox editDelete = new VBox(10);
        editDelete.setPadding(new Insets(20, 20, 20, 20));
        editDelete.getChildren().addAll(tenantEdit, delete, sendEmail);
        editDelete.setMaxHeight(Double.MAX_VALUE);
        editDelete.setAlignment(Pos.TOP_CENTER);

        //SET PLACEMENT CONSTRAINTS VIA GRIDPANE
        GridPane tenantLayout = new GridPane();
        tenantLayout.setPadding(new Insets(20, 20, 20, 20));
        tenantLayout.setVgap(8);
        tenantLayout.setHgap(8);

        GridPane.setConstraints(tenantTableView, 1, 0);
        GridPane.setConstraints(tenantInputs, 1, 1);
        GridPane.setConstraints(menuOptions, 0, 0);
        GridPane.setConstraints(editDelete, 2, 0);

        tenantLayout.getChildren().addAll(tenantTableView, tenantInputs, menuOptions, editDelete);

        tenantScene = new Scene(tenantLayout, 1600, 500);





        /*
        BEGIN AVAILABILITY SCENE SETUP
         */

        // Property Type Input
        typeInput = new ComboBox<>();
        typeInput.setPromptText("Property Type");
        typeInput.getItems().addAll(
                "Apartment",
                "House",
                "Vacation Rental"
        );

        // Bedroom Input:
        bedroomInput = new TextField();
        bedroomInput.setPromptText("# Bedrooms");

        // Prop ID Input:
        propIDInput = new TextField();
        propIDInput.setPromptText("Property ID");

        // Rent Amount Input:
        rentAmountInput = new TextField();
        rentAmountInput.setPromptText("Rent Price");

        //ADD PROPERTY

        addProperty = new Button("Add Property");
        addProperty.setOnAction(e -> {

            try {
                String type = String.valueOf(typeInput.getSelectionModel().getSelectedItem());
                int numBedrooms = Integer.parseInt(bedroomInput.getText());
                String propertyID = propIDInput.getText();
                double rentAmount = Double.parseDouble(rentAmountInput.getText());
                addPropertyButton("10", type, numBedrooms, propertyID, rentAmount, availTableView);
                typeInput.valueProperty().setValue(null);
                bedroomInput.clear();
                propIDInput.clear();
                rentAmountInput.clear();
                updateData("8", "availTable", availTableView);
                updateData("8", "occupied", occupiedTable);
                updateData("8", "tenant", tenantTableView);
                updateData("8", "allQuery", allTable);
                populateDropdown("5");
            } catch (NumberFormatException ex) {
                AlertBox.display("Input Error", "Please Fill All Fields Correctly");
            } catch (IOException | SQLException | ClassNotFoundException ioException) {
                ioException.printStackTrace();
            }
        });


        // DELETE PROPERTY

        deleteProperty = new Button("Delete Property");
        deleteProperty.setOnAction(e -> {
            try{
                boolean answer = ConfirmBox.display("Delete Property?", "Are You Sure You Want To Delete This Property?");
                if(answer){
                    Object selectedItems = availTableView.getSelectionModel().getSelectedItems().get(0);
                    String propertyID = selectedItems.toString().split(",")[2].substring(1);
                    deletePropertyButton("11", propertyID, availTableView);
                    updateData("8", "occupied", occupiedTable);
                    updateData("8", "tenant", tenantTableView);
                    updateData("8", "allQuery", allTable);
                    populateDropdown("5");
//                    updateData(occupiedQuery, occupiedTable);
//                    updateData(allQuery, allTable);
//                    populateDropdown();
                }
            } catch (IndexOutOfBoundsException | NullPointerException ex){
                AlertBox.display("Nothing Selected", "Please Select A Property");

            } catch (IOException | ClassNotFoundException | SQLException ioException) {
                ioException.printStackTrace();
            }

        });

        // EDIT PROPERTY

        Button availEdit = new Button("Edit Property");
        availEdit.setOnAction(e-> {
            try{
                Object selectedItems = availTableView.getSelectionModel().getSelectedItems().get(0);
                String type = selectedItems.toString().split(",")[0].substring(1);
                int numBedrooms = Integer.parseInt(selectedItems.toString().split(",")[1].substring(1));
                String propertyID = selectedItems.toString().split(",")[2].substring(1);
                String rentPrice = selectedItems.toString().split(",")[3].substring(1).replaceAll("]","");
                double parsedRentPrice = Double.parseDouble(rentPrice);
                PropertyEditWindow.display("Edit Property", "Edit All Desired Fields", type, numBedrooms, propertyID, parsedRentPrice, availTableView);
                updateData("8", "occupied", occupiedTable);
                updateData("8", "tenant", tenantTableView);
                updateData("8", "allQuery", allTable);
                populateDropdown("5");
//                updateData(allQuery, allTable);
//                updateData(occupiedQuery, occupiedTable);
//                updateData(tenantQuery, tenantTableView);
//                populateDropdown();

            } catch (NumberFormatException ex2){
                AlertBox.display("Input Error", "Please Fill Out All Fields Correctly");
            } catch (IndexOutOfBoundsException | NullPointerException ex3){
                AlertBox.display("No Property Selected", "Please Select A Property");
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
            } catch (IOException | ClassNotFoundException | SQLException ioException) {
                ioException.printStackTrace();
            }
        });
        availEdit.setMaxWidth(Double.MAX_VALUE);
        deleteProperty.setMaxWidth(Double.MAX_VALUE);



        // Add inputs to Hbox for display on the lower portion of screen
        HBox availInputs = new HBox();
        availInputs.setPadding(new Insets(10, 10, 10, 10));
        availInputs.setSpacing(10);
        availInputs.getChildren().addAll(typeInput, bedroomInput, propIDInput, rentAmountInput, addProperty);
        VBox addEditProp = new VBox(10);
        addEditProp.setAlignment(Pos.TOP_CENTER);
        addEditProp.setPadding(new Insets(20, 20, 20, 20));
        addEditProp.getChildren().addAll(availEdit, deleteProperty);

        availTableView = new TableView();
        buildData("2", availTableView);

        // Change to availability
        viewAvailability2 = new Button("View Availability");
        viewAvailability2.setOnAction(e-> {
            window.setMinWidth(1200);
            window.setMinHeight(500);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(availScene);
        });
        occupiedButton2 = new Button("View Occupied");
        occupiedButton2.setOnAction(e-> {
            window.setMinHeight(500);
            window.setMinWidth(1200);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(occupiedScene);
        });
        viewAllButton2 = new Button("All Properties");
        viewAllButton2.setOnAction(e-> {
            window.setMinHeight(500);
            window.setMinWidth(1200);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(viewAllScene);


        });


        //Change to tenants
        viewTenants2 = new Button("View Tenants");
        viewTenants2.setOnAction(e -> {
            window.setMinWidth(1600);
            window.setMinHeight(500);
            window.setMaxWidth(1600);
            window.setMaxHeight(500);
            window.centerOnScreen();
            window.setScene(tenantScene);

        });

        export2 =  new Button("Export Excel");
        export2.setOnAction(e-> {
            try {
                export("15");
                AlertBox.display("Excel Export", "Exported Excel File To Working Directory");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        viewTenants2.setMaxWidth(Double.MAX_VALUE);
        viewAvailability2.setMaxWidth(Double.MAX_VALUE);
        occupiedButton2.setMaxWidth(Double.MAX_VALUE);
        viewAllButton2.setMaxWidth(Double.MAX_VALUE);
        export2.setMaxWidth(Double.MAX_VALUE);


        VBox menuOptions2 = new VBox(10);
        menuOptions2.setPadding(new Insets(20, 20, 20, 20));
        menuOptions2.setAlignment(Pos.TOP_CENTER);
        menuOptions2.getChildren().addAll(viewAvailability2, viewTenants2, occupiedButton2, viewAllButton2, export2);

        GridPane availLayout = new GridPane();
        availLayout.setPadding(new Insets(20, 20, 20, 20));
        availLayout.setVgap(8);
        availLayout.setHgap(8);

        GridPane.setConstraints(availTableView, 1, 0);
        GridPane.setConstraints(availInputs, 1, 1);
        GridPane.setConstraints(menuOptions, 0, 0);
        GridPane.setConstraints(addEditProp, 2, 0);

        availLayout.getChildren().addAll(availTableView, availInputs, menuOptions2, addEditProp);

        availScene = new Scene(availLayout, 1200, 500);






        /*
        END AVAILABILITY SCENE SETUP
         */

        /*
        START OCCUPIED SETUP
         */


        viewAvailability3 = new Button("View Availability");
        viewAvailability3.setOnAction(e-> {
            window.setMinWidth(1200);
            window.setMinHeight(500);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(availScene);
        });
        occupiedButton3 = new Button("View Occupied");
        occupiedButton3.setOnAction(e-> {
            window.setMinWidth(1200);
            window.setMinHeight(500);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(occupiedScene);

        });
        viewAllButton3 = new Button("All Properties");
        viewAllButton3.setOnAction(e-> {
            window.setMinHeight(500);
            window.setMinWidth(1200);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(viewAllScene);
        });


        //Change to tenants
        viewTenants3 = new Button("View Tenants");
        viewTenants3.setOnAction(e -> {
            window.setMinWidth(1600);
            window.setMinHeight(500);
            window.setMaxWidth(1600);
            window.setMaxHeight(500);
            window.centerOnScreen();
            window.setScene(tenantScene);
        });
        export3 =  new Button("Export Excel");
        export3.setOnAction(e-> {
            try {
                export("15");
                AlertBox.display("Excel Export", "Exported Excel File To Working Directory");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        viewTenants3.setMaxWidth(Double.MAX_VALUE);
        viewAvailability3.setMaxWidth(Double.MAX_VALUE);
        occupiedButton3.setMaxWidth(Double.MAX_VALUE);
        viewAllButton3.setMaxWidth(Double.MAX_VALUE);
        export3.setMaxWidth(Double.MAX_VALUE);

        occupiedTable = new TableView();
        occupiedTable.setContextMenu(lateFeeContext);
        buildData("3", occupiedTable);


        // APPLY FEES RIGHT CLICK MENU
        applyFees.setOnAction(e->{
            boolean answer = ConfirmBox.display("Apply Late Fees", "Are You Sure You Want To Apply Late Fees?");
            if(answer){
                try{
                    Object selectedItems = occupiedTable.getSelectionModel().getSelectedItems().get(0);
                    String propertyID = selectedItems.toString().split(",")[1].substring(1);
                    double  rentAmount = Double.parseDouble(selectedItems.toString().split(",")[2].substring(1).replaceAll("]",""));
                    applyLateFees("13", propertyID, rentAmount, occupiedTable);
                    updateData("8", "occupied", occupiedTable);
                    updateData("8", "tenant", tenantTableView);
                    updateData("8", "allQuery", allTable);
                } catch (NullPointerException ex){
                    AlertBox.display("No Selection", "Please Select a Tenant/Property");
                }

//                updateData(availableQuery, availTableView);
//                updateData(tenantQuery, tenantTableView);
            }

        });





        VBox menuOptions3 = new VBox(10);
        menuOptions3.setPadding(new Insets(20, 20, 20, 20));
        menuOptions3.setAlignment(Pos.TOP_CENTER);
        menuOptions3.getChildren().addAll(viewAvailability3, viewTenants3, occupiedButton3, viewAllButton3, export3);

        GridPane occupiedLayout = new GridPane();
        occupiedLayout.setPadding(new Insets(20, 20, 20, 20));
        occupiedLayout.setVgap(8);
        occupiedLayout.setHgap(8);

        // APPLY FEES BUTTON
        Button applyFeesButton = new Button("Apply Late Fees");
        applyFeesButton.setOnAction(e->{
            boolean answer = ConfirmBox.display("Apply Late Fees", "Are You Sure You Want To Apply Late Fees?");
            if(answer){
                try{
                    Object selectedItems = occupiedTable.getSelectionModel().getSelectedItems().get(0);
                    String propertyID = selectedItems.toString().split(",")[1].substring(1);
                    double  rentAmount = Double.parseDouble(selectedItems.toString().split(",")[2].substring(1).replaceAll("]",""));
                    applyLateFees("13", propertyID, rentAmount, occupiedTable);
                    updateData("8", "occupied", occupiedTable);
                    updateData("8", "tenant", tenantTableView);
                    updateData("8", "allQuery", allTable);
                } catch (NullPointerException ex){
                    AlertBox.display("No Selection", "Please Select a Tenant/Property");
                }

//                updateData(availableQuery, availTableView);
//                updateData(tenantQuery, tenantTableView);
            }

        });

        TextField adjustCostField = new TextField();
        adjustCostField.setPromptText("Enter New Cost");
        Button adjustCost = new Button("Manually Adjust Cost");
        adjustCost.setOnAction(e->{
            try{
                Object selectedItems = occupiedTable.getSelectionModel().getSelectedItems().get(0);
                String propertyID = selectedItems.toString().split(",")[1].substring(1);
                double rentAmount = Double.parseDouble(adjustCostField.getText());
                adjustCostMethod("14", propertyID, rentAmount, occupiedTable);
                updateData("8", "occupied", occupiedTable);
                updateData("8", "tenant", tenantTableView);
                updateData("8", "allQuery", allTable);
                adjustCostField.clear();

            } catch (NumberFormatException ex){
                AlertBox.display("Input Error", "Please Enter Rent Cost - Numbers Only");
                System.out.println("test");
            } catch (IndexOutOfBoundsException | NullPointerException ex2){
                AlertBox.display("None Selected", "Please Select An Entry To Adjust Cost");
            }
        });
        Button createInvoice = new Button("Create Invoice");
        createInvoice.setOnAction(e ->{
            try{
                Object selectedItems=  occupiedTable.getSelectionModel().getSelectedItems().get(0);
                String name = selectedItems.toString().split(",")[0].substring(1);
                double rentOwed = Double.parseDouble(selectedItems.toString().split(",")[2].substring(1).replaceAll("]", ""));
                System.out.println(name);
                System.out.println(rentOwed);
                String fileName = (name + "Invoice.txt").replaceAll(" ", "");
                generateInvoice(fileName, name, rentOwed);

            } catch (NullPointerException ex){
                AlertBox.display("Nothing Selected", "Please Select A Tenant");
            }

        });

        createInvoice.setMaxWidth(Double.MAX_VALUE);
        applyFeesButton.setMaxWidth(Double.MAX_VALUE);


        VBox rightMenu = new VBox(10);
        rightMenu.setPadding(new Insets(20, 20, 20, 20));
        rightMenu.setAlignment(Pos.TOP_CENTER);
        rightMenu.getChildren().addAll(applyFeesButton, createInvoice);


        GridPane.setConstraints(occupiedTable, 1, 0);
        GridPane.setConstraints(menuOptions3, 0, 0);

        GridPane.setConstraints(rightMenu, 2, 0);
        GridPane.setConstraints(adjustCost, 1, 3);
        GridPane.setConstraints(adjustCostField, 1, 2);


        occupiedLayout.getChildren().addAll(occupiedTable, menuOptions3, rightMenu, adjustCost, adjustCostField);

        occupiedScene = new Scene(occupiedLayout, 1200, 500);








        /*
        END OCCUPIED SETUP
         */



        /*
        BEGIN ALL PROPERTIES SCENE SET UP
         */

        viewAvailability4 = new Button("View Availability");
        viewAvailability4.setOnAction(e-> {
            window.setMinWidth(1200);
            window.setMinHeight(500);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(availScene);
        });
        occupiedButton4 = new Button("View Occupied");
        occupiedButton4.setOnAction(e-> {
            window.setMinHeight(500);
            window.setMinWidth(1200);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(occupiedScene);

        });
        viewAllButton4 = new Button("All Properties");
        viewAllButton4.setOnAction(e-> {
            window.setMinHeight(500);
            window.setMinWidth(1200);
            window.setMaxHeight(500);
            window.setMaxWidth(1200);
            window.centerOnScreen();
            window.setScene(viewAllScene);

        });


        //Change to tenants
        viewTenants4 = new Button("View Tenants");
        viewTenants4.setOnAction(e -> {
            window.setMinWidth(1600);
            window.setMinHeight(500);
            window.setMaxWidth(1600);
            window.setMaxHeight(500);
            window.centerOnScreen();
            window.setScene(tenantScene);

        });

        export4 =  new Button("Export Excel");
        export4.setOnAction(e-> {
            try {
                export("15");
                AlertBox.display("Excel Export", "Exported Excel File To Working Directory");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        viewTenants4.setMaxWidth(Double.MAX_VALUE);
        viewAvailability4.setMaxWidth(Double.MAX_VALUE);
        occupiedButton4.setMaxWidth(Double.MAX_VALUE);
        viewAllButton4.setMaxWidth(Double.MAX_VALUE);
        export4.setMaxWidth(Double.MAX_VALUE);

        allTable = new TableView();

        buildData("4", allTable);

        VBox menuOptions4 = new VBox(10);
        menuOptions4.setPadding(new Insets(20, 20, 20, 20));
        menuOptions4.setAlignment(Pos.TOP_CENTER);
        menuOptions4.getChildren().addAll(viewAvailability4, viewTenants4, occupiedButton4, viewAllButton4, export4);

        GridPane allLayout = new GridPane();
        allLayout.setPadding(new Insets(20, 20, 20, 20));
        allLayout.setVgap(8);
        allLayout.setHgap(8);


        GridPane.setConstraints(allTable, 1, 0);
        GridPane.setConstraints(menuOptions3, 0, 0);

        allLayout.getChildren().addAll(allTable, menuOptions4);

        viewAllScene = new Scene(allLayout, 1200, 500);















        /*
        END ALL PROPERTIES SCENE SETUP
         */

        window.setOnCloseRequest(e-> {
            e.consume();
            boolean answer = ConfirmBox.display("Quit", "Are You Sure You Want To Quit?");
            if(answer){
                window.close();
            }
        });
        window.setTitle("RMS V1");
        window.setScene(viewAllScene);
        window.setHeight(viewAllScene.getHeight());
        window.setWidth(viewAllScene.getWidth());
        window.setMinHeight(500);
        window.setMinWidth(1400);
        window.show();




    }



    /******************
    BEGIN METHOD BUILD
    END GUI SET UP
     *****************/

    // Send Emails via SMTP server (gmail, using dummy email address)
    // Typically you wouldn't just include a password but rather have this prompt to user for email set up
    // But for the sake of demonstration, the dummy email and password will do
    private void deliverEmail(String email, String name, String propertyID, String subject, String body) throws MessagingException {

        System.out.println("Preparing To Send Email");
        Properties properties = new Properties();

        //Set Up SMTP Server
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        String myAccountEmail = "group4managementproject@gmail.com";
        String apiKey = "group4temp";

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, apiKey);
            }
        });

        try{
            // SET UP EMAIL MESSAGE AND ATTACH INVOICE
            String filename = (name + "Invoice.txt").replace(" ", "");
            DataSource source = new FileDataSource(filename);
            Message message = new MimeMessage(session);
            Multipart multipart = new MimeMultipart();
            message.setDataHandler(new DataHandler(source));
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject(subject);
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);



            Transport.send(message);
            System.out.println("Message Sent");
            AlertBox.display("Message Sent", "Message Delivered To Recipient");

        } catch (MessagingException e){
            AlertBox.display("Generate Invoice", "Please Generate An Invoice For This Tenant On The 'Occupied' Page First");
        }


    }


    // GENERATE INVOICE FOR SELECTED TENANT


    private void generateInvoice(String fileName, String name, double rentOwed) {

        String todaysDate = dtf.format(now);
        String formattedRent = df2.format(rentOwed);
        String message = "Dear " + name + ",\n" +
                "\n" +
                "Our records indicate that you have failed to pay the monthly amount owed for your rental property. If this invoice is not paid within seven business days from receival, late fees will be applied to the amount owed.\n" +
                "\n" +
                "This is a first and final warning.\n" +
                "\n" +
                "\n" +
                "Thank you for your cooperation.\n" +
                "\n" +
                "- Management\n" +
                "\n" +
                "\n" +
                "Rent Owed: " + formattedRent + "\n" +
                "\n" +
                "Todays Date: " + todaysDate;


        try{
            File myObj = new File(fileName);
            if(myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
                AlertBox.display("File Created", "File Has Been Created In Working Directory");
            } else{
                AlertBox.display("File Updated", "Previous File Updated");
                System.out.println("File already exists");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(message);
            myWriter.close();

        } catch (IOException e){
            e.printStackTrace();
        }




    }


    // ADJUST COST METHOD FROM OCCUPIED PAGE - Allows for the manual adjustment of cost
    private void adjustCostMethod(String command, String propertyID, double rentAmount, TableView table) {
        data = FXCollections.observableArrayList();
        try{
            out.writeUTF(command);
            out.flush();

            out.writeUTF(propertyID);
            out.flush();
            out.writeDouble(rentAmount);

            CachedRowSet afterAdjustment = (CachedRowSet) in.readObject();

            while (afterAdjustment.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= afterAdjustment.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(afterAdjustment.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }

            table.setItems(data);


        } catch (Exception e){

        }



    }

    // Apply late fees method from occupied window, works for both right click menu AND button

    private void applyLateFees(String command, String propertyID, double rentAmount, TableView table) {
        data = FXCollections.observableArrayList();

        try{
            out.writeUTF(command);
            out.flush();

            out.writeUTF(propertyID);
            out.flush();
            out.writeDouble(rentAmount);
            out.flush();

            CachedRowSet lateFee = (CachedRowSet) in.readObject();

            while (lateFee.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= lateFee.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(lateFee.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }
            // Set TableView Items for display
            table.setItems(data);






        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    // Delete Property method
    private void deletePropertyButton(String command, String propertyID, TableView table) {
        data = FXCollections.observableArrayList();
        try{
            // WRITE NECESSARY INFO TO SERVER TO COMMUNICATE TO DATABASE
            out.flush();
            out.writeUTF(command);
            out.flush();

            out.writeUTF(propertyID);
            out.flush();

            // READ INFO FROM DATABASE (Update Display after making changes)
            CachedRowSet delProperty = (CachedRowSet) in.readObject();

            // UNPACK INFO FROM DATABASE
            while (delProperty.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= delProperty.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(delProperty.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }

            //FINALLY ADDED TO TableView
            table.setItems(data);




        } catch (Exception e) {
            e.printStackTrace();
        }


    }

        // ADD PROPERTY BUTTON

    private void addPropertyButton(String command, String type, int numBedrooms, String propertyID, double rentAmount, TableView table) {

        try {

            data = FXCollections.observableArrayList();

            // WRITE NECESSARY INFO TO SERVER TO COMMUNICATE TO DATABASE
            out.writeUTF(command);
            out.flush();
//
            out.writeUTF(type);
            out.flush();
            out.writeInt(numBedrooms);
            out.flush();
            out.writeUTF(propertyID);
            out.flush();
            out.writeDouble(rentAmount);
            out.flush();

            CachedRowSet addProp = (CachedRowSet) in.readObject();


            while (addProp.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= addProp.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(addProp.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }

            //FINALLY ADDED TO TableView
            table.setItems(data);

        } catch (ClassNotFoundException | IOException | NullPointerException | SQLException e) {
            AlertBox.display("Duplicate Property", "Property Already Exists, Please Choose Another ID");
        }
    }

        // BUILD/REBUILD initial tableview display info, this method can be called on any button press in order to
        // update multiple displays after making a change to either one of the two main tables.
    public void buildData(String command, TableView table) {
        try {
            data = FXCollections.observableArrayList();
            out.writeUTF(command);
            out.flush();

            System.out.println("Build Data Success");
            CachedRowSet rs = (CachedRowSet) in.readObject();

            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                //We are using non property style for making dynamic table
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
                col.setMinWidth(200);

                table.getColumns().addAll(col);
            }

            while (rs.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }

            //FINALLY ADDED TO TableView
            table.setItems(data);

        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }


    // ADD TENANT BUTTON
    public void addTenant(String command, String name, double leaseLength, boolean rentPaid, String propID, String email, String phone, TableView table) {

            try {

                // Write out necessary information to communicate to database on server
                data = FXCollections.observableArrayList();

                out.writeUTF(command);
                out.flush();
//
                out.writeUTF(name);
                out.flush();
                out.writeDouble(leaseLength);
                out.flush();
                out.writeBoolean(rentPaid);
                out.flush();
                out.writeUTF(propID);
                out.flush();
                out.writeUTF(email);
                out.flush();
                out.writeUTF(phone);


                // Read In CachedRowSet from server for display on JavaFX TableView
                CachedRowSet rs = (CachedRowSet) in.readObject();


            while (rs.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }

            //FINALLY ADDED TO TableView
            table.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
    }

    public static void updateTenant(String command, String oldID, String nameReceived, double leaseLengthReceived, boolean rentPaidReceived, String propertyIDReceived, String emailReceived, String phoneReceived, TableView table) {

        try {

            data = FXCollections.observableArrayList();

            out.writeUTF(command);
            out.flush();
//
            out.writeUTF(oldID);
            out.writeUTF(nameReceived);
            out.flush();
            out.writeDouble(leaseLengthReceived);
            out.flush();
            out.writeBoolean(rentPaidReceived);
            out.flush();
            out.writeUTF(propertyIDReceived);
            out.flush();
            out.writeUTF(emailReceived);
            out.flush();
            out.writeUTF(phoneReceived);


            CachedRowSet rs = (CachedRowSet) in.readObject();


            while (rs.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }

            //FINALLY ADDED TO TableView
            table.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }

    }
    public static void updateProperty(String command, String oldID, String typeReceived, int numBedroomsReceived, String propertyIDReceived, double rentPriceReceived, TableView table) {
        try {

            data = FXCollections.observableArrayList();

            out.writeUTF(command);
            out.flush();
//
            out.writeUTF(oldID);
            out.writeUTF(typeReceived);
            out.flush();
            out.writeInt(numBedroomsReceived);
            out.flush();
            out.writeUTF(propertyIDReceived);
            out.flush();
            out.writeDouble(rentPriceReceived);
            out.flush();

            CachedRowSet rs = (CachedRowSet) in.readObject();


            while (rs.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }

            //FINALLY ADDED TO TableView
            table.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
    }

    public void populateDropdown(String command) throws IOException, SQLException, ClassNotFoundException {
        usnInput.getItems().clear();
        out.writeUTF(command);
        out.flush();

        CachedRowSet rs = (CachedRowSet) in.readObject();
        while(rs.next()){
            usnInput.getItems().addAll(rs.getString("propertyID"));
        }
    }


    public void updateData(String command, String query, TableView table) {
        data = FXCollections.observableArrayList();
        try{

            out.writeUTF(command);
            out.flush();
            out.writeUTF(query);
            out.flush();

            CachedRowSet rs = (CachedRowSet) in.readObject();

            while (rs.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }

            table.setItems(data);
        } catch (SQLException | ClassNotFoundException | IOException e){
            e.printStackTrace();
        }
    }

    public void deleteTenant(String command, String name, String propID, TableView table) throws IOException, ClassNotFoundException, SQLException {

        try{
            data = FXCollections.observableArrayList();
            out.writeUTF(command);
            out.flush();

            out.writeUTF(name);
            out.flush();
            out.writeUTF(propID);
            out.flush();
            CachedRowSet rsDeleteTenant= (CachedRowSet) in.readObject();


            while (rsDeleteTenant.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rsDeleteTenant.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(rsDeleteTenant.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }
            //FINALLY ADDED TO TableView
            table.setItems(data);
        } catch (Exception e){
            e.printStackTrace();
        }


    }


    /*
    EXCEL EXPORT FUNCTIONALITY

     */
    public void export(String command) throws IOException {

        String excelFilePath = "Rental-DB-Export.xls";

        try{
            out.writeUTF(command);
            out.flush();
            // RECEIVE ALL INFO FOR ALL TABLES FROM DATABASE
            CachedRowSet tenantsRS = (CachedRowSet) in.readObject();
            CachedRowSet allRS = (CachedRowSet) in.readObject();
            CachedRowSet availableRS = (CachedRowSet) in.readObject();
            CachedRowSet occupiedRS = (CachedRowSet) in.readObject();

            // create new workbook and sheets
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet tenants = workbook.createSheet("Tenants");
            XSSFSheet allProperties = workbook.createSheet("All Properties");
            XSSFSheet available = workbook.createSheet("Available Properties");
            XSSFSheet occupied = workbook.createSheet("Occupied Properties");

            // Make Call to write the header lines
            writeHeaderLine(tenants, allProperties, available, occupied);

            // Write Data to all sheets using appropriate resultset and sheet
            writeTenantData(tenantsRS, workbook, tenants);
            writeAllData(allRS, workbook, allProperties);
            writeAvailData(availableRS, workbook, available);
            writeOccupiedData(occupiedRS, workbook, occupied);

            FileOutputStream outputStream = new FileOutputStream(excelFilePath);
            workbook.write(outputStream);
            workbook.close();

        } catch (Exception e){
            e.printStackTrace();
        }


    }

    public void writeHeaderLine(XSSFSheet tenants, XSSFSheet allProperties, XSSFSheet availableProperties, XSSFSheet occupied){
        Row headerRowTenants = tenants.createRow(0);
        Cell headerCell = headerRowTenants.createCell(0);

        headerCell.setCellValue("Name");
        headerCell = headerRowTenants.createCell(1);
        headerCell.setCellValue("Lease Length (Monthly)");
        headerCell = headerRowTenants.createCell(2);
        headerCell.setCellValue("Property ID");
        headerCell = headerRowTenants.createCell(3);
        headerCell.setCellValue("Rent Paid?");
        headerCell = headerRowTenants.createCell(4);
        headerCell.setCellValue("Email");
        headerCell = headerRowTenants.createCell(5);
        headerCell.setCellValue("Phone");

        Row headerRowProperties = allProperties.createRow(0);
        Cell propHeader = headerRowProperties.createCell(0);
        propHeader.setCellValue("Type");
        propHeader = headerRowProperties.createCell(1);
        propHeader.setCellValue("Bedrooms");
        propHeader = headerRowProperties.createCell(2);
        propHeader.setCellValue("Property ID");
        propHeader = headerRowProperties.createCell(3);
        propHeader.setCellValue("Cost");
        propHeader = headerRowProperties.createCell(4);
        propHeader.setCellValue("Availability");

        Row headerAvailable = availableProperties.createRow(0);
        Cell availHeader = headerAvailable.createCell(0);
        availHeader.setCellValue("Type");
        availHeader = headerAvailable.createCell(1);
        availHeader.setCellValue("Bedrooms");
        availHeader = headerAvailable.createCell(2);
        availHeader.setCellValue("Property ID");
        availHeader = headerAvailable.createCell(3);
        availHeader.setCellValue("Cost");

        Row headerOccupied = occupied.createRow(0);
        Cell occupiedHeader = headerOccupied.createCell(0);
        occupiedHeader.setCellValue("Name");
        occupiedHeader = headerOccupied.createCell(1);
        occupiedHeader.setCellValue("Property ID");
        occupiedHeader = headerOccupied.createCell(2);
        occupiedHeader.setCellValue("Cost");









    }

    public void writeTenantData(CachedRowSet rs, XSSFWorkbook workbook, XSSFSheet sheet) throws SQLException {

        int rowCount = 1;

        while(rs.next()){
            String name = rs.getString("name");
            double leaseLength = rs.getDouble("Lease Length (Monthly)");
            String propertyID = rs.getString("Property ID");
            String rentPaid = rs.getString("RentPaid");
            String email = rs.getString("Email");
            String phone = rs.getString("Phone");

            Row row = sheet.createRow(rowCount++);

            int columnCount=0;
            Cell cell = row.createCell(columnCount++);
            cell.setCellValue(name);
            cell = row.createCell(columnCount++);
            cell.setCellValue(leaseLength);
            cell = row.createCell(columnCount++);
            cell.setCellValue(propertyID);
            cell = row.createCell(columnCount++);
            cell.setCellValue(rentPaid);
            cell = row.createCell(columnCount++);
            cell.setCellValue(email);
            cell = row.createCell(columnCount++);
            cell.setCellValue(phone);


        }

    }




    public void writeAllData(CachedRowSet rs, XSSFWorkbook workbook, XSSFSheet sheet) throws SQLException {

        int rowCount = 1;

        while(rs.next()){
            String type = rs.getString("Rental Type");
            int bedrooms = rs.getInt("Bedroom Count");
            String propertyID = rs.getString("Property ID");
            double cost = rs.getDouble("Cost (Monthly/Nightly)");
            String availability = rs.getString("Availability");

            Row row = sheet.createRow(rowCount++);

            int columnCount=0;
            Cell cell = row.createCell(columnCount++);
            cell.setCellValue(type);
            cell = row.createCell(columnCount++);
            cell.setCellValue(bedrooms);
            cell = row.createCell(columnCount++);
            cell.setCellValue(propertyID);
            cell = row.createCell(columnCount++);
            cell.setCellValue(cost);
            cell = row.createCell(columnCount++);
            cell.setCellValue(availability);


        }
    }

    public void writeAvailData(CachedRowSet rs, XSSFWorkbook workbook, XSSFSheet sheet) throws SQLException {

        int rowCount = 1;

        while(rs.next()){
            String type = rs.getString("Rental Type");
            int bedrooms = rs.getInt("Bedroom Count");
            String propertyID = rs.getString("Property ID");
            double cost = rs.getDouble("Cost (Monthly/Nightly)");

            Row row = sheet.createRow(rowCount++);

            int columnCount=0;
            Cell cell = row.createCell(columnCount++);
            cell.setCellValue(type);
            cell = row.createCell(columnCount++);
            cell.setCellValue(bedrooms);
            cell = row.createCell(columnCount++);
            cell.setCellValue(propertyID);
            cell = row.createCell(columnCount++);
            cell.setCellValue(cost);




        }
    }

    public void writeOccupiedData(CachedRowSet rs, XSSFWorkbook workbook, XSSFSheet sheet) throws SQLException {

        int rowCount = 1;

        while (rs.next()) {
            String name = rs.getString("Tenant Name");
            String propertyID = rs.getString("Property ID");
            double cost = rs.getDouble("Cost (Monthly/Nightly)");

            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;
            Cell cell = row.createCell(columnCount++);
            cell.setCellValue(name);
            cell = row.createCell(columnCount++);
            cell.setCellValue(propertyID);
            cell = row.createCell(columnCount++);
            cell.setCellValue(cost);

        }
    }
    

    

} // END CLIENT.JAVA
