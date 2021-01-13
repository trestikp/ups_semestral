#include "logger.h"

const char log_file[] = "server_log.log";

int log_message(char *message, log_level level) {
	FILE *f = fopen(log_file, "a+");
	
	if(!f) {
		printf("Failed to open log file!");
		return 0;
	}

	char *mark = NULL;

	switch(level) {
		case 1: mark = "INFO: \0"; break;
		case 2: mark = "ERROR: \0"; break;
		case 3: mark = "FATAL: \0"; break;
	}

	if(!mark) {
		printf("Unrecognized log level!");
		fclose(f);

		return 0;
	}

	fwrite(mark, strlen(mark), 1, f);
	fwrite(message, strlen(message), 1, f);
	fputc('\n', f);

	fclose(f);

	return 1;
}
