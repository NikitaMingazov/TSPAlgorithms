# TSPAlgorithms
Algorithms designed to solve the Travelling Salesman Optimisation problem <br>
The first is Dynamic Programming, the second is Hill Climbing

<h1>to use DynaTSP:</h1> <br>
java -Xmx64g DynaTSP [filename] <br>
    -Xmx[int]g is how many gigabytes to allocate <br>
        no clue how much memory my program really requires <br>
    replace [filename] with the name of file you want to solve <br>

<h1>to use ClimbTSP:</h1> <br>
java ClimbTSP [filename] [P] [K] <br>
    [P] is the number of iterations without a local maximum, in which case a plateau is assumed and a shuffle is performed (~7 neighbour operations at once and search that instead) <br>
    [K] is the number of shuffles without a new global maximum at which the search will end <br>
    as an improvement I removed the option to end after N iterations, so search ends when progress stops being made at the chosen depth level <br>
    Suggestion: P=1000,K=3000 - very fast and gets near-perfect solutions. Increase either value at the same 1:3 ratio for higher solution quality (especially for larger TSP instances, because time taken grows linearly while solution quality plummets)

