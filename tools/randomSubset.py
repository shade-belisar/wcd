import gzip
import sys

from random import randint

file = sys.argv[1]
number = int(sys.argv[2])

lineCount = 0

with gzip.open(file) as input:
    for line in input:
        lineCount += 1

if lineCount - 2 < number:
    print "ERROR: File has less lines than should be extracted"
    print lineCount - 2
    sys.exit(1)

lines = set()
for i in xrange(0,number):

    while True:
        number = randint(1,lineCount - 1)
        if number not in lines:
            break
    lines.add(number)

with gzip.open(file) as input, gzip.open(str(number) + "_random.json.gz", mode="wb") as output:
    output.write("[\n")

    for i, line in enumerate(input):

        if i in lines:
            lines.remove(i)
            if len(lines) == 0:
                if line.endswith(",\n"):
                    line = line[:-1] + "\n"
            else:
                if not line.endswith(",\n"):
                    line = line[:-1] + ",\n"
            output.write(line)

    output.write("]\n")
