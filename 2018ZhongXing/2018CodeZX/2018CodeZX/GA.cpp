#include "GA.h"
#include "util.h"

#define _error_

Individual group[Size_Group];	// 种群
Individual bestInd;				// 最好的个体
Individual bestnow;				// 目前最好的个体
int linesflow[Size_Group][Size_Graph][Size_Graph];

void testflow();	// 测试bug

void GA()
{
	srand((unsigned int)(SEED_GA));	// 固定随机种子
	initgroup();
	evaluate();
	bestInd = bestnow;
	report(0);
	int cnt = 0;
	int Length_Extinction = 10;		// 灾变界限
	for (int i = 0; i < Times_Iteration; i++)
	{	
		selector_championship();	// 选择算子
		crossover(i);			// 多点交叉
//		crossover_point(i);		// 单点交叉
		mutation(i);			// 变异
		elite();		// 精英原则
		evaluate();		// 适应度
		
		if (bestInd.fitness == bestnow.fitness)
		{
			cnt++;
			if (cnt > Length_Extinction)
			{
//				extinction();		// 灾变
				Length_Extinction *= 2;	// 扩大灾变长度
				cnt = 0;
			}
		}
		else if (bestnow.fitness < bestInd.fitness)
		{
			bestInd = bestnow;
			cnt = 0;
		}
		report(i + 1);
	}
	getBest();
}

void initgroup()
{
	for (int i = 0; i < Size_Group; i++)	// 每个个体
	{
		for (int j = 0; j < Business_Num; j++)
		{
			group[i].solution[j] = rand() % (Channel_Num);	// 0,1,2
		}
	}
	int a, b, s, best = 0;
	memset(linesflow, 0, sizeof(linesflow));
	for (int i = 0; i < Size_Group; i++)
	{
		for (int j = 0; j < Business_Num; j++)
		{
			s = business[j].path[group[i].solution[j]].size();
			for (int k = 0; k < s - 1; k++)
			{
				a = business[j].path[group[i].solution[j]][k];
				b = business[j].path[group[i].solution[j]][k + 1];
				linesflow[i][a][b] += business[j].flow;
			}
		}
	}
}

void evaluate()
{
	int a, b, best = 0;
	double d, maxpnt, minpnt = 1;
	for (int i = 0; i < Size_Group; i++)
	{
		maxpnt = 0;
		for (int j = 0; j < Node_Num; j++)
		{
			for (int k = 0; k < Node_Num; k++)
			{
				if (linesflow[i][j][k])
				{
					d = (double)linesflow[i][j][k] / graph[j][k];
					if (d > maxpnt)
					{
						maxpnt = d;
						a = j;
						b = k;
					}
				}
			}
		}
		group[i].fitness = maxpnt;
		group[i].line.from = a;
		group[i].line.to = b;
		if (minpnt > maxpnt)
		{
			minpnt = maxpnt;
			best = i;
		}
	}
	bestnow = group[best];		// 最优个体	fitness 越小越优
}

void selector_championship()	// 锦标赛选择算子
{
	const int Length_choose = 2;
	Individual new_group[Size_Group];
	int a, pos;
	double min;
	for (int i = 0; i < Size_Group; i++)
	{
		min = group[i].fitness;
		pos = i;
		for (int j = 0; j < Length_choose - 1; j++)
		{
			a = rand() % Size_Group;
			if (group[a].fitness < min)
			{
				min = group[a].fitness;
				pos = a;
			}
		}
		new_group[i] = group[pos];
		if (pos == i) continue;

		for (int k = 0; k < Business_Num; k++)
			changepoint(i, k, group[i].solution[k], group[pos].solution[k]);
	}
	for (int i = 0; i < Size_Group; i++)
		group[i] = new_group[i];
}

void changepoint(int i, int j, int from, int to)	// i个体 j位置 原form 新to
{
	if (from == to) return;
	int s = business[j].path[from].size(); 
	int t = business[j].path[to].size();
	int f = business[j].flow;
	int a, b;
	for (int m = 0; m < s - 1; m++)
	{
		a = business[j].path[from][m];
		b = business[j].path[from][m + 1];
		linesflow[i][a][b] -= f;
#ifdef _error_
		if (linesflow[i][a][b] < 0) 
			cout << "error flows." << endl;
#endif
	}
	for (int n = 0; n < t - 1; n++)
	{
		a = business[j].path[to][n];
		b = business[j].path[to][n + 1];
		linesflow[i][a][b] += f;
#ifdef _error_
		if (linesflow[i][a][b] < 0) 
			cout << "error flows." << endl;
#endif
	}
}

