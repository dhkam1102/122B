import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jakarta.servlet.ServletConfig;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class Main243Parser extends DefaultHandler {

    List<Director> directors;
    List<String> director_name;
    private String tempVal;
    //to maintain context
    private Director tempDirector;

    private static final String INSERT_MOVIE_SQL = "INSERT IGNORE INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
    private static final String INSERT_GENRES_IN_MOVIES_SQL = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";

    public Main243Parser() {
        this.directors = new ArrayList<>();
        this.director_name = new ArrayList<>();
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
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("stanford-movies/mains243.xml", this);

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
        String loginUrl = "jdbc:mysql://localhost:3306/parsing";

        try
        {
            // create database connection
            Connection conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            for (Director director : directors)
            {
                String directorName = director.getDirectorName();
                for (String movieID : director.getMovieIDList())
                {
                    Integer movieYear = director.getMovieYear(movieID);
                    String movieName = director.getMovie(movieID);

                    try (PreparedStatement statement = conn.prepareStatement(INSERT_MOVIE_SQL))
                    {
                        statement.setString(1, movieID);
                        statement.setString(2, movieName);
                        statement.setInt(3, movieYear);
                        statement.setString(4, directorName);
                        statement.executeUpdate();
                    }

                    List<String> genre_list = director.getMovieGenreList(movieID);
                    for (String genre : genre_list)
                    {
                        if(genre.equals("") || genre.isEmpty())
                        {
                            genre = "Undefined";
                        }
                        Integer genreID = getGenreId(conn, genre);
                        try (PreparedStatement genresInMoviesStatement = conn.prepareStatement(INSERT_GENRES_IN_MOVIES_SQL))
                        {
                            genresInMoviesStatement.setInt(1, genreID);
                            genresInMoviesStatement.setString(2, movieID);
                            genresInMoviesStatement.executeUpdate();
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private int getGenreId(Connection conn, String genre) throws SQLException {

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

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("directorfilms"))
        {
            //add it to the list
            if(tempDirector.getDirectorName() != null)
            {
                directors.add(tempDirector);
            }
        }
        else if (qName.equalsIgnoreCase("dirname"))
        {
            if(!director_name.contains(tempVal))
            {
                director_name.add(tempVal);
                tempDirector.setDirectorName(tempVal);
            }
        }
        else if (qName.equalsIgnoreCase("fid"))
        {
            tempDirector.addMovieID(tempVal);
        }
        else if (qName.equalsIgnoreCase("t"))
        {
            String movieID = tempDirector.getLastAddedMovieID();
            tempDirector.addMovieIDMovie(movieID, tempVal);
        }
        else if (qName.equalsIgnoreCase("year")) {
            String movieID = tempDirector.getLastAddedMovieID();
            try {
                int year = Integer.parseInt(tempVal);
                tempDirector.addMovieYear(movieID, year);
            } catch (NumberFormatException e) {
                System.out.println("bump1");
                tempDirector.addMovieYear(movieID, -1);
            }
        }
        else if (qName.equalsIgnoreCase("released"))
        {
            String movieID = tempDirector.getLastAddedMovieID();
            try {
                int year = Integer.parseInt(tempVal);
                tempDirector.addMovieYear(movieID, year);
            } catch (NumberFormatException e) {
                System.out.println("bump2");
                tempDirector.addMovieYear(movieID, -1);
            }
        }
        else if (qName.equalsIgnoreCase("cat"))
        {
            String movieID = tempDirector.getLastAddedMovieID();
            tempDirector.addGenreToMovie(movieID, tempVal);
        }
    }

    public static void main(String[] args) {
        Main243Parser spe = new Main243Parser();
        spe.runExample();
    }

}