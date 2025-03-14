package service;

import dao.VehiculeDao;
import entite.Vehicule;
import java.util.List;
import java.util.Optional;

public class VehiculeService {
    private final VehiculeDao vehiculeDao = new VehiculeDao();

    public boolean immatriculationExists(String immatriculation) {
        return vehiculeDao.immatriculationExists(immatriculation);
    }

    public boolean createVehicule(Vehicule vehicule) {
        return vehiculeDao.createVehicule(vehicule);
    }

    public Optional<Vehicule> getVehiculeById(int id) {
        return vehiculeDao.getVehiculeById(id);
    }

    public List<Vehicule> getAllVehicules() {
        return vehiculeDao.getAllVehicules();
    }

    public boolean updateVehicule(Vehicule vehicule) {
        return vehiculeDao.updateVehicule(vehicule);
    }

    public boolean deleteVehicule(int id) {
        return vehiculeDao.deleteVehicule(id);
    }
}
