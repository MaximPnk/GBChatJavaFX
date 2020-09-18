package server.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.handler.ClientHandler;
import server.inter.AuthService;
import server.inter.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ServerImpl implements Server {

    public static final Logger LOGGER = LogManager.getLogger(ServerImpl.class);

    public List<ClientHandler> clients;
    private AuthService authService;

    public ServerImpl() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            authService = new AuthServiceImpl();
            authService.start();
            clients = new LinkedList<>();
            LOGGER.info("Server starting normal");
            while (true) {
                LOGGER.info("Waiting for clients");
                Socket socket = serverSocket.accept();
                LOGGER.info("Client join");
                new ClientHandler(this, socket);
            }
        } catch (IOException | SQLException e) {
            LOGGER.warn("There are problems on the server");
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    @Override
    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler c : clients) {
            if (c.getNick() != null && c.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    @Override
    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
    }

    @Override
    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public synchronized void changeNick(String oldNick, String newNick) {
        for (ClientHandler c : clients) {
            if (oldNick.equalsIgnoreCase(c.getNick())) {
                c.setNick(newNick);
            }
        }
    }

    @Override
    public AuthService getAuthService() {
        return authService;
    }
}