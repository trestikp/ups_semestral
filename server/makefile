CC = gcc
PARAMS = -Wall -g

all: clean run remove_o

logger:
	${CC} ${PARAMS} -c src/logger.c

server:
	#${CC} ${PARAMS} -c src/network/{server,controller,message_builder}.c
	${CC} ${PARAMS} -c src/network/server.c
	${CC} ${PARAMS} -c src/network/controller.c
	${CC} ${PARAMS} -c src/network/message_builder.c

gf:
	${CC} ${PARAMS} -c src/general_functions.c

app:
	#${CC} ${PARAMS} -c src/app/{game,player,automaton}.c
	${CC} ${PARAMS} -c src/app/game.c
	${CC} ${PARAMS} -c src/app/player.c
	${CC} ${PARAMS} -c src/app/automaton.c

remove_o:
	rm -f *.o

clean:
	rm -f run_server
	rm -f server_log.log

run: logger server gf app
	${CC} ${PARAMS} -o run_server src/main.c logger.o server.o\
 general_functions.o game.o player.o controller.o automaton.o\
 message_builder.o
