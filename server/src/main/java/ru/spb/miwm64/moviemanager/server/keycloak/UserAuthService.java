package ru.spb.miwm64.moviemanager.server.keycloak;

import ru.spb.miwm64.moviemanager.common.net.LoginResult;

public interface UserAuthService {
    String createUser(UserInfo userInfo, String password);
    boolean deleteUser(String userId, String token);

    public void updateUserInfo(UserInfo userInfo, String token);
    public UserInfo getUserInfo(UserInfo userInfo);

    public LoginResult login(String username, String password);
    public boolean validateToken(String token);
    public String getUserIdFromToken(String token);

    String getUserIdByUsername(String username);
}
