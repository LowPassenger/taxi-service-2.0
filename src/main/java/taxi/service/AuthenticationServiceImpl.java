package taxi.service;

import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.exception.AuthenticationException;
import taxi.lib.Inject;
import taxi.lib.Service;
import taxi.model.Driver;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LogManager.getLogger(DriverService.class);

    @Inject
    private DriverService driverService;

    @Override
    public Driver login(String login, String password) throws AuthenticationException {
        logger.debug("start driver login method Params: login = {}, password OK", login);
        Optional<Driver> driver = driverService.findDriverByLogin(login);
        if (driver.isPresent() && driver.get().getPassword().equals(password)) {
            logger.info("driver authenticated successfully Params: login = {},"
                    + " password OK", login);
            return driver.get();
        }
        logger.error("Login or password was incorrect");
        throw new AuthenticationException("Login or password was incorrect");
    }
}
