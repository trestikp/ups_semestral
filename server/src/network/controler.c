#include "controler.h"
#include "message_builder.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

l_link* p_list = NULL;
game* quick_g = NULL;
l_link* g_lobby = NULL;
l_link* g_playing = NULL;

//char response[RESPONSE_SIZE] = {0};

// id_counter servers for assigning id to players
int id_counter = 1;

/*
char* inst_string[] = {
	[CONNECT] 	= "CONNECT",
	[QUICK_PLAY]	= "QUICK_PLAY",
	[LOBBY]		= "LOBBY",
	[PLAY_GAME]	= "PLAY_GAME",
	[TURN] 		= "TURN",
	[PAUSE_INST] 	= "PAUSE",
	[RESUME] 	= "RESUME",
	[PING] 		= "PING",
};

char* ok_messages[INSTRUCTION_COUNT][5] = {
	[CONNECT][0] = "Unspecified CONNECT ok",
	[CONNECT][1] = "Connected",
	[CONNECT][2] = "Reconnected",
	[QUICK_PLAY][0] = "Unspecified PLAY ok",
	[QUICK_PLAY][1] = "Game starting",
	[QUICK_PLAY][2] = "Waiting for opponent",
	[LOBBY][0] = "OK",
	[TURN]	 [0] = "Unspecified TURN ok",
	[TURN]	 [1] = "Turn OK",
	[TURN]	 [2] = "Turn WIN",
	[TURN]	 [3] = "Turn LOSS",
	[PAUSE_INST][0] = "Unspecified PAUSE ok",
	[PAUSE_INST][1] = "Pause",
	[RESUME] [0] = "Unspecified RESUME ok",
	[RESUME] [1] = "Resume",
	[PING]	 [0] = "Unspecified PING ok",
	[PING]	 [1] = "Ping"
};

char* error_messages[INSTRUCTION_COUNT][5] = {
	[CONNECT][0] = "Unspecified CONNECT error",
	[CONNECT][1] = "Inappropriate username",
	[CONNECT][2] = "No username",
	[CONNECT][3] = "Username already used",
	[CONNECT][4] = "Server is full/ busy",
	[QUICK_PLAY]	 [0] = "Unspecified PLAY error",
	[QUICK_PLAY]	 [1] = "Server out of resources",
	[QUICK_PLAY]	 [2] = "Game doens't exist",
	[TURN]	 [0] = "Unspecified TURN error",
	[TURN]	 [1] = "Wrong source location",
	[TURN]	 [2] = "Wrong target location",
	[TURN]	 [3] = "Unrecognized user",
	[TURN]	 [4] = "Not your turn",
	[PAUSE_INST][0] = "Unspecified PAUSE error",
	[PAUSE_INST][1] = "Game already paused",
	[RESUME] [0] = "Unspecified RESUME error",
	[RESUME] [1] = "Game isn't paused"
};

char* general_responses[] = {
	"Protocol parse mismatch",
	"Authentication error",
	"General error",
	"Instruction argument miscount",
	"Unknown instruction"
};
*/

/*****************************************************************************/
/*									     */
/*	Temporary functions						     */
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
		printf("gamestate: %d\n", ((game*) temp->data)->gamestate);
		printf("p1: %s\n", ((game*) temp->data)->p1 ? ((game*) temp->data)->p1->username : "NULL");
		printf("p2: %s\n", ((game*) temp->data)->p2 ? ((game*) temp->data)->p2->username : "NULL");
		temp = temp->next;
	}
}

void print_playing() {
	l_link* temp = g_playing;
	while(temp) {
		printf("gamestate: %d\n", ((game*) temp->data)->gamestate);
		printf("p1: %s\n", ((game*) temp->data)->p1 ? ((game*) temp->data)->p1->username : "NULL");
		printf("p2: %s\n", ((game*) temp->data)->p2 ? ((game*) temp->data)->p2->username : "NULL");
		temp = temp->next;
	}
}

