import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MovieList", urlPatterns = "/api/movie-list")
public class MovieList extends HttpServlet {
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

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        String name = request.getParameter("name");
        request.getServletContext().log("getting name: " + name);
        String title = request.getParameter("title");
        request.getServletContext().log("getting title: " + title);
        String year = request.getParameter("year");
        request.getServletContext().log("getting year: " + year);
        String director = request.getParameter("director");
        request.getServletContext().log("getting director: " + director);
        String genre = request.getParameter("genre");
        request.getServletContext().log("getting genre: " + genre);
        String first_letter = request.getParameter("letter");
        request.getServletContext().log("getting first letter: " + first_letter);

        System.out.println("name parameter: " + name);
        System.out.println("title parameter: " + title);
        System.out.println("year parameter: " + year);
        System.out.println("director parameter: " + director);
        System.out.println("genre parameter: " + genre);
        System.out.println("letter parameter: " + first_letter);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement

//            String query = "SELECT \n" +
//                    "    m.id AS movie_id, \n" +
//                    "    m.title, \n" +
//                    "    m.year, \n" +
//                    "    m.director,\n" +
//                    "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', '), ', ', 3) AS genres,\n" +
//                    "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', '), ', ', 3) AS stars,\n" +
//                    "    MAX(r.rating) AS rating \n" +
//                    "FROM \n" +
//                    "    movies m\n" +
//                    "JOIN \n" +
//                    "    ratings r ON m.id = r.movieId\n" +
//                    "JOIN \n" +
//                    "    genres_in_movies gim ON m.id = gim.movieId\n" +
//                    "JOIN \n" +
//                    "    genres g ON gim.genreId = g.id\n" +
//                    "JOIN \n" +
//                    "    stars_in_movies sim ON m.id = sim.movieId\n" +
//                    "JOIN \n" +
//                    "    stars s ON sim.starId = s.id\n" +
//                    "GROUP BY \n" +
//                    "    m.id, m.title, m.year, m.director \n" +
//                    "ORDER BY \n" +
//                    "    MAX(r.rating) DESC \n" +
//                    "LIMIT \n" +
//                    "    20;\n";
            String query = "";
            PreparedStatement statement = null;
            if (genre != null && first_letter == null){
                System.out.println("genre selected");
                query = "SELECT m.id AS movie_id, m.title, m.year, m.director, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', '), ', ', 3) AS genres, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', '), ', ', 3) AS stars, " +
                        "MAX(r.rating) AS rating " +
                        "FROM movies m " +
                        "JOIN ratings r ON m.id = r.movieId " +
                        "JOIN genres_in_movies gim ON m.id = gim.movieId " +
                        "JOIN genres g ON gim.genreId = g.id " +
                        "JOIN stars_in_movies sim ON m.id = sim.movieId " +
                        "JOIN stars s ON sim.starId = s.id " +
                        "WHERE g.name = '" + genre + "' " +
                        "GROUP BY m.id, m.title, m.year, m.director";

                statement = conn.prepareStatement(query);
            } else if (genre == null && first_letter != null) {
                System.out.println("letter selected");

                query = "SELECT m.id AS movie_id, m.title, m.year, m.director, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', '), ', ', 3) AS genres, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', '), ', ', 3) AS stars, " +
                        "MAX(r.rating) AS rating " +
                        "FROM movies m " +
                        "JOIN ratings r ON m.id = r.movieId " +
                        "JOIN genres_in_movies gim ON m.id = gim.movieId " +
                        "JOIN genres g ON gim.genreId = g.id " +
                        "JOIN stars_in_movies sim ON m.id = sim.movieId " +
                        "JOIN stars s ON sim.starId = s.id " +
                        "WHERE UPPER(LEFT(m.title, 1)) = '" + first_letter.toUpperCase() + "' " + // Filter titles starting with 'A'
                        "GROUP BY m.id, m.title, m.year, m.director";

                statement = conn.prepareStatement(query);
//                statement.setString(1, first_letter);
            } else if (genre == null && first_letter == null) {
                System.out.println("search selected");
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("SELECT m.id AS movie_id, m.title, m.year, m.director, ");
                queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', '), ', ', 3) AS genres, ");
                queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', '), ', ', 3) AS stars, ");
                queryBuilder.append("MAX(r.rating) AS rating ");
                queryBuilder.append("FROM movies m ");
                queryBuilder.append("JOIN ratings r ON m.id = r.movieId ");
                queryBuilder.append("JOIN genres_in_movies gim ON m.id = gim.movieId ");
                queryBuilder.append("JOIN genres g ON gim.genreId = g.id ");
                queryBuilder.append("JOIN stars_in_movies sim ON m.id = sim.movieId ");
                queryBuilder.append("JOIN stars s ON sim.starId = s.id ");
                queryBuilder.append("WHERE 1=1 ");

                if (year != null && !year.isEmpty()) {
                    queryBuilder.append("AND m.year = '").append(year).append("' ");
                }
                if (director != null && !director.isEmpty()) {
                    queryBuilder.append("AND m.director LIKE '%").append(director).append("%' ");
                }
                if (name != null && !name.isEmpty()) {
                    queryBuilder.append("AND s.name LIKE '%").append(name).append("%' ");
                }
                if (title != null && !title.isEmpty()) {
                    queryBuilder.append("AND m.title LIKE '%").append(title).append("%' ");
                }

                queryBuilder.append("GROUP BY m.id, m.title, m.year, m.director ");
                query = queryBuilder.toString();
                System.out.println(query);

                statement = conn.prepareStatement(query);
            }
            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("movie_id");
                String movie_title = rs.getString("m.title");
                String movie_year = rs.getString("m.year");
                String movie_director = rs.getString("m.director");
                String movie_genre = rs.getString("genres");
                String movie_star = rs.getString("stars");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genre", movie_genre);
                jsonObject.addProperty("movie_star", movie_star);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
