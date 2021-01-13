#ifndef _LOGGER_H
#define _LOGGER_H

#include <stdio.h>
#include <string.h>

typedef enum log_levels {
	LVL_INFO = 1,
	LVL_ERROR = 2,
	LVL_FATAL = 3
} log_level;

int log_message(char *message, log_level level);

#endif
