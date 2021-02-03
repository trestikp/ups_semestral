#include "controller.h"
#include "message_builder.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

#include <unistd.h>

#include <limits.h>
#include <time.h>


/**
	Variable from server.c, see server.c for more
*/
extern int additional_actions;
extern int max_con;

/** Player linked list */
l_link* p_list = NULL;
/** Game lobby list */
l_link* g_lobby = NULL;
/** Game games in progress list*/
l_link* g_playing = NULL;

/** id_counter servers for assigning id to players */
int id_counter = 1;

int player_count = 0;

/**
	Instruction strings for OPPONENT instruction
*/
char* inst_string[INSTRUCTION_COUNT] = {
	[OPPONENT_JOIN] = "OPPONENT_JOIN",
	[OPPONENT_TURN] = "OPPONENT_TURN",
	[OPPONENT_DISC]	= "OPPONENT_DISC",
	[OPPONENT_RECO] = "OPPONENT_RECO",
	[OPPONENT_LEFT] = "OPPONENT_LEFT",
	[PING]		= "PING"
};


/*****************************************************************************/
/*									     */
/*	Temporary functions -- for printing while debugging		     */
/*									     */
/*****************************************************************************/

void print_plist() {
	l_link *temp = p_list;
	while(temp) {
		printf("id: %d\n", ((player*)temp->data)->id);
		printf("FD: %d\n", ((player*)temp->data)->socket);
		printf("username: %s\n", ((player*)temp->data)->username);
		printf("connected: %d\n", ((player*)temp->data)->connected);
		printf("\n");
		temp = temp->next;
	}
}

void print_lobby() {
	l_link* temp = g_lobby;
	while(temp) {
		printf("gamename: %s\n", ((game*) temp->data)->gamename);
		printf("p1: %s\n", ((game*) temp->data)->p1 ? ((game*) temp->data)->p1->username : "NULL");
		printf("p2: %s\n", ((game*) temp->data)->p2 ? ((game*) temp->data)->p2->username : "NULL");
		temp = temp->next;
	}
}

void print_playing() {
	l_link* temp = g_playing;
	while(temp) {
		printf("gamename: %s\n", ((game*) temp->data)->gamename);
		printf("p1: %s\n", ((game*) temp->data)->p1 ? ((game*) temp->data)->p1->username : "NULL");
		printf("p2: %s\n", ((game*) temp->data)->p2 ? ((game*) temp->data)->p2->username : "NULL");
		temp = temp->next;
	}
}

/*****************************************************************************/
/*									     */
/*	Support functions						     */
/*									     */
/*****************************************************************************/

/**
	Finds player by File Descriptor and compares player ID from message to 
	player ID in memory

	Returns NULL on error and player pointer on success
*/
player* find_and_verify_player(int player_id, int fd) {
	player* p = find_player_by_fd(p_list, fd);

	if(!p) return NULL;

	if(!verify_player(p, player_id)) return NULL;
	
	return p;
}


/**
	Finds player in player list. If sockets match returns 0. If sockets don't match
	but player exists in list return 1 and disconnects socket @fd. If player isn't 
	found and id @player_id is 0 returns 0 (new connection). Otherwise return 2 and
	disconnect socket @fd.
*/
int verify_and_load_player(int player_id, player** p){
	if(((*p) = find_player_in_list(p_list, player_id))) {
		return 0;
	} else {
		if(player_id == 0) {
			return 0;
		} else {
			additional_actions = 1;
			return 2;
		}
	}
}


game* find_players_game(player* p) {
	game *g = NULL;

	g = find_game_by_player(g_lobby, p);
	
	if(!g) {
		g = find_game_by_player(g_playing, p);
	}

	return g;
}


