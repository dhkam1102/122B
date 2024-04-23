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

window.addEventListener('DOMContentLoaded', (event) => {
    let urlParams = new URLSearchParams(window.location.search);
    let pageSize = urlParams.get('size'); // Default to 25 if 'size' parameter is not present
    let title_sorting = urlParams.get('ts')
    let rating_sorting = urlParams.get('rs');
    let sorting_option = 'ts ' + title_sorting + ' rs ' + rating_sorting;
    document.getElementById('page-size').value = pageSize;
    document.getElementById('sorting').value = sorting_option;
});

function changePageSize(selectElement) {
    let newSize = selectElement.value;
    let urlParams = new URLSearchParams(window.location.search);
    urlParams.set('size', newSize);
    window.location.search = urlParams.toString();
}

function changeSorting(selectElement) {
    let sortValue = selectElement.value;
    let sortValues = sortValue.split(' ');
    let urlParams = new URLSearchParams(window.location.search);
    urlParams.set('ts', sortValues[1]); // Get the sorting direction for title
    urlParams.set('rs', sortValues[3]); // Get the sorting direction for rating
    window.location.search = urlParams.toString();
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

        rowHTML += "<th>";
        let genres = resultData[i]["movie_genre"].split(", ");
        for (let k = 0; k < genres.length; k++){
            // Get the current URL
            let currentUrl = window.location.href;

            // Update the 'genre' parameter in the current URL
            let updatedUrl;
            if (currentUrl.includes('genre=')) {
                updatedUrl = currentUrl.replace(/genre=([^&]*)/, 'genre=' + encodeURIComponent(genres[k]));
                updatedUrl = updatedUrl.replace(/page=([^&]*)/, `page=1`);
            } else {
                updatedUrl = currentUrl + (currentUrl.includes('?') ? '&' : '?') + 'genre=' + encodeURIComponent(genres[k]);
            }

            // Create the hyperlink with the updated URL
            rowHTML += '<a href="' + updatedUrl + '">' + genres[k] + '</a>';

            // Add a comma and space after each genre (except the last one)
            if (k < genres.length - 1) {
                rowHTML += ", ";
            }
        }
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


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
let name = getParameterByName('name');
let title = getParameterByName('title');
let year = getParameterByName('year');
let director = getParameterByName('director');
let genre = getParameterByName('genre');
let letter = getParameterByName('letter');
let ts = getParameterByName('ts');
let rs = getParameterByName('rs');
let size = getParameterByName('size');
let page = getParameterByName('page');
// let url_form = "api/movie-list?";
// if (name) {
//     url_form += "name=" + name + "&";
// }
// if (title) {
//     url_form += "title=" + title + "&";
// }
// if (year) {
//     url_form += "year=" + year + "&";
// }
// if (director) {
//     url_form += "director=" + director + "&";
// }
// if (genre) {
//     url_form += "genre=" + genre + "&";
// }
// if (letter) {
//     url_form += "letter=" + letter + "&";
// }
//
// // Remove the trailing "&" if there are any parameters
// if (url_form.endsWith("&")) {
//     url_form = url_form.slice(0, -1);
// }
// Makes the HTTP GET request and registers on success callback function handleStarResult


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


jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movie-list?name=" + name + "&title=" + title + "&year=" + year + "&director=" + director + "&genre=" + genre + "&letter=" + letter + '&ts=' + ts + '&rs=' + rs +'&size=' + size + '&page=' + page,
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});