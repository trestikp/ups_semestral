#ifndef _GENERAL_FUNCTIONS_H
#define _GENERAL_FUNCTIONS_H

#include <stdio.h>
#include <stdlib.h>

#include "logger.h"

/**************************************/
/*                                    */
/*      Structs                       */
/*                                    */
/**************************************/

typedef struct llist_link{
        void *data;
        struct llist_link *next;
} l_link;


/**************************************/
/*                                    */
/*      Functions                     */
/*                                    */
/**************************************/

int add_lifo(l_link **head, void *data);
int add_fifo(l_link **head, void *data);
l_link* pop_link(l_link** head);
void free_list(l_link *head);

#endif
