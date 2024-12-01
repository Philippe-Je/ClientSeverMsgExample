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

public class MainController implements Initializable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isServer = false;

    @FXML
    private ComboBox<String> dropdownPort;

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

    private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dropdownPort.getItems().addAll("7", "13", "21", "23", "71", "80", "119", "161");
    }

    @FXML
    void checkConnection(ActionEvent event) {
        String host = urlName.getText();
        int port = Integer.parseInt(dropdownPort.getValue());

        try {
            Socket sock = new Socket(host, port);
            resultArea.appendText(host + " listening on port " + port + "\n");
            sock.close();
        } catch (UnknownHostException e) {
            resultArea.setText(e.toString() + "\n");
        } catch (Exception e) {
            resultArea.appendText(host + " not listening on port " + port + "\n");
        }
    }

    @FXML
    void clearBtn(ActionEvent event) {
        resultArea.clear();
        urlName.clear();
    }

    @FXML
    void startServer(ActionEvent event) {
        isServer = true;
        createChatWindow("Server");
        new Thread(this::runServer).start();
    }

    @FXML
    void startClient(ActionEvent event) {
        isServer = false;
        createChatWindow("Client");
        new Thread(this::connectToServer).start();
    }

    private void createChatWindow(String title) {
        Stage stage = new Stage();
        Group root = new Group();

        Label titleLabel = new Label(title);
        titleLabel.setLayoutX(100);
        titleLabel.setLayoutY(100);

        statusLabel = new Label("Initializing...");
        statusLabel.setLayoutX(100);
        statusLabel.setLayoutY(200);

        TextArea chatArea = new TextArea();
        chatArea.setLayoutX(100);
        chatArea.setLayoutY(250);
        chatArea.setPrefSize(400, 200);
        chatArea.setEditable(false);

        TextField messageField = new TextField();
        messageField.setLayoutX(100);
        messageField.setLayoutY(460);
        messageField.setPrefWidth(300);

        Button sendButton = new Button("Send");
        sendButton.setLayoutX(410);
        sendButton.setLayoutY(460);
        sendButton.setOnAction(e -> sendMessage(messageField.getText(), chatArea));

        root.getChildren().addAll(titleLabel, statusLabel, chatArea, messageField, sendButton);
        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    private void runServer() {
        try {
            serverSocket = new ServerSocket(6666);
            updateStatus("Waiting for client connection...");
            clientSocket = serverSocket.accept();
            updateStatus("Client connected!");

            setupCommunication();
        } catch (IOException e) {
            updateStatus("Error: " + e.getMessage());
        }
    }

    private void connectToServer() {
        try {
            clientSocket = new Socket("localhost", 6666);
            updateStatus("Connected to server!");

            setupCommunication();
        } catch (IOException e) {
            updateStatus("Error: " + e.getMessage());
        }
    }

    private void setupCommunication() throws IOException {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            String finalInputLine = inputLine;
            Platform.runLater(() -> appendToChatArea((isServer ? "Client: " : "Server: ") + finalInputLine));
        }
    }

    private void sendMessage(String message, TextArea chatArea) {
        if (out != null && !message.isEmpty()) {
            out.println(message);
            appendToChatArea((isServer ? "Server: " : "Client: ") + message);
            ((TextField) chatArea.getParent().getChildrenUnmodifiable().stream()
                    .filter(node -> node instanceof TextField)
                    .findFirst()
                    .orElse(null)).clear();
        }
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    private void appendToChatArea(String message) {
        Platform.runLater(() -> {
            TextArea chatArea = (TextArea) statusLabel.getParent().getChildrenUnmodifiable().stream()
                    .filter(node -> node instanceof TextArea)
                    .findFirst()
                    .orElse(null);
            if (chatArea != null) {
                chatArea.appendText(message + "\n");
            }
        });
    }
}