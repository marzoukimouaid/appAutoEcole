package Authentication.service;

import Authentication.dao.UserDao;
import Authentication.entite.User;

public class UserService {
    private final UserDao userDAO = new UserDao();

    public User authenticateUser(String username, String password) {
        return userDAO.getUserByUsernameAndPassword(username, password);
    }

    public boolean createUser(String username, String password, String role) {
        return userDAO.createUser(username, password, role);
    }
}
