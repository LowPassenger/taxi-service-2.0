package taxi.controller.driver;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.lib.Injector;
import taxi.model.Driver;
import taxi.service.CarService;
import taxi.service.DriverService;

public class AddDriverController extends HttpServlet {
    private static final Injector injector = Injector.getInstance("taxi");
    private static final Logger logger = LogManager.getLogger(AddDriverController.class);
    private final DriverService driverService = (DriverService) injector
            .getInstance(DriverService.class);
    private CarService carService = (CarService) injector.getInstance(CarService.class);

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        logger.info("method doGet was called");
        req.getRequestDispatcher("/WEB-INF/views/drivers/add.jsp").forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("method doPost was called");
        String password = req.getParameter("password");
        String rePassword = req.getParameter("rePassword");
        logger.debug("take all parameters");
        if (!password.equals(rePassword)) {
            throw new RemoteException("Password and repeat password are not equal!");
        }
        logger.debug("password are equal with repeat password field");
        String name = req.getParameter("name");
        String licenseNumber = req.getParameter("license_number");
        String login = req.getParameter("login");
        Driver driver = new Driver(name, licenseNumber);
        driver.setLogin(login);
        driver.setPassword(password);
        driverService.create(driver);
        logger.info("driver create successfully Params: name = {}, license_number = {}, "
                + "login ={}, password equals with rePassword", name, licenseNumber, login);
        resp.sendRedirect(req.getContextPath() + "/drivers/add");
    }
}
