#ifndef _PLAYER_H
#define _PLAYER_H

#include "../general_functions.h"

#include <string.h>
#include <time.h>

/**
	player structure:
	contains: id, socket, username and statuses (connected, busy, onTop, strikes)
*/
typedef struct {
	int socket;
	int id;
	int strikes;
	int connected;
	time_t last_com;
	int busy;
	int on_top;
	char *username;
} player;

/**
	For function documentation see .c file
*/

int add_player_to_list(l_link *head, player *p);
player* find_player_in_list(l_link *head, int id);
player* find_player_by_fd(l_link *head, int fd);
int delete_player_with_id(l_link **head, int id);
int verify_player(player* p, int user_id);
int is_username_available(l_link *head, char *username);
player* init_player(int fd);
void free_player(player* p);

#endif
