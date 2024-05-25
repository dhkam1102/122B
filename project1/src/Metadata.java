import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "Metadata", urlPatterns = "/api/metadata")
public class Metadata extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_write");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // Get tables
            ResultSet tables = metaData.getTables("moviedb", null, null, new String[] {"TABLE"});
            JsonArray jsonArray = new JsonArray();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                System.out.println("Table Name: " + tableName);

                JsonObject tableObject = new JsonObject();
                tableObject.addProperty("table_name", tableName);
                JsonArray columnsArray = new JsonArray();

                ResultSet columns = metaData.getColumns("moviedb", null, tableName, null);
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");

                    JsonObject columnObject = new JsonObject();
                    columnObject.addProperty("column_name", columnName);
                    columnObject.addProperty("type_name", columnType);
                    columnsArray.add(columnObject);

                    System.out.println("\tColumn Name: " + columnName + ", Type: " + columnType);
                }
                columns.close();

                tableObject.add("columns", columnsArray);
                jsonArray.add(tableObject);
            }
            tables.close();

            // Write JSON string to output
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(jsonArray.toString());
                out.flush();
            }
            response.setStatus(200);

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }
}