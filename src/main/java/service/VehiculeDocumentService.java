package service;

import dao.VehiculeDocumentDao;
import entite.VehiculeDocument;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for VehiculeDocument,
 * now includes 'cost' in the entity if the user sets it.
 */
public class VehiculeDocumentService {

    private final VehiculeDocumentDao documentDao = new VehiculeDocumentDao();

    public boolean createDocument(VehiculeDocument doc) {
        // Potentially validate cost here (e.g. cost >= 0)
        // Then pass doc to the DAO
        return documentDao.create(doc);
    }

    public Optional<VehiculeDocument> getDocumentById(int docId) {
        return documentDao.findById(docId);
    }

    public List<VehiculeDocument> getDocumentsForVehicule(int vehiculeId) {
        return documentDao.findByVehiculeId(vehiculeId);
    }

    public List<VehiculeDocument> getAllDocuments() {
        return documentDao.findAll();
    }

    public boolean updateDocument(VehiculeDocument doc) {
        // Again, could validate cost if needed
        return documentDao.update(doc);
    }

    public boolean deleteDocument(int docId) {
        return documentDao.delete(docId);
    }

    /**
     * Finds documents that will expire within the next 'daysAhead' days.
     */
    public List<VehiculeDocument> findDocumentsExpiringSoon(int daysAhead) {
        LocalDate threshold = LocalDate.now().plusDays(daysAhead);
        return documentDao.findAll()
                .stream()
                .filter(d -> d.getDateExpiration() != null && d.getDateExpiration().isBefore(threshold))
                .collect(Collectors.toList());
    }
}