void crossover(int times)
{
	double cross;
	double rc = times > (Times_Iteration / 5 * 4) ? (Rate_Crossover / 2) : Rate_Crossover;
	int a, b, c, x, y;
	for (int i = 0; i < Size_Group; i++)
	{
		cross = (double)rand() / RAND_MAX;
		if (cross < rc)		// 进行交叉
		{
			a = i;
			while ((b = rand() % Size_Group) == a);
			for (int j = 0; j < Length_Crossover; j++)
			{
				c = rand() % Business_Num;
				x = group[a].solution[c];
				y = group[b].solution[c];
				changepoint(a, c, x, y);
				changepoint(b, c, y, x);
				group[a].solution[c] = y;
				group[b].solution[c] = x;
			}
		}
	}
}

void crossover_point(int times)
{
	double cross;
	double rc = times > (Times_Iteration / 5 * 4) ? (Rate_Crossover / 2) : Rate_Crossover;
	int a, b, c, x, y;
	for (int i = 0; i < Size_Group; i++)
	{
		cross = (double)rand() / RAND_MAX;
		if (cross < rc)
		{
			a = i;
			while ((b = rand() % Size_Group) == a);
			c = rand() % Business_Num;
			for (int j = c; j < Business_Num; j++)
			{
				x = group[a].solution[j];
				y = group[b].solution[j];
				changepoint(a, j, x, y);
				changepoint(b, j, y, x);
				group[a].solution[j] = y;
				group[b].solution[j] = x;
			}
		}
	}
}

void mutation(int times)
{
	double muta;
	double lm = times > (Times_Iteration / 5 * 4) ? (5 * Rate_Mutation) : Rate_Mutation;
	int size = times > (Times_Iteration / 5 * 4) ? 5 : 20;
	int x, y;
	for (int i = 0; i < Size_Group; i++)
	{
		muta = (double)rand() / RAND_MAX;
		if (muta < lm)
		{
			for (int n = 0; n < size; n++)
			{
				int j = rand() % Business_Num;	// 变异位置
				x = group[i].solution[j];
				while ((y = rand() % Channel_Num) == x);
				group[i].solution[j] = y;
				changepoint(i, j, x, y);
			}
		}
	}
}

void elite()
{
	group[0] = bestInd;
	int s, a, b;
	memset(linesflow, 0, sizeof(linesflow[0]));
	for (int j = 0; j < Business_Num; j++)
	{
		s = business[j].path[group[0].solution[j]].size();
		for (int k = 0; k < s - 1; k++)
		{
			a = business[j].path[group[0].solution[j]][k];
			b = business[j].path[group[0].solution[j]][k + 1];
			linesflow[0][a][b] += business[j].flow;
		}
	}
}

void extinction()
{
	int s;
	for (int i = 0; i < Size_Group / 5; i++)	// 选择较差的解
	{
		while ((s = rand() % Size_Group) == i);
		if (group[i].fitness < group[s].fitness)
		{
			for (int j = 0; j < Business_Num; j++)
			{
				changepoint(i, j, group[i].solution[j], group[s].solution[j]);
			}
			group[i] = group[s];
		}
	}
	for (int i = Size_Group / 2; i < Size_Group * 3 / 5; i++)	// 新的解
	{
		
	}
}

void report(int n)
{
	printf("%d  %f  %d-%d\n", n, bestInd.fitness, bestInd.line.from, bestInd.line.to);
}

void getBest()
{
	sprintf(Use_Percent, "%.2lf", bestInd.fitness * 100);
	Line_Max_One = bestInd.line.from;
	Line_Max_Two = bestInd.line.to;
	for (int i = 0; i < Size_Business; i++)
	{
		business[i].result_index = bestInd.solution[i];
	}
}

void testflow()		// 测试Bug
{
	int s, a, b;
	for (int i = 0; i < Size_Group; i++)
	{
		for (int j = 0; j < Business_Num; j++)
		{
			s = business[j].path[group[i].solution[j]].size();
			for (int k = 0; k < s - 1; k++)
			{
				a = business[j].path[group[i].solution[j]][k];
				b = business[j].path[group[i].solution[j]][k + 1];
				linesflow[i][a][b] -= business[j].flow;
				if (linesflow[i][a][b] < 0) cout << "error!" << " ";
			}
		}
	}
}