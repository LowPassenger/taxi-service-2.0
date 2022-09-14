package taxi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.exception.DataProcessingException;
import taxi.lib.Dao;
import taxi.model.Car;
import taxi.model.Driver;
import taxi.model.Manufacturer;
import taxi.util.ConnectionUtil;

@Dao
public class CarDaoImpl implements CarDao {
    private static final int ZERO_PLACEHOLDER = 0;
    private static final int SHIFT = 2;
    private static final Logger logger = LogManager.getLogger(CarDaoImpl.class);

    @Override
    public Car create(Car car) {
        logger.debug("start car creation");
        String query = "INSERT INTO cars (model, manufacturer_id)"
                + "VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                             query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, car.getModel());
            statement.setLong(2, car.getManufacturer().getId());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                car.setId(resultSet.getObject(1, Long.class));
            }
        } catch (SQLException e) {
            logger.error("Can't create car Params: car = {}", car, e);
            throw new DataProcessingException("can't create car", e);
        }
        insertAllDrivers(car);
        logger.info("car create successful Params: car = {}", car);
        return car;
    }

    @Override
    public Optional<Car> get(Long id) {
        logger.debug("start get car by id method");
        String query = "SELECT cars.id AS id, "
                + "model, "
                + "manufacturer_id, "
                + "manufacturers.name AS manufacturer_name, "
                + "manufacturers.country AS manufacturer_country "
                + "FROM cars "
                + "JOIN manufacturers m ON cars.manufacturer_id = manufacturers.id "
                + "WHERE cars.id = ? AND cars.is_deleted = FALSE";
        Car car = null;
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                car = parseCarFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            logger.error("Can't get car Params: carId = {}", id, e);
            throw new DataProcessingException("can't get car", e);
        }
        if (car != null) {
            car.setDrivers(getAllDriversByCarId(car.getId()));
        }
        logger.info("get car from id successful Params: carId = {}", id);
        return Optional.ofNullable(car);
    }

    @Override
    public List<Car> getAll() {
        logger.debug("start get all cars from DB");
        String query = "SELECT cars.id AS id, "
                + "model, "
                + "manufacturer_id, "
                + "manufacturers.name AS manufacturer_name, "
                + "manufacturers.country AS manufacturer_country "
                + "FROM cars "
                + " JOIN manufacturers ON cars.manufacturer_id = manufacturers.id"
                + " WHERE cars.is_deleted = FALSE";
        List<Car> cars = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                cars.add(parseCarFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Can't get all cars Params: list of cars = {}",
                    cars, e);
            throw new DataProcessingException("can't get all cars", e);
        }
        cars.forEach(car -> car.setDrivers(getAllDriversByCarId(car.getId())));
        logger.info("list of cars create successful");
        return cars;
    }

    @Override
    public Car update(Car car) {
        logger.debug("start car update");
        String query = "UPDATE cars SET model = ?, manufacturer_id = ? WHERE id = ?"
                + " AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setString(1, car.getModel());
            statement.setLong(2, car.getManufacturer().getId());
            statement.setLong(3, car.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Can't update car Params: car = {}", car, e);
            throw new DataProcessingException("can't update car", e);
        }
        deleteAllDriversExceptList(car);
        insertAllDrivers(car);
        logger.info("car update successful Params: car = {}", car);
        return car;
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("start car delete");
        String query = "UPDATE cars SET is_deleted = TRUE WHERE id = ?"
                + " AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(query)) {
            statement.setLong(1, id);
            logger.info("car delete successful Params: carId = {}", id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("can't delete car Params: carId = {}", id, e);
            throw new DataProcessingException("can't delete car", e);
        }
    }

    @Override
    public List<Car> getAllByDriver(Long driverId) {
        logger.debug("start get all cars for driver with id " + driverId);
        String query = "SELECT cars.id AS id, "
                + "model, "
                + "manufacturer_id, "
                + "manufacturers.name AS manufacturer_name, "
                + "manufacturers.country AS manufacturer_country "
                + "FROM cars "
                + " JOIN manufacturers ON cars.manufacturer_id = manufacturers.id"
                + " JOIN cars_drivers ON cars.id = cars_drivers.car_id"
                + " JOIN drivers ON cars_drivers.driver_id = drivers.id"
                + " WHERE cars.is_deleted = FALSE AND driver_id = ?"
                + " AND drivers.is_deleted = FALSE";
        List<Car> cars = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setLong(1, driverId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                cars.add(parseCarFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Can't get all cars", e);
            throw new DataProcessingException("can't get all cars", e);
        }
        cars.forEach(car -> car.setDrivers(getAllDriversByCarId(car.getId())));
        logger.info("list of cars create successful Params: driverId = {}", driverId);
        return cars;
    }

    private void insertAllDrivers(Car car) {
        logger.debug("start insert driver list to car");
        Long carId = car.getId();
        List<Driver> drivers = car.getDrivers();
        if (drivers.size() == 0) {
            return;
        }
        String query = "INSERT INTO cars_drivers (car_id, driver_id) VALUES "
                + drivers.stream().map(driver -> "(?, ?)").collect(Collectors.joining(", "))
                + " ON DUPLICATE KEY UPDATE car_id = car_id";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            for (int i = 0; i < drivers.size(); i++) {
                Driver driver = drivers.get(i);
                statement.setLong((i * SHIFT) + 1, carId);
                statement.setLong((i * SHIFT) + 2, driver.getId());
            }
            statement.executeUpdate();
            logger.info("list od drivers add successful Params: car = {}", car);
        } catch (SQLException e) {
            logger.error("Can't insert drivers Params: car = {}", drivers, e);
            throw new DataProcessingException("can't insert drivers to car", e);
        }
    }

    private void deleteAllDriversExceptList(Car car) {
        logger.debug("start insert driver list to car");
        Long carId = car.getId();
        List<Driver> exceptions = car.getDrivers();
        int size = exceptions.size();
        String query = "DELETE FROM cars_drivers WHERE car_id = ? "
                + "AND NOT driver_id IN ("
                + ZERO_PLACEHOLDER + ", ?".repeat(size)
                + ");";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setLong(1, carId);
            for (int i = 0; i < size; i++) {
                Driver driver = exceptions.get(i);
                statement.setLong((i) + SHIFT, driver.getId());
            }
            statement.executeUpdate();
            logger.info("list of drivers delete successful");
        } catch (SQLException e) {
            logger.error("Can't delete drivers Params: list of drivers = {}",
                    exceptions, e);
            throw new DataProcessingException("can't delete drivers from car", e);
        }
    }

    private List<Driver> getAllDriversByCarId(Long carId) {
        logger.debug("start get all drivers list by car");
        String query = "SELECT id, name, license_number, login, password "
                + "FROM cars_drivers "
                + "JOIN drivers ON cars_drivers.driver_id = drivers.id "
                + "WHERE car_id = ? AND is_deleted = false";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setLong(1, carId);
            ResultSet resultSet = statement.executeQuery();
            List<Driver> drivers = new ArrayList<>();
            while (resultSet.next()) {
                drivers.add(parseDriverFromResultSet(resultSet));
            }
            logger.info("list of drives from car complete Params: carId = {}", carId);
            return drivers;
        } catch (SQLException e) {
            logger.error("Can't get all drivers by car Params: carId = {}", carId, e);
            throw new DataProcessingException("can't get all drivers from car", e);
        }
    }

    private Driver parseDriverFromResultSet(ResultSet resultSet) throws SQLException {
        logger.debug("start parsing drivers from ResultSet");
        Long driverId = resultSet.getObject("id", Long.class);
        String name = resultSet.getNString("name");
        String licenseNumber = resultSet.getNString("license_number");
        String login = resultSet.getNString("login");
        String password = resultSet.getNString("password");
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setName(name);
        driver.setLicenseNumber(licenseNumber);
        driver.setLogin(login);
        driver.setPassword(password);
        logger.debug("parsing drivers from ResultSet complete");
        return driver;
    }

    private Car parseCarFromResultSet(ResultSet resultSet) throws SQLException {
        logger.debug("start parsing cars from ResultSet");
        Long manufacturerId = resultSet.getObject("manufacturer_id", Long.class);
        String manufacturerName = resultSet.getNString("manufacturer_name");
        String manufacturerCountry = resultSet.getNString("manufacturer_country");
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(manufacturerId);
        manufacturer.setName(manufacturerName);
        manufacturer.setCountry(manufacturerCountry);
        Long carId = resultSet.getObject("id", Long.class);
        String model = resultSet.getNString("model");
        Car car = new Car();
        car.setId(carId);
        car.setModel(model);
        car.setManufacturer(manufacturer);
        logger.debug("parsing drivers from ResultSet complete");
        return car;
    }
}
