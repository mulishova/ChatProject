package sample.server.service;

import sample.server.inter.AuthService;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class AuthServiceImpl implements AuthService {

    private List<UserEntity> usersList;

    public static final Logger LOGGER = LogManager.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl() {
        this.usersList = new LinkedList<>();
        this.usersList.add(new UserEntity("login1", "pass1", "nick1"));
        this.usersList.add(new UserEntity("login2", "pass2", "nick2"));
        this.usersList.add(new UserEntity("login3", "pass3", "nick3"));
        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public void start() {
        LOGGER.log(Level.INFO, "Сервис аутентификации запущен");
        System.out.println("Сервис аутентификации запущен");
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
    public void stop() {
        LOGGER.log(Level.INFO, "Сервис аутентификации остановлен");
        System.out.println("Сервис аутентификации остановлен");

    }

    private class UserEntity {
        private String login;
        private String password;
        private String nick;

        public UserEntity(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }
}
