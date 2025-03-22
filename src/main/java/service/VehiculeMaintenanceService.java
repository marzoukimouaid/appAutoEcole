package service;

import dao.VehiculeMaintenanceDao;
import entite.VehiculeMaintenance;
import entite.Vehicule;
import java.util.List;
import java.util.Optional;

public class VehiculeMaintenanceService {

    private final VehiculeMaintenanceDao maintenanceDao = new VehiculeMaintenanceDao();
    // New dependency to update the vehicle after maintenance insertion
    private final VehiculeService vehiculeService = new VehiculeService();

    /**
     * Creates a new maintenance record for a vehicle.
     * Upon successful creation, the vehicle's km_restant_entretien is reset to a default value.
     *
     * @param m the maintenance record to create.
     * @return true if creation (and vehicle update) succeeded; false otherwise.
     */
    public boolean createMaintenance(VehiculeMaintenance m) {
        boolean created = maintenanceDao.create(m);
        if (created) {
            // After a maintenance record is inserted, reinitialize the vehicle's km_restant_entretien.
            Optional<Vehicule> optVehicule = vehiculeService.getVehiculeById(m.getVehiculeId());
            if (optVehicule.isPresent()) {
                Vehicule vehicule = optVehicule.get();
                // Set default km_restant_entretien based on vehicle type:
                // For VOITURE: 10,000 km, MOTO: 5,000 km, CAMION: 15,000 km.
                switch (vehicule.getType()) {
                    case VOITURE:
                        vehicule.setKmRestantEntretien(10000);
                        break;
                    case MOTO:
                        vehicule.setKmRestantEntretien(5000);
                        break;
                    case CAMION:
                        vehicule.setKmRestantEntretien(15000);
                        break;
                    default:
                        vehicule.setKmRestantEntretien(10000);
                        break;
                }
                boolean updated = vehiculeService.updateVehicule(vehicule);
                if (!updated) {
                    System.err.println("Failed to update vehicule km_restant_entretien after maintenance.");
                }
            } else {
                System.err.println("Vehicule not found for maintenance update.");
            }
        }
        return created;
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
