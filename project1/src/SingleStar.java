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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStar", urlPatterns = "/api/single-star")
public class SingleStar extends HttpServlet {
    private static final long serialVersionUID = 3L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String star_name = request.getParameter("name");
        String movie_id = request.getParameter("id");
        // The log message can be found in localhost log
        request.getServletContext().log("getting name: " + star_name);
        request.getServletContext().log("getting movie id: " + movie_id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT sim.starId " +
                    "FROM stars_in_movies sim " +
                    "JOIN stars s ON sim.starId = s.id " +
                    "WHERE s.name = ? AND sim.movieId = ?";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);


            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, star_name);
            statement.setString(2, movie_id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            if (rs.next()) {
                String starId = rs.getString("starId");

                String query2 = "SELECT s.name, IFNULL(s.birthYear, 'N/A') AS birthYear, " +
                        "m.id AS movie_id, m.title AS movie_title " +
                        "FROM stars s " +
                        "LEFT JOIN stars_in_movies sim ON s.id = sim.starId " +
                        "LEFT JOIN movies m ON sim.movieId = m.id " +
                        "WHERE s.id = ? " +
                        "ORDER BY m.year DESC, m.title ASC";

                PreparedStatement statement2 = conn.prepareStatement(query2);
                statement2.setString(1, starId);
                ResultSet rs2 = statement2.executeQuery();

                // Iterate through each row of rs
                while (rs2.next()) {

                    String name = rs2.getString("name");
                    String birthYear = rs2.getString("birthYear");
                    String movie_id2 = rs2.getString("movie_id");
                    String movie_title = rs2.getString("movie_title");

                    // Create a JsonObject based on the data we retrieve from rs

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("star_id", starId);
                    jsonObject.addProperty("star_name", name);
                    jsonObject.addProperty("birthYear", birthYear);
                    jsonObject.addProperty("movie_id2", movie_id2);
                    jsonObject.addProperty("movie_title", movie_title);

                    jsonArray.add(jsonObject);
                }
                rs2.close();
                statement2.close();
            }
            rs.close();
            statement.close();
            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}

