package service;

import dao.SeanceCodeDao;
import entite.SeanceCode;
import java.util.List;
import java.util.Optional;

public class SeanceCodeService {

    private final SeanceCodeDao seanceCodeDao = new SeanceCodeDao();

    public boolean createSeanceCode(SeanceCode seance) {
        return seanceCodeDao.create(seance);
    }

    public Optional<SeanceCode> getSeanceCodeById(int id) {
        return seanceCodeDao.findById(id);
    }

    public List<SeanceCode> getSeancesByCandidatId(int candidatId) {
        return seanceCodeDao.findByCandidatId(candidatId);
    }


    public List<SeanceCode> getSeancesByMoniteurId(int moniteurId) {
        return seanceCodeDao.findByMoniteurId(moniteurId);
    }


    public List<SeanceCode> getAllSeances() {
        return seanceCodeDao.getAllSeances();
    }

    public boolean updateSeanceCode(SeanceCode seance) {
        return seanceCodeDao.update(seance);
    }

    public boolean deleteSeanceCode(int id) {
        return seanceCodeDao.delete(id);
    }
}
