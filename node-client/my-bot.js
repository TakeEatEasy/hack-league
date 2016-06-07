var network = require('./network');
var minimist = require('minimist');

var argv = minimist(process.argv.slice(2));

if (!argv.table) {
  console.log('--table required');
  process.exit(1);
}

if (!argv.name) {
    console.log('--name required');
    process.exit(1);
}

function start () {
	var city;
	
  	network.createClient(argv.table).join({
    	name: argv.name,
        
		//This method is called at each "new game" or when you rejoin a game.
        //data contains all the "static informations" that will not change during the game:
        init(data){
            console.log("init");
            city = data.city;
            console.log('Roads')
            console.log(city.roads);
        },

        //onTurn is called each time you can play a turn
    	onTurn(data, respond){
      	  	console.log("onTurn " + respond.turn);
			console.log(JSON.stringify(data,null,2));

            //Fetch my courier and possible order from the state
			var state = data.state;
			var myCourier;
            var myOrder;

			for(var i in state.couriers){
				var courier = state.couriers[i];
				if(courier.id == data.idCourier){
					myCourier = courier;
					break;
				}
			}
			for(var i in state.orders){
				var order = state.orders[i];
				if(order.idCourier == data.idCourier){
					myOrder = order;
					break;
				}
			}
			
			console.log("My Courier")
			console.log(myCourier);
            console.log("My Order");
            console.log(myOrder);
            //Game logic starts here:
            //If no current order

            //Go to 1,1 position
			respond(getDirection(data.possibleActions,myCourier.position,{x:1,y:2}));
    	}
    });


    //Get the distance between two x,y positions;
    function getDistance(fromX,fromY,toX,toY){
        var key =  fromX+"-"+fromY+";"+toX+"-"+toY;
        return city.distances[key];
    }

    //Get the best direction to go from "from" to "to" position based on the pre-computed distances
    function getDirection(possibleActions,from,to){
        var bestMove;
        var bestMoveDistance = Number.MAX_VALUE;

        for(var i in possibleActions){
            var action = possibleActions[i];

            if(action.action == ('MOVE_UP')){
                var distance = getDistance(from.x,from.y-1,to.x,to.y);
                if(distance < bestMoveDistance){
                    bestMove = action;
                    bestMoveDistance = distance;
                }
            }
            if(action.action == ('MOVE_DOWN')){
                var distance = getDistance(from.x,from.y+1,to.x,to.y);
                if(distance < bestMoveDistance){
                    bestMove = action;
                    bestMoveDistance = distance;
                }
            }
            if(action.action == ('MOVE_LEFT')){
                var distance = getDistance(from.x-1,from.y,to.x,to.y);
                if(distance < bestMoveDistance){
                    bestMove = action;
                    bestMoveDistance = distance;
                }
            }
            if(action.action == ('MOVE_RIGHT')){
                var distance = getDistance(from.x+1,from.y,to.x,to.y);
                if(distance < bestMoveDistance){
                    bestMove = action;
                    bestMoveDistance = distance;
                }
            }

        }
        return bestMove;
    }
}

console.log(`starting courier bot on ${argv.table}`);
start();
