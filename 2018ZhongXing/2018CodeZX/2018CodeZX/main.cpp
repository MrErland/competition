#include "file.h"
#include "GA.h"
#include "SA.h"
#include "util.h"

int main(int argc, char* argv[])
{
	clock_t tstart, tend;
	tstart = clock();

	printf("%s\n", argv[1]);
	char* data[MAX_DATA_LEN];
	int lines = read_file(argv[1], data);
	init_data(data);

//	GA();
	SA(0);

	release_buff(data, lines);
	printf("%s\n", argv[2]);
	write_result(argv[2], out_result(IsNA).c_str());

	tend = clock();
	printf("%.3fs using.\n", double(tend - tstart) / CLOCKS_PER_SEC);
	return 0;
}