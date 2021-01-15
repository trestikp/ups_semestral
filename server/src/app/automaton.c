#include "automaton.h"

/** transition matrix initialization */
state transition[STATE_COUNT][ACTION_COUNT] = {
	[INIT][JOIN_QUEUE]	= WAITING,
	[INIT][START_GAME]	= ERROR,
	[INIT][PAUSE_GAME] 	= ERROR,
	[INIT][RESUME_GAME] 	= ERROR,
	[INIT][END_GAME] 	= ERROR,

	[WAITING][JOIN_QUEUE] 	= ERROR,
	[WAITING][START_GAME] 	= WORKING,
	[WAITING][PAUSE_GAME] 	= ERROR,
	[WAITING][RESUME_GAME] 	= ERROR,
	[WAITING][END_GAME] 	= INIT,

	[WORKING][JOIN_QUEUE] 	= ERROR,
	[WORKING][START_GAME] 	= ERROR,
	[WORKING][PAUSE_GAME] 	= PAUSE,
	[WORKING][RESUME_GAME] 	= ERROR,
	[WORKING][END_GAME] 	= INIT,

	[PAUSE][JOIN_QUEUE] 	= ERROR,
	[PAUSE][START_GAME] 	= ERROR,
	[PAUSE][PAUSE_GAME] 	= ERROR,
	[PAUSE][RESUME_GAME] 	= WORKING,
	[PAUSE][END_GAME] 	= ERROR,

	[ERROR][JOIN_QUEUE] 	= ERROR,
	[ERROR][START_GAME] 	= ERROR,
	[ERROR][PAUSE_GAME] 	= ERROR,
	[ERROR][RESUME_GAME] 	= ERROR,
	[ERROR][END_GAME] 	= ERROR,
};

/** transition states from @current with @act */
state make_transition(state current, action act) {
	return transition[current][act];
}
