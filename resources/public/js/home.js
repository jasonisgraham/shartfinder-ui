var data = [], timestamps = [];
var socket = new WebSocket("ws://"+window.location.hostname+":8080/ws");

socket.onmessage = function(event) {
  var data = JSON.parse(event.data);
  var eventName = data["event-name"];
  var payload = data["payload"];

  console.log("eventName: " + eventName);
  console.log("payload: " + JSON.stringify(payload));
  if ("add-user" == eventName) {
    renderUsers(payload);
  }
  if ("roll-initiative" == eventName) {
    renderInitiative(payload);
  }
  if ("add-combatant" == eventName) {
    renderCombatant(payload);
  }
  if ("start-encounter" == eventName) {
    renderStartEncounter(payload);
  }

}

function clearInputs() {
  $('#name').val('');
  $('#password').val('');
  $('#password-confirm').val('');
}

function renderUsers(users) {
  $('#user-list').empty();

  users.forEach(function(user) {
    $('#user-list').append('<li>'+user+'</li>');
    $('#combatants_user').append($('<option>', {}).text(user));
  });
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
  clearInputs();
}

function addCombatant() {
  var user = $('#combatants_user').val(),
      combatantName = $('#combatants_combatant-name').val(),
      maxHP = $('#combatants_max-hp').val();
  var combatantData = { data: { user: user,
                                combatantName: combatantName,
                                maxHP : maxHP },
                        resource: "add-combatant" };
  var combatantDataString = JSON.stringify(combatantData);
  socket.send(combatantDataString);

  $('#combatants_user').val('');
  $('#combatants_combatant-name').val('');
  $('#combatants_max-hp').val('');
}

function renderCombatant(combatantData) {

  var combatantName = combatantData.combatantName,
      user = combatantData.user,
      maxHP = combatantData.maxHP;
  $('#combatant-list').append('<li>' + combatantName + ' here has max hp of ' + maxHP + '</li>');
  $('#initiative_combatant-name').append($('<option>', {}).text(combatantName));
}

function rollInitiative() {
  var user = $('#initiative_user').val(),
      combatantName = $('#initiative_combatant-name').val(),
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

  $('#combatant-wait-list-ul > li').filter(function() {
    return $(this).text() === combatantName;
  }).removeClass('combatant-waiting').addClass('combatant-ready');

}

function startEncounter() {
  socket.send(JSON.stringify( { resource: "start-encounter" }));
}

function renderStartEncounter(startEncounterData) {
  // $("#add-combatant-button").prop("disabled",true);
  // $("#start-encounter-button").prop("disabled",true);
  $("#roll-initiative-div").show();

  var combatantNames = startEncounterData.combatantNames;
  $('#combatant-wait-list-ul').empty();

  combatantNames.forEach(function(combatantName) {
    $('#combatant-wait-list-ul').append('<li class="combatant-waiting">'+combatantName+'</li>');
  });
}

$(function() {
  getUsers();
});
