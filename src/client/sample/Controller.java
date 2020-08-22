package client.sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Controller {

    @FXML
    TextArea mainTextArea;

    @FXML
    TextField messageArea;

    @FXML
    Button sendButton;

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public void clickAction(ActionEvent actionEvent) {
        if (!messageArea.getText().trim().isEmpty()) {
            if (socket == null || socket.isClosed()) {
                ClientService();
            }
            try {
                dos.writeUTF(messageArea.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
            messageArea.clear();
        }
    }

    public void enterPressed(ActionEvent actionEvent) {
        clickAction(actionEvent);
    }

    public void ClientService() {

        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            setAutorized(false);
            Thread t1 = new Thread(() -> {
                try {
                    while (true) {
                        String strMsg = dis.readUTF();
                        if (strMsg.startsWith("/authOk")) {
                            setAutorized(true);
                            break;
                        }
                        mainTextArea.appendText(strMsg + "\n");
                    }
                    while (true) {
                        String strMsg = dis.readUTF();
                        if (strMsg.equals("/exit")) {
                            break;
                        }
                        mainTextArea.appendText(strMsg + "\n");
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

    private void setAutorized(boolean b) {
        if (b) {
            mainTextArea.setStyle("-fx-background-color:#010000;");
        } else {
            mainTextArea.setStyle("-fx-background-color:#ff0000;");
        }
    }

}
