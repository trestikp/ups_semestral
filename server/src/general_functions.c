#include "general_functions.h"


/**
	Adds (void*) data @data to list @head as LIFO (stack).
*/
int add_lifo(l_link **head, void *data) {
        l_link *new = malloc(sizeof(l_link));

	if(!new) {
		printf("Failed to allocate memory!\n"); //LOGGER?
		return 1;
	}

        new->data = data;
	new->next = *head;
	*head = new;

	/*
	if(*head) {
		new->next = *head;
		*head = new;
	} else {
		new->next = NULL;
		*head = new;
	}
	*/

        return 0;
}


/**
	Adds (void*) data @data to list @head as FIFO (queue).
*/
int add_fifo(l_link **head, void *data) {
        l_link *new = malloc(sizeof(l_link)), *ptr = *head;

	if(!new) {
		printf("Failed to allocate memory!\n"); //LOGGER?
		return 1;
	}

        new->data = data;
        new->next = NULL;

        if(!*head) {
                *head = new;

		return 0;
        }

        while(ptr->next) ptr = ptr->next;

        ptr->next = new;

        return 0;
}

/**
	Pops link from start of list @head
*/
l_link* pop_link(l_link** head) {
	l_link* temp = *head;
	*head = (*head)->next;

	return temp;
}

/**
	Free list @head memory
*/
void free_list(l_link *head) {
        l_link *ptr = head;

        while(ptr) {
                head = ptr;
                ptr = ptr->next;
                free(head->data);
                free(head);
        }
}
