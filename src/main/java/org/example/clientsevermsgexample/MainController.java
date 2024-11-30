package org.example.clientsevermsgexample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

public class MainController implements Initializable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isServer = false;

    @FXML
    private ComboBox dropdownPort;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dropdownPort.getItems().addAll("7",     // ping
                "13",     // daytime
                "21",     // ftp
                "23",     // telnet
                "71",     // finger
                "80",     // http
                "119",     // nntp (news)
                "161"      // snmp);
        );
    }

    @FXML
    private Button clearBtn;


    @FXML
    private TextArea resultArea;

    @FXML
    private Label server_lbl;

    @FXML
    private Button testBtn;

    @FXML
    private Label test_lbl;

    @FXML
    private TextField urlName;

    Socket socket1;

    Label lb122, lb12;
    TextField msgText;

    @FXML
    void checkConnection(ActionEvent event) {

        String host = urlName.getText();
        int port = Integer.parseInt(dropdownPort.getValue().toString());

        try {
            Socket sock = new Socket(host, port);
            resultArea.appendText(host + " listening on port " + port + "\n");
            sock.close();
        } catch (UnknownHostException e) {
            resultArea.setText(String.valueOf(e) + "\n");
            return;
        } catch (Exception e) {
            resultArea.appendText(host + " not listening on port "
                    + port + "\n");
        }


    }


    @FXML
    void clearBtn(ActionEvent event) {
        resultArea.setText("");
        urlName.setText("");

    }


    @FXML
    void startServer(ActionEvent event) {
        isServer = true;
        Stage stage = new Stage();
        Group root = new Group();
        Label lb11 = new Label("Server");
        lb11.setLayoutX(100);
        lb11.setLayoutY(100);

        lb12 = new Label("Server is running and waiting for a client...");
        lb12.setLayoutX(100);
        lb12.setLayoutY(200);

        TextArea chatArea = new TextArea();
        chatArea.setLayoutX(100);
        chatArea.setLayoutY(250);
        chatArea.setPrefSize(400, 200);

        TextField messageField = new TextField();
        messageField.setLayoutX(100);
        messageField.setLayoutY(460);
        messageField.setPrefWidth(300);

        Button sendButton = new Button("Send");
        sendButton.setLayoutX(410);
        sendButton.setLayoutY(460);
        sendButton.setOnAction(e -> sendMessage(messageField.getText(), chatArea));

        root.getChildren().addAll(lb11, lb12, chatArea, messageField, sendButton);
        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.setTitle("Server");
        stage.show();

        new Thread(() -> runServer(chatArea)).start();
    }

    String message;


    private void runServer(TextArea chatArea) {
        try {
            serverSocket = new ServerSocket(6666);
            updateServer("Waiting for client connection...");
            clientSocket = serverSocket.accept();
            updateServer("Client connected!");

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String finalInputLine = inputLine;
                Platform.runLater(() -> chatArea.appendText("Client: " + finalInputLine + "\n"));
            }
        } catch (IOException e) {
            updateServer("Error: " + e.getMessage());
        }
    }
    private void updateServer(String message) {
        // Run on the UI thread
        javafx.application.Platform.runLater(() -> lb12.setText(message + "\n"));
    }


    @FXML
    void startClient(ActionEvent event) {
        isServer = false;
        Stage stage = new Stage();
        Group root = new Group();
        Label lb11 = new Label("Client");
        lb11.setLayoutX(100);
        lb11.setLayoutY(100);

        lb122 = new Label("Connecting to server...");
        lb122.setLayoutX(100);
        lb122.setLayoutY(200);

        TextArea chatArea = new TextArea();
        chatArea.setLayoutX(100);
        chatArea.setLayoutY(250);
        chatArea.setPrefSize(400, 200);

        TextField messageField = new TextField();
        messageField.setLayoutX(100);
        messageField.setLayoutY(460);
        messageField.setPrefWidth(300);

        Button sendButton = new Button("Send");
        sendButton.setLayoutX(410);
        sendButton.setLayoutY(460);
        sendButton.setOnAction(e -> sendMessage(messageField.getText(), chatArea));

        root.getChildren().addAll(lb11, lb122, chatArea, messageField, sendButton);
        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.setTitle("Client");
        stage.show();

        new Thread(() -> connectToServer(chatArea)).start();
    }

    private void connectToServer(TextArea chatArea) {
        try {
            clientSocket = new Socket("localhost", 6666);
            updateTextClient("Connected to server!");

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String finalInputLine = inputLine;
                Platform.runLater(() -> chatArea.appendText("Server: " + finalInputLine + "\n"));
            }
        } catch (IOException e) {
            updateTextClient("Error: " + e.getMessage());
        }
    }

    private void sendMessage(String message, TextArea chatArea) {
        if (out != null && !message.isEmpty()) {
            out.println(message);
            Platform.runLater(() -> {
                chatArea.appendText((isServer ? "Server: " : "Client: ") + message + "\n");
            });
        }
    }

    private void updateTextClient(String message) {
        Platform.runLater(() -> lb122.setText(message));
    }
}