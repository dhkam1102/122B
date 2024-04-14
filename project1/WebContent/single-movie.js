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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Movie Name:  " + resultData[0]["movie_title"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieDetailsElement = jQuery("#movie_details");

    // Concatenate the html tags with resultData jsonObject to create movie details
    for (let i = 0; i < Math.min(10, resultData.length); i++) {
        let movieHTML = "";
        movieHTML += "<div class='movie'>";
        movieHTML += "<h2>" + resultData[i]["movie_title"] + " (" + resultData[i]["movie_year"] + ")</h2>";
        movieHTML += "<p><strong>Director:</strong> " + resultData[i]["movie_director"] + "</p>";
        movieHTML += "<p><strong>Genres:</strong> " + resultData[i]["movie_genres"] + "</p>";

        let starList = resultData[i]["movie_stars"].split(", ");
        let starHTML = "<p><strong>Stars:</strong> ";
        for (let name of starList) {
            starHTML += '<a href="single-star.html?name=' + name + '&id=' + resultData[i]["movie_id"] + '">' + name + '</a>, ';
        }
        starHTML = starHTML.slice(0, -2); // Remove the last comma and space
        starHTML += "</p>";

        movieHTML += starHTML;
        movieHTML += "<p><strong>Rating:</strong> " + resultData[i]["movie_rating"] + "</p>";
        movieHTML += "</div>";

        // Append the movie details to the movie details container
        movieDetailsElement.append(movieHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});