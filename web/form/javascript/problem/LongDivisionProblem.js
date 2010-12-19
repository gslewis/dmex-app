function toggleHints(button) {
    if ($(button).attr("value") == "Show hints") {
        $(button).attr("value", "Hide hints");
    } else {
        $(button).attr("value", "Show hints");
    }

    $(".hintsContainer").toggle();
}
