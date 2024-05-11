import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import com.google.gson.JsonArray;

// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "confirmation", urlPatterns = "/api/confirmation")
public class Confirmation extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        List<Integer> saleIds = (List<Integer>) session.getAttribute("saleIds");
        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");
        int totalPrice = (int) session.getAttribute("totalPrice");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("total_price", totalPrice);

        if (saleIds != null) {
            JsonArray jsonSaleIds = new JsonArray();
            for (Integer id : saleIds) {
                jsonSaleIds.add(id);
            }
            responseObject.add("sale_ids", jsonSaleIds);
        } else {
            responseObject.addProperty("sale_ids", "No sales recorded");
        }

        if (cart != null) {
            JsonArray jsonCartItems = new JsonArray();
            for (String title : cart.keySet()) {
                JsonObject jsonItem = new JsonObject();
                jsonItem.addProperty("title", title);
                jsonItem.addProperty("quantity", cart.get(title));
                jsonCartItems.add(jsonItem);
            }
            responseObject.add("cart_items", jsonCartItems);
        } else {
            responseObject.addProperty("cart_items", "No items in cart");
        }

        out.write(responseObject.toString());
        response.setStatus(200);
        out.close();
    }
}