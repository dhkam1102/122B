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
        String title_sorting = request.getParameter("ts");
        request.getServletContext().log("getting title sorting: " + title_sorting);
        String rating_sorting = request.getParameter("rs");
        request.getServletContext().log("getting rating sorting: " + rating_sorting);
        String page_size = request.getParameter("size");
        request.getServletContext().log("getting page_size: " + page_size);
        int size = Integer.parseInt(page_size);
        String page_number = request.getParameter("page");
        request.getServletContext().log("getting page_number: " + page_number);
        int page = Integer.parseInt(page_number);
        int offset = (page - 1) * size;

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String currentURL = "movie-list.html?" + request.getQueryString();
        session.setAttribute("currentURL", currentURL);

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            String query = "";
            String orderClause = "";
            PreparedStatement statement = null;
            if (!genre.isEmpty() && first_letter.isEmpty()){
                if (title_sorting.equals("ASC1") || title_sorting.equals("DESC1")){
                    if (rating_sorting.equals("ASC2")){
                        if(title_sorting.equals("ASC1")){
                            orderClause = "ORDER BY m.title ASC, rating ASC ";
                        }
                        else if (title_sorting.equals("DESC1")){
                            orderClause = "ORDER BY m.title DESC, rating ASC ";
                        }
                    }
                    else if (rating_sorting.equals("DESC2")){
                        if(title_sorting.equals("ASC1")){
                            orderClause = "ORDER BY m.title ASC, rating DESC ";
                        }
                        else if (title_sorting.equals("DESC1")){
                            orderClause = "ORDER BY m.title DESC, rating DESC ";
                        }
                    }
                }
                else if (title_sorting.equals("ASC2") || title_sorting.equals("DESC2")){
                    if (rating_sorting.equals("ASC1")){
                        if(title_sorting.equals("ASC2")){
                            orderClause = "ORDER BY rating ASC, m.title ASC ";
                        }
                        else if (title_sorting.equals("DESC2")){
                            orderClause = "ORDER BY rating ASC, m.title DESC ";
                        }
                    }
                    else if (rating_sorting.equals("DESC1")){
                        if(title_sorting.equals("ASC2")){
                            orderClause = "ORDER BY rating DESC, m.title ASC ";
                        }
                        else if (title_sorting.equals("DESC2")){
                            orderClause = "ORDER BY rating DESC, m.title DESC ";
                        }
                    }
                }

                query = "SELECT COUNT(*) AS row_count, m.id AS movie_id, m.title, m.year, m.director, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', '), ', ', 3) AS genres, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY num_movies_played DESC, s.name SEPARATOR ', '), ', ', 3) AS stars, " +
                        "MAX(r.rating) AS rating " +
                        "FROM movies m " +
                        "JOIN ratings r ON m.id = r.movieId " +
                        "JOIN genres_in_movies gim ON m.id = gim.movieId " +
                        "JOIN genres g ON gim.genreId = g.id " +
                        "JOIN stars_in_movies sim ON m.id = sim.movieId " +
                        "JOIN stars s ON sim.starId = s.id " +
                        "LEFT JOIN (SELECT s.id AS star_id, COUNT(DISTINCT m2.id) AS num_movies_played FROM stars s JOIN stars_in_movies sim2 ON s.id = sim2.starId JOIN movies m2 ON sim2.movieId = m2.id GROUP BY s.id) AS star_counts ON s.id = star_counts.star_id " +
                        "WHERE m.id IN (SELECT m.id FROM movies m JOIN genres_in_movies gim ON m.id = gim.movieId JOIN genres g ON gim.genreId = g.id WHERE g.name = '" + genre + "') " +
                        "GROUP BY m.id, m.title, m.year, m.director " +
                        orderClause +
                        " LIMIT " + page_size + " OFFSET " + offset;
                statement = conn.prepareStatement(query);
            } else if (genre.isEmpty() && !first_letter.isEmpty()) {
                String whereClause;
                if ("*".equals(first_letter)) {
                    // Show movies that start with non-alphanumeric characters
                    whereClause = "WHERE UPPER(LEFT(m.title, 1)) REGEXP '[^A-Za-z0-9]'";
                } else {
                    // Show movies that start with the specified letter
                    whereClause = "WHERE UPPER(LEFT(m.title, 1)) = '" + first_letter.toUpperCase() + "'";
                }

                if (title_sorting.equals("ASC1") || title_sorting.equals("DESC1")){
                    if (rating_sorting.equals("ASC2")){
                        if(title_sorting.equals("ASC1")){
                            orderClause = "ORDER BY m.title ASC, rating ASC ";
                        }
                        else if (title_sorting.equals("DESC1")){
                            orderClause = "ORDER BY m.title DESC, rating ASC ";
                        }
                    }
                    else if (rating_sorting.equals("DESC2")){
                        if(title_sorting.equals("ASC1")){
                            orderClause = "ORDER BY m.title ASC, rating DESC ";
                        }
                        else if (title_sorting.equals("DESC1")){
                            orderClause = "ORDER BY m.title DESC, rating DESC ";
                        }
                    }
                }
                else if (title_sorting.equals("ASC2") || title_sorting.equals("DESC2")){
                    if (rating_sorting.equals("ASC1")){
                        if(title_sorting.equals("ASC2")){
                            orderClause = "ORDER BY rating ASC, m.title ASC ";
                        }
                        else if (title_sorting.equals("DESC2")){
                            orderClause = "ORDER BY rating ASC, m.title DESC ";
                        }
                    }
                    else if (rating_sorting.equals("DESC1")){
                        if(title_sorting.equals("ASC2")){
                            orderClause = "ORDER BY rating DESC, m.title ASC ";
                        }
                        else if (title_sorting.equals("DESC2")){
                            orderClause = "ORDER BY rating DESC, m.title DESC ";
                        }
                    }
                }
                query = "SELECT COUNT(*) AS row_count, m.id AS movie_id, m.title, m.year, m.director, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', '), ', ', 3) AS genres, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY movies_played DESC, s.name SEPARATOR ', '), ', ', 3) AS stars, " +
                        "MAX(r.rating) AS rating " +
                        "FROM movies m " +
                        "JOIN ratings r ON m.id = r.movieId " +
                        "JOIN genres_in_movies gim ON m.id = gim.movieId " +
                        "JOIN genres g ON gim.genreId = g.id " +
                        "JOIN stars_in_movies sim ON m.id = sim.movieId " +
                        "JOIN stars s ON sim.starId = s.id " +
                        "LEFT JOIN (SELECT starId, COUNT(movieId) AS movies_played FROM stars_in_movies GROUP BY starId) as star_counts ON s.id = star_counts.starId " +
                        whereClause +
                        "GROUP BY m.id, m.title, m.year, m.director " +
                        orderClause +
                        "LIMIT " + page_size + " OFFSET " + offset;

                statement = conn.prepareStatement(query);
            } else if (genre.isEmpty() && first_letter.isEmpty()) {

                if (title_sorting.equals("ASC1") || title_sorting.equals("DESC1")){
                    if (rating_sorting.equals("ASC2")){
                        if(title_sorting.equals("ASC1")){
                            orderClause = "ORDER BY m.title ASC, rating ASC ";
                        }
                        else if (title_sorting.equals("DESC1")){
                            orderClause = "ORDER BY m.title DESC, rating ASC ";
                        }
                    }
                    else if (rating_sorting.equals("DESC2")){
                        if(title_sorting.equals("ASC1")){
                            orderClause = "ORDER BY m.title ASC, rating DESC ";
                        }
                        else if (title_sorting.equals("DESC1")){
                            orderClause = "ORDER BY m.title DESC, rating DESC ";
                        }
                    }
                }
                else if (title_sorting.equals("ASC2") || title_sorting.equals("DESC2")){
                    if (rating_sorting.equals("ASC1")){
                        if(title_sorting.equals("ASC2")){
                            orderClause = "ORDER BY rating ASC, m.title ASC";
                        }
                        else if (title_sorting.equals("DESC2")){
                            orderClause = "ORDER BY rating ASC, m.title DESC";
                        }
                    }
                    else if (rating_sorting.equals("DESC1")){
                        if(title_sorting.equals("ASC2")){
                            orderClause = "ORDER BY rating DESC, m.title ASC";
                        }
                        else if (title_sorting.equals("DESC2")){
                            orderClause = "ORDER BY rating DESC, m.title DESC";
                        }
                    }
                }
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("SELECT COUNT(*) AS row_count, m.id AS movie_id, m.title, m.year, m.director, ");
                queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', '), ', ', 3) AS genres, ");
                queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY star_counts.movies_played DESC, s.name SEPARATOR ', '), ', ', 3) AS stars, ");
                queryBuilder.append("MAX(r.rating) AS rating ");
                queryBuilder.append("FROM movies m ");
                queryBuilder.append("JOIN ratings r ON m.id = r.movieId ");
                queryBuilder.append("JOIN genres_in_movies gim ON m.id = gim.movieId ");
                queryBuilder.append("JOIN genres g ON gim.genreId = g.id ");
                queryBuilder.append("JOIN stars_in_movies sim ON m.id = sim.movieId ");
                queryBuilder.append("JOIN stars s ON sim.starId = s.id ");
                queryBuilder.append("LEFT JOIN (SELECT starId, COUNT(movieId) AS movies_played FROM stars_in_movies GROUP BY starId) AS star_counts ON s.id = star_counts.starId ");
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
                queryBuilder.append(orderClause);
                queryBuilder.append("LIMIT ").append(page_size).append(" OFFSET ").append(offset);
                query = queryBuilder.toString();
                statement = conn.prepareStatement(query);
            }
            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String row_count = rs.getString("row_count");
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the URL stored in the session
        HttpSession session = request.getSession();
        String storedUrl = (String) session.getAttribute("currentURL");

        // Return the stored URL in the response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"redirectUrl\": \"" + storedUrl + "\"}");
    }
}
