/*
基于深度优先搜索算法寻找约束条件下的最短路问题
by:Erland
date:2017.5.12
test data: test1、test2、test3
图的基本设定：
1.基于图的通用性：图为无向图，边权为正，连接方式不限制。
2.图的顶点序号从1开始，点数最大为1000，边权最大为MaxPathCost,最小费用最大为INF。
优化方案：
1.当图的规模比较大的时候，缩小图的规模。
2.为了防止图中存在无效路径过深，产生浪费搜索效率的情况，根据当前路径有效性剪枝操作。
3.储存结构优化，邻接矩阵储存稀疏图空间利用率较低，时间复杂度较高，由用例数据:e<<n^2，因此可以归为稀疏图，采用邻接表。
*/

#include <iostream>
#include <vector>
#include <iterator>
#include <algorithm>
#include <time.h>
#include <fstream>
using namespace std;
const int INF = 9999999;
const int MaxNode = 1001;
const int MaxPathCost = 10000;
int minCost = INF;
bool recoData = false;
int Length;
int pointNum;
int lineNum;
int startPoint;
int finalPoint;
int passLineNum;
int banLineNum;
int passPointNum;
vector <int> banLine;
vector <int> passPoint;
vector <int> passLine;
vector <int> repeatPoint;
vector <int> path;
vector <int > result;

typedef struct EdgeNode
{
	int adj;
	int weight;
	struct EdgeNode *next;
}EdgeNode;

typedef struct Node
{
	int data;
	EdgeNode *firstedge;
}Node;

typedef struct
{
	Node nodeList[MaxNode];
	int numNode;
	int numEdge;
}Graph;

void readFile(vector<int>&data)
{
	cout << "Read File" << endl;
	int tem = 0;
	ifstream fin("test3.txt");
	while (!fin.eof())
	{
		fin >> tem;
		data.push_back(tem);
	}
	fin.close();
	pointNum = data[0];
	if (pointNum > MaxNode - 1)
		recoData = true;
	lineNum = data[1];
	cout << "------------------- Read File Over --------------------" << endl;
	return;
}

bool testConnected(Graph *graph, int x, int y)
{
	EdgeNode *p = graph->nodeList[x].firstedge;
	while (p != NULL)
	{
		if (p->adj == y)
			return true;
		p = p->next;
	}
	return false;
}

void deleEdge(Graph *graph, int i, int j)
{
	if (i <= 0 || j <= 0 || i == j)
		return;
	EdgeNode *p = graph->nodeList[i].firstedge;
	EdgeNode *q = p;
	while (p != NULL && p->adj != j)
	{
		q = p;
		p = p->next;
	}
	if (p != NULL && p->adj == j)
	{
		if (p == graph->nodeList[i].firstedge)
			graph->nodeList[i].firstedge = p->next;
		else
			q->next = p->next;
	}

	p = graph->nodeList[j].firstedge;
	while (p != NULL && p->adj != i)
	{
		q = p;
		p = p->next;
	}
	if (p != NULL && p->adj == i)
	{
		if (p == graph->nodeList[j].firstedge) 
			graph->nodeList[j].firstedge = p->next;
		else
			q->next = p->next;
	}
	free(p);
	graph->numEdge--;
	return;
}

void initGraph(Graph *graph, vector<int>&data)
{
	cout << "Initialize Graph" << endl;
	int n = 0;
	int i;
	EdgeNode *edge;
	graph->numNode = data[n++];
	graph->numEdge = data[n++];
	for (i = 1; i <= graph->numNode; i++)
	{
		graph->nodeList[i].data = i;
		graph->nodeList[i].firstedge = NULL;
	}
	int x, y, z;
	for (i = 1; i <= graph->numEdge; i++)
	{
		x = data[n];
		y = data[n + 1];
		z = data[n + 2];
		if (z > MaxPathCost)
			recoData = true;
		n += 3;
		edge = (EdgeNode *)malloc(sizeof(EdgeNode));
		edge->adj = y;
		edge->weight = z;
		edge->next = graph->nodeList[x].firstedge;
		graph->nodeList[x].firstedge = edge;
		edge = (EdgeNode *)malloc(sizeof(EdgeNode));	//无向图
		edge->adj = x;
		edge->weight = z;
		edge->next = graph->nodeList[y].firstedge;
		graph->nodeList[y].firstedge = edge;
	}
	startPoint = data[n++];
	finalPoint = data[n++];
	if (startPoint == finalPoint)
		recoData = true;
	Length = data[n++];
	passPointNum = data[n++];
	for (i = 0; i < passPointNum; i++)
	{
		passPoint.push_back(data[n++]);
	}
	banLineNum = data[n++];
	for (i = 0; i < banLineNum; i++)
	{
		banLine.push_back(data[n++]);
		banLine.push_back(data[n++]);
		deleEdge(graph, banLine[2 * i], banLine[2 * i + 1]);
	}
	passLineNum = data[n++];
	for (i = 0; i < passLineNum; i++)
	{
		passLine.push_back(data[n++]);
		passLine.push_back(data[n++]);
	}
	cout << "------------------- Initialize Over -------------------" << endl;
}

