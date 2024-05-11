import java.util.ArrayList;
import java.util.List;

public class Star {
    private String id;
    private String name;
    private int birthYear;  // Changed from String to int
    private String stageName;
    private List<String> movieIds;
    private String lastName;
    private String firstName;

    public Star() {
        this.movieIds = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirthYear() {  // Return type changed to int
        return birthYear;
    }

    public void setBirthYear(int birthYear) {  // Parameter type changed to int
        this.birthYear = birthYear;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public List<String> getMovieIds() {
        return movieIds;
    }

    public void addMovieId(String movieId) {
        this.movieIds.add(movieId);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String toString() {
        return "Star{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", birthYear=" + birthYear +  // Changed output to reflect int type
                ", stageName='" + stageName + '\'' +
                ", movieIds=" + movieIds +
                '}';
    }
}
