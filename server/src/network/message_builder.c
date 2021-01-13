#include "message_builder.h"


int append_parameter(char** message, char* par) {
	int msg_len = strlen(*message);
	int par_len = strlen(par);

	// +1 for zero
	if((msg_len + par_len + 1) > DEFAULT_SIZE) {
		char* temp = realloc(*message, msg_len + DEFAULT_SIZE);

		if(!temp) {
			printf("Failed to extend message size to append ");
			printf("parameter\n");
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

char* init_message_with_id(int player_id) {
	char* message = calloc(DEFAULT_SIZE, sizeof(char));
	
	if(!message) return NULL;

	sprintf(message, "%d", player_id);
	
	return message;
}


void append_instruction(char* message, char* instruction) {
	sprintf(message + strlen(message) * sizeof(char), "|%s", instruction);
}


void append_result(char* message, int code) {
	if(code >= 200 && code < 300) {
		append_instruction(message, "OK");
        } else if(code >= 400) {
		append_instruction(message, "ERROR");
        }
}


void append_code(char* message, int code) {
	sprintf(message + strlen(message) * sizeof(char), "|%d", code);
}


void append_message(char* message, char* msg) {
	sprintf(message + strlen(message) * sizeof(char), "|%s\n", msg);
}

/*
char* construct_message_short(int player_id, int code) {
	char* msg = init_message_with_id(player_id);

	if(!msg) return NULL;

        append_result(msg, code);
	append_code(msg, code);

	return msg;
}
*/

char* construct_message(int player_id, int code, char* message) {
	char* msg = init_message_with_id(player_id);

	if(!msg) return NULL;

        append_result(msg, code);
	append_code(msg, code);
	append_message(msg, message);

	return msg;
}


char* construct_message_with_inst(int player_id, char* inst, int code, char* message) {
	char* msg = init_message_with_id(player_id);

	if(!msg) return NULL;

	append_instruction(msg, inst);
	append_code(msg, code);
	append_message(msg, message);

	return msg;

}
