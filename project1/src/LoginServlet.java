import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletConfig;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("username");
        String password = request.getParameter("password");

        JsonObject responseJsonObject = new JsonObject();


        try (Connection conn = dataSource.getConnection()) {
            System.out.println("hello");
            String email_query = "SELECT email FROM customers WHERE email = ?";
            PreparedStatement email_statement = conn.prepareStatement(email_query);
            email_statement.setString(1, email);
            ResultSet email_result = email_statement.executeQuery();

            if (!email_result.next()) {
                System.out.println("hello1");

                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Email: " + email + " not found.");

            }
            else {
                System.out.println("hello2");
                String query = "SELECT id, firstName, lastName, ccId, address, email " +
                        "FROM customers " +
                        "WHERE email = ? AND password = ?";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, password);
                ResultSet rs = statement.executeQuery();

                if(rs.next()) {
                    System.out.println("hello3");

                    String id = rs.getString("id");
                    String first_name = rs.getString("firstName");
                    String last_name = rs.getString("lastName");
                    String cc_id = rs.getString("ccId");
                    String address = rs.getString("address");

                    request.getSession().setAttribute("user", new User(id, first_name, last_name, cc_id, address, email));

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                }
                else {
                    System.out.println("hello4");

                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Password is incorrect.");

                }
            }


        }

        catch (Exception e) {
            responseJsonObject.addProperty("errorMessage", e.getMessage());
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(600);
        }


        PrintWriter out = response.getWriter();
        out.write(responseJsonObject.toString());
        ;
    }
}