void cutNode(Graph *graph, int x, int y, vector<bool>&invaNode)
{
	vector<int> beside;
	EdgeNode *p;
	int t, f = 0, i = 0;
	beside.push_back(x);
	p = graph->nodeList[x].firstedge;
	if (p == NULL)
		return;
	while (true)
	{
		if (p == NULL && f == 0)
		{
			p = graph->nodeList[beside[++i]].firstedge;
		}
		else if (p == NULL && f == 1){
			break;
		}
		t = p->adj;
		beside.push_back(t);
		invaNode[t] = true;
		if (t == y)
			f = 1;
		p = p->next;
	}
	return;
}

void cutGraph(Graph *graph, vector<bool>&invaNode)
{	
	int i, j;
	for (i = 0; i < repeatPoint.size(); i++)
	{
		for (j = i + 1; j < repeatPoint.size(); j++)
		{
			cutNode(graph, repeatPoint[i], repeatPoint[j], invaNode);
		}
	}
	return;
}

void testData()
{
	cout << "Start Point: " << startPoint << endl;
	cout << "Final Point: " << finalPoint << endl;
	cout << "Path Length: " << Length << endl;
	cout << "Passed Points: ";
	for (int i = 0; i < passPointNum; i++)
	{
		cout << passPoint[i] << " ";
	}
	cout << endl;
	cout << "Banned Path: ";
	for (int i = 0; i < banLineNum; i++)
	{
		cout << banLine[2 * i] << "<-->" << banLine[2 * i + 1] << " ";
	}
	cout << endl;
	cout << "Passed Path: ";
	for (int i = 0; i < passLineNum; i++)
	{
		cout << passLine[2 * i] << "<-->" << passLine[2 * i + 1] << " ";
	}
	cout << endl;
	cout << "---------------------- Test Over ----------------------" << endl;
}

void coutResult()
{
	for (int i = 0; i < result.size(); i++)
	{
		if (i < result.size() - 1)
		{
			cout << result[i] << " -> ";
		}
		else{
			cout << result[i] << endl;
		}
	}
	return;
}

int getCost(Graph *graph, vector<int>&path)
{
	int sum = 0;
	int temp = 0;
	EdgeNode *p;
	for (int i = 0; i < path.size() - 1; i++)
	{
		p = graph->nodeList[path[i]].firstedge;
		while (p != NULL)
		{
			if (p->adj == path[i + 1])
			{
				temp = p->weight;
				sum += temp;
				break;
			}
			p = p->next;
		}
	}
	return sum;
}

void getResult(vector<int>&path)
{
	result.clear();
	for (int i = 0; i < path.size(); i++)
	{
		result.push_back(path[i]);
	}
	return;
}

bool testPath(vector<int>&path)
{
	bool a = false;
	bool b = a;
	int i, j;
	vector <bool> c(passPointNum);
	vector <bool> d(passLineNum);
	for (i = 0; i < path.size(); i++)
	{ 
		if (path[i] == startPoint)
			a = true;
		if (path[i] == finalPoint)
			b = true;
		for (j = 0; j < passPointNum; j++)
		{
			if (path[i] == passPoint[j])
			{
				c[j] = true;
				break;
			}
		}
		for (j = 0; j < passLineNum; j++)
		{
			if (i < path.size() - 1 && path[i] == passLine[2 * j] && path[i + 1] == passLine[2 * j + 1])
			{
				d[j] = true;
				break;
			}
			else if (i < path.size() - 1 && path[i] == passLine[2 * j + 1] && path[i + 1] == passLine[2 * j])
			{
				d[j] = true;
				break;
			}
		}
	}
	for (i = 0; i < c.size(); i++)
	{
		if (!c[i])
			return false;
	}
	for (i = 0; i < d.size(); i++)
	{
		if (!d[i])
			return false;
	}
	if (a && b)
		return true;
	else
		return false;
}

