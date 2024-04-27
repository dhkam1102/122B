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
import java.sql.*;
import java.time.LocalDate;
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
        System.out.println("he1");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        String first_name = request.getParameter("firstName");
        String last_name = request.getParameter("lastName");
        String ccId = request.getParameter("cardNumber");
        String expDate = request.getParameter("expDate");

        User user = (User) session.getAttribute("user");
        String customer_id = user.getId();

        JsonObject jsonResponse = new JsonObject();

        if (user != null && user.getFirstName().equals(first_name) && user.getLastName().equals(last_name)
                && user.getCcId().equals(ccId) && user.getExpDate().equals(expDate)) {


            HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");

            if (cart == null) {
                System.out.println("he1");

                jsonResponse.addProperty("status", "fail");
                jsonResponse.addProperty("message", "fail");
            }
            else {
                try(Connection conn = dataSource.getConnection()) {
                    String movie_query = "SELECT id FROM movies WHERE title = ?";
                    PreparedStatement movie_statement = conn.prepareStatement(movie_query);
//                    PreparedStatement movie_statement = conn.prepareStatement("SELECT id FROM movies WHERE title = ?");
                    PreparedStatement sales_statement = conn.prepareStatement("INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    System.out.println("heheheh");

                    LocalDate saleDate = LocalDate.now();
//                        conn.setAutoCommit(false);
                    for (String movie_title: cart.keySet()) {
                        System.out.println(movie_title);

                        movie_statement.setString(1, movie_title);
                        try(ResultSet rs = movie_statement.executeQuery()) {
                            if(rs.next()) {
                                System.out.println("helloooo");

                                String movie_id = rs.getString("id");
                                System.out.println("movie_id: " + movie_id);
                                System.out.println("sale Date:" + saleDate);
                                System.out.println("customerId: "+ customer_id);

                                System.out.println("hellooo23232o");

                                sales_statement.setString(1, customer_id);
                                sales_statement.setString(2, movie_id);
                                sales_statement.setDate(3, Date.valueOf(saleDate));
                                System.out.println("hellooohjehehe");

                                int rows_updated = sales_statement.executeUpdate();
                                System.out.println("rows_updated");
                                System.out.println(rows_updated);
                            }
                            else {
                                System.out.println("he2");
                                jsonResponse.addProperty("status", "fail");
                                jsonResponse.addProperty("message", "fail");
                            }
                        }
//                        System.out.println(movie_id);
                        System.out.println("helloooo222");
                    }
                    conn.commit();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "success");
                }
                catch (Exception e) {
                    System.out.println("he3");

                    jsonResponse.addProperty("status", "fail");
                    jsonResponse.addProperty("message", "fail");
                    response.setStatus(500);
                }

                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "success");
            }

        }
        else {
            System.out.println("he4");

            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "fail");
        }



        out.println(jsonResponse.toString());
        out.close();

    }
}
