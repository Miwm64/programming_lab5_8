package ru.spb.miwm64.moviemanager.server.keycloak;

public interface UserAuthService {
    String createUser(UserInfo userInfo, String password);
    void deleteUser(String userId, String token);

    public void updateUserInfo(UserInfo userInfo, String token);
    public UserInfo getUserInfo(UserInfo userInfo);

    public String login(String username, String password);
    public boolean validateToken(String token);
    public String getUserIdFromToken(String token);
}
