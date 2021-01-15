#ifndef _CONTROLER_H
#define _CONTROLER_H

#include "message_builder.h"
#include "../general_functions.h"
#include "../app/game.h"
#include "../app/player.h"

#include <sys/socket.h>

#define INSTRUCTION_COUNT 14
#define RESPONSE_SIZE 256

/**
	Message Instruction set
*/
typedef enum {
	INST_ERROR	= -1,
	CONNECT 	= 0,
	LOBBY		= 2,
	CREATE_LOBBY	= 3,
	JOIN_GAME 	= 4,
	TURN 		= 5,
	PING 		= 8,
	DISCONNECT	= 10,
	DELETE_LOBBY	= 11,
	OPPONENT_JOIN	= 12,
	OPPONENT_TURN	= 13
} instruction;

/**
	For function doc see .c file
*/

int add_connection(int fd);
char* handle_message(char *message, int fd);
char* construct_response(int user_id, instruction inst, int code);
void free_controller();

#endif
