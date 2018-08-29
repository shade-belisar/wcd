import csv
import os
import re
import sys

log = sys.argv[1]
out = os.path.dirname(log) + "/summary.tsv"

constraintIDLabel = "ConstraintID"
violationsLabel = "Violations"
totalTimeLabel = "Total time"
runtimeMaterializationLabel = "Runtime materialization"
iterationsLabel = "Iterations"
derivationsLabel = "Derivations"
rulesLabel = "Rules"
demandRulesLabel = "Demand rules"
totalRulesLabel = "Total rules"

with open(log) as log, open(out, "w") as summary:
    writer = csv.DictWriter(summary, [constraintIDLabel, violationsLabel, totalTimeLabel, runtimeMaterializationLabel, iterationsLabel, derivationsLabel, rulesLabel, demandRulesLabel, totalRulesLabel], delimiter="\t")
    writer.writeheader()

    blocks = list()
    block = list()

    rules = re.compile("Created ([0-9]+) rules.")
    demandRules = re.compile("Created ([0-9]+) additional demand-rules.")
    totalRules = re.compile("Added ([0-9]+) rules total.")
    iterations = re.compile("Iterations=([0-9]+)")
    runtime = re.compile("Runtime materialization = ([0-9]+),([0-9]+) milliseconds")
    derivations = re.compile("Total # derivations: ([0-9]+)")
    time = re.compile("Total time elapsed: ([0-9]+)ms")
    constraint = re.compile("Constraint: (Q[0-9]+), violations: ([0-9]+)")

    stats = dict()

    for line in log:
        rulesResult = rules.search(line)
        if rulesResult:
            stats[rulesLabel] = rulesResult.group(1)

        demandRulesResult = demandRules.search(line)
        if demandRulesResult:
            stats[demandRulesLabel] = demandRulesResult.group(1)

        totalRulesResult = totalRules.search(line)
        if totalRulesResult:
            stats[totalRulesLabel] = totalRulesResult.group(1)

        iterationsResult = iterations.search(line)
        if iterationsResult:
            stats[iterationsLabel] = iterationsResult.group(1)

        runtimeResult = runtime.search(line)
        if runtimeResult:
            stats[runtimeMaterializationLabel] = runtimeResult.group(1)

        derivationsResult = derivations.search(line)
        if derivationsResult:
            stats[derivationsLabel] = derivationsResult.group(1)

        timeResult = time.search(line)
        if timeResult:
            stats[totalTimeLabel] = timeResult.group(1)

        constraintResult = constraint.search(line)
        if constraintResult:
            stats[constraintIDLabel] = constraintResult.group(1)
            stats[violationsLabel] = constraintResult.group(2)
            writer.writerow(stats)
