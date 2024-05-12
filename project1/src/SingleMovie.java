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
@WebServlet(name = "SingleMovie", urlPatterns = "/api/single-movie")
public class SingleMovie extends HttpServlet {
    private static final long serialVersionUID = 2L;

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
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String midquery = "SELECT m.id AS movie_id, m.title, m.year, m.director, COALESCE(r.rating, 0.0) AS rating " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "WHERE m.id = ?";

            try(PreparedStatement midStatement = conn.prepareStatement(midquery))
            {
                JsonArray jsonArray = new JsonArray();

                midStatement.setString(1, id);
                ResultSet rs = midStatement.executeQuery();
                while (rs.next())
                {
                    JsonObject jsonObject = new JsonObject();

                    String movieTitle = rs.getString("title");
                    String movieYear = rs.getString("year");
                    String movieDirector = rs.getString("director");
                    String movie_rating_str = rs.getString("rating");

                    String rating = null;
                    if (movie_rating_str != null) {
                        double movie_rating_double = Double.parseDouble(movie_rating_str);
                        double rounded_rating = Math.round(movie_rating_double * 10.0) / 10.0;
                        rating = String.format("%.1f", rounded_rating);
                    }

                    jsonObject.addProperty("movie_id", id);
                    jsonObject.addProperty("movie_title", movieTitle);
                    jsonObject.addProperty("movie_year", movieYear);
                    jsonObject.addProperty("movie_director", movieDirector);
                    jsonObject.addProperty("movie_rating", rating);

                    String genreListQuery = "select * from genres as g, genres_in_movies as gim where gim.genreId = g.id AND gim.movieId = ? ORDER BY g.name";
                    String starListQuery = "SELECT s.id, s.name, COUNT(sim.movieId) AS total_count " +
                            "FROM (SELECT DISTINCT starId FROM stars_in_movies WHERE movieId = ?) AS distinct_stars " +
                            "JOIN stars AS s ON distinct_stars.starId = s.id " +
                            "JOIN stars_in_movies AS sim ON s.id = sim.starId " +
                            "GROUP BY s.id, s.name " +
                            "ORDER BY total_count DESC, s.name ASC ";

                    try(PreparedStatement genreListStatement = conn.prepareStatement(genreListQuery))
                    {
                        StringBuilder genreStringBuilder = new StringBuilder();
                        genreListStatement.setString(1, id);
                        ResultSet rs2 = genreListStatement.executeQuery();
                        while (rs2.next()) {
                            String genreName = rs2.getString("name");
                            if (genreStringBuilder.length() > 0) {
                                genreStringBuilder.append(", ");
                            }
                            genreStringBuilder.append(genreName);
                        }
                        String movie_genre = genreStringBuilder.toString();
                        if(movie_genre.isBlank() || movie_genre == null || movie_genre.isEmpty())
                        {
                            movie_genre = "N/A";
                        }
                        jsonObject.addProperty("movie_genre", movie_genre);
                    }

                    try(PreparedStatement starListStatement = conn.prepareStatement(starListQuery))
                    {
                        StringBuilder starStringBuilder = new StringBuilder();
                        starListStatement.setString(1, id);
                        ResultSet rs2 = starListStatement.executeQuery();
                        while (rs2.next()) {
                            String starName = rs2.getString("name");
                            if (starStringBuilder.length() > 0) {
                                starStringBuilder.append(", ");
                            }
                            starStringBuilder.append(starName);
                        }
                        String movie_star = starStringBuilder.toString();
                        if(movie_star.isBlank() || movie_star == null || movie_star.isEmpty())
                        {
                            movie_star = "N/A";
                        }
                        jsonObject.addProperty("movie_star", movie_star);
                    }
                    jsonArray.add(jsonObject);
                }
                out.write(jsonArray.toString());
            }
            // Set response status to 200 (OK)
            response.setStatus(200);
            // Declare our statement
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
