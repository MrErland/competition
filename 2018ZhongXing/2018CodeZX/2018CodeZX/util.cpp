#include "util.h"

int Node_Num;
int Line_Num;
int Business_Num;
int Channel_Num;
char Use_Percent[10];
int Line_Max_One, Line_Max_Two;
bool IsNA = false;
int graph[Size_Graph][Size_Graph];
Business business[Size_Business];

void split_string(const string& s, vector<int>& v, const string& c)
{
	string::size_type pos1, pos2;
	pos2 = s.find(c);
	pos1 = 0;
	while (string::npos != pos2)
	{
		v.push_back(atoi(s.substr(pos1, pos2 - pos1).c_str()));
		pos1 = pos2 + c.size();
		pos2 = s.find(c, pos1);
	}
	//if (pos1 != s.length())
	//{
	//	v.push_back(atoi(s.substr(pos1).c_str()));
	//}
}

void init_data(char *data[])
{
	for (int m = 0; m < Size_Graph; m++)
	{
		for (int n = 0; n < Size_Graph; n++)
		{
			if (m == n) graph[m][n] = 0;
			else graph[m][n] = INF_COST;
		}
	}
	int i = 0;
	sscanf(data[i++], "%d %d", &Node_Num, &Line_Num);
	int a, b, c;
	for (int j = 0; j < Line_Num; j++)
	{
		sscanf(data[i++], "%d %d %d", &a, &b, &c);
		graph[a][b] = c;
		graph[b][a] = c;
	}
	sscanf(data[i++], "%d %d", &Business_Num, &Channel_Num);
	for (int j = 0; j < Business_Num; j++)
	{
		sscanf(data[i++], "%d %d", &business[j].index, &business[j].flow);
		business[j].path.resize(Channel_Num);
		for (int k = 0; k < Channel_Num; k++)
		{
			string tmp(data[i++]);
			split_string(tmp, business[j].path[k], " ");
		}
	}
}

string out_result(bool isNa)
{
	string res = "";
	if (isNa) { res += "NA\n"; return res; }
	res += (string(Use_Percent) + " " + to_string(Line_Max_One) + " " + to_string(Line_Max_Two) + "\n");
	for (int i = 0; i < Business_Num; i++)
	{
		res += (to_string(business[i].index) + " " + to_string(business[i].flow) + "\n");
		int pos = business[i].result_index;
		for (unsigned int j = 0; j < business[i].path[pos].size(); j++)
		{
			res += (to_string(business[i].path[pos][j]));
			if (j < business[i].path[pos].size() - 1)  res += " ";
		}
		res += "\n";
	}
	return res;
}