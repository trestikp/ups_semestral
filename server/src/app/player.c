#include "player.h"
#include <string.h>


int add_player_to_list(l_link *head, player *p) {
	add_lifo(&head, p);
	
	return 0;
}

player* find_player_in_list(l_link *head, int id) {
	while(head) {
		if(id == ((player*) head->data)->id) {
			return ((player*) head->data);
		}

		head = head->next;
	}

	return NULL;
}


player* find_player_by_fd(l_link *head, int fd) {
	while(head) {
		if(fd == ((player*) head->data)->socket) {
			return ((player*) head->data);
		}

		head = head->next;
	}
	
	return NULL;
}

/*
  Basic check, if the id from request matches the id of player on server.
*/
int verify_player(player* p, int user_id) {
	if(p->id == user_id) {
		return 1;
	} else {
		return 0;
	}
}

int is_username_available(l_link *head, char *username) {
	while(head) {
		if(!strcmp(((player*) head->data)->username, username)) {
			return 0;	
		}

		head = head->next;
	}

	return 1;
}

player* init_player(int fd) {
        player *new = malloc(sizeof(player));
                
        if(!new) return NULL;
                
        new->socket = fd;
        new->id = 0;
        new->strikes = 0;
	new->busy = 0;
	new->onTop = 0;
        new->username = NULL;
        
        return new;	
}

void free_player(player* p) {
	if(p->username) free(p->username);
	free(p);
}
