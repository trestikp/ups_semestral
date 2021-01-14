#include "game.h"
#include "automaton.h"


/**
	Wow, this is stupid...
*/
void init_gameboard(game *g) {
	int i = 0;

	int temp[64] = {
		//-1 represents "WHITE" field (not possible), 0 available field ("BLACK")
                // 1 "player" stone, 3 "player" king, 2 "enemy" stone, 4 "enemy king
              // 0    1    2    3    4    5    6    7
                -1 ,  2 , -1 ,  2 , -1 ,  2 , -1 ,  2, //0
                 2 , -1 ,  2 , -1 ,  2 , -1 ,  2 , -1, //1
                -1 ,  2 , -1 ,  2 , -1 ,  2 , -1 ,  2, //2
                 0 , -1 ,  0 , -1 ,  0 , -1 ,  0 , -1, //3
                -1 ,  0 , -1 ,  0 , -1 ,  0 , -1 ,  0, //4
                 1 , -1 ,  1 , -1 ,  1 , -1 ,  1 , -1, //5
                -1 ,  1 , -1 ,  1 , -1 ,  1 , -1 ,  1, //6
                 1 , -1 ,  1 , -1 ,  1 , -1 ,  1 , -1, //7
	};

	for(i = 0; i < 64; i++) {
		g->gameboard[i] = temp[i];
	}
}

game* init_new_game() {
	game* new = malloc(sizeof(game));

	if(!new) return NULL;

	init_gameboard(new);
	bzero(new->gamename, 128);
	new->gamestate = WAITING;
	new->p1 = NULL;
	new->p2 = NULL;

	return new;
}

int add_player_to_game(game* g, player* p) {
	if(g->p1) {
		if(g->p2) {
			return 1;
		} else {
			g->p2 = p;
		}
	} else {
		g->p1 = p;
	}
	
	return 0;
}

void change_game_state(game* g, action a) {
	g->gamestate = make_transition(g->gamestate, a);
}

state get_gamestate(game* g) {
	return g->gamestate;
}

game* find_game_by_player(l_link* head, player* p) {
        while(head) {
                if(((game*) head->data)->p1 == p ||
                   ((game*) head->data)->p2 == p) {
                        return (game*) head->data;
                }

		head = head->next;
        }

        return NULL;
}

game* extract_game_by_name(l_link** head, char* lobby_name) { 
	game* out = NULL;
	l_link *prev = NULL, *curr = NULL;

	curr = *head;

	while(curr) {
		if(!strcmp(((game*) curr->data)->gamename, lobby_name)) {
                        out = (game*) curr->data;
			break;
		}

		prev = curr;
		curr = curr->next;
        }

	if(prev) {
		prev->next = curr->next;
	} else {
		*head = curr->next;
	}

	free(curr);

        return out;
}

void print_gameboard(game* g);


int validate_move(int from, int to, player* p, game* g) {
	int directionLeft;
	int directionRight;

	//board has 64 field, check of index out of bounds
	if(to > 63 || to < 0 || from > 63 || from < 0) {
		return 1;
	}

	if(p->onTop) {
		//inverze indexes for player on top
		from = 63 - from;
		to = 63 - to;

		directionLeft = +7;
		directionRight = +9;
	} else {
		directionLeft = -7;
		directionRight = -9;

	}

	//check if the piece moves in correct direction and correct amount of fields
	if((to - from) != directionLeft && (to - from) != directionRight &&
	   (to - from) != (2 * directionLeft) && (to - from) != (2 * directionRight)) {
		return 1;
	}

	//the piece moving isn't king
	if(g->gameboard[from] != 4 || g->gameboard[from] != 3) {
		//piece from top can only move downwards (index increases)
		//likewise piece from bot only moves upwards (index decreses)
		if(p->onTop) {
			if(to < from) return 1;
		} else {
			if(to > from) return 1;
		}

		//if target location isn't empty
		if(g->gameboard[to] != 0) return 1;

		//if jumping over stone
		if((to - from) == (2 * directionLeft))  {
			//player on top has pieces 2, 4
			if(p->onTop) {
				//check if jumping over enemy stone (1,3)
				if((g->gameboard[from + directionLeft] % 2) != 1) {
					return 1;
				}
			} else { //player on bot 1, 3
				//check if jumping over enemy stone (2, 4)
				if((g->gameboard[from + directionLeft] % 2) != 0) {
					return 1;
				}

			}
		} else if((to - from) == (2 * directionRight)) {
			//player on top has pieces 2, 4
			if(p->onTop) {
				//check if jumping over enemy stone (1,3)
				if((g->gameboard[from + directionRight] % 2) != 1) {
					return 1;
				}
			} else { //player on bot 1, 3
				//check if jumping over enemy stone (2, 4)
				if((g->gameboard[from + directionRight] % 2) != 0) {
					return 1;
				}

			}

		}
	//piece is king, can move in all directions
	} else {
		//if jumping over stone
		if((to - from) == (2 * directionLeft) || (to - from) == (2 * directionRight))  {
			//player on top has pieces 2, 4
			if(p->onTop) {
				//check if jumping over enemy stone (1,3)
				if((g->gameboard[from + directionLeft] % 2) != 1) {
					return 1;
				}
			} else { //player on bot 1, 3
				//check if jumping over enemy stone (2, 4)
				if((g->gameboard[from + directionLeft] % 2) != 0) {
					return 1;
				}

			}
		}	
	}
	

	return 0;
}

void print_gameboard(game* g) {
	int i = 0;

	for(i = 0; i < 64; i++) {
		printf("%d  ", g->gameboard[i]);
		if((i % 8) == 7) printf("\n");
	}
}

void make_move(int from, int to, player* p, game* g) {
	if(p->onTop) {
		g->gameboard[63 - to] = g->gameboard[63 - from];

		if((to - from) > 9 || (to - from) < -9) {
			g->gameboard[63 - (from + (to - from) / 2)] = 0;
		}

		g->gameboard[63 - from] = 0;
	} else {
		g->gameboard[to] = g->gameboard[from];

		if((to - from) > 9 || (to - from) < -9) {
			g->gameboard[from + (to - from) / 2] = 0;
		}

		g->gameboard[from] = 0;
	}
}

void free_game(game* g) {
	free(g);
}
