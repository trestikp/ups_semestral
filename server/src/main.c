#include "logger.h"
#include "network/server.h"

#include <string.h>
#include <signal.h>
#include <limits.h>


/** Server ip  */
char* ip = NULL;
/** Server port */
int port = -1;
/** Maximum number of connection */
int max_con = -1;

/**
	Prints help for the program
*/
void print_help() {
	printf("Usage:\n\trun_server [options]\n");
	printf("OPTIONS:\n");
	printf("-h\n\tPrints this help.\n");
	printf("-a IP\n\tServer uses IP as its server adress.\n");
	printf("-p PORT\n\tServer uses PORT as its port.\n");
	printf("-c NUM\n\tServer allows a maximum of NUM connections.\n");
}

int parse_string_to_int_main(char* str) {
	int res;
	char* leftover;

	res = strtol(str, &leftover, 10);

	if(leftover == str || res < 0 || *leftover != '\0') {
		printf("Failed to parse ID\n");
		return INT_MIN;
	}

	return res;
}

/**
	Parses command line argument
*/
int process_argument(char* sw, char* val) {
	if(val[0] == '-') {
		printf("Value starting with - implying it's a switch!\n");
		return 1;
	}

	if(!strcmp(sw, "-a")) {
		ip = val;
	} else if(!strcmp(sw, "-p")) {
		port = parse_string_to_int_main(val);

		if(port < 0 || port > 65536 || port == INT_MIN) {
			printf("Port is expected to be a number from 0 to 65536\n");
			return 1;
		}
	} else if(!strcmp(sw, "-c")) {
		//max_con = strtol(val, NULL, 10);
		max_con = parse_string_to_int_main(val);
			
		if(max_con < 2 || max_con > 100 || max_con == INT_MIN) {
			printf("Server allows between 2 and 100 connectinos\n");
			return 1;
		}

	} else {
		printf("Unknown option!\n");
	}

	return 0;
}

/**
	Parse all command line arguments
*/
int parse_arguments(int argc, char *argv[]) {
	int i = 0;

	if(argc == 2) {
		if(strcmp(argv[i], "-h")) {
			return 1;
		}
	}

	for(i = 1; i < argc; i++) {
		if(!strcmp(argv[i], "-h")) {
			print_help();
			return 2;
		}
	}

	switch(argc) {
		case 3: if(process_argument(argv[1], argv[2])) {
				return 1;
			}; break;

		case 5: if(process_argument(argv[1], argv[2])) {
				return 1;
			};

			if(process_argument(argv[3], argv[4])) {
				return 1;
			}; break;

		case 7: if(process_argument(argv[1], argv[2])) {
				return 1;
			};

			if(process_argument(argv[3], argv[4])) {
				return 1;
			}; 

			if(process_argument(argv[5], argv[6])) {
				return 1;
			}; break;
	}
	
	return 0;
}

/**
	Sets ip, port and max_con to default values
*/
void init_defaults() {
	if(!ip)
		ip = "127.0.0.1";
	if(port == -1)
		port = 61116;
	if(max_con == -1)
		max_con = 50;

}

/**
	Start main program function
*/
int run() {
	int rv = run_server(ip, port);

	if(rv) {
		printf("Server exited with rv: %d\n", rv);
		return 1;
	}

	return 0;
}

/**
	Main
*/
int main(int argc, char *argv[]) {
	log_message("Starting program", LVL_INFO);

	switch(parse_arguments(argc,argv)) {
		case 2: return 0;
		case 1: printf("Argument error. Cannot continue. Terminating...\n");
			return 1;

	}

	signal(SIGINT, stop_server_int);
	//signal(SIGTERM, stop_server_term);
	//signal(SIGSEGV, stop_server_segv);

	init_defaults();

	run();

	return 0;
}
