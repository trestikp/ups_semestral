#ifndef _GAME_H
#define _GAME_H

#include "player.h"


/** 
	game structure:

*/
typedef struct {
	player *p1;
	player *p2;
	player *on_turn;
	int gameboard[64];
	char gamename[64];
} game;

/**
	For function documentation see .c file
*/

//void init_gameboard(game *g);
game* init_new_game();
int add_player_to_game(game* g, player* p);

void make_move(int from, int to, player* p, game* g);
int validate_move(int from, int to, player* p, game* g, int subsequent);

game* find_game_by_player(l_link* head, player* p);
game* extract_game_by_name(l_link** head, char* lobby_name);

player* check_for_victory(game* g);
player* get_your_opponent(game* g, player* p);

int delete_game_from_list(l_link **head, game* g);

void free_game(game* g);


void print_gameboard(game* g);

#endif
