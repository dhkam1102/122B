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
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_write");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String movieTitle = request.getParameter("movieTitle");
        String movieDirector = request.getParameter("movieDirector");
        Integer movieYear = Integer.valueOf(request.getParameter("movieYear"));
        String movieGenre = request.getParameter("movieGenre");
        String starName = request.getParameter("starName");
        String starBirthYear = request.getParameter("starBirthYear");

        try (Connection conn = dataSource.getConnection())
        {
            String callProcedure = "call add_movie (?, ?, ?, ?, ?, ?)";
            try(PreparedStatement statement = conn.prepareStatement(callProcedure))
            {
                statement.setString(1, movieTitle);
                statement.setInt(2, movieYear);
                statement.setString(3, movieDirector);

                statement.setString(4, movieGenre);
                statement.setString(5, starName);
                if(starBirthYear != null) {
                    statement.setInt(6, Integer.parseInt(starBirthYear));
                }
                else {
                    statement.setNull(6, java.sql.Types.INTEGER);
                }

                // Set the parameter for the stored procedure

                // Execute the stored procedure
                ResultSet rs = statement.executeQuery();

                // Handle any result from the stored procedure, if necessary
                JsonArray jsonArray = new JsonArray();
                while(rs.next()) {
                    JsonObject jsonObject = new JsonObject();
                    String message = rs.getString("message");
                    System.out.println(message);
                    jsonObject.addProperty("message", message);
                    jsonArray.add(jsonObject);
                }
                // Close the statement and connection
                out.write(jsonArray.toString());
            }
            conn.close();
        } catch (SQLException e) {
            out.println("Error adding movie: " + e.getMessage());
        }
    }
}