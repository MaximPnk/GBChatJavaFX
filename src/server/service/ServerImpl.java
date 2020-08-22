package server.service;

import server.handler.ClientHandler;
import server.inter.AuthService;
import server.inter.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerImpl implements Server {

    public List<ClientHandler> clients;
    private AuthService authService;

    public ServerImpl() {
        try {
            ServerSocket serverSocket = new ServerSocket(8189);
            authService = new AuthServiceImpl();
            authService.start();
            clients = new LinkedList<>();
            while (true) {
                System.out.println("Wait join clients");
                Socket socket = serverSocket.accept();
                System.out.println("Client join");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.out.println("Problem in server");
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

    @Override
    public AuthService getAuthService() {
        return authService;
    }
}