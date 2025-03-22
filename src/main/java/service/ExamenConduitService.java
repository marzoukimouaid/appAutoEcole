package service;

import dao.ExamenConduitDao;
import entite.ExamenConduit;

import java.util.List;
import java.util.Optional;

public class ExamenConduitService {

    private final ExamenConduitDao examDao = new ExamenConduitDao();

    public boolean createExamenConduit(ExamenConduit exam) {
        return examDao.create(exam);
    }

    public Optional<ExamenConduit> getExamenConduitById(int id) {
        return examDao.findById(id);
    }

    public List<ExamenConduit> getExamenConduitsByCandidatId(int candidatId) {
        return examDao.findByCandidatId(candidatId);
    }

    public List<ExamenConduit> getExamenConduitsByMoniteurId(int moniteurId) {
        return examDao.findByMoniteurId(moniteurId);
    }

    public List<ExamenConduit> getAllExamenConduits() {
        return examDao.getAllExams();
    }

    public boolean updateExamenConduit(ExamenConduit exam) {
        return examDao.update(exam);
    }

    public boolean deleteExamenConduit(int id) {
        return examDao.delete(id);
    }
}
