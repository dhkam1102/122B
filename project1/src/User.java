/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String ccId;
    private final String address;
    private final String email;
//    private final String expDate;


    public User(String id, String firstName, String lastName, String ccId, String address, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ccId = ccId;
        this.address = address;
        this.email = email;
//        this.expDate = expDate;
    }
//    public String getExpDate() {
//        return expDate;
//    }
    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCcId() {
        return ccId;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

}
