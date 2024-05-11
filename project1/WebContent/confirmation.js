


function handleResultData(resultData) {
    const totalPriceSection = $('#totalPrice');
    totalPriceSection.html('<h3>Total Price:</h3><p>$' + resultData.total_price + '</p>');

    const saleIdsSection = $('#saleIds');
    let saleIdsHtml = '<h3>Sale IDs:</h3>';
    if (resultData.sale_ids && resultData.sale_ids.length > 0) {
        resultData.sale_ids.forEach(function(id) {
            saleIdsHtml += '<p>Sale ID: ' + id + '</p>';
        });
    } else {
        saleIdsHtml += '<p>No sales recorded</p>';
    }
    saleIdsSection.html(saleIdsHtml);

    // Extract and display cart items
    const cartItemsSection = $('#cartItems');
    let cartItemsHtml = '<h3>Cart Items:</h3>';
    if (resultData.cart_items && resultData.cart_items.length > 0) {
        resultData.cart_items.forEach(function(item) {
            cartItemsHtml += '<p>' + item.title + ' - Quantity: ' + item.quantity + '</p>';
        });
    } else {
        cartItemsHtml += '<p>No items in cart</p>';
    }
    cartItemsSection.html(cartItemsHtml);
}



jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/confirmation",
    success: (resultData) => handleResultData(resultData)
});