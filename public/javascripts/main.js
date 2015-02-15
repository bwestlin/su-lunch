$(function(){

  var $todaysLunches = $("#todaysLunches");

  var todaysLunchesShown = false;
  function showTodaysLunches() {
    if (!todaysLunchesShown) {
      $todaysLunches.show().transition({ opacity: 1 }, "slow");
      todaysLunchesShown = true;
    }
  }

  jsRoutes.controllers.LunchInfoController.todaysLunches().ajax({
    success: function(response, status, xhr) {
      $todaysLunches.html(response);
      showTodaysLunches();
    },
    error: function (xhr, status, errorThrown) {
      var msg = "Tyvärr uppstod ett fel: ";
      $todaysLunches.html("<div class=\"alert alert-error\"><b>Fel!</b> " + msg + xhr.status + " " + xhr.statusText + "</div>");
    }
  });

  setTimeout(function() {
    showTodaysLunches();
  }, 250);
});
