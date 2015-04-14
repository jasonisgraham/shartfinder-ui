var data = [],
timestamps = [],
socket = (function() {
  var ws_protocol = window.location.protocol === "https:" ? "wss:" : "ws:",
  ws_port = window.location.port ? ":" + window.location.port : "";
  return new WebSocket(ws_protocol + "//"+window.location.hostname+ws_port + "/ws");
})();

socket.onmessage = function(event) {
  var data = JSON.parse(event.data);
  var eventName = data["eventName"];
  var payload = JSON.parse(data["payload"]);

  console.log("data: " + data);
  console.log("eventName: " + eventName);
  console.log("JSON.stringify(payload): " + JSON.stringify(payload));
  if ("add-user" == eventName) {
    renderUsers(payload);
  } else if ("roll-initiative" == eventName) {
    renderInitiative(payload);
  } else if ("combatant-added" == eventName) {
    renderCombatants(payload);
  } else  if ("start-encounter" == eventName) {
    renderStartEncounter(payload);
  } else  if ("initiative-created" == eventName) {
    renderRound(payload);
  } else {
    console.error("Error: eventName: " + eventName + " not recognized");
  }
}

function clearInputs() {
  $('#name').val('');
  $('#password').val('');
  $('#password-confirm').val('');
}

function renderUsers(users) {
  $('#user-list').empty();
  $('#combatants_user').empty();
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
  $('#errors').text(xhr.statusText + ": " + xhr.responseText);
}

function addUser() {
  var jqxhr = $.post("/add-user",
                     { user: $('#name').val(),
                       password: $('#password').val(),
                       passwordConfirm: $('#password-confirm').val()
                     }, renderUsers)
    .fail(handleError);
  clearInputs();
}

function addCombatant() {
  var user = $('#combatants_user').val(),
  combatantName = $('#combatants_combatant-name').val(),
  maxHP = $('#combatants_max-hp').val(),
  combatantData = { data: { user: user,
                            combatantName: combatantName,
                            maxHP : maxHP },
                    eventName: "add-combatant-command" },
  combatantDataString = JSON.stringify(combatantData);

  console.log('addCombatant: ' + user + ' ' + combatantName + ' ' + maxHP + ' ' + combatantDataString);
  socket.send(combatantDataString);

  $('#combatants_user').val('');
  $('#combatants_combatant-name').val('');
  $('#combatants_max-hp').val('');
}

function renderCombatants(payload) {
  var combatants = payload.combatants;
  $('#combatant-list').empty();
  if (combatants) {
    combatants.forEach(function(combatantData) {
      var combatantName = combatantData.combatantName,
      user = combatantData.user,
      maxHP = combatantData.maxHP;
      $('#combatant-list').append('<li>' + combatantName + ' here has max hp of ' + maxHP + '</li>');
      $('#initiative_combatant-name').append($('<option>', {}).text(combatantName));
    });
  }
}

function rollInitiative() {
  var user = $('#initiative_user').val(),
  combatantName = $('#initiative_combatant-name').val(),
  diceRoll = $('#dice-roll').val(),
  initiativeRolledData = { data: { user: user,
                                   combatantName: combatantName,
                                   diceRoll: diceRoll},
                           eventName: "roll-initiative" },
  initiativeRolledDataString = JSON.stringify(initiativeRolledData);
  socket.send(initiativeRolledDataString);
}

function renderInitiative(initiativeData) {
  var user = initiativeData.user,
  diceRoll = initiativeData.diceRoll,
  combatantName = initiativeData.combatantName,
  li = '<li>' + user + ' rolled a ' + diceRoll + ' for ' + combatantName + '</li>';
  console.log("li:  " +  li);
  $('#initiative-rolls').append(li);

  $('#combatant-wait-list-ul > li').filter(function() {
    return $(this).text() === combatantName;
  }).removeClass('combatant-waiting').addClass('combatant-ready');
}

function startEncounter() {
  socket.send(JSON.stringify( { eventName: "start-encounter" }));
}

function renderStartEncounter(startEncounterData) {
  // $("#add-combatant-button").prop("disabled",true);
  // $("#start-encounter-button").prop("disabled",true);
  $("#roll-initiative-div").show();

  var combatants = startEncounterData.combatants;
  $('#combatant-wait-list-ul').empty();

  console.log("combatants: " + JSON.stringify(combatants));

  combatants.forEach(function(combatant) {
    $('#combatant-wait-list-ul').append('<li class="combatant-waiting">'+combatant.combatantName+'</li>');
  });
}

function renderRound(initiativeCreatedData) {
  $("#round-div").show();

  console.log("combatants: " + JSON.stringify(initiativeCreatedData));
  var orderedCombatants = initiativeCreatedData.orderedCombatants;

  for (var key in orderedCombatants) {
    var name = orderedCombatants[key].combatantName,
    initiative = orderedCombatants[key].initiative,
    li = '<li>'+name+' '+initiative+'</li>';
    $('#round-order').append(li);
  }
}

$(function() {
  getUsers();
});
