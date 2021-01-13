#ifndef _PLAYER_H
#define _PLAYER_H

#include "../general_functions.h"

typedef struct {
	int socket;
	int id;
	int strikes;
	int connected;
	int busy;
	int onTop;
	char *username; //?
} player;

int add_player_to_list(l_link *head, player *p);
player* find_player_in_list(l_link *head, int id);
player* find_player_by_fd(l_link *head, int fd);
int remove_player_from_list();
int verify_player(player* p, int user_id);
int is_username_available(l_link *head, char *username);
player* init_player(int fd);
void free_player(player* p);

#endif
