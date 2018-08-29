#ifndef _util_
#define _util_

#include <iostream>
#include <algorithm>
#include <vector>
#include <string>
#include <ctime>
#include <cstring>
#include <cstdio>
#include <cstdlib>
#include <vector>
#include <fstream>

using namespace std;

#define MAX_LINE_LEN 5000
#define MAX_DATA_LEN 6000
#define Size_Graph 505
#define Size_Business 1005
#define INF_COST 99999999	// 不连通

struct Business{ int index; int flow; int result_index; vector<vector<int> > path; };	// 0 - Channel_Num-1
extern Business business[Size_Business];
extern int graph[Size_Graph][Size_Graph];
extern int Node_Num;		// 结点数
extern int Line_Num;		// 边数
extern int Business_Num;	// 业务数
extern int Channel_Num;		// 备选数
extern char Use_Percent[10];		// 利用率
extern int Line_Max_One, Line_Max_Two;		// 最大链路
extern bool IsNA;


struct Line{ int from; int to;
bool operator<(const Line &a)const { if (a.from != from) return a.from > from; return a.to > to; }
Line(int x, int y){ from = x; to = y; }
Line(){}
};

struct LineFlow{ int flow; int cap;
LineFlow(int x, int y){ flow = x; cap = y; }
LineFlow(){ flow = 0; }
};

void init_data(char**);
void split_string(const string&, vector<int>&, const string&);
string out_result(bool);

#endif