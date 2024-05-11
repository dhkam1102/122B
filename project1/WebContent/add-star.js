$(document).ready(function() {
    $('form').submit(function(event) {
        event.preventDefault(); // Prevent the form from submitting normally

        // Serialize the form data only if the Birth Year field is not empty
        let formData = {};
        let starName = $('#starName').val().trim(); // Get the value of the Star Name field
        formData["starName"] = starName;
        let starBirthYear = $('#starBirthYear').val().trim(); // Get the value of the Birth Year field
        if (starBirthYear !== '')
        {
            formData["starBirthYear"] = starBirthYear;
        }

        $.ajax({
            type: 'GET',
            url: 'api/add-star', // URL of your servlet
            data: formData,
            success: function(response) {
                alert(response); // Show success message
            },
            error: function(xhr, status, error) {
                alert('Error adding star: ' + xhr.responseText); // Show error message
            }
        });
    });
});