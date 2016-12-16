# Multi-partite community detection
It discovers overlapping communities in hyper-network or multi-partite networks. A hyper-network is made of hyper-edges, where each hyper-edge contains 3 nodes, and in addition each node is of a different type. For instance, the nodes could be users, named entities and hashtags in Twitter.  Each community will contain nodes from all types or partites.
The algorithm is a based on a simple community detection method.
## how to run it
Compile it with java 1.8, and then run it as: 
`javaClass  -parameter  -inputHyperNetwork  -resultingCommunities`
The parameter is related to the strictness of the community detection.
The higher the number the more compact are the communities. Also,
it is possible that some nodes will not be included in any community,
they could be considered as noise.

### example
Let net be a hyper-network of 6 hyper-edges and three partites (denoted as columns)
`net=`
`1 2 3`
`5 4 3`
`5 6 7`
`9 8 3`
`11 10 7`
`13 12 7`

>


`java javaClass   ---6   ---net  ---results`

results ={
          "1":{"1":[1],"9":[1],"11":[2],"5":[1,2],"13":[2]},
          "2":{"2":[1],"4":[1],"6":[2],"8":[1],"10":[2],"12":[2]},
          "3":{"3":[1],"7":[2]}
         }
*  1, 2, 3 denote the three partites
*  the number in the brackets denote the community 
*  the number in quotes denotes the node, for instance node 5 belongs to communities 1 and 2