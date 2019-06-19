var cookie = {
    auth: null,
    game: null,
    player: null,
};

$( document ).ready(function() {
    if (document.cookie.length > 0) {
        var temp = JSON.parse(document.cookie);
        if (temp && temp.auth) {
            cookie.auth = temp.auth;
        }
        if (temp && temp.game) {
            cookie.game = temp.game;
        }
    }

    $('#turn-buttons').hide();
    $('#play-card-button').hide();
    $('.page-cont').hide();

    if (cookie.game) {
        console.log(cookie.game);
        updateGameInfo(cookie.game.id, true);
    }
    else {
        determinePage();
    }
});

$('.log-button').on('click', function (e) {
    $('.log-button').addClass("disabled");
    $('.alert').hide();
});

$('#login-button').on('click', function (e) {
    var username = $('#username').val();
    var password = $('#password').val();
    if (username.length <= 0 || password.length <= 0) {
        showAlert("login-alert", "Please enter username and password");
        return;
    }

    $.post( "/api/users/auth", { name: username, password: password })
        .done(function( data ) {
            console.log(data);
            cookie.auth = data;
            document.cookie = JSON.stringify(cookie);
            determinePage();
        })
        .fail(function( data ) {
            showAlert("login-alert", data.responseJSON.message);
        })
        .always(function() {
            $('.log-button').removeClass("disabled");
        });
});

$('#register-button').on('click', function (e) {
    var username = $('#username').val();
    var password = $('#password').val();
    if (username.length <= 0 || password.length <= 0) {
        showAlert("login-alert", "Please enter username and password");
        return;
    }

    $.post( "/api/users", { name: username, password: password })
        .done(function( data ) {
            console.log(data);
            cookie.auth = data;
            document.cookie = JSON.stringify(cookie);
            determinePage();
        })
        .fail(function( data ) {
            showAlert("login-alert", data.responseJSON.message);
        })
        .always(function() {
            $('.log-button').removeClass("disabled");
        });
});

$('#join-game-button').on('click', function (e) {
    var gameId = $('#game-id').val();
    if (gameId.length <= 0) {
        showAlert("find-game-alert", "Please enter game id");
        return;
    }

    $.ajaxSetup({
        headers:{
            'Authorization': createAuthHeader()
        }
    });
    $.post( "/api/games/" + gameId + "/players", {})
        .done(function( data ) {
            console.log(data);
            updateGameInfo(gameId, true);
            determinePage();
        })
        .fail(function( data ) {
            showAlert("find-game-alert", data.responseJSON.message);
        })
        .always(function() {
            $('.log-button').removeClass("disabled");
        });
});

$('#create-game-button').on('click', function (e) {
    $.ajaxSetup({
        headers:{
            'Authorization': createAuthHeader()
        }
    });

    $.post( "/api/games", {})
        .done(function( data ) {
            console.log(data);
            cookie.game = data;
            document.cookie = JSON.stringify(cookie);
            determinePage();
        })
        .fail(function( data ) {
            showAlert("find-game-alert", data.responseJSON.message);
        })
        .always(function() {
            $('.log-button').removeClass("disabled");
        });
});

$('#start-game-button').on('click', function (e) {
    if (cookie && cookie.auth && cookie.game) {
        $.ajaxSetup({
            headers:{
                'Authorization': createAuthHeader()
            }
        });

        $.post( "/api/games/" + cookie.game.id, { state: "ACTIVE"})
            .done(function( data ) {
                console.log(data);
                cookie.game = data;
                document.cookie = JSON.stringify(cookie);
                $('#start-game-button').hide();
            })
            .fail(function( data ) {
                showAlert("game-alert", data.responseJSON.message);
            })
            .always(function() {
                $('.log-button').removeClass("disabled");
            });
    }
});

$('#end-turn-button').on('click', function (e) {
    if (cookie && cookie.auth && cookie.game) {
        for (var i = 0; i < cookie.game.players.length; i++) {
            if (cookie.game.players[i].userId === cookie.auth.id) {
                $.ajaxSetup({
                    headers: {
                        'Authorization': createAuthHeader()
                    }
                });

                $.post("/api/games/" + cookie.game.id + "/actions", {type: "SKIP", player: i, index: 0})
                    .done(function (data) {
                        console.log(data);
                        updateGameInfo(cookie.game.id, false);
                    })
                    .fail(function (data) {
                        showAlert("game-alert", data.responseJSON.message);
                    })
                    .always(function () {
                        $('.log-button').removeClass("disabled");
                    });
                break;
            }
        }
    }
});

$('#play-card-button').on('click', function (e) {
    if (cookie && cookie.auth && cookie.game && cookie.player) {
        var cardIndex = $('#play-card-button').data("index");
        if (cardIndex >= 0 && cardIndex < cookie.player.hand.length && cookie.player.mana >= cookie.player.hand[cardIndex].mana) {
            for (var i = 0; i < cookie.game.players.length; i++) {
                if (cookie.game.players[i].userId === cookie.auth.id) {
                    $.ajaxSetup({
                        headers: {
                            'Authorization': createAuthHeader()
                        }
                    });

                    $.post("/api/games/" + cookie.game.id + "/actions", {type: "PLAY_CARD", player: i, index: cardIndex})
                        .done(function (data) {
                            console.log(data);
                            updateGameInfo(cookie.game.id, false);
                        })
                        .fail(function (data) {
                            showAlert("game-alert", data.responseJSON.message);
                        })
                        .always(function () {
                            $('.log-button').removeClass("disabled");
                        });
                    break;
                }
            }
        }
    }
});

$('#logout-button').on('click', function (e) {
    cookie.auth = null;
    cookie.game = null;
    document.cookie = JSON.stringify(cookie);
    determinePage();
});

