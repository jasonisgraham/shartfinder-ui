var data = [], timestamps = [];
var socket = new WebSocket("ws://localhost:8080/happiness");

socket.onmessage = function(event) {
  getUsers();
}

function renderUsers(users) {
  $('#user-list').empty();
  $.each(users, function(idx,user_name) {
    $('#user-list').append('<li>'+user_name+'</li>');
  });
  $('#name').val('');
  $('#password').val('');
  $('#password-confirm').val('');
}

function getUsers() {
  $.get("/users", renderUsers);
}

function handleError(xhr) {
  $('#error').text(xhr.statusText + ": " + xhr.responseText);
}

function addUser() {
  var jqxhr = $.post("/add-user", { user: $('#name').val(),
                                    password: $('#password').val(),
                                    password_confirm: $('#password-confirm').val()
                                  }, renderUsers)
    .fail(handleError);
}

$(function() {
  getUsers();
});
