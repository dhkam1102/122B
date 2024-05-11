import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StarParser extends DefaultHandler {
    //map= {stagename: starinfo}
    private Map<String, Star> starMap;
    //    private List<Star> stars;
    private Star tempStar;
    private String tempVal;
    private boolean ActorFile;
    private int id_count = 1;
    private String tempMovie;

    PreparedStatement starStatment = null;
    PreparedStatement starInMovieStatment = null;
    PreparedStatement checkMovieExistStatment = null;

    public StarParser() {
//        stars = new ArrayList<>();
        starMap = new HashMap<>();
    }

    public void parseDocument(String filename, boolean isActor) {
        ActorFile = isActor;
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse(filename, this);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    public void addData(List<Star> stars) {
        //아직 거를애들 안거름 루프돌릴때 그러니까 주의

        String starInMoviesFile = "stars_in_movies.csv";
        String starFile = "stars.csv";

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/parsing";

        try (FileWriter starWriter = new FileWriter(starFile);
             FileWriter starInMoviesWriter = new FileWriter(starInMoviesFile)) {

            starWriter.append("id,name,birthYear\n");
            starInMoviesWriter.append("starId,movieId\n");

            try {
                Connection conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
//            conn.setAutoCommit(false);

//                String insertStarSQL = "INSERT IGNORE INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
//            String insertStarSQL = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), birthYear=VALUES(birthYear)";
//                String insertStarInMovieSQL = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?) ON DUPLICATE KEY UPDATE starId=VALUES(starId), movieId=VALUES(movieId)";
                String checkingMovieExist = "SELECT COUNT(*) FROM movies WHERE id=?";
                checkMovieExistStatment = conn.prepareStatement(checkingMovieExist);
                for (Star star: stars) {
                    starWriter.append(star.getId());
                    starWriter.append(',');
                    starWriter.append(star.getName());
                    starWriter.append(',');
                    if (star.getBirthYear() == 0) {
                        starWriter.append("NULL");
                    } else {
                        starWriter.append(String.valueOf(star.getBirthYear()));
                    }
                    starWriter.append('\n');

                    for (String movie_id: star.getMovieIds()) {
                        checkMovieExistStatment.setString(1, movie_id);
                        ResultSet rs = checkMovieExistStatment.executeQuery();
                        if(rs.next() && rs.getInt(1) > 0) {
                            starInMoviesWriter.append(star.getId());
                            starInMoviesWriter.append(',');
                            starInMoviesWriter.append(movie_id);
                            starInMoviesWriter.append('\n');
                        }
                    }
                }
//            conn.commit();
            }
            catch (SQLException e) {
                System.err.println("SQL Exception: " + e.getMessage());
            }

            starInMoviesWriter.flush();
            starWriter.flush();
        }
        catch(IOException e) {
            System.err.println("Error writing to star in movie file");
        }
    }


    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (ActorFile && qName.equalsIgnoreCase("actor")) {
            tempStar = new Star();
            tempStar.setId("xml" + id_count++);
        }
        else if (!ActorFile && qName.equalsIgnoreCase("m")) {
//            String stageName = attributes.getValue("a");
//            tempStar = starMap.getOrDefault(stageName, null);
//            tempStar = new Star();
            tempMovie = null;
            tempStar = null;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length).trim();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ActorFile) {
            if (qName.equalsIgnoreCase("actor")) { }

            else if (qName.equalsIgnoreCase("stagename")) {
                tempStar.setStageName(tempVal);
                starMap.put(tempVal, tempStar);
            }
            else if (qName.equalsIgnoreCase("familyname")) {
                tempStar.setLastName(tempVal);
            }
            else if (qName.equalsIgnoreCase("firstname")) {
                tempStar.setFirstName(tempVal);
                tempStar.setName(tempStar.getFirstName() + " " + tempStar.getLastName());
            }

            else if (qName.equalsIgnoreCase("dob")) {
                Pattern yearPattern = Pattern.compile("(\\d{4})\\+?");
                Matcher matcher = yearPattern.matcher(tempVal);
                int birthYear = 0;

                if (matcher.find()) {
                    birthYear =Integer.parseInt(matcher.group(1));
                }

                tempStar.setBirthYear(birthYear);
            }
        }
        else {
            if (qName.equalsIgnoreCase("f")) {
                tempMovie = tempVal;

            }
            else if (qName.equalsIgnoreCase("a")) {
                if (starMap.containsKey(tempVal)) {
                    tempStar = starMap.get(tempVal);
                } else {
                    tempStar = new Star();
//                    tempStar.setId(id_count++);
                    tempStar.setStageName(tempVal);
                    starMap.put(tempVal, tempStar);
                }
                if (tempMovie != null) {
                    tempStar.addMovieId(tempMovie);
                }
            }
        }
    }

    public List<Star> getStars() {
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        List<Star> filteredStars = new ArrayList<>();

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            conn.setAutoCommit(false);

            try (PreparedStatement checkExistence = conn.prepareStatement("SELECT id, name, birthYear FROM stars WHERE name = ?")) {
                for (Star star : starMap.values()) {
                    if (star.getName() != null && star.getId() != null && !star.getName().trim().isEmpty()) {
                        checkExistence.setString(1, star.getName());
                        try (ResultSet rs = checkExistence.executeQuery()) {
                            boolean foundMatch = false;
                            while (rs.next()) {
                                int dbBirthYear = rs.getInt("birthYear");
                                String dbName = rs.getString("name");
                                if (star.getBirthYear() == 0 && rs.wasNull()) {
                                    star.setId(rs.getString("id"));
                                    foundMatch = true;
                                    break;
                                }
                                else if (star.getBirthYear() == 0 && !rs.wasNull()) {
                                    star.setId(rs.getString("id"));
                                    star.setBirthYear(dbBirthYear);
                                    foundMatch = true;
                                    break;
                                }
                                else if (star.getBirthYear() == dbBirthYear && star.getName().equalsIgnoreCase(dbName)) {
                                    star.setId(rs.getString("id"));
                                    star.setBirthYear(dbBirthYear);
                                    foundMatch = true;
                                    break;
                                }
                            }
                            filteredStars.add(star);
                        }
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }
        return filteredStars;
    }


    public static void main(String[] args) {
        StarParser parser = new StarParser();
        parser.parseDocument("stanford-movies/actors63.xml", true); // Parse actors
        parser.parseDocument("stanford-movies/casts124.xml", false); // Parse casts
        List<Star> stars = parser.getStars();
        stars.forEach(System.out::println);
//        System.out.println("from here I will start the insertion part =======================");
        parser.addData(stars);


    }
}
