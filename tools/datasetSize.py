import gzip
import sys

path = sys.argv[1]
if not path.endswith("/") and not path == "":
    path += "/"

fileExtension = ".csv.gz"

def file_len(fname):
    i = 0
    try:
        with gzip.open(path + fname + fileExtension) as f:
            for i, l in enumerate(f):
                pass
        return i + 1
    except IOError:
        return 0

files = ["statement", "qualifier", "reference", "items", "properties", "units", "ranks", "first", "next", "last", "firstQualifier", "nextQualifier", "lastQualifier"]
constraints = [
    ("q21502410","distinct values"),
    ("q21514353","allowed units"),
    ("q21510851","allowed qualifiers"),
    ("q21510859","one-of"),
    ("q52712340","one-of qualifier value"),
    ("q21503247","item requires statement"),
    ("q21510864","value requires statement"),
    ("q21510855","inverse"),
    ("q21510862","symmetric"),
    ("q21510857","multi-value"),
    ("q19474404","single value"),
    ("q21510856","mandatory qualifier"),
    ("q52060874","single best value")
    ]


for f in files:
    print f + "," + str(file_len(f))

for f, n in constraints:
    print n + "," + str(file_len(f + "/letter0"))
