import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class Main243Parser extends DefaultHandler {

    List<Director> directors;
    List<String> director_name;
    private String tempVal;
    //to maintain context
    private Director tempDirector;
    private Integer unknown_num;

    private static final String INSERT_MOVIE_SQL = "INSERT IGNORE INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
    private static final String INSERT_GENRES_IN_MOVIES_SQL = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";

    public Main243Parser() {
        this.directors = new ArrayList<>();
        this.director_name = new ArrayList<>();
        this.unknown_num = 0;
    }

    public void runExample() {
        parseDocument();
        addData();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
//            SAXParser sp = spf.newSAXParser();
            SAXParser sp = spf.newSAXParser();
            FileInputStream fis = new FileInputStream("src/stanford-movies/mains243.xml");
            InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
            InputSource inputSource = new InputSource(isr);
            inputSource.setEncoding("ISO-8859-1");
            //parse the file and also register this class for call backs
            sp.parse(inputSource, this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void addData() {
        // add data to database
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        String moviesFile = "movies.csv";
        String genresInMovies = "genres_in_movies.csv";

        try (FileWriter moviesWriter = new FileWriter(moviesFile);
             FileWriter genresInMoviesWriter = new FileWriter(genresInMovies)) {

            moviesWriter.write("id,title,year,director\n");
            genresInMoviesWriter.write("genreId,movieId\n");

            // create database connection
            Connection conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
//            conn.setAutoCommit(false);

            for (Director director : directors)
            {
                String directorName = director.getDirectorName();
                for (String movieID : director.getMovieIDList())
                {
                    Integer movieYear = director.getMovieYear(movieID);
                    String movieName = director.getMovie(movieID);

                    moviesWriter.write(String.format("%s,%s,%d,%s\n", movieID, movieName, movieYear, directorName));

                    List<String> genre_list = director.getMovieGenreList(movieID);
                    for (String genre : genre_list)
                    {
                        Integer genreID = getGenreId(conn, genre);
                        genresInMoviesWriter.write(String.format("%d,%s\n", genreID, movieID));
                    }
                }
//                conn.commit();
            }
            moviesWriter.flush();
            genresInMoviesWriter.flush();
        }
        catch (IOException e)
        {
            System.err.println("Error in writing to the csv file: " + e.getMessage());
        }
        catch(SQLException e) {
            System.err.println("Error in sql connection: " + e.getMessage());
        }
    }

    private int getGenreId(Connection conn, String genre) throws SQLException
    {
        try (PreparedStatement statement = conn.prepareStatement("SELECT id FROM genres WHERE name = ?"))
        {
            statement.setString(1, genre);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    return resultSet.getInt("id");
                }
                else
                {
                    // Genre does not exist, insert it and return the generated ID
                    try (PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO genres (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS))
                    {
                        insertStatement.setString(1, genre);
                        insertStatement.executeUpdate();
                        try (ResultSet generatedKeys = insertStatement.getGeneratedKeys())
                        {
                            if (generatedKeys.next())
                            {
                                return generatedKeys.getInt(1);
                            }
                            else
                            {
                                throw new SQLException("Failed to insert genre, no ID obtained.");
                            }
                        }
                    }
                }
            }
        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("directorfilms")) {
            //create a new instance of employee
            tempDirector = new Director();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        try(FileWriter inconsistencyMovieWriter = new FileWriter("inconsistencyMovie.txt", true)) {
            if (qName.equalsIgnoreCase("directorfilms"))
            {
                //add it to the list
                if(tempDirector.getDirectorName() != null)
                {
                    directors.add(tempDirector);
                }
            }
            else if (qName.equalsIgnoreCase("dirid"))
            {
                //add it to the list
                tempDirector.setDirectorId(tempVal);
            }
            else if (qName.equalsIgnoreCase("dirname"))
            {
                String realVal = "";
                if((tempVal.isEmpty()) || (tempVal == null) || tempVal.isBlank())
                {
                    realVal = "NULL";
                    tempVal = "Unknown";
                }

                for (Director director : this.directors)
                {
                    if (director.getDirectorName().equals(tempVal))
                    {
                        tempDirector = director;
                        break;
                    }
                }

                tempDirector.setDirectorName(tempVal);
                if(tempVal.equals("UnKnown"))
                {
                    String directorId = tempDirector.getDirectorId();
                    inconsistencyMovieWriter.write("Inconsistency Report Where Director Id <dirid>: " + directorId + ", Tag: <dirname>, Value: " + realVal + " Set as: " + tempVal + ".\n");
                }
            }
            else if (qName.equalsIgnoreCase("fid"))
            {
                String realVal = "";
                if(tempVal.isEmpty() || tempVal == null || tempVal.isBlank())
                {
                    realVal = "NULL";
                    tempVal = "Unknown" + unknown_num;
                    unknown_num ++;
                }
                List <String> movie_ID_list = tempDirector.getMovieIDList();
                if(!movie_ID_list.contains(tempVal))
                {
                    tempDirector.addMovieID(tempVal);
                }
                if(tempVal.contains("UnKnown"))
                {
                    String directorId = tempDirector.getDirectorId();
                    inconsistencyMovieWriter.write("Inconsistency Report Where Director Id <dirid>: " + directorId + ", Tag: <fid>, Value: " + realVal + " Set as: " + tempVal + ".\n");
                }
            }
            else if (qName.equalsIgnoreCase("t"))
            {
                String realVal = "";
                if(tempVal.isEmpty() || tempVal == null || tempVal.isBlank())
                {
                    realVal = "NULL";
                }
                String movieID = tempDirector.getLastAddedMovieID();
                tempDirector.addMovieIDMovie(movieID, tempVal);
                if(tempVal.isEmpty() || tempVal == null || tempVal.isBlank())
                {
                    String directorId = tempDirector.getDirectorId();
                    inconsistencyMovieWriter.write("Inconsistency Report Where Director Id <dirid>: " + directorId + ", Tag: <t>, Value: " + realVal + " Ignored this case.\n");
                }
            }
            else if (qName.equalsIgnoreCase("year"))
            {
                String movieID = tempDirector.getLastAddedMovieID();
                try {
                    int year = Integer.parseInt(tempVal);
                    tempDirector.addMovieYear(movieID, year);
                } catch (NumberFormatException e) {
                    tempDirector.addMovieYear(movieID, -1);
                    String realVal = "";
                    if(tempVal.isEmpty() || tempVal == null || tempVal.isBlank())
                    {
                        realVal = "NULL";
                    }
                    else
                    {
                        realVal = tempVal;
                    }
                    String directorId = tempDirector.getDirectorId();
                    inconsistencyMovieWriter.write("Inconsistency Report Where Director Id <dirid>: " + directorId + ", Tag: <year>, Value: " + realVal + " Set as: -1.\n");
                }
            }
            else if (qName.equalsIgnoreCase("released"))
            {
                String movieID = tempDirector.getLastAddedMovieID();
                try {
                    int year = Integer.parseInt(tempVal);
                    tempDirector.addMovieYear(movieID, year);
                } catch (NumberFormatException e) {
                    tempDirector.addMovieYear(movieID, -1);
                    String realVal = "";
                    if(tempVal.isEmpty() || tempVal == null || tempVal.isBlank())
                    {
                        realVal = "NULL";
                    }
                    else
                    {
                        realVal = tempVal;
                    }

                    String directorId = tempDirector.getDirectorId();
                    inconsistencyMovieWriter.write("Inconsistency Report Where Director Id <dirid>: " + directorId + ", Tag: <released>, Value: " + realVal + " Set as: -1.\n");
                }
            }
            else if (qName.equalsIgnoreCase("cat"))
            {
                String realVal = "";
                if(tempVal.isEmpty() || tempVal == null || tempVal.isBlank())
                {
                    realVal = "NULL";
                    tempVal = "Undefined";
                }
                else
                {
                    realVal = tempVal;
                }
                String movieID = tempDirector.getLastAddedMovieID();
                tempDirector.addGenreToMovie(movieID, tempVal);

                if(tempVal.equals("Undefined"))
                {
                    String directorId = tempDirector.getDirectorId();
                    inconsistencyMovieWriter.write("Inconsistency Report Where Director Id <dirid>: " + directorId + ", Tag: <cat>, Value: " + realVal + " Set as: Undefined.\n");
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error writing to inconsistency file");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Main243Parser spe = new Main243Parser();
        spe.runExample();
    }

}