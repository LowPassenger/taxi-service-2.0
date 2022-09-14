package taxi.service;

import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.dao.DriverDao;
import taxi.lib.Inject;
import taxi.lib.Service;
import taxi.model.Driver;

@Service
public class DriverServiceImpl implements DriverService {
    private static final Logger logger = LogManager.getLogger(DriverService.class);
    @Inject
    private DriverDao driverDao;

    @Override
    public Driver create(Driver driver) {
        logger.debug("start create method");
        return driverDao.create(driver);
    }

    @Override
    public Driver get(Long id) {
        logger.debug("start get method");
        return driverDao.get(id).get();
    }

    @Override
    public List<Driver> getAll() {
        logger.debug("start getAll method");
        return driverDao.getAll();
    }

    @Override
    public Driver update(Driver driver) {
        logger.debug("start update method");
        return driverDao.update(driver);
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("start delete method");
        return driverDao.delete(id);
    }

    @Override
    public Optional<Driver> findDriverByLogin(String login) {
        logger.debug("start findDriverByLogin method");
        return driverDao.findByLogin(login);
    }
}
