var data = [], timestamps = [];
var socket = new WebSocket("ws://"+window.location.hostname+":8080/ws");

socket.onmessage = function(event) {
  // getUsers();
  var data = JSON.parse(event.data);
  var eventName = data["event-name"];
  var payload = data["payload"];

  console.log("eventName: " + eventName);
  if ("add-user" == eventName) {
    renderUsers(payload);
  }
  if ("roll-initiative" == eventName) {
    renderInitiative(payload);
  }
}

function clearInputs() {
  $('#name').val('');
  $('#password').val('');
  $('#password-confirm').val('');
}

function renderUsers(users) {
  $('#user-list').empty();
  console.log("users: " + users);

  users.forEach(function(user) {
    $('#user-list').append('<li>'+user+'</li>');
  });
  clearInputs();
}

function resetUsers() {
  $('#user-list').empty();
  clearInputs();
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

// function addCombatant() {
//   var user = $('#combatants_user').val(),
//       combatantName = $('#combatants_combatant-name').val(),
//       maxHP = $('#combatants_max-hp').val();
//   var combatantData = { data: { user: user,
//                                 combatantName: combatantName,
//                                 maxHP : maxHP },
//                         resource: "add-combatant" };
//   var combatantDataString = JSON.stringify(combatantDataString);
//   socket.send(combatantDataString);

//   $('#combatant-list').append('<li>' + combatantName + ' here has max hp of ' + maxHP + '</li>');
// }

function rollInitiative() {
  var user = $('#initiative_user').val(),
      combatantName = $('#combatant-name').val(),
      diceRoll = $('#dice-roll').val();
  var initiativeRolledData = { data: { user: user,
                                       combatantName: combatantName,
                                       diceRoll: diceRoll},
                               resource: "roll-initiative" };
  var initiativeRolledDataString = JSON.stringify(initiativeRolledData);
  socket.send(initiativeRolledDataString);

}

function renderInitiative(initiativeData) {
  var user = initiativeData.user,
      diceRoll = initiativeData.diceRoll,
      combatantName = initiativeData.combatantName;
    $('#initiative-rolls').append('<li>' + user + ' rolled a ' + diceRoll + ' for ' + combatantName + '</li>');
}

$(function() {
  getUsers();
});
