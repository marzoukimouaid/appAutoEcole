package service;

import dao.ExamenCodeDao;
import entite.ExamenCode;

import java.util.List;
import java.util.Optional;

public class ExamenCodeService {

    private final ExamenCodeDao examDao = new ExamenCodeDao();

    public boolean createExamenCode(ExamenCode exam) {
        return examDao.create(exam);
    }

    public Optional<ExamenCode> getExamenCodeById(int id) {
        return examDao.findById(id);
    }

    public List<ExamenCode> getExamenCodesByCandidatId(int candidatId) {
        return examDao.findByCandidatId(candidatId);
    }

    public List<ExamenCode> getExamenCodesByMoniteurId(int moniteurId) {
        return examDao.findByMoniteurId(moniteurId);
    }

    public List<ExamenCode> getAllExamenCodes() {
        return examDao.getAllExams();
    }

    public boolean updateExamenCode(ExamenCode exam) {
        return examDao.update(exam);
    }

    public boolean deleteExamenCode(int id) {
        return examDao.delete(id);
    }
}
