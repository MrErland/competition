// https://www.nowcoder.com/acm/contest/5/B
#include <iostream>
#include <cstdio>
#include <vector>
#include <cmath>
using namespace std;

int main()
{
    int diff = 999999999;
    int n, m;
    vector<int> first;
    vector<int> second;
    cin >> n;
    while(n--)
    {
        scanf("%d", &m);
        first.push_back(m);
    }
    cin >> n;
    while(n--)
    {
        scanf("%d", &m);
        second.push_back(m);
    }
    n = first.size();
    m = second.size();
    for(int i=0;i<=m-n;i++)
    {
        int t = 0;
        for(int j=0,s=i;j<n;j++,s++) t += pow(first[j]-second[s], 2);
        if(t < diff) diff = t;
        if(diff == 0) break;
    }
    cout << diff << endl;
    return 0;
}

