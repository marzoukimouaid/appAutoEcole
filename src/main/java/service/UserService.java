package service;

import dao.UserDao;

public class UserService {
    private final UserDao userDAO = new UserDao();

    public String authenticateUser(String username, String password) {
        return userDAO.authenticateUser(username, password);
    }

    public boolean createUser(String username, String password, String role) {
        return userDAO.createUser(username, password, role);
    }
}
