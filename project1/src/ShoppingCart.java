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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

//need to do


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "ShoppingCart", urlPatterns = "/api/shopping")
public class ShoppingCart extends HttpServlet {
    private static final long serialVersionUID = 1L;

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


    public static int getMoviePrice(String movieTitle) {
        int price = 0;
        movieTitle = movieTitle.toLowerCase();
        for (int i = 0; i < movieTitle.length(); i++) {
            //calc the price by adding the ascii number of each letter
            price += (int) movieTitle.charAt(i);
        }
        //make the price fall around 20
        price = (price % 20) + 1;
        return price;
    }



    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        //get cart, if not exist make one
        // <movie_title, quantity> or <movie_id, quantity>
        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");
        if(cart == null) {
            cart = new HashMap<String, Integer>();
            session.setAttribute("cart", cart);
        }

        int totalPrice = 0;

        JsonArray jsonArray = new JsonArray();
        for (String movie_title: cart.keySet()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("movie_title", movie_title);
            jsonObject.addProperty("quantity", cart.get(movie_title));
            int movie_price = getMoviePrice(movie_title);
            jsonObject.addProperty("price", movie_price);
            totalPrice += movie_price * cart.get(movie_title);
            jsonArray.add(jsonObject);
        }

        session.setAttribute("totalPrice", totalPrice);
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("total_price", totalPrice);
        responseObject.add("cart_items", jsonArray);

        request.getServletContext().log("getting " + jsonArray.size() + " results");

        out.write(responseObject.toString());
        response.setStatus(200);
        out.close();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        String movie_title = request.getParameter("movie_title");
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        String delete = request.getParameter("delete");



        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");

        JsonObject jsonResponse = new JsonObject();

        if ("YES".equals(delete) && cart.containsKey(movie_title)) {
            cart.remove(movie_title);
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Movie removed from cart");
            response.setStatus(200); // 200 status code
        } else {
            if (cart == null) {
                cart = new HashMap<String, Integer>();
                session.setAttribute("cart", cart);
            }

            if (cart.containsKey(movie_title)) {
                int current_quantity = cart.get(movie_title);
                if (current_quantity - quantity < 0) {
                    jsonResponse.addProperty("status", "failure");
                    jsonResponse.addProperty("message", "Cannot have negative quantity");
                    response.setStatus(400); // 400 status code
                } else {
                    cart.put(movie_title, current_quantity + quantity);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Quantity updated");
                    response.setStatus(200); // 200 status code
                }
            } else {
                if (quantity < 0) {
                    jsonResponse.addProperty("status", "failure");
                    jsonResponse.addProperty("message", "Something is really wrong");
                    response.setStatus(400); // 400 status code
                } else {
                    cart.put(movie_title, quantity);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "New item added to cart");
                    response.setStatus(200); // 200 status code
                }
            }

        }

        out.println(jsonResponse.toString());
        out.close();

    }
}
