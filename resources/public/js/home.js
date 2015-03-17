var data = [], timestamps = [];
var socket = new WebSocket("ws://192.168.1.107:8080/ws");

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
  var user = $('#user-id').val(),
      combatantName = $('#combatant-name').val(),
      diceRoll = $('#dice-roll').val();
  var initiativeRolledData = { data: { user: user,
                                       combatantName: combatantName,
                                       diceRoll: diceRoll},
                               resource: "roll-initiative" };
  var initiativeRolledDataString = JSON.stringify(initiativeRolledData);
  socket.send(initiativeRolledDataString);

  $('#initiative-rolls').append('<li>' + user + ' rolled a ' + diceRoll + ' for ' + combatantName + '</li>');
}

$(function() {
  getUsers();
});
