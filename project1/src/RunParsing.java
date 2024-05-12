import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class RunParsing {

    private static void loadStarData(String filePath, String user, String password, String url) {
        String loadQuery = "LOAD DATA LOCAL INFILE '" + filePath + "' INTO TABLE stars " +
                "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' " +
                "LINES TERMINATED BY '\\n' IGNORE 1 LINES " +
                "(id, name, birthYear)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(loadQuery);
            System.out.println("Data successfully loaded into the database for stars. Rows affected: " + rowsAffected);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private static void loadStarMovieData(String filePath, String user, String password, String url) {
//        String loadQuery = "LOAD DATA LOCAL INFILE '" + filePath + "' INTO TABLE stars_in_movies " +
//                "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\n' " +
//                "IGNORE 1 LINES (starId, movieId) " +
//                "ON DUPLICATE KEY UPDATE starId=VALUES(starId), movieId=VALUES(movieId);";
//
//        try (Connection conn = DriverManager.getConnection(url, user, password);
//             Statement stmt = conn.createStatement()) {
//            stmt.execute(loadQuery);
//            System.out.println("Data successfully loaded into the database for stars_in_movies.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private static void loadStarMovieData(String filePath, String user, String password, String url) {
        String loadQuery = "LOAD DATA LOCAL INFILE '" + filePath + "' INTO TABLE stars_in_movies " +
                "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\n' " +
                "IGNORE 1 LINES (starId, movieId);";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            int rowsAffected = stmt.executeUpdate(loadQuery);
            System.out.println("Data successfully loaded into the database for stars_in_movie. Rows affected: " + rowsAffected);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadMovieData(String filePath, String user, String password, String url) {
        String loadQuery = "LOAD DATA LOCAL INFILE '" + filePath + "' INTO TABLE movies " +
                "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' " +
                "LINES TERMINATED BY '\\n' IGNORE 1 LINES " +
                "(id, title, year, director);";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(loadQuery);
            System.out.println("Data successfully loaded into the database for movies. Rows affected: " + rowsAffected);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadGenreInMovieData(String filePath, String user, String password, String url) {
        String loadQuery = "LOAD DATA LOCAL INFILE '" + filePath + "' INTO TABLE genres_in_movies " +
                "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' " +
                "LINES TERMINATED BY '\\n' IGNORE 1 LINES " +
                "(genreId, movieId);";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

                int rowsAffected = stmt.executeUpdate(loadQuery);
                System.out.println("Data successfully loaded into the database for genres_in_movie. Rows affected: " + rowsAffected);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        String pathToStar = "stars.csv";
        String pathToStarMovie = "stars_in_movies.csv";
        String pathToMovie = "movies.csv";
        String pathToGenreInMovie = "genres_in_movies.csv";

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb?allowLoadLocalInfile=true";

        String[] mainParserArgs = {};
        Main243Parser.main(mainParserArgs);

        loadMovieData(pathToMovie, loginUser, loginPasswd, loginUrl);
        loadGenreInMovieData(pathToGenreInMovie, loginUser, loginPasswd, loginUrl);

        String[] starParserArgs = {};
        StarParser.main(starParserArgs);

        loadStarData(pathToStar, loginUser, loginPasswd, loginUrl);
        loadStarMovieData(pathToStarMovie, loginUser, loginPasswd, loginUrl);


    }
}
