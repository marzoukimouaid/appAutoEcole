package service;

import dao.UserDao;
import entite.User;
import java.util.List;

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

    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }


    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public List<User> getSecretaires() {
        return userDAO.getUsersByRole("secretaire");
    }

    public boolean updateUserPassword(int userId, String newPassword) {
        return userDAO.updateUserPassword(userId, newPassword);
    }

}
