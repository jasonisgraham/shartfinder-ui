var data = [], timestamps = [];
var socket = new WebSocket("ws://128.237.162.79:8080/happiness");

socket.onmessage = function(event) {
  renderUsers(JSON.parse(event.data));
}

function renderUsers(users) {
  $('#user-list').empty();

  users.forEach(function(user) {
    $('#user-list').append('<li>'+user+'</li>');
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
  var jqxhr = $.post("/add-user",
                     { user: $('#name').val(),
                       password: $('#password').val(),
                       password_confirm: $('#password-confirm').val()
                     }, renderUsers)
    .fail(handleError);
}

function rollInitiative() {
  var jqxhr = $.post("/roll-initiative",
                    { user: $('#user-id').val(),
                      combatantName: $('#combatant-name').val(),
                      diceRoll: $('#dice-roll').val()
                    }, function(xhr) { $('#initiative-message').text('rolled a: ' + xhr)})
  .fail(function(xhr) { $('#initiative-message').text(xhr.statusText + ": " + xhr.responseText) });
}

$(function() {
  getUsers();
});
