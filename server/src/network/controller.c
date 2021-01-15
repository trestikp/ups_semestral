#include "controller.h"
#include "message_builder.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

#include <limits.h>
#include <time.h>


/**
	Variable from server.c, see server.c for more
*/
extern int additionalActions;

/** Player linked list */
l_link* p_list = NULL;
/** Game lobby list */
l_link* g_lobby = NULL;
/** Game games in progress list*/
l_link* g_playing = NULL;

/** id_counter servers for assigning id to players */
int id_counter = 1;

/**
	Instruction strings for OPPONENT instruction
*/
char* inst_string[INSTRUCTION_COUNT] = {
	[OPPONENT_JOIN] = "OPPONENT_JOIN",
	[OPPONENT_TURN] = "OPPONENT_TURN"
};


/*****************************************************************************/
/*									     */
/*	Temporary functions -- are to be removed			     */
/*									     */
/*****************************************************************************/

//TODO: remove
void print_plist() {
	l_link *temp = p_list;
	while(temp) {
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
		printf("gamestate: %d\n", ((game*) temp->data)->gamestate);
		printf("p1: %s\n", ((game*) temp->data)->p1 ? ((game*) temp->data)->p1->username : "NULL");
		printf("p2: %s\n", ((game*) temp->data)->p2 ? ((game*) temp->data)->p2->username : "NULL");
		temp = temp->next;
	}
}

