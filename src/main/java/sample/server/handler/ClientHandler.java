package sample.server.handler;

import sample.server.inter.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String nick;

    public String getNick() {
        return this.nick;
    }

    public static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.nick = "";
            new Thread(() -> {
                try {
                    boolean authenticationSuccessful = false;
                    int timeout = 120000;
                    long connectionTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - connectionTime <= timeout) {
                        authenticationSuccessful = authentication();
                        if(authenticationSuccessful){break;}
                    }
                    if(authenticationSuccessful) {
                        readMessage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    private boolean authentication() throws IOException {
        while (true) {
            String str = dis.readUTF();
            if (str.startsWith("/auth")) {
                String[] dataArray = str.split("\\s");
                String nick = server.getAuthService().getNick(dataArray[1], dataArray[2]);
                if (nick != null) {
                    if (!server.isNickBusy(nick)) {
                        sendMsg("/authOk " + nick);
                        this.nick = nick;
                        LOGGER.log(Level.INFO, this.nick + " join to chat");
                        server.broadcastMsg(this.nick + " join the chat");
                        server.subscribe(this);
                    } else {
                        LOGGER.log(Level.WARN, this.nick + " already logged in");
                        sendMsg("You are already logged in");
                    }
                    return true;
                } else {
                    LOGGER.log(Level.WARN, "Incorrect password or login");
                    sendMsg("Incorrect password or login");
                    return false;
                }
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            dos.writeUTF(msg);
            LOGGER.log(Level.INFO, getNick() + " sendMsg");
        } catch (IOException e) {
            LOGGER.log(Level.WARN, getNick() + "Error 'sendMsg'");
        }
    }

    public void readMessage() throws IOException {
        while (true) {
            String clientStr = dis.readUTF();
            if (clientStr.startsWith("/")) {
                if (clientStr.equals("/exit")) {
                    LOGGER.log(Level.INFO, getNick() + " send message '/exit'");
                    return;
                }
                if (clientStr.startsWith("/w")) {
                    String[] strArray = clientStr.split("\\s");
                    String nickName = strArray[1];
                    String msg = clientStr.substring(4 + nickName.length());
                    server.sendMsgToClient(this, nickName, msg);
                    LOGGER.log(Level.INFO, getNick() + "send private message");
                }
                continue;
            }
            server.broadcastMsg(this.nick + ": " + clientStr);
            LOGGER.log(Level.INFO, getNick() + " send message to all users");
        }
    }

    private void closeConnection() {
        server.unsubscribe(this);
        server.broadcastMsg(this.nick + ": out from chat");
        LOGGER.log(Level.INFO, this.nick + " out from chat");

        try {
            dis.close();
        } catch (IOException e) {
            //e.printStackTrace();
            LOGGER.log(Level.TRACE, e);
        }

        try {
            dos.close();
        } catch (IOException e) {
            //e.printStackTrace();
            LOGGER.log(Level.TRACE, e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            //e.printStackTrace();
            LOGGER.log(Level.TRACE, e);
        }
    }
}
