#include "player.h"
#include "automaton.h"


/**
	Adds player @p to list @head
*/
int add_player_to_list(l_link *head, player *p) {
	add_lifo(&head, p);
	
	return 0;
}

/**
	Find player with @id in list @head. Return NULL if it fails to find player
*/ player* find_player_in_list(l_link *head, int id) {
	while(head) {
		if(id == ((player*) head->data)->id) {
			return ((player*) head->data);
		}

		head = head->next;
	}

	return NULL;
}

/**
	Finds player in list @head with socket @fd. Returns NULL if it fails to find player
*/
player* find_player_by_fd(l_link *head, int fd) {
	while(head) {
		if(fd == ((player*) head->data)->socket) {
			return ((player*) head->data);
		}

		head = head->next;
	}
	
	return NULL;
}

/**
	Basic check to see if player id @p->id in server memory matches parse id @user_id
*/
int verify_player(player* p, int user_id) {
	if(p->id == user_id) {
		return 1;
	} else {
		return 0;
	}
}


/**
	Checks if username is available. Return 0 if it is NOT.
*/
int is_username_available(l_link *head, char *username) {
	while(head) {
		if(!strcmp(((player*) head->data)->username, username)) {
			return 0;	
		}

		head = head->next;
	}

	return 1;
}

int delete_player_with_id(l_link **head, int id) {
	int found = 1;
	l_link *prev = NULL;
	l_link *temp = *head;

	if(!temp) return 1;

	while(temp) {
		if(((player*) temp->data)->id == id) {
			free_player((player*) temp->data);
			found = 0;
			break;
		}

		prev = temp;
		temp = temp->next;
	}

	if(found) { //failed to find player in list
		return 1;
	}

	if(prev) {
		prev->next = temp->next;
	} else {
		*head = (*head)->next;		
	}

	free(temp);

	return 0;
}

/**
	Allocates player structure memory and initializes it with default values.
*/
player* init_player(int fd) {
        player *new = calloc(1, sizeof(player));
                
        if(!new) return NULL;
                
        new->socket = fd;
        new->id = 0;
        new->strikes = 0;
	//new->busy = 0;
	new->on_top = 0;
        new->username = NULL;
	new->last_com = time(NULL);
	new->at.state = C_CONNECTED;
        
        return new;	
}

/**
	Free player memory
*/
void free_player(player* p) {
	if(p->username) free(p->username);
	free(p);
}
