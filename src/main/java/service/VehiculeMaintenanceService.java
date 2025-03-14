package service;

import dao.VehiculeMaintenanceDao;
import entite.VehiculeMaintenance;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for VehiculeMaintenance,
 * could hold advanced logic e.g. total cost over a period, etc.
 */
public class VehiculeMaintenanceService {

    private final VehiculeMaintenanceDao maintenanceDao = new VehiculeMaintenanceDao();

    public boolean createMaintenance(VehiculeMaintenance m) {
        // Potentially add validations or checks
        return maintenanceDao.create(m);
    }

    public Optional<VehiculeMaintenance> getMaintenanceById(int maintenanceId) {
        return maintenanceDao.findById(maintenanceId);
    }

    public List<VehiculeMaintenance> getMaintenanceForVehicule(int vehiculeId) {
        return maintenanceDao.findByVehiculeId(vehiculeId);
    }

    public List<VehiculeMaintenance> getAllMaintenance() {
        return maintenanceDao.findAll();
    }

    public boolean updateMaintenance(VehiculeMaintenance m) {
        return maintenanceDao.update(m);
    }

    public boolean deleteMaintenance(int maintenanceId) {
        return maintenanceDao.delete(maintenanceId);
    }
}
