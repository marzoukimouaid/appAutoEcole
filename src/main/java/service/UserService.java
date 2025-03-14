package service;

import dao.UserDao;
import entite.User;

public class UserService {
    private final UserDao userDAO = new UserDao();

    public User authenticateUser(String username, String password) {
        return userDAO.getUserByUsernameAndPassword(username, password);
    }

    public boolean createUser(String username, String password, String role) {
        return userDAO.createUser(username, password, role);
    }

    public int getUserIdByUsername(String username) {
        return userDAO.getUserIdByUsername(username);
    }
}
