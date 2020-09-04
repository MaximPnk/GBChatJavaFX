package server.service;

import server.db.DBConnection;
import server.handler.ClientHandler;
import server.inter.AuthService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class AuthServiceImpl implements AuthService {
    PreparedStatement ps;

    public List<UserEntity> usersList;

    public AuthServiceImpl() throws SQLException {
        this.usersList = new LinkedList<>();

        ps = DBConnection
                .getInstance()
                .getConnection()
                .prepareStatement("SELECT * FROM users");
        ResultSet set = ps.executeQuery();
        while (set.next()) {
            this.usersList.add(new UserEntity(
                    set.getString("LOGIN"),
                    set.getString("PASS"),
                    set.getString("NICK")));
        }
    }

    @Override
    public void start() {
        System.out.println("Server run");
    }

    @Override
    public String getNick(String login, String password) {

        for (UserEntity u : usersList) {

            if (u.login.equals(login) && u.password.equals(password)) {
                return u.nick;
            }

        }
        return null;
    }

    @Override
    public void setNick(String oldNick, String newNick) {
        for (UserEntity u : usersList) {
            if (u.nick.equalsIgnoreCase(oldNick)) {
                u.nick = newNick;
            }
        }
        try {
            ps = DBConnection.getInstance().getConnection()
                    .prepareStatement("UPDATE users SET NICK=? WHERE NICK=?");
            ps.setString(1, newNick);
            ps.setString(2, oldNick);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println("Authentication service stopped");

    }

    public class UserEntity {
        private String login;
        private String password;
        private String nick;

        public UserEntity(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }

        public String getNick() {
            return nick;
        }
    }
}
