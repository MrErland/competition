#include "SA.h"

Answer now;		// 当前解
Answer best;	// 最优解
int flow[Size_Graph][Size_Graph];

//#define _error_

double SA(int seed)
{
	srand((unsigned int)seed);
	initsa();
	printsa(0);

	int x = 0; 
	double T = Temp_Init, d, de;
	Answer tmp = now;
	int a, b;
	while (T > TemP_End)
	{
		for (int i = 0; i < LOOP; i++)
		{
			b = rand() % Business_Num;
			a = now.solution[b];
			getnext(tmp, b);
			de = tmp.fit - now.fit;
			if (de <= 0)
			{
				now = tmp;
				if (tmp.fit < best.fit)
					best = tmp;
			}
			else{
				d = (double)rand() / RAND_MAX;
				if (d < exp(-de / T))	
					now = tmp;
				else {
					getfit(tmp, b, tmp.solution[b], a);
					tmp = now;
				}
			}
		}
		T *= SPEED;
#ifdef _error_
		printsa(++x);
#endif
	}
	getbestsa();
	printsa(1);
	return best.fit;
}


bool getfit(Answer &a, int b, int from, int to)
{
	bool change = false;
	if (from == to) return false;
	int f = business[b].flow;
	int s = business[b].path[from].size();
	int t = business[b].path[to].size();
	int u, v; double d;
	for (int i = 0; i < s - 1; i++)
	{
		u = business[b].path[from][i];
		v = business[b].path[from][i + 1];
		flow[u][v] -= f;
		if (u == a.from && v == a.to) change = true;
	}
	for (int i = 0; i < t - 1; i++)
	{
		u = business[b].path[to][i];
		v = business[b].path[to][i + 1];
		flow[u][v] += f;
		d = (double)flow[u][v] / graph[u][v];
		if (d > a.fit)
		{
			a.fit = d;
			a.from = u;
			a.to = v;
		}
	}
	return change;
}

void fitness(Answer &n)
{
	double d, maxpnt = 0;
	int a, b;
	for (int i = 0; i < Node_Num; i++)
	{
		for (int j = 0; j < Node_Num; j++)
		{
			if (flow[i][j])
			{
				d = (double)flow[i][j] / graph[i][j];
				if (d > maxpnt)
				{
					maxpnt = d;
					a = i;
					b = j;
				}
			}
		}
	}
	n.fit = maxpnt;
	n.from = a;
	n.to = b;
}

void initsa()
{
	memset(flow, 0, sizeof(flow));
	for (int i = 0; i < Business_Num; i++)
	{
		now.solution[i] = rand() % Channel_Num;
	}
	int s, a, b;
	for (int j = 0; j < Business_Num; j++)
	{
		s = business[j].path[now.solution[j]].size();
		for (int k = 0; k < s - 1; k++)
		{
			a = business[j].path[now.solution[j]][k];
			b = business[j].path[now.solution[j]][k + 1];
			flow[a][b] += business[j].flow;
		}
	}
	fitness(now);
	best = now;
}

void printsa(int i)
{
	printf("%d\t%f  %f %d-%d\n", i, now.fit, best.fit, best.from, best.to);
}

void getbestsa()
{
	sprintf(Use_Percent, "%.2lf", best.fit * 100);
	Line_Max_One = best.from;
	Line_Max_Two = best.to;
	for (int i = 0; i < Size_Business; i++)
	{
		business[i].result_index = best.solution[i];
	}
}

void getnext(Answer &a, int b)
{
	int s = a.solution[b];
	int t;
	while ((t = rand() % Channel_Num) == s);
	a.solution[b] = t;
	if (getfit(a, b, s, t))
		fitness(a);
}
