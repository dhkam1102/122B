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

@WebServlet(name = "AddMovie", urlPatterns = "/api/add-movie")
public class AddMovie extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String starName = request.getParameter("starName");
        String starBirthYear = request.getParameter("starBirthYear");

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");

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
            }

            conn.close();
            out.println("Star added successfully!");
        } catch (SQLException e) {
            out.println("Error adding star: " + e.getMessage());
        }
    }
}