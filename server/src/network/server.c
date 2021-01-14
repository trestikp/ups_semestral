#include "server.h"
#include "controler.h"
#include <asm-generic/socket.h>
#include <sys/select.h>
#include <arpa/inet.h>
#include <errno.h>
#include <sys/socket.h>


int additionalActions = 0;

int ss;

int create_server_socket(char* ip, int port) {
	int server_socket = 0;
	struct sockaddr_in server_address;
	int enable = 1;

	server_socket = socket(AF_INET, SOCK_STREAM, 0);

	if(server_socket == -1) {
		log_message("Failed to create server socket", LVL_FATAL);
		return -1;
	} else {
		log_message("Successfully created socket", LVL_INFO);
	}

	memset(&server_address, 0, sizeof(struct sockaddr_in));

	server_address.sin_family = AF_INET;
	server_address.sin_port = htons(port);
	if(!inet_aton(ip, &server_address.sin_addr)) {
		log_message("Failed to assign IP to socket", LVL_FATAL);
		return -1;
	}
	//server_address.sin_addr.s_addr = inet_addr(ip);

	if(setsockopt(server_socket, SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int)) < 0) {
		log_message("Failed to set server socket options", LVL_FATAL);
		return -1;
	}

	if(bind(server_socket,(struct sockaddr*) &server_address,
	   sizeof(struct sockaddr_in))) {
		log_message("Failed to bind addres to socket", LVL_FATAL);		
		return -1;
	} else {
		log_message("Successfully bound socket with address", LVL_INFO);
	}

	//set socket to listen here?
	if(listen(server_socket, 32)) {
		log_message("Failed to set server to listen", LVL_FATAL);
		return -1;
	} else {
		log_message("Server is listening", LVL_INFO);
	}

	return server_socket;
}

int establish_server() {
	create_server_socket("127.0.0.1", 61116);

	return 0;
}

int run_server(char* ip, int port) {
	int rv = 0, i = 0, data_size;
	#define BUFFER_SIZE 256
	char buffer[BUFFER_SIZE];
	char *response;
	socklen_t addr_len;
	struct sockaddr_in client_socket;
	fd_set clients, tests;

	ss = create_server_socket(ip, port);

	if(ss == -1) {
		log_message("Socktet creation failed", LVL_FATAL);
		return 1;
	}

	memset(&client_socket, 0, sizeof(struct sockaddr_in));

	FD_ZERO(&clients);
	FD_SET(ss, &clients);

	//char c;
	while(1) {
		printf("> ");

		tests = clients;
		errno = 0;
		rv = select(FD_SETSIZE, &tests, NULL, NULL, NULL);

		if(rv < 0) {
			if(errno == EINTR) {
				printf("FD_SET interrupt error. Closing server.\n");
				return 0;
			} else {
				printf("FD_SET error: %d\n", errno);
				return 1;
			}
		}

		for(i = 3; i < FD_SETSIZE; i++) {
			if(FD_ISSET(i, &tests)) {
				if(i == ss) {
					rv = accept(ss, (struct sockaddr*) &client_socket, &addr_len);
					//add_connection(rv);
					FD_SET(rv, &clients);
					printf("New connection. Client not CONNECTED.\n");
				} else {
					//printf("Client sending data.\n");
					ioctl(i, FIONREAD, &data_size);

					if(data_size > 0 && data_size < BUFFER_SIZE) {
						memset(buffer, 0, BUFFER_SIZE);
						recv(i, buffer, BUFFER_SIZE - 1, 0);

						printf(">>> BUFFER: %s\n", buffer);
						response = handle_message(buffer, i);
						
						if(!response) {
							close(i);
							FD_CLR(i, &clients);
							continue;
						}
						printf(">>> RESPONSE: %s\n", response);
										//  MSG_DONTWAIT?
						int rv = send(i, response, strlen(response), MSG_CONFIRM);

						if(rv == -1) printf("Failed to send response");
						
						free(response);

						switch(additionalActions) {
							case 1: close(i); FD_CLR(i, &clients); break;
						}

						additionalActions = 0;
					} else {
						//printf("Client is gone?\n");
						//TODO check if client was in-game -> wait for reconnect/ remove game
						close(i);
						FD_CLR(i, &clients);
					}
				}
			}
		}

		memset(&client_socket, 0, sizeof(struct sockaddr_in));

		sleep(1);
	}
	
	return 0;
}

void stop_server() {
	log_message("Stopping server", LVL_INFO);
	close(ss);

	free_controler();

	exit(1);
}
