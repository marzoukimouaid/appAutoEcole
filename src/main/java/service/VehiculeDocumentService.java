package service;

import dao.VehiculeDocumentDao;
import entite.VehiculeDocument;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class VehiculeDocumentService {

    private final VehiculeDocumentDao documentDao = new VehiculeDocumentDao();

    public boolean createDocument(VehiculeDocument doc) {
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
        return documentDao.update(doc);
    }

    public boolean deleteDocument(int docId) {
        return documentDao.delete(docId);
    }

    
    public List<VehiculeDocument> findDocumentsExpiringSoon(int daysAhead) {
        LocalDate threshold = LocalDate.now().plusDays(daysAhead);
        return documentDao.findAll()
                .stream()
                .filter(d -> d.getDateExpiration() != null
                        && !d.getDateExpiration().isAfter(threshold)
                        && !d.isNotified())
                .collect(Collectors.toList());
    }
}
