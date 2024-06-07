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

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_write");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

//        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
//        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
        JsonObject responseJsonObject = new JsonObject();

//        try {
//            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//        } catch (Exception e) {
//            // Return a proper HTML response with an error message
//            responseJsonObject.addProperty("status", "fail");
//            responseJsonObject.addProperty("message", "reCAPTCHA verification error: " + e.getMessage());
//            out.write(responseJsonObject.toString());
//            return;
//        }

        String email = request.getParameter("username");
        String typed_password = request.getParameter("password");


        try (Connection conn = dataSource.getConnection()) {
            String email_query = "SELECT id FROM customers WHERE email = ?";
            PreparedStatement email_statement = conn.prepareStatement(email_query);
            email_statement.setString(1, email);
            ResultSet email_result = email_statement.executeQuery();

            if (!email_result.next()) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Email: " + email + " not found.");
            }
            else {
                String cid = email_result.getString("id");
                String query = "SELECT c.id, c.firstName, c.lastName, c.ccId, c.address, c.email, c.password, cc.expiration " +
                        "FROM customers c " +
                        "JOIN creditcards cc ON c.ccId = cc.id " +
                        "WHERE c.email = ? AND c.id = ?";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, cid);
                ResultSet rs = statement.executeQuery();

                if(rs.next()) {
                    String encryptedPassword = rs.getString("password");
                    boolean success = false;
                    success = new StrongPasswordEncryptor().checkPassword(typed_password, encryptedPassword);

                    if(success)
                    {
                        String id = rs.getString("id");
                        String first_name = rs.getString("firstName");
                        String last_name = rs.getString("lastName");
                        String cc_id = rs.getString("ccId");
                        String address = rs.getString("address");
                        String expiration_date = rs.getString("expiration");


                        request.getSession().setAttribute("user", new User(id, first_name, last_name, cc_id, address, email, expiration_date));

                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                    }
                    else {
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "Password is incorrect.");
                    }
                }
                else {
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

        PrintWriter out2 = response.getWriter();
        out2.write(responseJsonObject.toString());
    }
}
