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
    let genreTableBodyElement = jQuery("#genre_table_body");
    let numRows = Math.ceil(resultData.length / 5); // Calculate number of rows needed
    let dataIndex = 0; // Index for accessing data in resultData array

    // Iterate through each row
    for (let i = 0; i < numRows; i++) {
        let row = "<tr>"; // Start a new row
        // Iterate through each column in the row
        for (let j = 0; j < 5; j++) {
            if (dataIndex < resultData.length) {
                // Add data as a hyperlink to the cell if there is data available
                row += "<td><a href='movie-list.html?name=&title=&year=&director=&genre=" + encodeURIComponent(resultData[dataIndex]['genre_name']) + "&letter=&ts=ASC1&rs=DESC2&size=25&page=1'>" + resultData[dataIndex]['genre_name'] + "</a></td>";
                dataIndex++;
            } else {
                // Add empty cell if no more data is available
                row += "<td></td>";
            }
        }
        row += "</tr>"; // Close the row
        genreTableBodyElement.append(row); // Add the row to the table
    }

    let specialCharsTableBodyElement = jQuery("#first_letter_table_body");
    let specialCharsRow = "<tr><td>";

    // Define the characters to include in the hyperlinks
    let characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*";

    // Iterate through each character and add a hyperlink to it in the row
    for (let char of characters) {
        specialCharsRow += "<a href='movie-list.html?name=&title=&year=&director=&genre=&letter=" + encodeURIComponent(char) + "&ts=ASC1&rs=DESC2&size=25&page=1'>" + " " + char + " " + "</a>";
    }

    specialCharsRow += "</td></tr>";
    specialCharsTableBodyElement.append(specialCharsRow);
}

$(document).ready(function() {
    if (typeof $.fn.autocomplete !== 'function') {
        console.error('jQuery Autocomplete plugin is not loaded');
        return;
    }

    function handleLookup(query, doneCallback) {
        console.log("autocomplete initiated");
        console.log("sending AJAX request to backend Java Servlet");

        jQuery.ajax({
            method: "GET",
            url: "api/movie-list?query=" + escape(query),
            success: function(data) {
                handleLookupAjaxSuccess(data, query, doneCallback);
            },
            error: function(errorData) {
                console.log("lookup ajax error");
                console.log(errorData);
            }
        });
    }

    function handleLookupAjaxSuccess(data, query, doneCallback) {
        console.log("lookup ajax successful");

        // Check if the data is already an object
        var jsonData;
        if (typeof data === "string") {
            try {
                jsonData = JSON.parse(data);
            } catch (e) {
                console.error("Parsing error:", e);
                return;
            }
        } else {
            jsonData = data;
        }

        console.log(jsonData);

        if (jsonData.error) {
            console.log("Error(handleLookupAjaxSuccess): " + jsonData.error);
        } else {
            doneCallback({ suggestions: jsonData });
        }
    }

    function handleSelectSuggestion(suggestion) {
        console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieID"]);
    }

    $('#star_name').autocomplete({
        lookup: function(query, doneCallback) {
            handleLookup(query, doneCallback);
        },
        onSelect: function(suggestion) {
            handleSelectSuggestion(suggestion);
        },
        deferRequestBy: 300,
        minChars: 2
    });

    $('#movie_title').autocomplete({
        lookup: function(query, doneCallback) {
            handleLookup(query, doneCallback);
        },
        onSelect: function(suggestion) {
            handleSelectSuggestion(suggestion);
        },
        deferRequestBy: 300,
        minChars: 2
    });

    // Makes the HTTP GET request and registers on success callback function handleStarResult
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
});