bool testCutPath(vector<int>&path)	//剪枝
{
	int n = 0;
	int i, j;
	for (i = 0; i < path.size(); i++)
	{
		for (j = 0; j < repeatPoint.size(); j++)
		{
			if (path[i] == repeatPoint[j])
			{
				n++;
				break;
			}
		}
	}
	if (path.size() - n > Length - repeatPoint.size())
		return false;
	else
		return true;
}

void getRepeatPoint()
{
	int i;
	for (i = 0; i < passLine.size(); i++)
	{
		repeatPoint.push_back(passLine[i]);
	}
	for (i = 0; i < passPoint.size(); i++)
	{
		repeatPoint.push_back(passPoint[i]);
	}
	repeatPoint.push_back(startPoint);
	repeatPoint.push_back(finalPoint);
	sort(repeatPoint.begin(), repeatPoint.end());
	int n = repeatPoint.size();
	for (i = 0; i < repeatPoint.size() - 1; i++)
	{
		if (repeatPoint[i] == repeatPoint[i + 1])
		{
			repeatPoint.erase(repeatPoint.begin() + i + 1);
			i--;
		}			
	}
	if (repeatPoint.size() < n)
	{
		cout << "There Is The Same Inputed Points While Must Be Passed." << endl;
	}	
	return;
}

void mergePath(Graph *graph, vector<int>&repPath, vector<int>&misPoint, int i, int j)
{
	vector <int> temPoint;
	vector <int> temPath;
	temPoint = misPoint;
	int p = repPath[i];
	int q = misPoint[j];
	EdgeNode *pr;
	int n, m, k;
	repPath.insert(repPath.begin() + i + 1, temPoint[j]);
	temPoint.erase(temPoint.begin() + j);
	temPath = repPath;
	for (m = 0; m < misPoint.size(); m++)
	{
		n = i + 1;
		pr = graph->nodeList[q].firstedge;
		for (k = 0; k < temPoint.size(); k++)
		{
			if (temPoint.empty())
				break;
			pr = graph->nodeList[q].firstedge;
			while (pr != NULL)
			{
				if (pr->adj == temPoint[k])
				{
					n++;
					q = temPoint[k];
					repPath.insert(repPath.begin() + n, q);
					temPoint.erase(temPoint.begin() + k);
					k = -1;				
					if (getCost(graph, repPath) >= minCost)
						return;
					break;
				}
				pr = pr->next;
			}	
		}
		if (temPoint.empty() && testConnected(graph, pr->adj, p))
		{
			n++;
			repPath.insert(repPath.begin() + n, p);	
			if (testPath(repPath))
			{
				int gC = getCost(graph, repPath);
				if (gC < minCost)
				{
					minCost = gC;
					getResult(repPath);
				}
			}
		}
		repPath = temPath;
		temPoint = misPoint;
	}
	return;
}

int searchAdj(Graph *graph, int x, vector<int>&repPath)
{
	vector<int> beside;
	EdgeNode *p;
	int t, i = 0;
	beside.push_back(x);
	p = graph->nodeList[x].firstedge;
	if (p == NULL)
		return -1;
	while (true)
	{
		if (p == NULL)		
			p = graph->nodeList[beside[++i]].firstedge;		
		t = p->adj;
		beside.push_back(t);
		for (i = 0; i < repPath.size();i++)
		if (t == repPath[i])
			return t;
		p = p->next;
	}
	return -1;
}

