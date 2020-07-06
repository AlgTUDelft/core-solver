# core-solver
The Conditional Return Policy Solver used in the experiments of the article "Solving Transition-Independent Multi-Agent MDPs with Sparse Interactions." by Scharpff, Roijers, Oliehoek, Spaan, Whiteson and De Weerdt published in the Proceedings of the Thirtieth AAAI Conference on Artificial Intelligence.

The article can be cited as:
```
@inproceedings{Scharpff2016core,
    author =    {Scharpff, Joris and Roijers, Diederik M and  Oliehoek, Frans A and Spaan, Matthijs T J and de Weerdt, Mathijs M},
    title =     {Solving Transition-Independent Multi-agent {MDPs} with Sparse Interactions},
    booktitle = {Proceedings of the Thirtieth AAAI Conference on Artificial Intelligence},
    year =      2016,
}
```

The project folder contains an Eclipse project file that describes the Java project and its files. To run the solver, see the example code in the file ```src/core/test/TestCoRe.java```. For the experiments of the paper, the solver was run with Java version 1.6.

## Running CoRe on other domains
To run CoRe on other domains, first a domain description must implemented. This can be done by implementing the abstract classes ```Instance```, ```State``` and ```StateReward``` found in the ```src/core/domains``` folder. For an example of how to implement a domain, see the implementation of the Maintenance Planning Problem (mpp) domain.
