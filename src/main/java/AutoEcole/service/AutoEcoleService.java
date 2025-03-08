package AutoEcole.service;

import AutoEcole.dao.AutoEcoleDao;

import java.util.List;

public class AutoEcoleService {
    private static final AutoEcoleDao autoEcoleDao = new AutoEcoleDao();

    public void initializeAutoEcole(String name, String address, String phone, String email) {
        autoEcoleDao.initializeAutoEcole(name, address, phone, email);
    }

    public static List<String[]>  getAutoEcoleData() {
        return autoEcoleDao.fetchAutoEcoleData();
    }
}