void print_playing() {
	l_link* temp = g_playing;
	while(temp) {
		printf("gamename: %s\n", ((game*) temp->data)->gamename);
		printf("gamestate: %d\n", ((game*) temp->data)->gamestate);
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
int verify_and_load_player(int player_id, int fd, player** p){
	if(((*p) = find_player_in_list(p_list, player_id))) {
		if((*p)->socket == fd) {
			return 0;
		} else {
			additionalActions = 1;
			return 1;
		}
	} else {
		if(player_id == 0) {
			return 0;
		} else {
			additionalActions = 1;
			return 2;
		}
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
	add_lifo(&p_list, temp);

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


/**
*/
int delete_game_from_gplaying(game* g) {
	l_link *prev = NULL, *curr = NULL;

	curr = g_playing;

	while(curr) {
		if((game*) curr->data == g) {
			break;
		}

		prev = curr;
		curr = curr->next;
	}

	if(!curr) {
		return 1;
	}

	free_game((game*) curr->data);
	//free(curr->data);

	if(prev) {
		prev->next = curr->next;
	} else {
		//if prev = NULL, its the beginning of the lobby
		// -> need to change 
		g_playing = g_playing->next;
	}

	free(curr);

	return 0;
}


/**
	Does basic instruction validation. Validates number of tokens (message infromation
	+ parameters) that are allowed with any instruction.
*/
int validate_instruction(instruction inst, int token_cnt) {
	int params = 0;

	if(token_cnt < 2 || token_cnt > 32) {
		return 1;
	}

	if(inst == INST_ERROR) {
		return 2;
	}
	
	switch(inst) {
		case LOBBY: case PING: case OPPONENT_JOIN: case OPPONENT_TURN:
		case DELETE_LOBBY: case DISCONNECT:  params = 0; break;
		case CONNECT: case CREATE_LOBBY: case JOIN_GAME: params = 1; break;
		case TURN: params = 30; break;
		case INST_ERROR: params = -1; //never occurs, it's just to prevent warning
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

/**
	TEMP? Handles reply from client to OPPONENT instruction.
	Return 0 on success and erro number on error.
*/
int handle_reply(char* reply, int id) {
	int token_cnt = 0, parsed_id = -1;
	char *token, *leftover = NULL;
	char *parts[2];

	token = strtok(reply, "|");

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

	if(token_cnt != 2) {
		return 1;
	}

	parsed_id = strtol(parts[0], &leftover, 10);

	if(leftover == parts[0] || parsed_id < 0 || *leftover != '\0') {
		printf("Failed to parse ID\n");
		return 2;
	}

	if(id != parsed_id) return 3;

	if(!strcmp(parts[1], "OK")) {
		return 3;
	} else if(!strcmp(parts[1], "ERROR")) {
		return 4;
	} else {
		return 5;
	}

	return 0;
}


/**
	Sends message @op_msg to socket @socket 
*/
int contact_player(int socket, int requester, int target, char* op_msg) {
	//char* msg = NULL;
	time_t timeout;

	send(socket, op_msg, strlen(op_msg), MSG_CONFIRM);

	free(op_msg);

	char resp[256];
	int code = 0;

	recv(socket, resp, 255, 0);

	return handle_reply(resp, target);

	/*
	timeout = time(NULL);
	while(code != 1 || timeout != (timeout + 30)) {
		code = recv(socket, resp, 255, 0);
	}
	*/

	/*
	switch(handle_reply(resp, target)) {
		case 0: msg = construct_message(requester, 201, "Successfully joined game"); break;
		case 1: msg = construct_message(requester, 404, "Unexpected token count"); break;
		case 2: msg = construct_message(requester, 404, "Failed to parse ID"); break;
		case 3: msg = construct_message(requester, 201, "Successfully joined game"); break;
		case 4: msg = construct_message(requester, 404, "Opponent starting error"); break;
		case 5: msg = construct_message(requester, 404, "Unrecognized instruction"); break;
	}

	return msg;
	*/
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
//TODO: do some id/fd/username check if it doesn't exist
	player *p = NULL;

	if(!username) {
		printf("username is NULL??\n");
		return construct_message(*player_id, 402,
					      "Username is empty");
		//return 402;
	}

	if(!is_username_available(p_list, username)) {
		//printf("Username already taken, RV: %d\n", rv);
		return construct_message(*player_id, 403,
					      "Username already in use");

		//return 403;
	}


	if(*player_id == 0) {
		//new player connecting

		p = add_player(fd);
	
		if(!p) {
			//TODO think of error code
			return construct_message(*player_id, 404,
					      "Server failed to add player");
			//return 410;
		}
	
		p->connected = 1;
		p->username = malloc(strlen(username) * sizeof(char));
		strcpy(p->username, username);
		p->id = id_counter;
		id_counter++;

		*player_id = p->id;
	
		//print_plist();
		return construct_message(*player_id, 201,
					      "Connection success");
		//return 201;
	} else {
		//reconnection/ attack
		//TODO change to id check
		p = find_and_verify_player(*player_id, fd);

		if(!p) {
			printf("Player with ID %d doesn't exist!\n", *player_id);
			return construct_message(*player_id, 405,
					      "Player with this ID doesn't exist");
			//return 400;
		}
		
		//if player exists but isn't connect = reconnecting
		if(p->id == *player_id && p->connected == 0) {
			printf("Player id %d reconnecting\n", *player_id);
			return construct_message(*player_id, 202,
					      "Reconnection success");
			//return 202;
		} else {
			printf("Player is either already connected or IDs don't match\n");
			return construct_message(*player_id, 406,
					      "Is this an attack attempt");
			//return 400;
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

	msg = construct_message(p->id, 201, "Available lobbies");
	
	while(temp) {
		if(append_parameter(&msg,
		   ((game*) temp->data)->gamename)) {
			return construct_message(p->id, 402, "Failed to fetch game");
		}
		
		temp = temp->next;
	}

	return msg;
	//return 200;
}


/**
	Creates lobby with @lobby_name and sets player @p as p1 ("white" stones = (onTop = 0))
	Returns message that is sent to player @p
*/
char* create_lobby(player* p, char* lobby_name) {
	game* g = NULL;

	g = init_new_game();
	if(!g) {
		printf("Failed to init new game\n");

		return construct_message(p->id, 402, "Server failed to create lobby");
	}

	p->busy = 1;

	//This will be handled on client input so theorethically shouldn't occur
	if(strlen(lobby_name) > 127) {
		return construct_message(p->id, 403, "Lobby name is too long");
	}

	strcpy(g->gamename, lobby_name);

	add_player_to_game(g, p);
	add_lifo(&g_lobby, g);
	
	return construct_message(p->id, 201, "Successfully created lobby");
}


/**
	Removes game player @p is in from lobby list and frees it (if @p has a game).
	Returns message that is sent to player @p
*/
char* delete_lobby(player* p) {
	l_link *prev = NULL, *curr = NULL;

	curr = g_lobby;

	while(curr) {
		if(((game*) curr->data)->p1 == p) {
			break;
		}

		prev = curr;
		curr = curr->next;
	}

	if(!curr) {
		return construct_message(p->id, 402, "No game found for this player");
	}

	free(curr->data);

	if(prev) {
		prev->next = curr->next;
	} else {
		//if prev = NULL, its the beginning of the lobby
		// -> need to change 
		g_lobby = g_lobby->next;
	}

	free(curr);

	p->busy = 0;

	return construct_message(p->id, 201, "Lobby deleted");
}


/**
	Player *p joins game with @lobby_name name (if possible).
	Returns message that is sent to player @p
*/
char* join_game(player* p, char* lobby_name) {
	game* g = NULL;

	if(strlen(lobby_name) > 127) {
		return construct_message(p->id, 402, "Lobby name is too long");
	}

	g = extract_game_by_name(&g_lobby, lobby_name);

	if(!g) {
		return construct_message(p->id, 403, "Failed to find game lobby");
	}

	p->busy = 1;
	p->onTop = 1;

	g->p2 = p;

	add_lifo(&g_playing, g);

	char* op_msg = construct_message_with_inst(g->p1->id, inst_string[OPPONENT_JOIN], 201,
						   "Opponent connected. Starting");
	append_parameter(&op_msg, p->username);

	int rv = contact_player(g->p1->socket, p->id, g->p1->id, op_msg);
	char* msg;

	switch(rv) {
		case 0: msg = construct_message(p->id, 201, "Successfully joined game"); break;
		case 1: msg = construct_message(p->id, 404, "Unexpected token count"); break;
		case 2: msg = construct_message(p->id, 405, "Failed to parse ID"); break;
		case 3: msg = construct_message(p->id, 201, "Successfully joined game"); break;
		case 4: msg = construct_message(p->id, 406, "Opponent starting error"); break;
		case 5: msg = construct_message(p->id, 407, "Unrecognized instruction"); break;
	}

	//msg = construct_message(p->id, 201, "Successfully joined game");
	append_parameter(&msg, g->p1->username);

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
	player* winner = NULL;
	game* g = NULL;
	int i = 0, rv;
	int pars[parts_count];

	g = find_game_by_player(g_playing, p);

	if(!g) {
		return construct_message(p->id, 402, "Failed to find game");
	}

	if(parts[2] == NULL) {
		return construct_message(p->id, 403, "Need starting position");
	}

	for(i = 0; i < parts_count; i++) {
		pars[i] = parse_string_to_int(parts[i + 2]);
		if(pars[i] == INT_MIN) {
			return construct_message(p->id, 406, "Parameter isn't number");

		}
	}

	for(i = 1; i < parts_count; i++) {
		if(validate_move(pars[i - 1], pars[i], p, g, i)) {
			printf("Failed verify move from %d to %d\n", pars[i - 1], pars[i]);

		validate_move(pars[i - 1], pars[i], p, g, i);

			print_gameboard(g);
			//validate_move(pars[i - 1], pars[i], p, g);
			return construct_message(p->id, 404, "Failed to validate move");
		}
	}

	for(i = 1; i < parts_count; i++) {
		make_move(pars[i - 1], pars[i], p, g);
	}

	if((winner =  check_for_victory(g))) {
		char* op_msg;

		g->p1->busy = 0;
		g->p1->onTop = 0;

		g->p2->busy = 0;
		g->p2->onTop = 0;

		if(winner == p) {
			if(p == g->p1) {
				op_msg = construct_message_with_inst(g->p2->id,
					inst_string[OPPONENT_TURN], 204, "You lost!");

				rv = contact_player(g->p2->socket, winner->id, g->p2->id, op_msg);
			} else {
				op_msg = construct_message_with_inst(g->p1->id,
					inst_string[OPPONENT_TURN], 204, "You lost!");
				rv = contact_player(g->p1->socket, winner->id, g->p1->id, op_msg);
			}

			delete_game_from_gplaying(g);

			return construct_message(winner->id, 203, "You won!");
		} else {
			if(p == g->p1) {
				op_msg = construct_message_with_inst(g->p2->id,
					inst_string[OPPONENT_TURN], 203, "You won!");

				rv = contact_player(g->p2->socket, winner->id, g->p2->id, op_msg);
			} else {
				op_msg = construct_message_with_inst(g->p1->id,
					inst_string[OPPONENT_TURN], 203, "You won!");
				rv = contact_player(g->p1->socket, winner->id, g->p1->id, op_msg);
			}

			delete_game_from_gplaying(g);

			return construct_message(winner->id, 204, "You lost!");

		}
	}


	print_gameboard(g);


	if(g->p1 == p) {
		char* op_msg = construct_message_with_inst(g->p2->id, inst_string[OPPONENT_TURN], 201, "Opponent moved");
		
		for(i = 0; i < parts_count; i++) {
			char num[3]; //shouldn't need more than 2 numbers as indexes should be between 0-63
			sprintf(num, "%d", pars[i]);

			append_parameter(&op_msg, num);
		}
		printf("Contanting opponent\n");

		//send(g->p2->socket, op_msg, strlen(op_msg), MSG_CONFIRM);

		rv = contact_player(g->p2->socket, p->id, g->p2->id, op_msg);
		//rv = 0;
	} else if(g->p2 == p) {
		char* op_msg = construct_message_with_inst(g->p1->id, inst_string[OPPONENT_TURN], 201, "Opponent moved");

		for(i = 0; i < parts_count; i++) {
			char num[3]; //shouldn't need more than 2 numbers as indexes should be between 0-63
			sprintf(num, "%d", pars[i]);

			append_parameter(&op_msg, num);
		}

	printf("Contanting opponent\n");

		//send(g->p1->socket, op_msg, strlen(op_msg), MSG_CONFIRM);

		rv =contact_player(g->p1->socket, p->id, g->p1->id, op_msg);
		//rv = 0;
	} else {
		return construct_message(p->id, 405, "This is not your game");
	}

	if(rv == 3 || rv == 0) {
		return construct_message(p->id, 202, "Turn successful");
	} else {
		return construct_message(p->id, 407, "Failed to contact opponent");
	}
}


/**
	Test for client to see if they are still connected.
	Returns message that is sent to player @p
*/
char* ping(player* p) {
	return construct_message(p->id, 201, "Pinging");
}


/**
	Removes player @p from player list and disconnect socket.
	Returns message that is sent to player @p
*/
char* disconnect(player* p) {
	l_link *prev = NULL, *curr = NULL;
	int found = 1;

/*
	if(strcmp(p->username, username)) {
		return construct_message(p->id, 402, "Usernames don't match");
	}
*/

	curr = p_list;

	while(curr) {
		if(((player*) curr->data) == p) {
			found = 0;
			break;
		}

		prev = curr;
		curr = curr->next;
	}

	if(found) {
		return construct_message(0, 402, "Player not found");
	}

	if(prev) {
		prev->next = curr->next;
	} else {
		p_list = p_list->next;
	}


	free_player(curr->data);
	free(curr);

	additionalActions = 1;

	printf("Disconnected user");

	return construct_message(0, 201, "You were disconnected");
}

/*****************************************************************************/
/*									     */
/*	Core functions							     */
/*									     */
/*****************************************************************************/


/**
	Parses recieved message, calls appropriate handling function and returns
	constructed response
*/
char* handle_message(char *message, int fd) {
	int token_cnt = 0, inst = -1, parsed_id = -1, rv = 0;
	player* p = NULL;
	char *token, *reply, *leftover = NULL;
	char *parts[32];

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

	if((rv = verify_and_load_player(parsed_id, fd, &p))) {
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
	switch(validate_instruction(inst, token_cnt)) {
		case 1: return construct_message(parsed_id, 401, "Instruction got too many parameters");
		case 2: return construct_message(parsed_id, 401, "Unrecognized instruction");
		case 3: return construct_message(parsed_id, 401, "Unexpected parameter count");
		case 4: return construct_message(parsed_id, 401, "TURN needs at least 2 parameters");
		case 5: return construct_message(parsed_id, 401, "Too many parameters for TURN");
	}

	//player not found and used ID = 0 and wasn't trying to CONNECT! -> disconnect from server
	if(!p && inst != CONNECT) {
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

	/*
	printf("\n------------------------------\n");
	printf("PRINTING PLAYER LIST\n");
	print_plist();
	printf("\nPRINTING LOBBY\n");
	print_lobby();
	printf("\nPRINTING PLAYING\n");
	print_playing();
	printf("\n==============================\n");
	*/

	//NULL is a "valid" reply - disconnects user	
	return reply;

	/*
	if(reply) {
		return reply;
	} else {
		return construct_message(parsed_id, 400, "Failed to construct reply");
	}
	*/
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
