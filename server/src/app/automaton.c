#include "automaton.h"

state transitions[STATE_COUNT][ACTION_COUNT] = {
	[C_CONNECTED][A_CREATE_L] 	= C_IN_LOBBY,
	[C_CONNECTED][A_JOIN_L] 	= C_IN_GAME,
	[C_CONNECTED][A_LEAVE_L] 	= C_NOT_POSSIBLE,
	[C_CONNECTED][A_END_GAME] 	= C_NOT_POSSIBLE,

	[C_IN_LOBBY][A_CREATE_L] 	= C_NOT_POSSIBLE,
	[C_IN_LOBBY][A_JOIN_L] 		= C_IN_GAME,
	[C_IN_LOBBY][A_LEAVE_L] 	= C_CONNECTED,
	[C_IN_LOBBY][A_END_GAME] 	= C_NOT_POSSIBLE,

	[C_IN_GAME][A_CREATE_L] 	= C_NOT_POSSIBLE,
	[C_IN_GAME][A_JOIN_L] 		= C_NOT_POSSIBLE,
	[C_IN_GAME][A_LEAVE_L] 		= C_NOT_POSSIBLE,
	[C_IN_GAME][A_END_GAME] 	= C_CONNECTED
};

int verify_transition(automaton* at, action act) {
	if(transitions[at->state][act] == C_NOT_POSSIBLE) return 1;
	return 0;
}

void make_transition(automaton* at, action act) {
	at->state = transitions[at->state][act];
}

void set_state(automaton* at, state s) {
	at->state = s;
}
