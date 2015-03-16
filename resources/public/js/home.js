var data = [], timestamps = [];
var socket = new WebSocket("ws://localhost:8080/happiness");

socket.onmessage = function(event) {
  getUsers();
}

function renderUsers(users) {
  $('#user-list').empty();
  $.each(users, function(idx,user) {
    $('#user-list').append('<li>'+user+'</li>');
  });
}

function getUsers() {
  $.get("/users", renderUsers);
}

function handleError(xhr) {
  $('#error').text(xhr.statusText + ": " + xhr.responseText);
}

function addUser() {
  var jqxhr = $.post("/add-user", { user: $('#name').val() }, renderUsers)
    .fail(handleError);
}

$(function() {
  getUsers();
});