char* do_win(player* loser, game* g, player* p) {
		char* op_msg;
		player* opponent = get_your_opponent(g, p);
		int rv = 0;

		make_transition(&(g->p1->at), A_END_GAME);
		make_transition(&(g->p2->at), A_END_GAME);

		//g->p1->busy = 0;
		g->p1->on_top = 0;

		//g->p2->busy = 0;
		g->p2->on_top = 0;

		if(loser == opponent) {
			op_msg = construct_message_with_inst(opponent->id, inst_string[OPPONENT_TURN],
							203, "You lost!");

			if(!op_msg) return construct_message(p->id, 499, "Failed to contact opponent");

			rv = send(opponent->socket, op_msg, strlen(op_msg), MSG_CONFIRM);
			if(rv < 0) return construct_message(p->id, 499, "Failed to contact opponent");

			if(delete_game_from_list(&g_playing, g)) {
				log_message("Failed to delete game, continuing (possible memory leak)", LVL_ERROR);
			}

			free(op_msg);

			return construct_message(p->id, 204, "You won!");
		} else {
			op_msg = construct_message_with_inst(opponent->id, inst_string[OPPONENT_TURN],
							204, "You won!");

			if(!op_msg) return construct_message(p->id, 499, "Failed to contact opponent");

			rv = send(opponent->socket, op_msg, strlen(op_msg), MSG_CONFIRM);
			if(rv < 0) return construct_message(p->id, 499, "Failed to contact opponent");

			if(delete_game_from_list(&g_playing, g)) {
				log_message("Failed to delete game, continuing (possible memory leak)", LVL_ERROR);
			}

			free(op_msg);

			return construct_message(p->id, 203, "You lost!");
		}
}


/**
	Initializes a new player with File Descriptor and adds new player to
	player list.

	Return NULL on error and player pointer on success
*/
player* add_player(int fd) {
	player *temp = init_player(fd);

	if(!temp) return NULL;
	
	printf("Adding connection with FD %d\n", temp->socket);

	if(add_lifo(&p_list, temp)) {
		log_message("Failed to add player to list", LVL_ERROR);
		printf("Failed to add player to list\n");

		return NULL;
	}

	return temp;
}


/**
	Returns instruction from string parameter. Support function
	to message handling.
*/
instruction parse_instruction(char* str) {
	instruction inst;

	if(!strcmp(str, "PING")) {
		inst = PING;
	} else if(!strcmp(str, "TURN")) {
		inst = TURN;
	} else if(!strcmp(str, "LOBBY")) {
		inst = LOBBY;
	} else if(!strcmp(str, "DELETE_LOBBY")) {
		inst = DELETE_LOBBY;
	} else if(!strcmp(str, "CREATE_LOBBY")) {
		inst = CREATE_LOBBY;
	} else if(!strcmp(str, "JOIN_GAME")) {
		inst = JOIN_GAME;
	} else if(!strcmp(str, "CONNECT")) {
		inst = CONNECT;
	} else if(!strcmp(str, "DISCONNECT")) {
		inst = DISCONNECT;
	} else {
		inst = INST_ERROR;
	}

	return inst;
}


/**
	Parses string @str to int and return number extraced or INT_MIN on error.
	This could (should) be moved to general functions
*/
int parse_string_to_int(char* str) {
	int res;
	char* leftover;

	res = strtol(str, &leftover, 10);

	if(leftover == str || res < 0 || *leftover != '\0') {
		printf("Failed to parse ID\n");
		return INT_MIN;
	}

	return res;
}



void remove_disconnected_player(player* p) {
	game* g = NULL;

	g = find_players_game(p);

	if(g) {
		player* opponent = get_your_opponent(g, p);

		if(!opponent) {
			if(delete_game_from_list(&g_lobby, g)) printf("Server didn't find the game to delete");

			return;
		}

		if(opponent->connected) {
			char* op_msg = construct_message_with_inst(opponent->id, inst_string[OPPONENT_LEFT],
						201, "Terminating game. Opponent left");

			if(!op_msg) printf("Failed to construct message to opponent");

			int rv = send(opponent->socket, op_msg, strlen(op_msg), MSG_CONFIRM);

			if(rv < 0) printf("Failed to send message to opponent");

			free(op_msg);

			if(delete_game_from_list(&g_playing, g)) printf("Server didn't find the game to delete");
		}
	}

	delete_player_with_id(&p_list, p->id);
}

/**
	Does basic instruction validation. Validates number of tokens (message infromation
	+ parameters) that are allowed with any instruction.
*/
int validate_instruction_par_count(instruction inst, int token_cnt) {
	int params = 0;

	if(token_cnt < 2 || token_cnt > 32) {
		return 1;
	}

	if(inst == INST_ERROR) {
		return 2;
	}
	
	switch(inst) {
		case LOBBY: case PING: case OPPONENT_JOIN: case OPPONENT_TURN: case OPPONENT_DISC:
		case DELETE_LOBBY: case DISCONNECT:  params = 0; break;
		case CONNECT: case CREATE_LOBBY: case JOIN_GAME: params = 1; break;
		case TURN: params = 30; break;
		case INST_ERROR: params = -1; //never occurs, it's just to prevent warning
		default: params = 0;
	}

	if((params + 2) != token_cnt) {
		if(inst != TURN) {
			return 3;
		} else {
			if(token_cnt < 4) return 4;
			if(token_cnt > (params + 2)) return 5;
		}
	}

	return 0;
}


