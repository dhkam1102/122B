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
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#star_table_body");
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_genre"] + "</th>";
        //rowHTML += "<th>" + resultData[i]["movie_star"] + "</th>";
        rowHTML += "<th>";
        let stars = resultData[i]["movie_star"].split(", ");
        for (let j = 0; j < stars.length; j++) {
            rowHTML += '<a href="single-star.html?name=' + stars[j] + '&id=' + resultData[i]["movie_id"] + '">' + stars[j] + '</a>';
            // Add a comma and space after each star (except the last one)
            if (j < stars.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";

        // edited this part to make the buttons
        rowHTML += "<td>";
        rowHTML += "<button onclick='addToCart(\"" +
            resultData[i]["movie_id"] + "\", \"" +
            resultData[i]["movie_title"] + "\")'>Add to Cart</button>";
        rowHTML += "</td>";

        rowHTML += "</tr>";


        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


function good(response) {
    console.log("success", response);  // Log the success message and server response
    if (response.status === "success") {
        alert('Added to cart successfully!');  // Alert the user of success
    } else {
        alert('Failed to add to cart: ' + response.message);  // Alert the user of the failure message from server
    }
}

function bad(jqXHR, textStatus, errorThrown) {
    console.log("Fail", textStatus, errorThrown, jqXHR.responseText);  // Log the failure message along with server response
    let responseJson = {};
    try {
        responseJson = JSON.parse(jqXHR.responseText);
        alert('Failed to add to cart: ' + (responseJson.message || 'Unknown error occurred'));  // Alert the user of failure with server message
    } catch (e) {
        console.error("Error parsing server response: ", e);
        alert('Failed to add to cart. An unknown error occurred.');  // Alert the user of failure without server message
    }
}

function addToCart(movie_id, movie_title) {
    // Makes the HTTP GET request and registers on success callback function handleResult
    let dataSend = {
        "movie_id": movie_id,
        "movie_title": movie_title,
        "quantity": "1",
        "delete": "NO"
    };

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/shopping",
        data: dataSend,
        success: resultDataString => good(resultDataString),
        error: bad
    });
};

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json",
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});