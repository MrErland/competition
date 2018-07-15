// https://www.nowcoder.com/acm/contest/5/A
// Percent of Pass: 50%
#include <iostream>
#include <cstdio>
#include <vector>
#include <cstring>
#include <cmath>
using namespace std;
vector<bool> isPrime;

void getPrime(long long x)
{
    isPrime.resize(x+1, 1);
    for(long long i=2;i<=x;i++)
    {
        if(!isPrime[i]) continue;
        for(long long j=2;j<=x;j++)
        {
            long long t = i * j;
            if(t > x) break;
            isPrime[t] = 0;
        }
    }
}

int getint(long long now)
{
    long long t;
    while((t = now / 10) != 0)
        now = t;
    return now;
}

int main()
{
    long long left, right;
    cin >> left >> right;
    vector<int> ans(10);    // 0-9
    getPrime(right);
    for(long long x = left; x <= right; x++)
    {
        ans[1]++;
        if(x != 1) ans[getint(x)]++;
        if(isPrime[x]) continue;    // 素数
        double over = sqrt(x);
        for(long long start = 2; start <= over; start++)
        {
            long long a = x % start;
            if(a != 0) continue;
            long long b = x / start;
            if(b == start) ans[getint(start)]++;
            else {ans[getint(b)]++; ans[getint(start)]++;}
        }
    }
    for(int i=1;i<=9;i++) cout << ans[i] << endl;
    return 0;
}