void repeatNodeEntr(Graph *graph)
{
	vector <int> repPath;
	repPath = path;
	int i, j, repPathLen = 0;
	for (i = 0; i < repPath.size(); i++)
	{
		for (j = 0; j < repeatPoint.size(); j++)
		{
			if (repPath[i] == repeatPoint[j])
			{
				repPathLen++;
				break;
			}
		}
	}
	if (repPath.size() - repPathLen <= Length - repeatPoint.size() && repeatPoint.size() - repPathLen >= 1)
	{
		vector <int> misPoint;
		vector <int> temp;
		EdgeNode *p;
		int i, j, t;
		int mark;
		int size = Length - repPath.size() - 1;
		temp = repPath;
		sort(temp.begin(), temp.end());
		set_difference(repeatPoint.begin(), repeatPoint.end(), temp.begin(), temp.end(), inserter(misPoint, misPoint.begin()));
		if (misPoint.size() > size)
			return;
		//for (i = 0; i < misPoint.size(); i++)
		//{
		//	mark = 0;
		//	t = searchAdj(graph, misPoint[i], repPath);
		//	if (t != -1)
		//	{
		//		for (j = 0; j < repPath.size(); j++)
		//		{
		//			if (t == repPath[j])
		//			{
		//				mark = j;
		//				break;
		//			}
		//		}
		//		if (mark > 0 && mark < repPath.size())
		//		{	
		//			merge(graph, repPath, mark, t);
		//			repPath = path;
		//		}
		//	}
		//}
		//
		for (i = 0; i < repPath.size(); i++)
		{
			for (j = 0; j < misPoint.size(); j++)
			{
				p = graph->nodeList[repPath[i]].firstedge;
				
				while (p != NULL)
				{
					if (p->adj == misPoint[j])
					{
						mergePath(graph, repPath, misPoint, i, j);
						repPath = path;
						break;
					}
					p = p->next;
				}
			}
		}
	}
	return;
}

void dfs(Graph *graph, vector<int>&bookNode, vector<bool>&invaNode, int curPoint)
{
	int i;
	EdgeNode *p = graph->nodeList[curPoint].firstedge;
	if (path[path.size() - 1] == finalPoint)
	{
		if (path.size() < Length - 1)
		{
			repeatNodeEntr(graph);
		}
		if (path.size() <= Length)
		{
			if (testPath(path))
			{
				int gC = getCost(graph, path);
				if (gC < minCost)
				{
					minCost = gC;
					getResult(path);
				}
			}
		}
	}
	if (path.empty())
		return;
	else{
		while (p != NULL)
		{
			i = p->adj;
			if (bookNode[i] == 0 && invaNode[i])
			{
				path.push_back(i);
				if (getCost(graph, path) >= minCost)	//剪枝1
				{
					path.pop_back();
					p = p->next;
					continue;
				}

				if (path.size() > Length)			//剪枝2
				{
					path.pop_back();
					p = p->next;
					continue;
				}
				else if (path.size() == Length && path[path.size() - 1] != finalPoint)
				{
					path.pop_back();
					p = p->next;
					continue;
				}
				else if (!testCutPath(path))
				{
					path.pop_back();
					p = p->next;
					continue;
				}
				bookNode[i] = 1;
				dfs(graph, bookNode, invaNode, i);
				path.pop_back();
				bookNode[i] = 0;
			}
			p = p->next;
		}
	}
	return;
}

void solve(Graph *graph, vector<int>&bookNode, vector<bool>&invaNode)
{
	cout << "------------------------ Result -----------------------" << endl;
	if (recoData)
	{
		cout << "Data Error." << endl;
		return;
	}
	cout << "Waiting..." << endl;
	path.push_back(startPoint);
	bookNode[startPoint] = 1;
	invaNode[startPoint] = true;
	dfs(graph, bookNode, invaNode, startPoint);
	if (minCost == INF)
	{
		cout << "There Is No Eligible Path." << endl;
	}
	else{
		cout << "Path With Minimal Cost: " << endl;
		cout << "Cost: " << minCost << endl;
		cout << "Path: ";
		coutResult();
	}
	return;
}

int main()
{
	clock_t start = clock();
	cout << "------------------------ Start ------------------------" << endl;
	vector <int> data;
	Graph graph;
	readFile(data);							//读取测试文件
	initGraph(&graph, data);				//初始化数据
	getRepeatPoint();						//记录必经节点
	testData();								//测试读入数据
	vector <int> bookNode(pointNum + 1);	//标记走过节点
	vector <bool> invaNode(pointNum + 1);	//无效点
	cutGraph(&graph, invaNode);				//优化1:缩小地图
	solve(&graph, bookNode, invaNode);		//寻找最短路径
	clock_t over = clock();
	double RunningTime = (double)(over - start) / CLOCKS_PER_SEC;
	cout << "Running Time: " << RunningTime << endl;
	cout << "------------------------- End -------------------------" << endl;
	return 0;
}
