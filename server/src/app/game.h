#ifndef _GAME_H
#define _GAME_H

#include "automaton.h"
#include "player.h"

//is included in player, so this isn't necessary 
//#include <string.h>

typedef struct {
	player *p1;
	player *p2;
	player *onTurn;
	int gameboard[64];
	state gamestate;
	char gamename[128];
} game;

void init_gameboard(game *g);
game* init_new_game();
int add_player_to_game(game* g, player* p);
void change_game_state(game* g, action a);
state get_gamestate(game* g);
game* find_game_by_player(l_link* head, player* p);
game* extract_game_by_name(l_link** head, char* lobby_name);
int validate_move(int from, int to, player* p, game* g);
void make_move(int from, int to, player* p, game* g);
void free_game(game* g);


void print_gameboard(game* g);

#endif
