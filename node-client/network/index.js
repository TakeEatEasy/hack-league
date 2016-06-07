'use strict';

var querystring = require('querystring');

function createClient (endpoint) {
    return {
        join(player) {
            var io = require('socket.io-client');
            var socket = io(endpoint, {});

            socket.emit('register', {'name': player.name}, function (data) {
                player.init(data);
            });

            socket.on('turn', (data, respond) => player.onTurn(data, respond));

            return { quit() { socket.emit('quit'); } };
        }
    };
}

module.exports = {
    createClient
};
