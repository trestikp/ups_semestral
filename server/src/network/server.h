#ifndef _SERVER_H
#define _SERVER_H

#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <netinet/in.h>

#include "../logger.h"
#include "controller.h"
#include "../general_functions.h"

/**
	For function doc see .c file
*/

int run_server(char* ip, int port);
int establish_server();
void stop_server();

#endif