/*****************************************************************************/
/*									     */
/*	Event functions							     */
/*									     */
/*****************************************************************************/


/**
	Connect player to the server. Creates player instance and puts him
	to player list
*/
char* connect_my(int* player_id, char* username, int fd) {
	player *p = NULL;
	game *g = NULL;

	if(!username) {
		return construct_message(*player_id, 402, "Username is empty");
	}

	if(strlen(username) > 32) {
		return construct_message(*player_id, 403, "Username too long");
	}

	if(!is_username_available(p_list, username) && *player_id == 0) {
		return construct_message(*player_id, 404, "Username already in use");
	}

	if(*player_id == 0) { //new player
		p = find_player_by_fd(p_list, fd); //!!!!!!!! if using nc to test (and sets disconnect to long
		// time) -> this will be in list, however with normal disconnect time, the socket will be freed
		if(p) { //socket already in the list
			return construct_message(*player_id, 410, "This socket is already connected");
		}


		if(player_count >= max_con) {
			return construct_message(*player_id, 409, "Maximum number of connections reached");
		}

		p = add_player(fd);
	
		if(!p) {
			if(delete_player_with_id(&p_list, p->id)) {
				log_message("Failed to delete player. Continuing (possible memory leak)", LVL_ERROR);
				printf("ERROR: Failed to delete player but continuing (possible memory leak)");
			}

			return construct_message(*player_id, 405, "Server failed to add player");
		}
	
		p->connected = 1;
		p->username = calloc(strlen(username), sizeof(char));
			
		if(!p->username) {
			if(delete_player_with_id(&p_list, p->id)) {
				log_message("Failed to delete player. Continuing (possible memory leak)", LVL_ERROR);
				printf("ERROR: Failed to delete player but continuing (possible memory leak)");
			}

			return construct_message(*player_id, 405, "Server failed to add player");
		}

		strcpy(p->username, username);
		p->id = id_counter;
		p->last_com = time(NULL);

		id_counter++;

		*player_id = p->id;
		player_count++;

		printf("Player count %d out of %d\n", player_count, max_con);
	
		return construct_message(*player_id, 201, "Connection success");
	} else { //reconnection/ attack
		p = find_player_in_list(p_list, *player_id);

		if(!p) {
			printf("Player with ID %d doesn't exist!\n", *player_id);
			return construct_message(*player_id, 406, "Player with this ID doesn't exist");
		}

		//if player exists but isn't connect = reconnecting
		if(p->id == *player_id && p->connected == 0) {
			printf("Player id %d reconnecting\n", *player_id);

			char* msg = construct_message(*player_id, 202, "Reconnection success");

			if(!msg) return NULL;
			
			//p->busy can be used to see if player is in game/ lobby, but all checks
			//would be done regardless, so there is no reason to check for p->busy
			g = find_players_game(p);

			if(!g) {
				append_parameter(&msg, "connected"); //no game
				set_state(&(p->at), C_CONNECTED);
			}
			else if(!g->on_turn) { //no opponent = was waiting for opponent
				delete_game_from_list(&g_lobby, g);
				append_parameter(&msg, "connected");

				set_state(&(p->at), C_CONNECTED);
			} else { //has game, has opponent
				player* opponent = get_your_opponent(g, p);
				char* op_msg = construct_message_with_inst(opponent->id, inst_string[OPPONENT_RECO],
								201, "Opponent reconnected");

				int rv = send(opponent->socket, op_msg, strlen(op_msg), MSG_CONFIRM);
				if(rv < 0) printf("Failed to contact opponent about reconnect");

				free(op_msg);

				if(g->on_turn == p) { //TODO append gamestate in case opponent moved before disc
					append_parameter(&msg, "turn");

					char* board = gameboard_to_string(g);
					if(!board) return construct_message(p->id, 408, "Failed to attach gameboard");

					append_parameter(&msg, board);
					free(board);

					char temp[2] = {0};
					sprintf(temp, "%d", p->on_top);

					append_parameter(&msg, temp);
					append_parameter(&msg, opponent->username);
				} else {
					append_parameter(&msg, "opponents_turn");

					char* board = gameboard_to_string(g);
					if(!board) return construct_message(p->id, 408, "Failed to attach gameboard");

					append_parameter(&msg, board);
					free(board);

					char temp[2] = {0};
					sprintf(temp, "%d", p->on_top);

					append_parameter(&msg, temp);
					append_parameter(&msg, opponent->username);
				}

				//this is probably unnecessary 
				set_state(&(p->at), C_IN_GAME);
			}

			p->connected = 1;
			p->socket = fd;
			p->last_com = time(NULL); //should be done in handle_message(), but to be sure
			//reset players strikes?
			//p->strikes = 0;

			return msg;
		} else { //could branch connected and id mismatch for more detailed response
			//printf("Player is either already connected or IDs don't match\n");
			//if(p->connected != 0) printf("Player is already connected!\n");
			//if(p->id != *player_id) printf("id %d mismatch %d\n", p->id, *player_id);
			return construct_message(*player_id, 407, "Is this an attack attempt");
		}
	}
}

