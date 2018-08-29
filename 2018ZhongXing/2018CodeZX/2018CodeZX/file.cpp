#include "file.h"
#include "util.h"

int read_file(char* const path, char** buff)
{
	FILE *fp = fopen(path, "r");
	if (fp == NULL) return 0;
	char line[MAX_LINE_LEN + 2];
	unsigned int cnt = 0;
	while (!feof(fp))
	{
		line[0] = 0;
		if (fgets(line, MAX_LINE_LEN + 2, fp) == NULL)  continue;
		buff[cnt] = (char *)malloc(MAX_LINE_LEN + 2);
		strncpy(buff[cnt], line, MAX_LINE_LEN + 2 - 1);
		buff[cnt][MAX_LINE_LEN + 1] = 0;
		cnt++;
	}
	fclose(fp);
	return cnt;
}

void write_result(char* const path, const char* buff)
{
	if (buff == NULL) return;
	const char *write_type = "w";
	FILE *fp = fopen(path, write_type);
	if (fp == NULL)	return;
	fputs(buff, fp);
	fputs("\n", fp);
	fclose(fp);
}

void release_buff(char **buff, int len)
{
	for (int i = 0; i < len; i++) free(buff[i]);
}