package Utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConnexionDB  {
    private static Connection connexion;

    private final String DB_URL = "jdbc:mysql://localhost:3306/testDB";
    private final String USER = "root";
    private final String PASS = "";
    private ConnexionDB() throws SQLException {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        connexion = DriverManager.getConnection(DB_URL, USER, PASS);

    }

    public static Connection getInstance() {
        if (connexion == null) {
            try {
                new ConnexionDB();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        }
        return connexion;
    }



}
