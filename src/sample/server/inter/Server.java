package sample.server.inter;


import sample.server.handler.ClientHandler;

public interface Server {
    int PORT = 8189;

    boolean isNickBusy(String nick);

    void broadcastMsg(String msg);

    void subscribe(ClientHandler client);

    void unsubscribe(ClientHandler client);

    AuthService getAuthService();

    void sendMsgToClient(ClientHandler sender, String receiver, String msg);

}