$('#leave-game-button').on('click', function (e) {
    cookie.game = null;
    document.cookie = JSON.stringify(cookie);
    determinePage();
});

$('#leave-end-button').on('click', function (e) {
    cookie.game = null;
    document.cookie = JSON.stringify(cookie);
    determinePage();
});

function determinePage() {
    console.log(cookie);
    $('.page-cont').hide();
    $('.alert').hide();
    updateTexts();

    if (cookie && cookie.auth) {
        if (cookie.game) {
            if (cookie.game.state === "END") {
                $('#end-game').show();
            }
            else {
                $('#game').show();
            }
        }
        else {
            $('#find-game').show();
        }
    }
    else {
        $('#login').show();
    }

    $('.log-button').removeClass("disabled");
}

function showAlert(id, msg) {
    $('.alert').hide();
    $('#' + id).html(msg);
    $('#' + id).show();
    $('.log-button').removeClass("disabled");
}

function createAuthHeader() {
    if (cookie && cookie.auth) {
        var plain = cookie.auth.id + ":" + cookie.auth.authToken;
        return "Bearer " + btoa(plain);
    }

    return "";
}

var lastUpdateAction = -1;
function updateTexts() {
    if (cookie && cookie.auth) {
        $('#welcome').html("Welcome " + cookie.auth.name + "!");
        if (cookie.game) {
            $('#game-room').html("Game Room: " + cookie.game.id);
            var players = "";
            var healths = "";
            for (var i = 0; i < cookie.game.players.length; i++) {
                players += cookie.game.players[i].userId + " ";
                healths += '<p>' + cookie.game.players[i].userId + ' ' + cookie.game.players[i].health + '</p>';
            }
            $('#players').html("Players: " + players);
            $('#healths').html(healths);

            if (lastUpdateAction !== cookie.game.actions.length) {
                var info = '<h4>Opponent</h4>';
                for (var i = 0; i < cookie.game.players.length; i++) {
                    var opponent = cookie.game.players[i];
                    if (opponent.userId !== cookie.auth.id) {
                        var info = '<h4>Opponent</h4>';
                        info += '<p>Health: ' + opponent.health + ', Mana: ' + opponent.mana + '</p>';
                        var deck = '<div class="card closed-card">Deck: ' + opponent.deckSize + '</div>';
                        info += deck;

                        for (var j = 0; j < opponent.handSize; j++) {
                            var card = '<div class="card closed-card"></div>';
                            info += card;
                        }
                    }
                }
                $('#opponent').html(info);

                info = $('#player').html();
                if (cookie.player) {
                    info = '<h4>You</h4>';
                    info += '<p>Health: ' + cookie.player.health + ', Mana: ' + cookie.player.mana + '</p>';
                    var deck = '<div class="card closed-card">Deck: ' + cookie.player.deck.length + '</div>';
                    info += deck;

                    for (var j = 0; j < cookie.player.hand.length; j++) {
                        var mana = cookie.player.hand[j].mana;
                        var card = '<div class="card open-card">' + mana + '</div>';
                        if (mana <= cookie.player.mana) {
                            card = '<div class="card playable-card" data-index="' + j + '">' + mana + '</div>';
                        }
                        info += card;
                    }
                }

                $('#player').html(info);
                $('#play-card-button').data("index", -1);
                $('#play-card-button').hide();

                $('.playable-card').on('click', function (e) {
                    $('.playable-card').removeClass("active-card");
                    $(this).addClass("active-card");
                    $('#play-card-button').data("index", $(this).data("index"));
                    $('#play-card-button').html("Play Card " + $(this).data("index"));
                    $('#play-card-button').show();
                });
                lastUpdateAction = cookie.game.actions.length;
            }
        }
    }
}

function updateGameInfo(id, isDeterminePage) {
    var previousTurn = cookie.game ? cookie.game.turn : -1;
    var previousState = cookie.game ? cookie.game.state : "";
    $.get( "/api/games/" + id, {})
        .done(function( data ) {
            //console.log(data);
            cookie.game = data;
            document.cookie = JSON.stringify(cookie);

            if (data.state === "INITIAL") {
                $('#start-game-button').show();
            }
            else {
                $('#start-game-button').hide();
            }

            if (data.turn !== previousTurn || !cookie.player) {
                lastUpdateAction = -1;
            }

            updatePlayerInfo();

            if (isDeterminePage || previousState !== cookie.game.state) {
                determinePage();
            }
        })
        .fail(function( data ) {
            console.log(data.responseJSON.message)
        });
}

function updatePlayerInfo() {
    if (cookie && cookie.auth && cookie.game) {
        for (var i = 0; i < cookie.game.players.length; i++) {
            if (cookie.game.players[i].userId === cookie.auth.id) {
                if (cookie.game.turn % cookie.game.players.length === i) {
                    $('#turn-buttons').show();
                }
                else {
                    $('#turn-buttons').hide();
                }

                $.ajaxSetup({
                    headers:{
                        'Authorization': createAuthHeader()
                    }
                });
                $.get( "/api/games/" + cookie.game.id + "/players/" + i, {})
                    .done(function( data ) {
                        cookie.player = data;
                        document.cookie = JSON.stringify(cookie);
                        updateTexts();
                    })
                    .fail(function( data ) {
                        showAlert("find-game-alert", data.responseJSON.message);
                    })
                    .always(function() {
                        $('.log-button').removeClass("disabled");
                    });
                break;
            }
        }
    }
}

setInterval(function(){
    if (cookie && cookie.game) {
        updateGameInfo(cookie.game.id, false);
    }
}, 1000);