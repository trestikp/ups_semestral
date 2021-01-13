#!/bin/bash

valgrind --leak-check=full --track-origins=yes -s ./run_server
