package server;

public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String password);
    boolean registation(String login, String password, String nickname);
}
