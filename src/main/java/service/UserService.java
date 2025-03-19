package service;

import dao.UserDao;
import entite.User;
import java.util.List;

public class UserService {
    private final UserDao userDAO = new UserDao();

    /**
     * Authenticates a user by verifying the password.
     */
    public User authenticateUser(String username, String password) {
        return userDAO.getUserByUsernameAndPassword(username, password);
    }

    /**
     * Creates a new user with the given username, password, and role.
     */
    public boolean createUser(String username, String password, String role) {
        return userDAO.createUser(username, password, role);
    }

    /**
     * Retrieves the user ID corresponding to the given username.
     */
    public int getUserIdByUsername(String username) {
        return userDAO.getUserIdByUsername(username);
    }

    /**
     * NEW: Retrieves a User object by username.
     */
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    /**
     * Retrieves all users that have the role "secretaire".
     */
    public List<User> getSecretaires() {
        return userDAO.getUsersByRole("secretaire");
    }
}
