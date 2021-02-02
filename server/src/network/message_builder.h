#ifndef _MESSAGE_BUILDER_H
#define _MESSAGE_BUILDER_H

#include <string.h>
#include <stdlib.h>
#include <stdio.h>

#define DEFAULT_SIZE 1024


/**
	For function doc see .c file
*/

//char* construct_message_short(int player_id, int code);
char* construct_message(int player_id, int code, char* message);
char* construct_message_with_inst(int player_id, char* inst, int code, char* message);
int append_parameter(char** message, char* par);

#endif
