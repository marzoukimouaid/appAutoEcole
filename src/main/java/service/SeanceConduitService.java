package service;

import dao.SeanceConduitDao;
import entite.SeanceConduit;
import java.util.List;
import java.util.Optional;

public class SeanceConduitService {

    private final SeanceConduitDao seanceConduitDao = new SeanceConduitDao();

    public boolean createSeanceConduit(SeanceConduit seance) {
        return seanceConduitDao.create(seance);
    }

    public Optional<SeanceConduit> getSeanceConduitById(int id) {
        return seanceConduitDao.findById(id);
    }

    public List<SeanceConduit> getSeancesByCandidatId(int candidatId) {
        return seanceConduitDao.findByCandidatId(candidatId);
    }


    public List<SeanceConduit> getSeancesByMoniteurId(int moniteurId) {
        return seanceConduitDao.findByMoniteurId(moniteurId);
    }


    public List<SeanceConduit> getAllSeances() {
        return seanceConduitDao.getAllSeances();
    }

    public boolean updateSeanceConduit(SeanceConduit seance) {
        return seanceConduitDao.update(seance);
    }

    public boolean deleteSeanceConduit(int id) {
        return seanceConduitDao.delete(id);
    }
}
