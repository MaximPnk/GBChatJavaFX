package client.sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class Controller {

    @FXML
    TextArea mainTextArea;

    @FXML
    TextField messageArea;

    @FXML
    Button sendButton;

    @FXML
    TextField loginArea;

    @FXML
    TextField passwordArea;

    @FXML
    Button loginButton;

    @FXML
    HBox logPassBox;

    @FXML
    HBox chatBox;

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isClosed;
    private ArrayList<String> history;

    public void clickAction(ActionEvent actionEvent) {
        if (!messageArea.getText().trim().isEmpty()) {
            if (socket == null || socket.isClosed()) {
                clientService();
            }
            try {
                dos.writeUTF(messageArea.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
            messageArea.clear();
        }
    }

    public void loginAction(ActionEvent actionEvent) {
        if (!loginArea.getText().trim().isEmpty() && !passwordArea.getText().trim().isEmpty()) {
            if (socket == null || socket.isClosed()) {
                clientService();
            }
            try {
                dos.writeUTF(String.format("/auth %s %s", loginArea.getText(), passwordArea.getText()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loginArea.clear();
        passwordArea.clear();
    }

    public void clientService() {

        try {
            isClosed = false;
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            setAuth(false);
            Thread t1 = new Thread(() -> {
                try {
                    while (!isClosed) {
                        if (dis.available() > 0) {
                            String strMsg = dis.readUTF();
                            if (strMsg.startsWith("/authOk")) {
                                setAuth(true);
                                history = new ArrayList<>();
                                try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/History.txt"))) {
                                    while (reader.ready()) {
                                        history.add(reader.readLine());
                                    }
                                }
                                if (history.size() > 100) {
                                    Platform.runLater(() -> mainTextArea.setText(String.join("\n", history.subList(history.size()-100, history.size()-1)) + "\n\n" + "End of history" + "\n\n"));
                                } else {
                                    Platform.runLater(() -> mainTextArea.setText(String.join("\n", history) + "\n\n" + "End of history" + "\n\n"));
                                }
                                mainTextArea.setScrollTop(Double.MIN_VALUE);
                                break;
                            } else if (strMsg.equals("/disconnect")) {
                                closeConnection();
                                return;
                            }
                            mainTextArea.appendText(strMsg + "\n");
                        }
                    }
                    while (!isClosed) {
                        String strMsg = dis.readUTF();
                        if (strMsg.equals("/exit")) {
                            break;
                        }
                        mainTextArea.appendText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " " + strMsg + "\n");
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/History.txt", true))) {
                            writer.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")) + " " + strMsg + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t1.setDaemon(true);
            t1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAuth(boolean b) {
        if (b) {
            logPassBox.setVisible(false);
            chatBox.setVisible(true);
        } else {
            logPassBox.setVisible(true);
            chatBox.setVisible(false);
        }
    }

    private void closeConnection() {
        isClosed = true;

        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
