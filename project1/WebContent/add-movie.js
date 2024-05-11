$(document).ready(function() {
    $('form').submit(function(event) {
        event.preventDefault(); // Prevent the form from submitting normally

        // Serialize the form data only if the Birth Year field is not empty
        let formData = {};
        let movieTitle = $('#movieTitle').val().trim(); // Get the value of the Star Name field
        formData["movieTitle"] = movieTitle;
        let movieDirector = $('#movieDirector').val().trim(); // Get the value of the Star Name field
        formData["movieDirector"] = movieDirector;
        let movieYear = $('#movieYear').val().trim(); // Get the value of the Star Name field
        formData["movieYear"] = movieYear;
        let starName = $('#starName').val().trim(); // Get the value of the Star Name field
        formData["starName"] = starName;
        let movieGenre = $('#movieGenre').val().trim(); // Get the value of the Star Name field
        formData["movieGenre"] = movieGenre;
        let starBirthYear = $('#starBirthYear').val().trim(); // Get the value of the Birth Year field
        if (starBirthYear !== '')
        {
            formData["starBirthYear"] = starBirthYear;
        }


        $.ajax({
            type: 'GET',
            url: 'api/add-movie',
            data: formData,
            success: function(response) {
                // Check if response is an array
                if (Array.isArray(response)) {
                    // Iterate over the array and append each message to the resultMessage table
                    response.forEach(function(item) {
                        $('#resultMessage').append('<tr><td>' + item.message + '</td></tr>');
                    });
                } else {
                    // If response is not an array, assume it's a single message object
                    $('#resultMessage').append('<tr><td>' + response.message + '</td></tr>');
                }

                // Show the result message table
                $('#resultTable').show();
            },
        });
    });
});