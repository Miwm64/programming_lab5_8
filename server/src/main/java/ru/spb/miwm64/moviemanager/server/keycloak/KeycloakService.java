package ru.spb.miwm64.moviemanager.server.keycloak;

public class KeycloakService implements UserAuthService{
    public KeycloakService(){}


    @Override
    public String createUser(UserInfo userInfo, String password) {
        return "";
    }

    @Override
    public void deleteUser(String userId, String token) {

    }

    @Override
    public void updateUserInfo(UserInfo userInfo, String token) {

    }

    @Override
    public UserInfo getUserInfo(UserInfo userInfo) {
        return null;
    }

    @Override
    public String login(String username, String password) {
        return "";
    }

    @Override
    public boolean validateToken(String token) {
        return false;
    }
}