/**
	Sends lobby name lists to player *p
	Returns message that is sent to player @p
*/
char* lobby(player* p) {
	char* msg = NULL;
	l_link* temp = g_lobby;

	if(p->at.state != C_CONNECTED) {
		return construct_message(p->id, 403, "This cannot be done in current state");
	}

	msg = construct_message(p->id, 201, "Available lobbies");

	if(!msg) return NULL;
	
	while(temp) {
		if(append_parameter(&msg, ((game*) temp->data)->gamename)) {
			return construct_message(p->id, 402, "Failed to fetch game");
		}
		
		temp = temp->next;
	}

	return msg;
}


/**
	Creates lobby with @lobby_name and sets player @p as p1 ("white" stones = (on_top = 0))
	Returns message that is sent to player @p
*/
char* create_lobby(player* p, char* lobby_name) {
	l_link* temp = g_lobby;
	game* g = NULL;

	g = init_new_game();
	if(!g) {
		return construct_message(p->id, 402, "Server failed to create lobby");
	}

	if(strlen(lobby_name) > 63) {
		return construct_message(p->id, 403, "Lobby name is too long");
	}

	while(temp) {
		if(!strcmp(((game*) temp->data)->gamename, lobby_name)) {
			return construct_message(p->id, 404, "Lobby name already exists");
		}

		temp = temp->next;
	}

	if(p->at.state != C_CONNECTED) {
		return construct_message(p->id, 405, "This cannot be done in current state");
	}
	
	if(add_player_to_game(g, p)) { //theorethically shouldn't happen...
		return construct_message(p->id, 406, "Failed to add player to game");
	}

	if(add_lifo(&g_lobby, g)) {
		return construct_message(p->id, 407, "Failed to add game");
	}

	//p->busy = 1;

	strcpy(g->gamename, lobby_name);

	//set state to "IN LOBBY"
	make_transition(&(p->at), A_CREATE_L);
	
	return construct_message(p->id, 201, "Successfully created lobby");
}


/**
	Removes game player @p is in from lobby list and frees it (if @p has a game).
	Returns message that is sent to player @p
*/
char* delete_lobby(player* p) {
	game *g = NULL;
	//l_link *prev = NULL, *curr = NULL;

	if(p->at.state != C_IN_LOBBY) {
		return construct_message(p->id, 402, "This cannot be done in current state");
	}

	g = find_game_by_player(g_lobby, p);

	if(!g) return construct_message(p->id, 403, "You don't have lobby");

	//theorethically doesn't need to be checked because find_game_by_player makes sure the game exists
	if(delete_game_from_list(&g_lobby, g)) {
		return construct_message(p->id, 404, "No game found");
	}

	make_transition(&(p->at), A_LEAVE_L);

	//p->busy = 0;

	return construct_message(p->id, 201, "Lobby deleted");
}


