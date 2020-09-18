package sample.server.service;

import sample.server.handler.ClientHandler;
import sample.server.inter.AuthService;
import sample.server.inter.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerImpl implements Server {

    private List<ClientHandler> clients;
    private AuthService authService;

    public ServerImpl() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT); // Создаем сокет на сервере
            authService = new AuthServiceImpl(); // Создаем список зарегистрированных учетных записей
            clients = new LinkedList<>(); // Создаем список клиентов
            // Цикл подключения клиентов
            while (true) { // Подключение клиентов
                System.out.println("Ожидаем подключения клиентов");
                Socket socket = serverSocket.accept(); // Ожидание подключения клиента
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket); // Создаем для каждого клиент свой обработчик
            }
        } catch (IOException e) {
            System.out.println("Проблема на сервере");
        } finally {
            if (authService != null) {
                authService.stop(); // Сообщение об остановке сервера аутентификации
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

    // Метод рассылки сообщений списку
    @Override
    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    // Метод добавления клиента в список рассылки сообщений
    @Override
    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
    }

    // Метод удаления клиента из рассылки
    @Override
    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }


    @Override
    public AuthService getAuthService() {
        return authService;
    }

    // Метод отсылки приватного сообщения
    @Override
    public synchronized void sendMsgToClient(ClientHandler sender, String receiver, String msg) {
        for (ClientHandler c : clients) {
            if (c.getNick().equals(receiver)) {
                c.sendMsg("Private from " + sender.getNick() + ": " + msg);
                sender.sendMsg("Private to: " + receiver + ": " + msg);
                return;
            } else {
                sender.sendMsg(receiver + " не подключен к чату либо пользователь с таким ником отсутствует.");
            }
        }
    }
}
