CC = gcc
PARAMS = -Wall -g

all: clean run remove_o

logger:
	${CC} ${PARAMS} -c src/logger.c

server:
	${CC} ${PARAMS} -c src/network/{server,controler2,message_builder}.c

gf:
	${CC} ${PARAMS} -c src/general_functions.c

app:
	${CC} ${PARAMS} -c src/app/{automaton,game,player,validator}.c

remove_o:
	rm -f *.o

clean:
	rm -f run_server
	rm -f server_log.log

run: logger server gf app
	${CC} ${PARAMS} -o run_server src/main.c logger.o server.o\
 general_functions.o automaton.o game.o player.o validator.o controler2.o\
 message_builder.o
