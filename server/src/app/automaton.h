#ifndef _AUTOMATON_H
#define _AUTOMATON_H

#define STATE_COUNT 4
#define ACTION_COUNT 4


typedef enum {
	C_CONNECTED 	= 0,
	C_IN_LOBBY 	= 1,
	C_IN_GAME 	= 2,
	C_NOT_POSSIBLE 	= 3
} state;

typedef enum {
	A_CREATE_L 	= 0,
	A_JOIN_L 	= 1,
	A_LEAVE_L 	= 2,
	A_END_GAME 	= 3
} action;

typedef struct {
	state state;
} automaton;


int verify_transition(automaton* at, action act);
void make_transition(automaton* at, action act); 

#endif
