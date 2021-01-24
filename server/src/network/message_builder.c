#include "message_builder.h"


/**
	Appends parameter @par to message @message. Default message size is 1kB
	if @par exceeds 1kB resizes original message, adding additional 1kB
*/
int append_parameter(char** message, char* par) {
	int msg_len = strlen(*message);
	int par_len = strlen(par);

	// +1 for zero
	if((msg_len + par_len + 1) > DEFAULT_SIZE) {
		char* temp = realloc(*message, msg_len + DEFAULT_SIZE);

		if(!temp) {
			printf("Failed to extend message size to append parameter\n");
			return 1;
		} else {
			*message = temp;
		}
	}

	if((*message)[msg_len - 1] == '\n') {
		sprintf((*message) + (msg_len - 1) * sizeof(char), "|%s\n", par);
	} else {
		sprintf((*message) + (msg_len) * sizeof(char), "|%s\n", par);
	}
	
	memset(*message + ((msg_len + par_len + 1) * sizeof(char)), 0, 1);

	return 0;
}

/**
	Initializes message with @player_id. Return message pointer or NULL on fail.
*/
char* init_message_with_id(int player_id) {
	char* message = calloc(DEFAULT_SIZE, sizeof(char));
	
	if(!message) return NULL;

	sprintf(message, "%d", player_id);
	
	return message;
}

/**
	Append instruction @instruction to message @message
*/
void append_instruction(char* message, char* instruction) {
	sprintf(message + strlen(message) * sizeof(char), "|%s", instruction);
}

/**
	Append result instruction determined by @code to message @message
*/
void append_result(char* message, int code) {
	if(code >= 200 && code < 300) {
		append_instruction(message, "OK");
        } else if(code >= 400) {
		append_instruction(message, "ERROR");
        }
}

/**
	Append number @code to message @message
*/
void append_code(char* message, int code) {
	sprintf(message + strlen(message) * sizeof(char), "|%d", code);
}

/**
	Append message @msg to message @message
*/
void append_message(char* message, char* msg) {
	sprintf(message + strlen(message) * sizeof(char), "|%s\n", msg);
}

/**
	Constructs message with id @player_id, return code @code and message @message
	Determines instruction with @code.
	Return message pointer or NULL
*/
char* construct_message(int player_id, int code, char* message) {
	char* msg = init_message_with_id(player_id);

	if(!msg) return NULL;

        append_result(msg, code);
	append_code(msg, code);
	append_message(msg, message);

	return msg;
}


/**
	Constructs message with id @player_id, return code @code, instruction @inst, 
	message @message.
	Returns message pointer or NULL
*/
char* construct_message_with_inst(int player_id, char* inst, int code, char* message) {
	char* msg = init_message_with_id(player_id);

	if(!msg) return NULL;

	append_instruction(msg, inst);
	append_code(msg, code);
	append_message(msg, message);

	return msg;

}
