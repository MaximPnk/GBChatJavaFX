package server.inter;

import server.service.AuthServiceImpl;

import java.sql.SQLException;
import java.util.List;

public interface AuthService {
    void start();

    String getNick(String login, String password);

    void stop();

    void setNick(String oldNick, String newNick);

}