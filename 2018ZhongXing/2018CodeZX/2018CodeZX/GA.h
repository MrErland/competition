#ifndef _GA_h
#define _GA_h
	
#include "util.h"
#include <cstdlib>

#define SEED_GA 2				// �������
#define Size_Group 70			// ��Ⱥ��ģ
#define Times_Iteration 400		// ��������
#define Rate_Crossover 0.9		// ������
#define Length_Crossover 300	// ���泤��
#define Rate_Mutation 0.1		// ������	
#define Bad_Fit -1

struct Individual{			// ����
	int solution[Size_Business];	// ��ǰ����
	double fitness;		// ����ֵ
	double rate_fit;	// ����ֵ����
	double sum_fit;		// �ۼ�
	Line line;
	Individual(){}
};

extern Individual bestInd;

void GA();
void initgroup();				// ��ʼ����Ⱥ
void evaluate();				// ������Ӧ��
void selector_championship();	// ������
void crossover(int);			// ��㽻��
void crossover_point(int);
void mutation(int);				// ����
void elite();					// ��Ӣ
void extinction();				// �ֱ�
void report(int);				// ��ӡÿһ��
void getBest();
void changepoint(int, int, int, int);

#endif 