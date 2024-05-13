import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Director {
    private String director_name;
    private String director_id;
    private List<String> movieID_list;
    private Map<String, String> movieID_movie_map;
    private Map<String, Integer> movieID_year_map;
    private Map<String, List<String>> movieID_genre_map;

    public Director() {
        this.movieID_list = new ArrayList<>();
        this.movieID_movie_map = new HashMap<>();
        this.movieID_year_map = new HashMap<>();
        this.movieID_genre_map = new HashMap<>();
    }

    public Director(String director_name) {
        this.director_name = director_name;
        this.movieID_list = new ArrayList<>();
        this.movieID_movie_map = new HashMap<>();
        this.movieID_year_map = new HashMap<>();
        this.movieID_genre_map = new HashMap<>();
    }

    // Getters and setters
    public String getDirectorName() {
        return director_name;
    }

    public void setDirectorName(String director_name) {
        this.director_name = director_name;
    }

    public String getDirectorId() {
        return director_id;
    }

    public void setDirectorId(String director_id) {
        this.director_id = director_id;
    }

    public List<String> getMovieIDList() {
        return movieID_list;
    }

    public void addMovieID(String movieID) {
        if(!movieID_list.contains(movieID))
        {
            movieID_list.add(movieID);
        }
    }

    public void setMovieIDList(List<String> movieID_list) {
        this.movieID_list = movieID_list;
    }

    public String getLastAddedMovieID() {
        if (movieID_list.isEmpty()) {
            return null; // or throw an exception
        }
        return movieID_list.get(movieID_list.size() - 1);
    }

    public Map<String, String> getMovieIdMovieMap() {
        return movieID_movie_map;
    }

    public void setMovieIdMovieMap(Map<String, String> movieId_movie_map) {
        this.movieID_movie_map = movieId_movie_map;
    }

    public String getMovie(String movieID) {
        return movieID_movie_map.getOrDefault(movieID, "");
    }

    public void addMovieIDMovie(String movieID, String movieName) {
        movieID_movie_map.put(movieID, movieName);
    }

    public Map<String, Integer> getMovieIDYearMap() {
        return movieID_year_map;
    }

    public void setMovieIDYearMap(Map<String, Integer> movieID_year_map) {
        this.movieID_year_map = movieID_year_map;
    }

    public Integer getMovieYear(String movieID) {
        return movieID_year_map.getOrDefault(movieID, -1);
    }

    public void addMovieYear(String movieID, int year) {
        movieID_year_map.put(movieID, year);
    }

    public Map<String, List<String>> getMovieIDGenreMap() {
        return movieID_genre_map;
    }

    public void setMovieIDGenreMap(Map<String, List<String>> movieID_genre_map) {
        this.movieID_genre_map = movieID_genre_map;
    }

    public List<String> getMovieGenreList(String movieID) {
        return movieID_genre_map.getOrDefault(movieID, new ArrayList<>());
    }

    // Other methods
    public void addGenreToMovie(String movieID, String genre) {
        if (!movieID_genre_map.containsKey(movieID)) {
            movieID_genre_map.put(movieID, new ArrayList<>());
        }
        String newGenre = "";
        if(genre.equalsIgnoreCase("AvGa"))
        {
            newGenre = "Avant Garde";
        }
        else if(genre.equalsIgnoreCase("Dram"))
        {
            newGenre = "Drama";
        }
        else if(genre.equalsIgnoreCase("Comd"))
        {
            newGenre = "Comedy";
        }
        else if(genre.equalsIgnoreCase("CnRb"))
        {
            newGenre = "Cops and Robbers";
        }
        else if(genre.equalsIgnoreCase("Susp"))
        {
            newGenre = "Thriller";
        }
        else if(genre.equalsIgnoreCase("Advt"))
        {
            newGenre = "Adventure";
        }
        else if(genre.equalsIgnoreCase("Actn"))
        {
            newGenre = "Action";
        }
        else if(genre.equalsIgnoreCase("Myst"))
        {
            newGenre = "Mystery";
        }
        else if(genre.equalsIgnoreCase("Porn"))
        {
            newGenre = "Pornography";
        }
        else if(genre.equalsIgnoreCase("TV"))
        {
            newGenre = "TV show";
        }
        else if(genre.equalsIgnoreCase("TVs"))
        {
            newGenre = "TV series";
        }
        else if(genre.equalsIgnoreCase("Cart"))
        {
            newGenre = "Animation";
        }
        else if(genre.equalsIgnoreCase("Camp"))
        {
            newGenre = "Camp";
        }
        else if(genre.equalsIgnoreCase("Faml"))
        {
            newGenre = "Family";
        }
        else if(genre.equalsIgnoreCase("BioP"))
        {
            newGenre = "Biography";
        }
        else if(genre.equalsIgnoreCase("Romt"))
        {
            newGenre = "Romance";
        }
        else if(genre.equalsIgnoreCase("West"))
        {
            newGenre = "Western";
        }
        else if(genre.equalsIgnoreCase("Musc"))
        {
            newGenre = "Musical";
        }
        else if(genre.equalsIgnoreCase("Docu"))
        {
            newGenre = "Documentary";
        }
        else if(genre.equalsIgnoreCase("SciF"))
        {
            newGenre = "Sci-Fi";
        }
        else if(genre.equalsIgnoreCase("Horr"))
        {
            newGenre = "Horror";
        }
        else if(genre.equalsIgnoreCase("Noir"))
        {
            newGenre = "Black";
        }
        else if(genre.equalsIgnoreCase("TVmini"))
        {
            newGenre = "TV miniseries";
        }
        else if(genre.equalsIgnoreCase("Fast"))
        {
            newGenre = "Fantasy";
        }
        else if(genre.equalsIgnoreCase("Ctxx"))
        {
            newGenre = "Uncategorized";
        }
        else if(genre.equalsIgnoreCase("Disa"))
        {
            newGenre = "Disaster";
        }
        else if(genre.equalsIgnoreCase("Surl"))
        {
            newGenre = "Surreal";
        }
        movieID_genre_map.get(movieID).add(newGenre);
    }
}