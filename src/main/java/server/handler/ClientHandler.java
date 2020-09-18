package server.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.service.AuthServiceImpl;
import server.service.ServerImpl;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

    private static final Logger LOGGER = LogManager.getLogger(AuthServiceImpl.class);
    private ServerImpl server;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isClosed;
    private int timeout = 120;

    private volatile String nick;

    public ClientHandler(ServerImpl server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.nick = "";
            ExecutorService service = Executors.newCachedThreadPool();
            service.execute(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
            service.execute(() -> {
                try {
                    Thread.sleep(timeout * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (getNick().equals("")) {
                    sendMsg("Your connection closed due to timeout");
                    sendMsg("/disconnect");
                    service.shutdownNow();
                    closeConnection();
                }
            });
            /*Thread connection = new Thread(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
            connection.start();
            new Thread(() -> {
                try {
                    Thread.sleep(timeout * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (getNick().equals("")) {
                    sendMsg("Your connection closed due to timeout");
                    sendMsg("/disconnect");
                    connection.interrupt();
                    closeConnection();
                }
            }).start();*/
        } catch (IOException e) {
            LOGGER.error("Problems with creating a client handler");
            throw new RuntimeException("Problems with creating a client handler");
        }
    }

    private void authentication() throws IOException {
        while (!isClosed) {
            if (dis.available() > 0) {
                String str = dis.readUTF();
                if (str.startsWith("/auth") && str.split("\\s").length == 3) {
                    String[] dataArray = str.split("\\s");
                    String nick = server.getAuthService().getNick(dataArray[1], dataArray[2]);
                    if (nick != null) {
                        if (!server.isNickBusy(nick)) {
                            sendMsg("/authOk " + nick);
                            this.nick = nick;
                            server.subscribe(this);
                            server.broadcastMsg(this.nick + " connected");
                            return;
                        } else {
                            sendMsg("You are already logged in");
                        }
                    } else {
                        sendMsg("Incorrect login or password");
                    }
                }
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            LOGGER.info(nick + " closed chat and disconnected");
        }
    }

    public void readMessage() {
        try {
            while (true) {
                String clientStr = dis.readUTF();
                LOGGER.info("from " + this.nick + ": " + clientStr);

                if (clientStr.startsWith("/")) {
                    if (clientStr.startsWith("/w") && clientStr.split("\\s").length > 2) {
                        String toUser = clientStr.split("\\s")[1];
                        String msg = clientStr.split("\\s", 3)[2];
                        privateMsg(ClientHandler.this, toUser, msg);
                    } else if (clientStr.equals("/exit")) {
                        return;
                    } else if (clientStr.equals("/online")) {
                        sendMsg("Users online:");
                        for (ClientHandler c : server.clients) {
                            sendMsg(c.getNick());
                        }
                    } else if (clientStr.startsWith("/changeNick") && clientStr.split("\\s").length == 2) {
                        server.broadcastMsg(String.format("%s changed nick to %s", this.getNick(), clientStr.split("\\s")[1]));
                        server.getAuthService().setNick(this.getNick(), clientStr.split("\\s")[1]);
                        server.changeNick(this.getNick(), clientStr.split("\\s")[1]);
                    } else if (clientStr.equals("/commands")) {
                        sendMsg("/w nick - whisper to smbd");
                        sendMsg("/online - show who is online");
                        sendMsg("/changeNick - change your nick");
                        sendMsg("/exit - leave the chat");
                    } else {
                        sendMsg("Wrong command");
                    }
                } else {
                    server.broadcastMsg(nick + ": " + clientStr);
                }
            }
        } catch (IOException e) {
            sendMsg("Wrong command");
        }
    }

    public void privateMsg(ClientHandler fromUser, String toUser, String msg) {
        if (!server.isNickBusy(toUser)) {
            sendMsg(toUser + " is not connected!");
        } else {
            for (ClientHandler c : server.clients) {
                if (toUser.equals(c.getNick())) {
                    c.sendMsg("Whisper from " + fromUser.getNick() + ": " + msg);
                    break;
                }
            }
            fromUser.sendMsg("Whisper to: " + toUser + " " + msg);
        }
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    private void closeConnection() {
        isClosed = true;
        server.unsubscribe(this);
        server.broadcastMsg(this.nick + ": exit chat");

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