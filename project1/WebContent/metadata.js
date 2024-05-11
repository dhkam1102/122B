/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    console.log("handleStarResult: populating metadata from resultData");

    // Populate the table container
    let tableContainer = jQuery("#table-container");

    // Create a table element
    let table = jQuery("<table border='1'><tr><th>Table Name</th><th>Column Name</th><th>Type Name</th></tr></table>");

    // Loop through the resultData to populate the table
    for (let i = 0; i < resultData.length; i++) {
        let tableObject = resultData[i];
        let tableName = tableObject["table_name"];
        let columns = tableObject["columns"];

        for (let j = 0; j < columns.length; j++) {
            let columnName = columns[j]["column_name"];
            let columnType = columns[j]["type_name"];

            let tableRow = jQuery("<tr></tr>");
            tableRow.append("<td>" + tableName + "</td>");
            tableRow.append("<td>" + columnName + "</td>");
            tableRow.append("<td>" + columnType + "</td>");

            // Append the table row to the table
            table.append(tableRow);
        }
    }

    // Append the table to the table container
    tableContainer.append(table);
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/metadata", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});