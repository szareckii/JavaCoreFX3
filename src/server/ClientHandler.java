package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nick;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    try {

                        socket.setSoTimeout(120000);

                        //цикл аутентификации
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/reg ")) {
                                String[] token = str.split(" ");

                                if (token.length < 4) {
                                    continue;
                                }

                                boolean succeed = server.getAuthService().registation(token[1], token[2], token[3]);

                                if (succeed) {
                                    sendMsg("Регистрация прошла успешно");
                                } else {
                                    sendMsg("Регистрация не удалась. \n" +
                                            "Возможно логин уже занят, или данные содержат пробел");
                                }
                            }

                            if (str.startsWith("/auth ")) {
                                String[] token = str.split(" ");

                                if (token.length < 3) {
                                    continue;
                                }

                                String newNick = server.getAuthService()
                                        .getNicknameByLoginAndPassword(token[1], token[2]);

                                login = token[1];

                                if (newNick != null) {
                                    if (!server.isLoginAuthorized(login)) {
                                        sendMsg("/authok " + newNick);
                                        nick = newNick;
                                        server.subscribe(this);
                                        System.out.println("Клиент: " + nick + " подключился");

                                        socket.setSoTimeout(0);
                                        break;
                                    } else {
                                        sendMsg("С эти логином уже прошли аутентификацию");
                                    }

                                } else {
                                    sendMsg("Неверный логин / пароль");
                                }
                            }
                        }

                        //цикл работы
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/")) {

                                if (str.equals("/end")) {
                                    sendMsg("/end");
                                    break;
                                }

                                if (str.startsWith("/w")) {
                                    String[] token = str.split(" ", 3);

                                    if (token.length < 3)
                                        continue;

                                    server.privateMsg(this, token[1], token[2]);
                                }

                            } else {
                                server.broadcastMsg(nick, str);
                            }
                        }

                    } catch (SocketTimeoutException e) {
                        sendMsg("Соединение закрыто");
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Клиент отключился");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }
}
