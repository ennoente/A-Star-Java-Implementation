# A-Star-Java-Implementation
This project is an implementation of the A* pathfinding algorithm for Java. It is based on code from "codebytes.in"

Generally, I learned what I know about the A* algorithm from these sources:

    Codebytes:                          http://www.codebytes.in/2015/02/a-shortest-path-finding-algorithm.html
    
    YouTube: The Coding Train
    Coding challenge 51, A* algorithm   https://www.youtube.com/watch?v=aKYlikFAV4k
    
    YouTube: Sebastian Lague
    A* explanation                      https://www.youtube.com/watch?v=-L-WgKMFuhE
    

There are several changes I made to codebytes' code.
The biggest of which is that I built a GUI showing the progress,
as well as the open set (green tiles), the closed set (red tiles) and the path once the algorithm has finished (cyan tiles).
Another change is the option to press ENTER and let the algorithm complete on its own, or to make one step each time you press ENTER.
Also I reversed lambda expressions to work on older versions.

Please note that I am neither a professional nor a student, but a self-taught nerd. I am not sure if the code I provide is optimal
(are JPanels the best way to display each Cell? Worked fine for me, but... I don't know...)
