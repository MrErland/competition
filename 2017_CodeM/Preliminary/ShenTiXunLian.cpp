// https://www.nowcoder.com/acm/contest/6/D
#include <cstdio>
using namespace std;
int main()
{
    int n;
    double u,v,c[1002],d[1002];
    double ans=0;
    scanf("%d%lf%lf", &n, &v, &u);
    for(int i=0;i<n;i++) scanf("%lf", &c[i]);
    for(int i=0;i<n;i++) scanf("%lf", &d[i]);
    for(int i=0;i<n;i++)    // 第i个人
    {
        double x = c[i];
        double y = d[i];
        for(int j=1;j<=n;j++)   // 作为第j个跑时花的时间
        {
            ans += (n*u)/(x - (n-j)*y - v);
        }
    }
    ans /= n;
    printf("%.3lf", ans);
    return 0;
}
