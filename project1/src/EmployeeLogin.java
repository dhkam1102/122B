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

@WebServlet(name = "Dashboard", urlPatterns = "/api/_dashboard")
public class EmployeeLogin extends HttpServlet {

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_read");
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
            String email_query = "SELECT fullname FROM employees WHERE email = ?";
            PreparedStatement email_statement = conn.prepareStatement(email_query);
            email_statement.setString(1, email);
            ResultSet email_result = email_statement.executeQuery();

            if (!email_result.next()) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Email: " + email + " not found.");
            }
            else {
                String fullname = email_result.getString("fullname");
                String query = "SELECT * FROM employees WHERE email = ? AND fullname = ?";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, fullname);
                ResultSet rs = statement.executeQuery();

                if(rs.next()) {
                    String encryptedPassword = rs.getString("password");
                    boolean success = false;
                    success = new StrongPasswordEncryptor().checkPassword(typed_password, encryptedPassword);

                    if(success)
                    {
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

