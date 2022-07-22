%imports partitions from CSV file
% change the import file destination to correct one
import:-
    csv_read_file('/Users/yasin/Desktop/Prolog Comprehensive/partition65.csv', Data65, [functor(partition)]),maplist(assert, Data65),
    csv_read_file('/Users/yasin/Desktop/Prolog Comprehensive/partition74.csv', Data74, [functor(partition)]),maplist(assert, Data74),
    csv_read_file('/Users/yasin/Desktop/Prolog Comprehensive/partition75.csv', Data75, [functor(partition)]),maplist(assert, Data75),
    csv_read_file('/Users/yasin/Desktop/Prolog Comprehensive/partition76.csv', Data76, [functor(partition)]),maplist(assert, Data76),
    csv_read_file('/Users/yasin/Desktop/Prolog Comprehensive/partition84.csv', Data84, [functor(partition)]),maplist(assert, Data84),
    csv_read_file('/Users/yasin/Desktop/Prolog Comprehensive/partition85.csv', Data85, [functor(partition)]),maplist(assert, Data85),
    csv_read_file('/Users/yasin/Desktop/Prolog Comprehensive/partition86.csv', Data86, [functor(partition)]),maplist(assert, Data86),listing(partition).

%base case
mergeClusters([],R,L):-!,sort(R,L). % base case

%loop through every cluster until base case is reached
% findall(...) : find all the points of a specific cluster
% inter(...) : find the intersecting points of the gloabl list and the specific cluster list just found
% relabel(...) : relabels the points of cluster list with label O if the point ID's are the same
% union(...) : add the cluster list to the gloabl list

% first cluster needs to be added to the global cluster list
mergeClusters([CH|CT],[],L):-
  findall([D,X,Y,CH],partition(_,D,X,Y,CH),Clist),
  mergeClusters(CT,Clist,L),!.

% every other cluster added this way
mergeClusters([CH|CT],L,R):-
  findall([D,X,Y,CH],partition(_,D,X,Y,CH),Clist),
  inter(Clist,L,I),
  relabel(CH,I,L,M),
  union(Clist,M,N),
  mergeClusters(CT,N,R),!.


% mergeClusters/4
%produces the list of all points with their cluster ID
% findall(...) : find all the cluster ID's of every partition and insert it into a list
% sort(...) : sort the list, removing duplicates
mergeClusters(L):-
  findall(A,partition(_,_,_,_,A),K),
  sort(K,X),
  mergeClusters(X,[],L),!.

%merge(C,M,N,L):-

% same_pointID/2
% returns true or false wether point ID is in selected list
% built in prolog predicate
% ?- same_pointID([1,1,2,3],[[2,1,2,3],[3,2,5,4],[1,2,3,4]]).
% true .
% ?- same_pointID([1,1,2,3],[[2,1,2,3],[3,2,5,4],[3,2,3,4]]).
% false.

same_pointID([A,_,_,_],[[A,_,_,_]|_]).
same_pointID([A,_,_,_],[_|T]):- same_pointID([A,_,_,_],T).


% relabel/4
% relabels the points of Clist with label O if the point ID's are the same
% ?- relabel(5,[[1,2,3,5],[2,3,4,5]],[[1,2,3,7],[4,3,6,8],[7,3,6,8],[9,3,6,8],[2,0,0,0]],X).

relabel(O,I,CList,COUT):- relabel(O,I,CList,[],COUT),!.

relabel(_,_,[],L,L):-!.

relabel(O,I,[[C1,C2,C3,C4]|TIN],COUT,L):- same_pointID([C1,C2,C3,C4],I),relabel(O,I,TIN,[[C1,C2,C3,O]|COUT],L).
relabel(O,I,[[C1,C2,C3,C4]|TIN],COUT,L):- relabel(O,I,TIN,[[C1,C2,C3,C4]|COUT],L).





% inter/3
% Finds the intersecting points
% built in prolog predicate
% ?- inter([[1,1,2,3],[3,2,3,4]],[[2,1,2,3],[3,2,5,4]],X).

test():-write('inter([[1,1,2,3],[3,2,3,4]],[[2,1,2,3],[3,2,5,4]],X)'),inter([[1,1,2,3],[3,2,3,4]],[[2,1,2,3],[3,2,5,4]],X).

inter([], _, []):-!.

inter([H1|T1], L2, [H1|Res]) :-
    same_pointID(H1, L2),
    inter(T1, L2, Res),!.

inter([_|T1], L2, Res) :-
    inter(T1, L2, Res),!.
