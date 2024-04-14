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
    let starContainerElement = jQuery("#star_container");

    for (let i = 0; i < resultData.length; i++) {
        let starDiv = jQuery("<div>");
        starDiv.addClass("star-item");

        let movieNumber = i + 1;
        let movieId = resultData[i]['movie_id'];
        let movieTitle = resultData[i]['movie_title'];
        let movieYear = resultData[i]['movie_year'];
        let movieDirector = resultData[i]['movie_director'];
        let movieGenre = resultData[i]['movie_genre'];
        let movieStars = resultData[i]['movie_star'].split(", ");
        let movieRating = resultData[i]['movie_rating'];

        let details = `
        <span><i>${movieNumber}.</i> <b>Movie:</b> <a href="single-movie.html?id=${movieId}">${movieTitle}</a> (${movieYear}), <b>Directed by:</b> ${movieDirector}, <b>Genres:</b> ${movieGenre}, <b>Stars:</b> `;

        for (let j = 0; j < movieStars.length; j++) {
            details += `<a href="single-star.html?name=${movieStars[j]}&id=${movieId}">${movieStars[j]}</a>`;
            if (j < movieStars.length - 1) {
                details += ", ";
            }
        }

        details += `, <b>Rating:</b> ${movieRating}</span>`;
        starDiv.append(details);

        starContainerElement.append(starDiv);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});