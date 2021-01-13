#ifndef _AUTOMATON_H
#define _AUTOMATON_H

#define STATE_COUNT 5
#define ACTION_COUNT 5

typedef enum {
	INIT = 0,
	WAITING = 1,
	WORKING = 2,
	PAUSE = 3,
	ERROR = 4
} state;

typedef enum {
	JOIN_QUEUE = 0,
	START_GAME = 1,
	PAUSE_GAME = 2,
	RESUME_GAME = 3,
	END_GAME = 4
} action;


state make_transition(state current, action act);

#endif