/**
	Player *p joins game with @lobby_name name (if possible).
	Returns message that is sent to player @p
*/
char* join_game(player* p, char* lobby_name) {
	char *op_msg, *msg;
	int rv = 0;
	game* g = NULL;

	if(strlen(lobby_name) > 63) {
		return construct_message(p->id, 402, "Lobby name is too long");
	}

	if(p->at.state != C_CONNECTED) {
		return construct_message(p->id, 403, "This cannot be done in current state");
	}

	g = extract_game_by_name(&g_lobby, lobby_name);

	if(!g) {
		return construct_message(p->id, 404, "Failed to find game lobby");
	}

	if(add_lifo(&g_playing, g)) { //server failed to add game to g_playing
		if(add_lifo(&g_lobby, g)) { //return game back to lobby
			//server failed to return game to lobby, cancel lobby for p1
			//g->p1->busy = 0;

			//contact lobby creator
			op_msg = construct_message_with_inst(g->p1->id, inst_string[OPPONENT_JOIN], 401,
						   "Server lost your lobby");

			if(!op_msg) return construct_message(p->id, 405, "Failed to contact opponent");

			rv = send(g->p1->socket, op_msg, strlen(op_msg), MSG_CONFIRM);
			
			if(rv < 0) {
				printf("Server lost lobby and failed to contact player about it\n");
			}

			return construct_message(p->id, 406, "Server lost game");
		}
		return construct_message(p->id, 407, "Failed to add game");
	}

	//contact opponent
	op_msg = construct_message_with_inst(g->p1->id, inst_string[OPPONENT_JOIN], 201,
						   "Opponent connected. Starting");

	if(!op_msg) return construct_message(p->id, 405, "Failed to contact opponent");

	append_parameter(&op_msg, p->username);
	rv = send(g->p1->socket, op_msg, strlen(op_msg), MSG_CONFIRM);

	if(rv < 0) {
		return construct_message(p->id, 405, "Failed to contact opponent");
	}


	msg = construct_message(p->id, 201, "Successfully joined game");
	if(!msg) return NULL;
	append_parameter(&msg, g->p1->username);

	//p->busy = 1;
	p->on_top = 1;

	g->p2 = p;
	g->on_turn = g->p1;

	make_transition(&(g->p1->at), A_JOIN_L);
	make_transition(&(p->at), A_JOIN_L);

	return msg;
}


/**
	Validates and makes move in gameboard that player @p is in. Moves can be 
	sequence that is passed as string array @parts. First 2 parts are 
	player id and instruction, so parameters are only from 2 to 31.
	@parts_coutn is number of parametrs.
	Returns message that is sent to player @p
*/
char* turn(player* p, char* parts[32], int parts_count) {
	player* loser = NULL, *opponent = NULL;
	game* g = NULL;
	char *op_msg = NULL;
	int i = 0, rv = 0;
	int pars[parts_count]; //2 parts are ID and INST

	if(p->at.state != C_IN_GAME) {
		return construct_message(p->id, 402, "This cannot be done in current state");
	}

	g = find_game_by_player(g_playing, p);

	if(!g) {
		return construct_message(p->id, 403, "Failed to find game");
	}

	if(g->on_turn != p) {
		return construct_message(p->id, 404, "It is not your turn"); //TODO code
	}

	if(parts[2] == NULL) { //this if is kinda useless
		return construct_message(p->id, 405, "Need starting position");
	}

	if(parts_count < 2) { 
		return construct_message(p->id, 406, "Too few parameters");
	}

	for(i = 0; i < parts_count; i++) {
		pars[i] = parse_string_to_int(parts[i + 2]);
		if(pars[i] == INT_MIN) {
			return construct_message(p->id, 407, "Parameter isn't number");

		}
	}

	for(i = 1; i < parts_count; i++) {
		if(validate_move(pars[i - 1], pars[i], p, g, i)) {
			printf("Failed verify move from %d to %d\n", pars[i - 1], pars[i]);

			return construct_message(p->id, 408, "Failed to validate move");
		}
	}

	opponent = get_your_opponent(g, p);

	if(!opponent) return construct_message(p->id, 409, "Failed to find opponent");

	op_msg = construct_message_with_inst(opponent->id, inst_string[OPPONENT_TURN], 201, "Opponent moved");

	if(!op_msg) return construct_message(p->id, 410, "Failed to contact opponent");

	for(i = 0; i < parts_count; i++) {
		char num[3]; //shouldn't need more than 2 numbers as indexes should be between 0-63
		sprintf(num, "%d", pars[i]);

		if(append_parameter(&op_msg, num)) {
			return construct_message(p->id, 411, "Opponent message error");
		}
	}


	for(i = 1; i < parts_count; i++) {
		make_move(pars[i - 1], pars[i], p, g);
	}

	g->on_turn = opponent;

	if((loser =  check_for_victory(g))) {
		return do_win(loser, g, p);
	} else {
		rv = send(opponent->socket, op_msg, strlen(op_msg), MSG_CONFIRM);
		if(rv < 0) return construct_message(p->id, 410, "Failed to contact opponent");
	}

	return construct_message(p->id, 202, "Turn successful");
}


