#include "general_functions.h"

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

l_link* pop_link(l_link** head) {
	l_link* temp = *head;
	*head = (*head)->next;

	return temp;
}

void free_list(l_link *head) {
        l_link *ptr = head;

        while(ptr) {
                head = ptr;
                ptr = ptr->next;
                free(head->data);
                free(head);
        }
}

/*
int main() {
	l_link* lifo = NULL;

	int a = 1;
	int b = 2;
	int c = 3;
	int d = 4;
	int e = 5;

	add_lifo(&lifo, &a);
	add_lifo(&lifo, &b);
	add_lifo(&lifo, &c);
	add_lifo(&lifo, &d);
	add_lifo(&lifo, &e);

	l_link* temp = lifo;
	while(temp) {
		printf("%d\n", *((int*) temp->data));
		temp = temp->next;
	}

	l_link* fifo = NULL;

	add_fifo(&fifo, &e);
	add_fifo(&fifo, &d);
	add_fifo(&fifo, &c);
	add_fifo(&fifo, &b);
	add_fifo(&fifo, &a);

	l_link* temp2 = lifo;
	while(temp2) {
		printf("%d\n", *((int*) temp2->data));
		temp2 = temp2->next;
	}

}
*/
