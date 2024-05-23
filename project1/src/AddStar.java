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

@WebServlet(name = "AddStar", urlPatterns = "/api/add-star")
public class AddStar extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String starName = request.getParameter("starName");
        String starBirthYear = request.getParameter("starBirthYear");
        JsonArray jsonArray = new JsonArray();

        Integer birthYear = null;
        try {
            birthYear = Integer.parseInt(starBirthYear);
        }
        catch (NumberFormatException e) {
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("errorMessage", "Invalid birth year format.");
            jsonArray.add(errorJson);
            out.println(jsonArray.toString());
            return;
        }



        try(Connection conn = dataSource.getConnection()) {
            // Retrieve the current maximum id
            String getIDQuery = "SELECT MAX(id) AS max_id FROM stars WHERE id LIKE 'insert%'";
            int newId = 0;
            try (PreparedStatement getIDStatement = conn.prepareStatement(getIDQuery)) {
                ResultSet resultSet = getIDStatement.executeQuery();
                if (resultSet.next()) {
                    String maxId = resultSet.getString("max_id");
                    if (maxId != null && maxId.matches("insert\\d+")) {
                        newId = Integer.parseInt(maxId.substring(6)) + 1; // Get the number part and increment
                    }
                }
            }

            System.out.println(newId);
            String insertionQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            try (PreparedStatement insertionStatement = conn.prepareStatement(insertionQuery)) {
                insertionStatement.setString(1, "insert" + newId);
                insertionStatement.setString(2, starName);
                insertionStatement.setString(3, starBirthYear);
                System.out.println(insertionQuery);
                insertionStatement.executeUpdate();

                JsonObject jsonObject = new JsonObject();
                String SnewId = Integer.toString(newId);
                System.out.println(SnewId);
                jsonObject.addProperty("id", "insert"+SnewId);
                System.out.println(starName);

                jsonObject.addProperty("name", starName);
                System.out.println(starBirthYear);

                jsonObject.addProperty("birthYear", starBirthYear);
                jsonArray.add(jsonObject);

                conn.close();
                out.println(jsonArray.toString());
            }
        } catch (SQLException e) {
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("errorMessage", "Error adding star: " + e.getMessage());
            jsonArray.add(errorObject);
            out.println(jsonArray.toString());
//            out.println("Error adding star: " + e.getMessage());
        }
    }
}