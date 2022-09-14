package taxi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.exception.DataProcessingException;
import taxi.lib.Dao;
import taxi.model.Driver;
import taxi.util.ConnectionUtil;

@Dao
public class DriverDaoImpl implements DriverDao {
    private static final Logger logger = LogManager.getLogger(DriverDaoImpl.class);

    @Override
    public Driver create(Driver driver) {
        logger.debug("start create driver method Params: driver = {}", driver);
        String query = "INSERT INTO drivers (name, license_number, login, password) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query,
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, driver.getName());
            statement.setString(2, driver.getLicenseNumber());
            statement.setString(3, driver.getLogin());
            statement.setString(4, driver.getPassword());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                driver.setId(resultSet.getObject(1, Long.class));
            }
            logger.info("driver create successfully Params: driver = {}", driver);
            return driver;
        } catch (SQLException e) {
            logger.error("can't create driver Params: driver = {}", driver, e);
            throw new DataProcessingException("can't create driver Params: driver "
                    + driver, e);
        }
    }

    @Override
    public Optional<Driver> get(Long id) {
        logger.debug("start get driver method Params: driverId = {}", id);
        String query = "SELECT * FROM drivers WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            Driver driver = null;
            if (resultSet.next()) {
                driver = parseDriverFromResultSet(resultSet);
            }
            logger.info("driver get successfully Params: driverId = {}", id);
            return Optional.ofNullable(driver);
        } catch (SQLException e) {
            logger.info("can't get driver Params: driverId = {}", id, e);
            throw new DataProcessingException("can't get driver by id " + id, e);
        }
    }

    @Override
    public List<Driver> getAll() {
        logger.debug("start get all drivers method");
        String query = "SELECT * FROM drivers WHERE is_deleted = FALSE";
        List<Driver> drivers = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                drivers.add(parseDriverFromResultSet(resultSet));
            }
            logger.info("get all drivers from DB method successfully");
            return drivers;
        } catch (SQLException e) {
            logger.error("can't get all drivers from DB Params: drivers list = {}",
                    drivers, e);
            throw new DataProcessingException("can't get a list of drivers from driversDB.",
                    e);
        }
    }

    @Override
    public Driver update(Driver driver) {
        logger.debug("start update driver method Params: driver = {}", driver);
        String query = "UPDATE drivers "
                + "SET name = ?, license_number = ?, login = ?, password = ? "
                + "WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement
                        = connection.prepareStatement(query)) {
            statement.setString(1, driver.getName());
            statement.setString(2, driver.getLicenseNumber());
            statement.setString(2, driver.getLogin());
            statement.setString(2, driver.getPassword());
            statement.setLong(5, driver.getId());
            statement.executeUpdate();
            logger.info("driver update successfully Params: driver = {}", driver);
            return driver;
        } catch (SQLException e) {
            logger.error("can't update driver Params: driver = {}", driver, e);
            throw new DataProcessingException("can't update "
                    + driver + " in driversDB.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("start delete driver method Params: driverId = {}", id);
        String query = "UPDATE drivers SET is_deleted = TRUE WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            logger.info("delete driver successfully Params: driverId = {}", id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(" can't delete driver Params: driverId = {}", id, e);
            throw new DataProcessingException("can't delete driver with id " + id, e);
        }
    }

    private Driver parseDriverFromResultSet(ResultSet resultSet) throws SQLException {
        logger.debug("start parse driver from RsultSet method");
        Long id = resultSet.getObject("id", Long.class);
        String name = resultSet.getString("name");
        String licenseNumber = resultSet.getString("license_number");
        String login = resultSet.getString("login");
        String password = resultSet.getString("password");
        Driver driver = new Driver();
        driver.setId(id);
        driver.setName(name);
        driver.setLicenseNumber(licenseNumber);
        driver.setLogin(login);
        driver.setPassword(password);
        logger.debug("parse driver from RsultSet complete");
        return driver;
    }

    @Override
    public Optional<Driver> findByLogin(String login) {
        logger.debug("start find driver by login method Params: login = {}", login);
        String query = "SELECT * FROM drivers WHERE login = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            Driver driver = null;
            if (resultSet.next()) {
                driver = parseDriverFromResultSet(resultSet);
            }
            logger.info("find driver by login method successfully Params: login = {}, "
                    + "driver = {}", login, driver);
            return Optional.ofNullable(driver);
        } catch (SQLException e) {
            logger.error("can't find driver by login Params: login = {}", login, e);
            throw new DataProcessingException("can't get driver by login " + login, e);
        }
    }
}