/**
	Test for client to see if they are still connected.
	Returns message that is sent to player @p
*/
char* ping(player* p) {
	return construct_message_with_inst(p->id, inst_string[PING], 201, "Pinging");
}


/**
	Removes player @p from player list and disconnect socket.
	Returns message that is sent to player @p
*/
char* disconnect(player* p) {
	if(delete_player_with_id(&p_list, p->id)) {
		log_message("Failed to find player to delete but closed connection", LVL_ERROR);
		printf("ERROR: Failed to find player to delete but closing connection\n");
	}

	additional_actions = 1;

	printf("Disconnected user");

	player_count--;

	return construct_message(0, 201, "You were disconnected");
}

/*****************************************************************************/
/*									     */
/*	Core functions							     */
/*									     */
/*****************************************************************************/


void check_for_disconnects(fd_set* clients) {
	l_link* temp = p_list;
	player* p = NULL;
	game* g = NULL;

	//printf("checking for disconnects \n");

	while(temp) {
		if((time(NULL) - ((player*) temp->data)->last_com) >= 5) {
			p = (player*) temp->data;

			if(p->connected == 0) {
				//if he had one
				printf("Player %d got %lds left to reconnect\n", p->id, 
						(300 - (time(NULL) - p->last_com)));
						//(60 - (time(NULL) - p->last_com)));
				temp = temp->next;

				if((time(NULL) - p->last_com) >= 300) {
				//if((time(NULL) - p->last_com) >= 60) {
					FD_CLR(p->socket, clients);
					close(p->socket);
				
					remove_disconnected_player(p);

					player_count--;
				} else {
					if(p->at.state != C_CONNECTED) {
						g = find_players_game(p);

						if(g) {
							player* opponent = get_your_opponent(g, p);

							if(opponent && opponent->connected == 1 &&
							   opponent->socket > 0) {
								char* op_msg = construct_message_with_inst(g->p2->id,
									inst_string[OPPONENT_DISC], 201, "Opponent disconnected");
								int rv = send(opponent->socket, op_msg, strlen(op_msg), MSG_CONFIRM);

								if(rv < 0) printf("Failed to contact opponent about disconnect\n");

								free(op_msg);
							}
						}
					}
				}

				continue;
			}

			printf("Player %d lost connection\n", p->id);

			p->connected = 0;
			//if(p->busy) {
			if(p->at.state != C_CONNECTED) {
				g = find_game_by_player(g_lobby, p);

				if(!g) {
					g = find_game_by_player(g_playing, p);
				}

				if(!g) {
					printf("ERROR: Player doesn't have game while they should\n");
				} else {
					char* op_msg;
					int rv = 0;

					//p1 should be always initialized because game cannot be create without p1
					//however p2 can be NULL if game is in lobby
					if(g->p2 == NULL) {
						temp = temp->next;
						continue;
					}

					player* opponent = get_your_opponent(g, p);

					if(opponent) {
						if(opponent->socket < 0) {
							printf("Opponent doesn't have socket. Disconnected\n");
						} else {
							op_msg = construct_message_with_inst(g->p2->id,
								inst_string[OPPONENT_DISC], 201, "Opponent disconnected");
							rv = send(opponent->socket, op_msg, strlen(op_msg), MSG_CONFIRM);

							if(rv < 0) printf("Failed to contact opponent about disconnect\n");

							free(op_msg);
						}
					} else {
						printf("Failed to find opponent while reconnecting\n");
					}

				}
			}

			p->socket = -1;
		}

		temp = temp->next;
	}
}


