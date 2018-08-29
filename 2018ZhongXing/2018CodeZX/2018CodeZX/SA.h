
#ifndef _SA_
#define _SA_

#include "util.h"

#define SEED_SA 121		
#define SPEED 0.999				// 降温速度
#define Temp_Init 1				// 初始温度
#define TemP_End 0.0000001		// 终止温度
#define LOOP 60					// 内循环次数

struct Answer{ 
	int solution[Size_Business]; double fit;
	int from; int to;
};

double SA(int);
void initsa();
void getnext(Answer &, int);
bool getfit(Answer &, int, int, int);
void fitness(Answer &);
void printsa(int);
void getbestsa();

#endif

/*
	best seed: 
	121 -> 37.31
	224 -> 37.26
*/