
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

let payment_form = $("#paymentForm");

function handleTotalPriceResult(resultData) {
    console.log("handleShoppingResult: populating shopping cart from from resultData");
    document.getElementById("total-price").innerText = resultData.total_price;
}


function handlePaymentResult(resultDataJson) {
    console.log("handle payment response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        window.location.replace("confirmation.html")
        // alert("Payment was successful!");
    }
    else {
        alert("Payment failed");
    }
}

function submitPaymentForm(formSubmitEvent) {
    console.log("submit payment form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    );
}

payment_form.on("submit", submitPaymentForm);


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/payment", // Setting request url, which is mapped by StarsServlet in Stars.java
    // success: handlePaymentResult
    success: (resultData) => handleTotalPriceResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});