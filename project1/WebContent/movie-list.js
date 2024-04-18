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
 * @param target
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
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
let name = getParameterByName('name');
let title = getParameterByName('title');
let year = getParameterByName('year');
let director = getParameterByName('director');
let genre = getParameterByName('genre');
let letter = getParameterByName('letter');

let url_form = "api/movie-list?";
if (name) {
    url_form += "name=" + name + "&";
}
if (title) {
    url_form += "title=" + title + "&";
}
if (year) {
    url_form += "year=" + year + "&";
}
if (director) {
    url_form += "director=" + director + "&";
}
if (genre) {
    url_form += "genre=" + genre + "&";
}
if (letter) {
    url_form += "letter=" + letter + "&";
}

// Remove the trailing "&" if there are any parameters
if (url_form.endsWith("&")) {
    url_form = url_form.slice(0, -1);
}
// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: url_form,
    // url: "api/movie-list?name=" + name + "&title=" + title + "&year=" + year + "&director=" + director,
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});