#include <stdlib.h>
#include <stdio.h>
/*
class A {

	public void m() {
		print 1;
	}

}
*/

typedef struct _classe_A {
        struct _methodes_A {
               void (*m)(struct _classe_A*);
               } *methodes;
} classe_A;

void A_m(struct _classe_A *this) {
     printf( "%d\n", 1);
}

classe_A *new_A() {
     classe_A *this = (classe_A *) malloc( sizeof( classe_A ));
     this->methodes = (struct _methodes_A *) malloc( sizeof( struct _methodes_A ));
     this->methodes->m = &A_m;
     return this;
}

void new_A_0(classe_A *this) {
}

/*
class B extends A {

	public void m() {
		print 2;
	}

}
*/

typedef struct _classe_B {
        struct _methodes_B {
               void (*m)(struct _classe_B*);
               } *methodes;
} classe_B;

void B_m(struct _classe_B *this) {
     printf( "%d\n", 2);
}

classe_B *new_B() {
     classe_B *this = (classe_B *) malloc( sizeof( classe_B ));
     this->methodes = (struct _methodes_B *) malloc( sizeof( struct _methodes_B ));
     this->methodes->m = &B_m;
     return this;
}

void new_B_0(classe_B *this) {
}

/*
public class Main {

	public static void main(String args[]) {
		A a = new A();
		a.m();

		B b = new B();
		b.m();

		a = b;
		a.m();

	}
}
*/

int main(int argc, char **argv) {
     classe_A *a = new_A();
     new_A_0( a );
     a->methodes->m(a);

     classe_B *b = new_B();
     new_B_0( b );
     b->methodes->m(b);

     a = (struct _classe_A *) b;
     a->methodes->m(a);
}
