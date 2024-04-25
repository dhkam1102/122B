/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleShoppingResult(resultData) {
    console.log("handleShoppingResult: populating shopping cart from from resultData");

    let starTableBodyElement = jQuery("#cart-table-body");
    // Iterate through resultData, no more than 10 entries
    if(Array.isArray(resultData.cart_items))
        console.log('this is an array');

    for (let i = 0; i < resultData.cart_items.length; i++) {
        let item = resultData.cart_items[i];
        let singleMovieTotalPrice = item["price"] * item["quantity"];
        // totalPrice += singleMovieTotalPrice;

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + item["movie_title"] + "</th>";
        rowHTML += "<th>" + item["quantity"] + "</th>";
        rowHTML += "<th>" + item["price"] + "</th>";
        rowHTML += "<th>" + singleMovieTotalPrice + "</th>";
        rowHTML += "<td>";
        rowHTML += "<button onclick='updateQuantity(\"" + item["movie_title"] + "\", \"add\")'>+</button>";
        rowHTML += "<button onclick='updateQuantity(\"" + item["movie_title"] + "\", \"sub\")'>-</button>";
        rowHTML += "<button onclick='updateQuantity(\"" + item["movie_title"] + "\", \"delete\")'>remove</button>";
        rowHTML += "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }


    document.getElementById("total-price").innerText = resultData.total_price;


}

function updateQuantity(movie_title, action) {
    let quant = (action === "add") ? 1 : -1;
    let del = "NO";

    if (action === "delete") {
        del = "YES";
    }

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/shopping", // Replace with your actual API endpoint for updating the cart
        data: {
            "movie_title": movie_title,
            "quantity": quant,
            "delete": del // you could pass "add" or "subtract" to determine the action on the server
        },
        success: function (response) {
            console.log("success", response);  // Log the success message and server response
            if (response.status === "success") {
                alert('Added to cart successfully!');  // Alert the user of success
                refresh();
            } else {
                alert('Failed to add to cart: ' + response.message);  // Alert the user of the failure message from server
            }
        },
        error: function (error) {
            // Handle error
            alert('Failed to update quantity!');
            console.error(error);
        }
    });
}

function refresh() {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/shopping", // Setting request url, which is mapped by StarsServlet in Stars.java
        success: function(resultData) {
            jQuery("#cart-table-body").empty();
            handleShoppingResult(resultData);
        },
        error: function(error) {
            alert("error occured when trying to refresh");
            console.error(error);
        }
    });
}



/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/shopping", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleShoppingResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});