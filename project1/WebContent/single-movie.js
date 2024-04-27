/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

$("#redirectButton").click(function() {
    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/movie-list",
        success: function(response) {
            window.location.href = response.redirectUrl;
        }
    });
});
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Movie Name:  " + resultData[0]["movie_title"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < Math.min(10, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["movie_title"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        let genre_list = resultData[i]["movie_genres"].split(", ");
        let genre_html = "";

        for (let genre of genre_list) {
            genre_html += '<a href="movie-list.html?name=&title=&year=&director=&genre=' + encodeURIComponent(genre) + '&letter=&ts=ASC1&rs=DESC2&size=25&page=1">' + genre + '</a>, ';
        }
        rowHTML += "<th>" + genre_html + "</th>"
        // rowHTML += "<th>" + resultData[i]["movie_stars"] + "</th>";
        let star_list = resultData[i]["movie_stars"].split(", ");
        let star_html = "";
        for (let name of star_list) {
            star_html += '<a href="single-star.html?name=' + name + '&id=' + resultData[i]["movie_id"] + '">' + name + '</a>, ';
        }
        rowHTML += "<th>" + star_html + "</th>"

        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        // edited this part to make the buttons
        rowHTML += "<td>";
        rowHTML += "<button onclick='addToCart(\"" +
            resultData[i]["movie_id"] + "\", \"" +
            resultData[i]["movie_title"] + "\")'>Add to Cart</button>";
        rowHTML += "</td>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */
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


// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});