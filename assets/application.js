var timeout = 600000; // 10 minutes

function fetchData(repeat) {
  $.ajax({
    dataType: 'json',
    success: function(data, textStatus, jqXHR) {
      var decay = data.decay + "/" + data.max_decay;
      $('#decay_level').html(decay);

      var battery = data.battery + '%';
      $('#battery_level').html(battery);

      if (repeat) {
        setTimeout("fetchData(true)", timeout);
      }
    },
    type: 'GET',
    url: "/rose/data"
  });
}

$(document).ready(function() {
  $('#decay_button, #revert_button').click(function(event) {
    event.preventDefault();
    event.stopImmediatePropagation();
    event.stopPropagation();
    
    $.ajax({
      complete: function(jqXHR, textStatus) {
      	fetchData(false);
      },
      type: 'GET',
      url: $(this).attr('href')
    });
  });
  
  fetchData(true);
});

