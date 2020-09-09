package sample.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Controller {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String myNick;

    private File log;
    private int SIZE = 100; // количество выводимых строк чата

    @FXML
    TextArea mainTextArea;

    @FXML
    TextField textField;

    public Controller() {
    }

    public void start() {
        myNick = "";

        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            Thread t1 = new Thread(() -> {
                try {
                    while (true) {
                        String strMsg = dis.readUTF();
                        if (strMsg.startsWith("/authOk")) {
                            getLog();
                            myNick = strMsg.split("\\s")[1];
                            mainTextArea.appendText(strMsg + "\n");
                            break;
                        }
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
                } finally {
                    try {
                        mainTextArea.appendText("Вы вышли из чата.");
                        log();
                        socket.close();
                        myNick = "";
                        System.exit(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t1.setDaemon(true);
            t1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            start();
        }
        try {
            if (textField.getText().trim().isEmpty() || textField.getText().trim().equals("")) {
                textField.clear();
                return;
            }
            dos.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            System.out.println("По техническим причинам сообщение не было отправлено");
        }
    }

    public void log() throws IOException {
        PrintWriter pw = null;

        try {
            log = new File("src\\sample\\client\\log.txt");

            pw = new PrintWriter(new BufferedWriter(new FileWriter(log, true)));

            String[] row = mainTextArea.getText().split("\n");

            for (int i = 0; i < SIZE; i++) {
                mainTextArea.setText(mainTextArea.getText().replace(row[i], ""));
            }
            mainTextArea.deleteText(0, SIZE);

            pw.println(mainTextArea.getText());
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLog() {
        try {
            log = new File("src\\sample\\client\\log.txt");

            List<String> logs= new ArrayList<>();

            FileInputStream fis = new FileInputStream(log);

            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String str;
            while ((str = br.readLine()) != null) {
                logs.add(str);
            }

            if (logs.size() > SIZE) {
                for (int i = logs.size() - SIZE; i < logs.size(); i++) {
                    mainTextArea.appendText(logs.get(i) + "\n");
                }
            }
            else {
                for (int i = 0; i < logs.size(); i++) {
                    mainTextArea.appendText(logs.get(i) + "\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
