import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

//need to do


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "payment", urlPatterns = "/api/payment")
public class Payment extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        int totalPrice = (int) session.getAttribute("totalPrice");

        JsonObject responseObject = new JsonObject();

        responseObject.addProperty("total_price", totalPrice);

        out.write(responseObject.toString());
        response.setStatus(200);
        out.close();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        String first_name = request.getParameter("firstName");
        String last_name = request.getParameter("lastName");
        String ccId = request.getParameter("cardNumber");
        String expDate = request.getParameter("expDate");

        try (Connection conn = dataSource.getConnection()) {
            User user = (User) session.getAttribute("user");

            if (user != null && user.getFirstName().equals(first_name) && user.getLastName().equals(last_name)
             && user.getCcId().equals(ccId) && user.getExpDate().equals(expDate)) {
        }

        JsonObject jsonResponse = new JsonObject();



        out.println(jsonResponse.toString());
        out.close();

    }
}
