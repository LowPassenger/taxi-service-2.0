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
import taxi.model.Manufacturer;
import taxi.util.ConnectionUtil;

@Dao
public class ManufacturerDaoImpl implements ManufacturerDao {
    private static final Logger logger = LogManager.getLogger(ManufacturerDaoImpl.class);

    @Override
    public Manufacturer create(Manufacturer manufacturer) {
        logger.debug("start create manufacturer method Params: manufacturer = {}", manufacturer);
        String query = "INSERT INTO manufacturers (name, country) VALUES (?,?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement
                        = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setUpdate(statement, manufacturer).executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                manufacturer.setId(resultSet.getObject(1, Long.class));
            }
            logger.info("create manufacturer successfully Params: manufacturer = {}",
                    manufacturer);
            return manufacturer;
        } catch (SQLException e) {
            logger.error("can't create manufacturer Params: manufacturer = {}",
                    manufacturer, e);
            throw new DataProcessingException("Couldn't create manufacturer. " + manufacturer, e);
        }
    }

    @Override
    public Optional<Manufacturer> get(Long id) {
        logger.debug("start create manufacturer method Params: manufacturerId = {}", id);
        String query = "SELECT * FROM manufacturers WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            Manufacturer manufacturer = null;
            if (resultSet.next()) {
                manufacturer = parseManufacturerFromResultSet(resultSet);
            }
            logger.info("get manufacturer successfully Params: manufacturer = {}",
                    manufacturer);
            return Optional.ofNullable(manufacturer);
        } catch (SQLException e) {
            logger.error("start create manufacturer method Params: manufacturerId "
                    + id, e);
            throw new DataProcessingException("can't get manufacturer", e);
        }
    }

    @Override
    public List<Manufacturer> getAll() {
        logger.debug("start get all manufacturers method");
        String query = "SELECT * FROM manufacturers WHERE is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            List<Manufacturer> manufacturers = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                manufacturers.add(parseManufacturerFromResultSet(resultSet));
            }
            logger.info("get all manufacturers successfully");
            return manufacturers;
        } catch (SQLException e) {
            logger.error("can't get all manufacturers", e);
            throw new DataProcessingException("can't get a list of manufacturers ", e);
        }
    }

    @Override
    public Manufacturer update(Manufacturer manufacturer) {
        logger.debug("start update manufacturer method Params: manufacturer = {}",
                manufacturer);
        String query = "UPDATE manufacturers SET name = ?, country = ?"
                + " WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement
                        = setUpdate(connection.prepareStatement(query), manufacturer)) {
            statement.setLong(3, manufacturer.getId());
            statement.executeUpdate();
            logger.info("update manufacturer successfully Params: manufacturer = {}",
                    manufacturer);
            return manufacturer;
        } catch (SQLException e) {
            logger.error("update manufacturer successfully Params: manufacturer = {}",
                    manufacturer);
            throw new DataProcessingException("can't update a manufacturer ", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("start delete manufacturer method Params: manufacturerId = {}", id);
        String query = "UPDATE manufacturers SET is_deleted = TRUE WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            logger.info("delete manufacturer successfully Params: manufacturerId = {}", id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.debug("can't delete manufacturer Params: manufacturerId = {}", id);
            throw new DataProcessingException("can't delete manufacturer ", e);
        }
    }

    private Manufacturer parseManufacturerFromResultSet(ResultSet resultSet) throws SQLException {
        logger.debug("start parse manufacturer from ResultSet");
        Long id = resultSet.getObject("id", Long.class);
        String name = resultSet.getString("name");
        String country = resultSet.getString("country");
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(id);
        manufacturer.setName(name);
        manufacturer.setCountry(country);
        logger.debug("parse manufacturer from ResultSet complete");
        return manufacturer;
    }

    private PreparedStatement setUpdate(PreparedStatement statement,
                                        Manufacturer manufacturer) throws SQLException {
        logger.debug("start setUpdate method");
        statement.setString(1, manufacturer.getName());
        statement.setString(2, manufacturer.getCountry());
        logger.debug("setUpdate method complete");
        return statement;
    }
}
