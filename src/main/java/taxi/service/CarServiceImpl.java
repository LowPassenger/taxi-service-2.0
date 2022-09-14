package taxi.service;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.dao.CarDao;
import taxi.lib.Inject;
import taxi.lib.Service;
import taxi.model.Car;
import taxi.model.Driver;

@Service
public class CarServiceImpl implements CarService {
    private static final Logger logger = LogManager.getLogger(CarServiceImpl.class);
    @Inject
    private CarDao carDao;

    @Override
    public void addDriverToCar(Driver driver, Car car) {
        logger.debug("start addDriverToCar method");
        car.getDrivers().add(driver);
        carDao.update(car);
    }

    @Override
    public void removeDriverFromCar(Driver driver, Car car) {
        logger.debug("start removeDriverFromCar method");
        car.getDrivers().remove(driver);
        carDao.update(car);
    }

    @Override
    public List<Car> getAllByDriver(Long driverId) {
        logger.debug("start getAllByDriver method");
        return carDao.getAllByDriver(driverId);
    }

    @Override
    public Car create(Car car) {
        logger.debug("start create method");
        return carDao.create(car);
    }

    @Override
    public Car get(Long id) {
        logger.debug("start get method");
        return carDao.get(id).get();
    }

    @Override
    public List<Car> getAll() {
        logger.debug("start getAll method");
        return carDao.getAll();
    }

    @Override
    public Car update(Car car) {
        logger.debug("start update method");
        return carDao.update(car);
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("start delete method");
        return carDao.delete(id);
    }
}