void print_quick() {
	if(quick_g) {
		printf("gamestate: %d\n", quick_g->gamestate);
		printf("p1: %s\n", quick_g->p1 ? quick_g->p1->username : "NULL");
		printf("p2: %s\n", quick_g->p2 ? quick_g->p2->username : "NULL");
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


int verify_and_load_player(int player_id, int fd, player** p, instruction inst){
	if(((*p) = find_player_by_fd(p_list, fd))) {
		if(verify_player(*p, player_id))
			return 0;
		else
			return 1;
	} else {
		if(inst == CONNECT && player_id == 0)
			return 0;
		else
			return 1;
	}
}

game* find_game_by_player(l_link* head, player* p) {
	while(head) {
		if(((game*) head->data)->p1 == p ||
		   ((game*) head->data)->p2 == p) {
			return (game*) head->data;
		}
	}

	return NULL;
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

	if(!p_list) {
		printf("Oh this shit is it\n");
	}

	return temp;
}


/**
	Returns instruction from string parameter. Support function
	to message handling.
*/
instruction parse_instruction(char* str) {
	instruction inst;
	if(!strcmp(str, "QUICK_PLAY")) {
		inst = QUICK_PLAY;
	} else if(!strcmp(str, "LOBBY")) {
		inst = LOBBY;
	} else if(!strcmp(str, "CANCEL_QUICK")) {
		inst = CANCEL_QUICK;
	} else if(!strcmp(str, "DELETE_LOBBY")) {
		inst = DELETE_LOBBY;
	} else if(!strcmp(str, "CREATE_LOBBY")) {
		inst = CREATE_LOBBY;
	} else if(!strcmp(str, "PLAY_GAME")) {
		inst = PLAY_GAME;
	} else if(!strcmp(str, "PAUSE")) {
		inst = PAUSE_INST;
	} else if(!strcmp(str, "RESUME")) {
		inst = RESUME;
	} else if(!strcmp(str, "CONNECT")) {
		inst = CONNECT;
	} else if(!strcmp(str, "TURN")) {
		inst = TURN;
	} else if(!strcmp(str, "PING")) {
		inst = PING;
	} else{
		inst = INST_ERROR;
	}

	return inst;
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
char* connect(int* player_id, char* username, int fd) {
//TODO: do some id/fd/username check if it doesn't exist
	player *p = NULL;

	if(!username) {
		printf("username is NULL??\n");
		return construct_message_long(*player_id, 402,
					      "Username is empty");
		//return 402;
	}

	if(!is_username_available(p_list, username)) {
		//printf("Username already taken, RV: %d\n", rv);
		return construct_message_long(*player_id, 403,
					      "Username already in use");

		//return 403;
	}


	if(*player_id == 0) {
		//new player connecting

		p = add_player(fd);
	
		if(!p) {
			//TODO think of error code
			return construct_message_long(*player_id, 410,
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
		return construct_message_long(*player_id, 201,
					      "Connection success");
		//return 201;
	} else {
		//reconnection/ attack
		p = find_and_verify_player(*player_id, fd);

		if(!p) {
			printf("Player with ID %d doesn't exist!\n", *player_id);
			return construct_message_long(*player_id, 410,
					      "Player with this ID doesn't exist");
			//return 400;
		}
		
		//if player exists but isn't connect = reconnecting
		if(p->id == *player_id && p->connected == 0) {
			printf("Player id %d reconnecting\n", *player_id);
			return construct_message_long(*player_id, 202,
					      "Reconnection success");
			//return 202;
		} else {
			printf("Player is either already connected or IDs don't match\n");
			return construct_message_long(*player_id, 410,
					      "Is this an attack attempt");
			//return 400;
		}
	}
}


/**
	Adds player to an available game or creates a new game for the player
	waiting for an opponent
*/
char* quick_play(int player_id,int fd) {
	player *p = NULL;

	p = find_and_verify_player(player_id, fd);

	if(!p) {
		printf("Player is NULL, can't play"); //TODO log
		return construct_message_long(player_id, 401,
					      "Failed to find and verify player");
		//return 400;
	}
	//TODO: check if player isn't alrady in game!

	if(quick_g) {
		if(get_gamestate(quick_g) == WAITING) {
			printf("Joining an existing game!\n");

			if(add_player_to_game(quick_g, p)) {
				printf("Two players already exist in game!");
				printf("Game is in wrong state\n");

				//return construct_message_long(player_id, 402,
				return construct_message_long(p->id, 402,
					      "Game already full");
				//return 400;
			}

			change_game_state(quick_g, START_GAME);

			//movig existing game from g_waiting to g_playing, using
			//new variable as it isn't used in this branch
			//new = (game*) pop_link(&g_waiting)->data;
			add_lifo(&g_playing, quick_g);
			quick_g = NULL;
		} else {
			//maybe TODO some state error checking? of existing game

			//return construct_message_long(player_id, 403,
			return construct_message_long(p->id, 403,
					      "Existing quick game in wrong state");

			/*
			printf("Creating a new game!\n");

			new = init_new_game();
			if(!new) {
				printf("Failed to init new game\n");

				return construct_message_long(player_id, 400,
					      "Server failed to start new game");
				//return 400;
			}

			add_player_to_game(new, p);
			add_fifo(&g_waiting, new);
			*/
		}
	} else {
		printf("Creating new game\n");

		quick_g = init_new_game();
		if(!quick_g) {
			printf("Failed to init new game\n");

			//return construct_message_long(player_id, 404,
			return construct_message_long(p->id, 404,
					      "Server failed to start quick game");
			//return 400;
		}

		add_player_to_game(quick_g, p);
	}
	
	//return construct_message_long(player_id, 201,
	return construct_message_long(p->id, 201,
				      "QPlay success");
	//return 200;
}


char* lobby(int player_id, int fd) {
	player* p = NULL;
	char* msg = NULL;
	l_link* temp = g_lobby;

	p = find_and_verify_player(player_id, fd);

	if(!p) {
		printf("Failed to find and verify player\n"); //TODO log
		
		return construct_message_long(player_id, 401,
					      "Failed to find and verify player");
		//return 400;

	}

	msg = init_message_with_id(player_id);
	append_instruction(msg, "GAMES");
	
	while(temp) {
		if(append_parameter(&msg,
		   ((game*) temp->data)->p1->username)) {
			return construct_message_long(player_id, 402,
						      "Failed to fetch game");
		}
		
		temp = temp->next;
	}

	return msg;
	//return 200;
}


char* create_lobby(int player_id, int fd) {
	player* p = NULL;
	game* g = NULL;

	p = find_and_verify_player(player_id, fd);

	if(!p) {
		printf("Failed to find and verify player\n"); //TODO log
		
		return construct_message_long(player_id, 401,
					      "Failed to find and verify player");
		//return 400;

	}

	g = init_new_game();
	if(!g) {
		printf("Failed to init new game\n");

		return construct_message_long(player_id, 404,
				      "Server failed to start quick game");
	}

	p->busy = 1;

	add_player_to_game(g, p);
	add_lifo(&g_lobby, g);
	
	return construct_message_long(player_id, 201,
				      "Successfully created lobby");
}


char* cancel_quick(int player_id, int fd) {
	player* p = NULL;

	p = find_and_verify_player(player_id, fd);

	if(!p) {
		printf("Failed to find and verify player\n"); //TODO log
		
		return construct_message_long(player_id, 401,
					      "Failed to find and verify player");

	}

	if(quick_g->p1 != p) {
		printf("Player mismatch on game cancel %p != %p", quick_g->p1, p);
		return construct_message_long(player_id, 402,
					      "You aren't queued for quick game");
	}

	p->busy = 0;
	free(quick_g);

	return construct_message_long(player_id, 201,
				      "Game canceled");
}

char* delete_lobby(int player_id, int fd) {
	l_link *prev, *curr;
	player* p = NULL;
	game* g = NULL;

	p = find_and_verify_player(player_id, fd);

	if(!p) {
		printf("Failed to find and verify player\n"); //TODO log
		
		return construct_message_long(player_id, 401,
					      "Failed to find and verify player");
	}

	//prev = g_playing;
	curr = g_playing;

	//TODO: check if g_playing isn't null?

	while(curr) {
		if(((game*) curr->data)->p1 == p) {
			g = (game*) curr->data;
			break;
		}

		prev = curr;
		curr = curr->next;
	}

	if(prev) {
		prev->next = curr->next;
	} else {
		curr = curr->next;
	}

	free(curr->data);
	free(curr);

	p->busy = 0;
	/*
	g = find_game_by_player(g_lobby, p);

	if(!g) {
		printf("Failed to find game\n"); //TODO log
		
		return construct_message_long(player_id, 402,
					      "Failed to find game lobby");
	}

	p->busy = 0;
	//free(g);
	*/

	return construct_message_long(player_id, 201,
				      "Lobby deleted");
}


char* pause_game(int player_id, int fd) {
	player* p = NULL;
	game* g = NULL;

	p = find_and_verify_player(player_id, fd);

	if(!p) {
		printf("Failed to find and verify player\n"); //TODO log
		
		return construct_message_long(player_id, 401,
					      "Failed to find and verify player");
	}

	g = find_game_by_player(g_playing, p);

	if(!g) {
		printf("Failed to find game\n"); //TODO log
		
		return construct_message_long(player_id, 402,
					      "Failed to find game lobby");
	}

	change_game_state(g, PAUSE_GAME);
	
	return construct_message_long(player_id, 201,
				      "Game paused");
}


char* resume_game(int player_id, int fd) {
	player* p = NULL;
	game* g = NULL;

	p = find_and_verify_player(player_id, fd);

	if(!p) {
		printf("Failed to find and verify player\n"); //TODO log
		
		return construct_message_long(player_id, 401,
					      "Failed to find and verify player");
	}

	g = find_game_by_player(g_playing, p);

	if(!g) {
		printf("Failed to find game\n"); //TODO log
		
		return construct_message_long(player_id, 402,
					      "Failed to find game lobby");
	}

	change_game_state(g, RESUME_GAME);
	
	return construct_message_long(player_id, 201,
				      "Game resumed");
}


char* join_game(int player_id, int fd, char* host_uname) {
	player* p = NULL;
	game* g = NULL;

	p = find_and_verify_player(player_id, fd);

	if(!p) {
		printf("Failed to find and verify player\n"); //TODO log
		
		return construct_message_long(player_id, 401,
					      "Failed to find and verify player");
	}



	return construct_message_long(player_id, 201,
				      "Successfully joined game");
}

char* turn() {
	return construct_message_long(0, 400, "BAF");
}


/*****************************************************************************/
/*									     */
/*	Core functions							     */
/*									     */
/*****************************************************************************/


/**
	Parses recieved message, calls appropriate functions and returns
	constructed response
*/
char* handle_message(char *message, int fd) {
	int token_cnt = 0, inst = -1, params = -1, parsed_id = -1;
	player* p = NULL;
	char *token, *reply;
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

	parsed_id = strtol(parts[0], NULL, 10);

	if(token_cnt < 2 || token_cnt > 32) {
		printf("Instructions got 2 to 32 token count\n");
		return construct_message_long(parsed_id, 400,
					      "Instruction argument miscount");
		//return general_negative_resposne(parsed_id, PARSE_ERROR);
		//TODO strike?
		//return 400;
	}
	
	inst = parse_instruction(parts[1]);
	
	if(inst == INST_ERROR) {
		printf("Unrecognized instruction!");
		return construct_message_long(parsed_id, 400,
					      "Unknown instruction");
		//return general_negative_resposne(parsed_id, UNKNW_INST);

	}
	
	switch(inst) {
		case QUICK_PLAY: case PAUSE_INST: case RESUME: case LOBBY:
		case PING: case CREATE_LOBBY: case CANCEL_QUICK:
		case DELETE_LOBBY: params = 0; break;
		case CONNECT: case PLAY_GAME: params = 1; break;
		case TURN: params = 30; break;
	}

	if((params + 2) != token_cnt) {
		printf("Parameter count mismatch!\n");
		return construct_message_long(parsed_id, 400,
					      "Parameter count mismatch");
		//return general_negative_resposne(parsed_id, ARG_CNT_ERROR);
		//return 400;
	}

	if(verify_and_load_player(parsed_id, fd, &p, inst)) {
		printf("Player verification failed\n");
		return construct_message_long(parsed_id, 400,
					      "Player verification failed");

	}

	switch(inst) {
		case QUICK_PLAY: reply = quick_play(parsed_id, fd); break; //TODO: start game
		case LOBBY: reply = lobby(parsed_id, fd); break;
		case CREATE_LOBBY: reply = create_lobby(parsed_id, fd); break;
		case PLAY_GAME: reply = join_game(parsed_id, fd, parts[2]);
				break;
		case CANCEL_QUICK: reply = cancel_quick(parsed_id, fd); break;
		case DELETE_LOBBY: reply = delete_lobby(parsed_id, fd); break;
		case PAUSE_INST: reply = pause_game(parsed_id, fd); break;
		case RESUME: reply = resume_game(parsed_id, fd); break;
		case CONNECT: reply = connect(&parsed_id, parts[2], fd); break;
		case TURN: reply = turn(); //validate(); update_board(); break;
		case PING: break; //ping(); break;
	}


	printf("\n------------------------------\n");
	printf("PRINTING PLAYER LIST\n");
	print_plist();
	printf("\nPRINTING QUICK GAME\n");
	print_quick();
	printf("\nPRINTING LOBBY\n");
	print_lobby();
	printf("\nPRINTING PLAYING\n");
	print_playing();
	printf("\n==============================\n");


	if(reply) {
		return reply;
	} else {
		return construct_message_long(parsed_id, 400,
					      "Failed to construct reply");
	}

	//return construct_response(parsed_id, inst, rv);
}
