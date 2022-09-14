package taxi.web.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthenticationFilter implements Filter {
    private static final Set<String> ALLOWED_URL = new HashSet<>();
    private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        logger.debug("start init method");
        ALLOWED_URL.add("/login");
        ALLOWED_URL.add("/drivers/add");
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        logger.debug("start doFilter method");
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        HttpSession session = req.getSession();
        Long driverId = (Long) session.getAttribute("driverId");
        if (driverId != null || ALLOWED_URL.contains(req.getServletPath())) {
            logger.info("doFilter method works Params: driverId = {}", driverId);
            filterChain.doFilter(req, resp);
            return;
        }
        logger.info("doFilter method redirect Params: driverId = {}", driverId);
        resp.sendRedirect("/login");
    }
}
