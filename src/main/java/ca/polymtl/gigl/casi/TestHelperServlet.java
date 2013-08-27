package ca.polymtl.gigl.casi;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

public abstract class TestHelperServlet extends HttpServlet {

    protected int serverPort;

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        serverPort = req.getServerPort();
        super.service(req, res);
    }
}
