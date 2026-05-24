#include <stdlib.h>
#include <stdio.h>
/*
class A {
	public int v;

	public void setV(int _v) {
		this.v = _v;
	}

	public int getV() {
		return this.v;
	}

	public A() {
		this( -1 );
	}

	public A(int _v) {
		this.v = _v;
	}

}
*/

typedef struct _classe_A {
        struct _methodes_A {
               void (*setV)(struct _classe_A*, int);
               int (*getV)(struct _classe_A*);
               } *methodes;
       int v;
} classe_A;

void A_setV(struct _classe_A *this, int _v) {
     this->v = _v;
}

int A_getV(classe_A *this) {
    return this->v;
}

classe_A *new_A() {
     classe_A *this = (classe_A *) malloc( sizeof( classe_A ));
     this->methodes = (struct _methodes_A *) malloc( sizeof( struct _methodes_A ));
     this->methodes->getV = &A_getV;
     this->methodes->setV = &A_setV;
     return this;
}

void new_A_1(classe_A *this, int _v) {
         this->v = _v;
}

void new_A_0(classe_A *this) {
         new_A_1( this, -1);
}

/*
public class Main {

	public static void main(String args[]) {
		A a1 = new A();
		A a2 = new A( 5 );
		print a1.v;
		print a1.getV();
		print a2.v;
		print a2.getV();
		a1.v = a2.v;
		print a1.v;
		a1.setV( 3 );
		print a1.getV();
		a2.setV( 7 );
		print a2.getV();
		a1 = a2;
		a2.setV( 9 );
		print a1.getV();
		a1.setV( 11 );
		print a2.getV();

	}
}
*/

int main(int argc, char **argv) {
     classe_A *a1 = new_A();
     new_A_0( a1 );
     classe_A *a2 = new_A();
     new_A_1( a2, 5 );
     printf( "%d\n", a1->v);
     printf( "%d\n", a1->methodes->getV( a1 ));
     printf( "%d\n", a2->v);
     printf( "%d\n", a2->methodes->getV( a2 ));
     a1->v = a2->v;
     printf( "%d\n", a1->v);
     a1->methodes->setV( a1, 3 );
     printf( "%d\n", a1->methodes->getV( a1 ));
     a2->methodes->setV( a2, 7 );
     printf( "%d\n", a2->methodes->getV( a2 ));
     a1 = a2;
     a2->methodes->setV( a2,  9 );
     printf( "%d\n", a1->methodes->getV( a1 ));
     a1->methodes->setV( a1, 11 );
     printf( "%d\n", a2->methodes->getV( a2 ));
}
