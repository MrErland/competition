// https://www.nowcoder.com/acm/contest/6/C
// https://blog.csdn.net/a664607530/article/details/74912459
// 参考以上博客
#include<cstdio>
#include<vector>
#include<cstring>
#include<algorithm>
using namespace std;
char s1[60],s2[60];
bool dp[60][60][60][60];

int main()
{
    int t;
    scanf("%d",&t);
    while(t--)
    {
        scanf("%s%s",s1+1,s2+1);
        int len1=strlen(s1+1),len2=strlen(s2+1);
        memset(dp,0,sizeof dp);
        int ans=0;
        for(int d1=0; d1<=len1; d1++)
        {
            for(int d2=0; d2<=len2; d2++)
            {
                for(int i=1,j=d1; j<=len1; i++,j++)
                {
                    for(int k=1,l=d2; l<=len2; k++,l++)
                    {
                        if(d1+d2<=1) dp[i][j][k][l]=1;
                        else
                        {
                            if(d1>1&&s1[i]==s1[j]) dp[i][j][k][l]|=dp[i+1][j-1][k][l];
                            if(d1&&d2&&s1[i]==s2[l]) dp[i][j][k][l]|=dp[i+1][j][k][l-1];
                            if(d1&&d2&&s2[k]==s1[j]) dp[i][j][k][l]|=dp[i][j-1][k+1][l];
                            if(d2>1&&s2[k]==s2[l]) dp[i][j][k][l]|=dp[i][j][k+1][l-1];
                        }
                        if(dp[i][j][k][l])  ans=max(ans,d1+d2);
                    }
                }
            }
        }
        printf("%d\n",ans);
    }
    return 0;
}
