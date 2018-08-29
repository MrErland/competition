#ifndef _GA_h
#define _GA_h
	
#include "util.h"
#include <cstdlib>

#define SEED_GA 2				// 随机种子
#define Size_Group 70			// 种群规模
#define Times_Iteration 400		// 迭代次数
#define Rate_Crossover 0.9		// 交叉率
#define Length_Crossover 300	// 交叉长度
#define Rate_Mutation 0.1		// 变异率	
#define Bad_Fit -1

struct Individual{			// 个体
	int solution[Size_Business];	// 当前策略
	double fitness;		// 评估值
	double rate_fit;	// 评估值比率
	double sum_fit;		// 累加
	Line line;
	Individual(){}
};

extern Individual bestInd;

void GA();
void initgroup();				// 初始化种群
void evaluate();				// 评估适应度
void selector_championship();	// 锦标赛
void crossover(int);			// 多点交叉
void crossover_point(int);
void mutation(int);				// 变异
void elite();					// 精英
void extinction();				// 灾变
void report(int);				// 打印每一代
void getBest();
void changepoint(int, int, int, int);

#endif 