// https://www.nowcoder.com/acm/contest/6/A
#include <cstdio>
#include <iostream>
#include <vector>
#include <string>
using namespace std;
long long int N, T, C;
struct CUP{double t; double c;};

int main()
{
    string posi = "Possible";
    string imps = "Impossible";
    cin >> N >> T >> C;
    vector<CUP> cup(N);
    vector<double> add(N);     // 需要加的水
    int minT = 99999999;
    int maxT = 0;
    for(int i=0;i<N;i++)
    {
        cin >> cup[i].t >> cup[i].c;
        if(cup[i].t < minT) minT = cup[i].t;
        if(cup[i].t > maxT) maxT = cup[i].t;
    }
    if(minT == maxT) {cout << posi << "\n"; printf("%.4f", 1.0*minT); return 0;}    // 最大温度和最小温度一样，则相等
    if(T > minT && T < maxT) {cout << imps << "\n";return 0;}   // 若温度在之间，则不可能存在
    if(T == minT || T == maxT) {cout << imps << "\n";return 0;}
    if(T < minT)       // 假如可行，则最大的温度为minT
    {
        double sum = 0;
        for(int i=0;i<N;i++)
        {
            add[i] = (cup[i].t * cup[i].c - minT * cup[i].c) / (minT - T);
            sum += add[i];
        }
        if(sum <= C) {cout << posi << "\n"; printf("%.4f", 1.0*minT); return 0;}
        else {cout << imps << "\n";return 0;}
    }
    else if(T > maxT)
    {
        double sum = 0;
        for(int i=0;i<N;i++)
        {
            add[i] = (maxT * cup[i].c - cup[i].c * cup[i].t) / (T - maxT);
            sum += add[i];
       //     cup[i].t = (cup[i].t * cup[i].c + T * add[i]) / (add[i] + cup[i].c);
            cup[i].c += add[i];
        }
        if(sum > C) {cout << imps << "\n";return 0;}
        else if(sum == C) {cout << posi << "\n"; printf("%.4f", 1.0*maxT); return 0;}
        else                // 剩余的水还可以继续分下去, 此时温度都相同，但是体积不同
        {
            double left = C - sum;
            double nowT = (double)(T + maxT) / 2;         // 用二分法去接近一个最大的温度
            double ansT = maxT;
            double side_left = maxT, side_right = T;
            vector <double> speed(N);
            for(int m=0;m<N;m++) speed[m] = cup[m].c * maxT;       // 加快速度
            while((int)(side_left * 100000) % 10 == 0)      // 注意这个左边的选择范围比要求更小，保证范围足够小
            {
                double s = 0;
                for(int i=0;i<N;i++) s += (nowT * cup[i].c - speed[i]) / (T - nowT);
                if(s > left)
                    side_right = nowT;
                else
                    side_left = nowT;
                nowT = (side_left + side_right) / 2;
            }
            double step = 0.01;
            ansT = side_left;
            while(1)
            {
                ansT += step;
                double s = 0;
                for(int i=0;i<N;i++) s += (ansT * cup[i].c - speed[i]) / (T - ansT);
                if(s > left) { ansT -= step; step *= 0.1;}
                if(step < 0.0001) break;
            }
            cout << posi << "\n";
            printf("%.4f", ansT);
        }
    }
    return 0;
}

