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
import java.util.ArrayList;
import java.util.List;

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

        //create order clause
        String orderClause = "";
        if (title_sorting.equals("ASC1") || title_sorting.equals("DESC1"))
        {
            if (rating_sorting.equals("ASC2"))
            {
                if(title_sorting.equals("ASC1"))
                {
                    orderClause = "ORDER BY m.title ASC, rating ASC ";
                }
                else if (title_sorting.equals("DESC1"))
                {
                    orderClause = "ORDER BY m.title DESC, rating ASC ";
                }
            }
            else if (rating_sorting.equals("DESC2"))
            {
                if(title_sorting.equals("ASC1"))
                {
                    orderClause = "ORDER BY m.title ASC, rating DESC ";
                }
                else if (title_sorting.equals("DESC1"))
                {
                    orderClause = "ORDER BY m.title DESC, rating DESC ";
                }
            }
        }
        else if (title_sorting.equals("ASC2") || title_sorting.equals("DESC2"))
        {
            if (rating_sorting.equals("ASC1"))
            {
                if(title_sorting.equals("ASC2"))
                {
                    orderClause = "ORDER BY rating ASC, m.title ASC ";
                }
                else if (title_sorting.equals("DESC2"))
                {
                    orderClause = "ORDER BY rating ASC, m.title DESC ";
                }
            }
            else if (rating_sorting.equals("DESC1"))
            {
                if(title_sorting.equals("ASC2"))
                {
                    orderClause = "ORDER BY rating DESC, m.title ASC ";
                }
                else if (title_sorting.equals("DESC2"))
                {
                    orderClause = "ORDER BY rating DESC, m.title DESC ";
                }
            }
        }
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String currentURL = "movie-list.html?" + request.getQueryString();
        session.setAttribute("currentURL", currentURL);
        List<String> query_list = new ArrayList<>();
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection())
        {
            if (!genre.isEmpty() && first_letter.isEmpty())
            {
                JsonArray jsonArray = new JsonArray();
                String genreIdQuery = "SELECT id FROM genres WHERE name = ?";
                try(PreparedStatement genreIdStatement = conn.prepareStatement(genreIdQuery))
                {
                    genreIdStatement.setString(1, genre);
                    ResultSet result = genreIdStatement.executeQuery();
                    while(result.next())
                    {
                        String genreId = result.getString(1);
                        String movieListQuery = "SELECT DISTINCT m.id AS movie_id, m.title, m.year, m.director, COALESCE(r.rating, 0.0) AS rating " +
                            "FROM movies AS m " +
                            "JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                            "LEFT JOIN ratings AS r ON m.id = r.movieId " +
                            "WHERE gim.genreId = ? " + // Placeholder for genre ID
                            orderClause + " LIMIT " + page_size + " OFFSET " + offset;// Inject order clause
                        try(PreparedStatement movieListStatement = conn.prepareStatement(movieListQuery))
                        {
                            movieListStatement.setString(1, genreId);
                            ResultSet rs = movieListStatement.executeQuery();
                            while (rs.next())
                            {
                                JsonObject jsonObject = new JsonObject();
                                String movie_id = rs.getString("movie_id");
                                String movie_title = rs.getString("m.title");
                                String movie_year = rs.getString("m.year");
                                String movie_director = rs.getString("m.director");
                                String movie_rating_str = rs.getString("rating");
                                String movie_rating = null;
                                if (movie_rating_str != null) {
                                    double movie_rating_double = Double.parseDouble(movie_rating_str);
                                    double rounded_rating = Math.round(movie_rating_double * 10.0) / 10.0;
                                    movie_rating = String.format("%.1f", rounded_rating);
                                }

                                jsonObject.addProperty("movie_id", movie_id);
                                jsonObject.addProperty("movie_title", movie_title);
                                jsonObject.addProperty("movie_year", movie_year);
                                jsonObject.addProperty("movie_director", movie_director);
                                jsonObject.addProperty("movie_rating", movie_rating);

                                String genreListQuery = "select * from genres as g, genres_in_movies as gim where gim.genreId = g.id AND gim.movieId = ? ORDER BY g.name LIMIT 3";
                                try(PreparedStatement genreListStatement = conn.prepareStatement(genreListQuery))
                                {
                                    StringBuilder genreStringBuilder = new StringBuilder();
                                    genreListStatement.setString(1, movie_id);
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

                                String starListQuery = "SELECT s.id, s.name, COUNT(sim.movieId) AS total_count " +
                                        "FROM (SELECT DISTINCT starId FROM stars_in_movies WHERE movieId = ?) AS distinct_stars " +
                                        "JOIN stars AS s ON distinct_stars.starId = s.id " +
                                        "JOIN stars_in_movies AS sim ON s.id = sim.starId " +
                                        "GROUP BY s.id, s.name " +
                                        "ORDER BY total_count DESC, s.name ASC " +
                                        "LIMIT 3";
                                try(PreparedStatement starListStatement = conn.prepareStatement(starListQuery))
                                {
                                    StringBuilder starStringBuilder = new StringBuilder();
                                    starListStatement.setString(1, movie_id);
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
                        }
                    }
                }
                request.getServletContext().log("getting " + jsonArray.size() + " results");

                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
            else if (genre.isEmpty() && !first_letter.isEmpty())
            {
                String whereClause;
                if ("*".equals(first_letter))
                {
                    // Show movies that start with non-alphanumeric characters
                    whereClause = "WHERE UPPER(LEFT(m.title, 1)) REGEXP '[^A-Za-z0-9]' ";
                }
                else
                {
                    // Show movies that start with the specified letter
                    whereClause = "WHERE UPPER(LEFT(m.title, 1)) = '" + first_letter.toUpperCase() + "' ";
                }

                JsonArray jsonArray = new JsonArray();
                String movieListQuery = "SELECT m.id as movie_id, m.title, m.year, m.director, COALESCE(r.rating, 0.0) AS rating" +
                        " FROM movies AS m LEFT JOIN ratings AS r ON m.id = r.movieId " + whereClause + orderClause + "LIMIT " + page_size + " OFFSET " + offset;
                try(PreparedStatement movieListStatement = conn.prepareStatement(movieListQuery))
                {
                    ResultSet rs = movieListStatement.executeQuery();
                    JsonObject jsonObject = new JsonObject();
                    while (rs.next())
                    {
                        String movie_id = rs.getString("movie_id");
                        String movie_title = rs.getString("m.title");
                        String movie_year = rs.getString("m.year");
                        String movie_director = rs.getString("m.director");
                        String movie_rating_str = rs.getString("rating");
                        String movie_rating = null;
                        if (movie_rating_str != null) {
                            double movie_rating_double = Double.parseDouble(movie_rating_str);
                            double rounded_rating = Math.round(movie_rating_double * 10.0) / 10.0;
                            movie_rating = String.format("%.1f", rounded_rating);
                        }

                        jsonObject.addProperty("movie_id", movie_id);
                        jsonObject.addProperty("movie_title", movie_title);
                        jsonObject.addProperty("movie_year", movie_year);
                        jsonObject.addProperty("movie_director", movie_director);
                        jsonObject.addProperty("movie_rating", movie_rating);

                        String genreListQuery = "select * from genres as g, genres_in_movies as gim where gim.genreId = g.id AND gim.movieId = ? ORDER BY g.name LIMIT 3";
                        try(PreparedStatement genreListStatement = conn.prepareStatement(genreListQuery))
                        {
                            StringBuilder genreStringBuilder = new StringBuilder();
                            genreListStatement.setString(1, movie_id);
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

                        String starListQuery = "SELECT s.id, s.name, COUNT(sim.movieId) AS total_count " +
                                "FROM (SELECT DISTINCT starId FROM stars_in_movies WHERE movieId = ?) AS distinct_stars " +
                                "JOIN stars AS s ON distinct_stars.starId = s.id " +
                                "JOIN stars_in_movies AS sim ON s.id = sim.starId " +
                                "GROUP BY s.id, s.name " +
                                "ORDER BY total_count DESC, s.name ASC " +
                                "LIMIT 3";
                        try(PreparedStatement starListStatement = conn.prepareStatement(starListQuery))
                        {
                            StringBuilder starStringBuilder = new StringBuilder();
                            starListStatement.setString(1, movie_id);
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
                }
                request.getServletContext().log("getting " + jsonArray.size() + " results");

                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
            else if (genre.isEmpty() && first_letter.isEmpty())
            {
//                StringBuilder mid_query = new StringBuilder();
//                mid_query.append("SELECT m.id AS movie_id ");
//                mid_query.append("FROM movies m JOIN stars_in_movies sim ON m.id = sim.movieId ");
//                mid_query.append("JOIN stars s ON sim.starId = s.id ");
//                mid_query.append("WHERE 1=1 "); // Start with a true condition to simplify subsequent ANDs
//
//                if (year != null && !year.isEmpty()) {
//                    mid_query.append("AND m.year = '").append(year).append("' ");
//                }
//                if (director != null && !director.isEmpty()) {
//                    mid_query.append("AND m.director LIKE '%").append(director).append("%' ");
//                }
//                if (name != null && !name.isEmpty()) {
//                    mid_query.append("AND s.name LIKE '%").append(name).append("%' ");
//                }
//                if (title != null && !title.isEmpty()) {
//                    mid_query.append("AND m.title LIKE '%").append(title).append("%' ");
//                }
//                String mid_query_string = mid_query.toString();
//                PreparedStatement statement2 = conn.prepareStatement(mid_query_string);
//                ResultSet mid_result = statement2.executeQuery();
//
//                List<String> movieIds = new ArrayList<>();
//                while (mid_result.next()) {
//                    movieIds.add(mid_result.getString("movie_id"));
//                }
//
//                StringBuilder joiner = new StringBuilder("(");
//                for (int i = 0; i < movieIds.size(); i++)
//                {
//                    if (i > 0) {
//                        joiner.append(",");
//                    }
//                    joiner.append("'");
//                    joiner.append(movieIds.get(i));
//                    joiner.append("'");
//                }
//                joiner.append(")");
//                String movie_id_list = joiner.toString();
//
//                System.out.println("movie id list" + movie_id_list);
//
//                if (!movie_id_list.equals("()"))
//                {
//                    StringBuilder queryBuilder = new StringBuilder();
//                    queryBuilder.append("SELECT COUNT(*) AS row_count, m.id AS movie_id, m.title, m.year, m.director, ");
//                    queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', '), ', ', 3) AS genres, ");
//                    queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY star_counts.movies_played DESC, s.name SEPARATOR ', '), ', ', 3) AS stars, ");
//                    queryBuilder.append("MAX(r.rating) AS rating ");
//                    queryBuilder.append("FROM movies m ");
//                    queryBuilder.append("JOIN ratings r ON m.id = r.movieId ");
//                    queryBuilder.append("JOIN genres_in_movies gim ON m.id = gim.movieId ");
//                    queryBuilder.append("JOIN genres g ON gim.genreId = g.id ");
//                    queryBuilder.append("JOIN stars_in_movies sim ON m.id = sim.movieId ");
//                    queryBuilder.append("JOIN stars s ON sim.starId = s.id ");
//                    queryBuilder.append("LEFT JOIN (SELECT starId, COUNT(movieId) AS movies_played FROM stars_in_movies GROUP BY starId) AS star_counts ON s.id = star_counts.starId ");
//                    queryBuilder.append("WHERE ");
//                    queryBuilder.append("m.id IN ").append(movie_id_list);
//                    queryBuilder.append(" GROUP BY m.id, m.title, m.year, m.director ");
//                    queryBuilder.append(orderClause);
//                    queryBuilder.append(" LIMIT ").append(page_size).append(" OFFSET ").append(offset);
//                    query = queryBuilder.toString();
//                }
//                else
//                {
//                    query = "SELECT * FROM movies WHERE 1=0";
//                }
//                statement = conn.prepareStatement(query);
            }
        }
        catch (Exception e)
        {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally
        {
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
