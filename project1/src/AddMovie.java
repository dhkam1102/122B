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

        String movieTitle = request.getParameter("movieTitle");
        String movieDirector = request.getParameter("movieDirector");
        String movieYear = request.getParameter("movieYear");
        String movieGenre = request.getParameter("movieGenre");
        String starName = request.getParameter("starName");
        String starBirthYear = request.getParameter("starBirthYear");

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");

            CallableStatement statement = conn.prepareCall("{call add_movie(?, ?, ?, ?, ?, ?)}");

            // Set the parameter for the stored procedure
            statement.setString(1, movieTitle);
            statement.setInt(1, Integer.parseInt(movieDirector));
            statement.setString(1, movieYear);
            statement.setString(1, movieGenre);
            statement.setString(1, starName);
            statement.setInt(1, Integer.parseInt(starBirthYear));

            // Execute the stored procedure
            statement.execute();

            // Handle any result from the stored procedure, if necessary

            // Close the statement and connection
            statement.close();
            conn.close();
        } catch (SQLException e) {
            out.println("Error adding star: " + e.getMessage());
        }
    }
}