/**
	Parses recieved message, calls appropriate handling function and returns
	constructed response
*/
char* handle_message(char *message, int fd) {
	int token_cnt = 0, inst = -1, parsed_id = -1, rv = 0;
	player* p = NULL;
	char *token, *reply, *leftover = NULL;
	char *parts[32];

	char* debug_copy = malloc(sizeof(char) * strlen(message));
	strcpy(debug_copy, message);

	token = strtok(message, "|");

	do {
		if(token_cnt >= 32) {
			printf("Too many tokens!\n"); //TODO logger?
			break;
		}

		//netcat sends \n with message
		if(token[strlen(token) - 1] == '\n') {
			token[strlen(token) - 1] = 0;
		}

		parts[token_cnt] = token;
		token_cnt++;
	} while((token = strtok(NULL, "|")));

	parsed_id = strtol(parts[0], &leftover, 10);

	if(leftover == parts[0] || parsed_id < 0 || *leftover != '\0') {
		printf("Failed to parse ID\n");
		return NULL;
	}

	if((rv = verify_and_load_player(parsed_id, &p))) {
		char* msg = NULL;

		switch(rv) {
			case 1: msg = construct_message(parsed_id, 400, "Socekts don't match"); break;
			case 2: msg = construct_message(parsed_id, 400, "Uknown connection"); break;
			default: msg = construct_message(parsed_id, 400, "Verification failed");
		}

		return msg;
		/*
		printf("Player verification failed\n");
		return construct_message(parsed_id, 400, "Player verification failed");
		*/
	}

	inst = parse_instruction(parts[1]);

	if(inst == INST_ERROR) return NULL;

	if((rv = validate_instruction_par_count(inst, token_cnt))) {
		if(p) {
			p->strikes++;

			//player didn't send CONNECT, don't disconnect! (giberrish from previous request)
			if(p->connected == 0 && inst != CONNECT) {
				additional_actions = 2;
				return NULL;
			}


			if(p->strikes == 3) {
				if(delete_player_with_id(&p_list, p->id)) {
					log_message("Failed to delete player. Continuing (possible memory leak)", LVL_ERROR);
					printf("ERROR: Failed to delete player but continuing (possible memory leak)");
				}

				return NULL;
			}

		}
		
		switch(rv) {
			case 1: return construct_message(parsed_id, 401, "Instruction got too many parameters");
			case 2: return construct_message(parsed_id, 401, "Unrecognized instruction");
			case 3: return construct_message(parsed_id, 401, "Unexpected parameter count");
			case 4: return construct_message(parsed_id, 401, "TURN needs at least 2 parameters");
			case 5: return construct_message(parsed_id, 401, "Too many parameters for TURN");
		}	
	}
	

	//player not found and used ID = 0 and wasn't trying to CONNECT! -> disconnect from server
	if(!p && inst != CONNECT) {
		return NULL;
	}

	//if player exists, but isn't connected, and instruction isn't CONNECT -> disconnect
	//doesn't remove player from list, so player can still reconnect with CONNECT
	if(p && !p->connected && inst != CONNECT) {
		additional_actions = 2;

		return NULL;
	}

	switch(inst) {
		case LOBBY: reply = lobby(p); break;
		case CREATE_LOBBY: reply = create_lobby(p, parts[2]); break;
		case JOIN_GAME: reply = join_game(p, parts[2]); break;
		case DELETE_LOBBY: reply = delete_lobby(p); break;
		case CONNECT: reply = connect_my(&parsed_id, parts[2], fd); break;
		case TURN: reply = turn(p, parts, (token_cnt - 2)); break;
		case DISCONNECT: reply = disconnect(p); break;
		case PING: reply = ping(p); break;
	}

	if(p) p->last_com = time(NULL);
	
	//printf("\n------------------------------\n");
	//printf("PRINTING PLAYER LIST\n");
	//print_plist();
	//printf("\nPRINTING LOBBY\n");
	//print_lobby();
	//printf("\nPRINTING PLAYING\n");
	//print_playing();
	//printf("\n==============================\n");

	if(inst != PING) printf(">>> BUFFER: %s\n", debug_copy);
	//if(reply && inst != PING) printf(">>> RESPONSE: %s with len %ld to socket %d\n\n", reply, strlen(reply), fd);
	if(reply && inst != PING) printf(">>> RESPONSE: %s\n", reply);

	if(reply) return reply;
	else 	  return construct_message(parsed_id, 400, "Failed to construct reply");
}

/**
	Frees controller memory
*/
void free_controller() {
	l_link* temp = p_list;

	while(temp) {
		p_list = p_list->next;
		free_player((player*) temp->data);
		free(temp);
		temp = p_list;
	}

	temp = g_lobby;
	while(temp) {
		g_lobby = g_lobby->next;
		free_game((game*) temp->data);
		free(temp);
		temp = g_lobby;
	}

	temp = g_playing;
	while(temp) {
		g_playing = g_playing->next;
		free_game((game*) temp->data);
		free(temp);
		temp = g_playing;
	}
}
