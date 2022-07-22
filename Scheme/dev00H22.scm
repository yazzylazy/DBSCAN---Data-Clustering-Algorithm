#lang racket
; Yasin Elmi 300163765
(define (readlist filename)
 (call-with-input-file filename
  (lambda (in)
    (read in))))

(define (import)
  (let ((p65 (readlist "partition65.scm"))
        (p74 (readlist "partition74.scm")) 
        (p75 (readlist "partition75.scm")) 
        (p76 (readlist "partition76.scm")) 
        (p84 (readlist "partition84.scm")) 
        (p85 (readlist "partition85.scm")) 
        (p86 (readlist "partition86.scm")))
    (append p65 p74 p75 p76 p84 p85 p86)))

; save list to output file
(define (saveList filename L)
(call-with-output-file filename (lambda (out) (write L out))))


; gets the last element of the list which is the cluster ID
; input : point
; output : cluster ID
(define (cluster-ID l)
  (cond ((null? (cdr l)) (car l))
        (else (cluster-ID (cdr l)))))

; append-list taken from class
; input : 2 lists 
; output : 1 list
(define (append-list L1 L2)
  (if (null? L1) L2 (cons (car L1) (append-list (cdr L1) L2))))

;invert-list taken from class
; input : 1 list
; output : 1 list 
(define (invert-list L)
  (if (null? L) '() (append-list (invert-list (cdr L)) (list (car L)) )))

; removes the unchanged labels from the global list
; input : list of points
; output : list of points (removeing duplicate ID)
(define (remove-same-ID list)
  (cond
    ((null? list) '())
    ((intersecting? (car list) (cdr list)) (remove-same-ID (cdr list)))
    (else (cons (car list) (remove-same-ID (cdr list))))
    ))

;changes the value of list at specific index
; input : list, index , value
; output : list with changed value at index
(define (update l i val)
  (if (null? l) l
    (cons (if (zero? i) val (car l))
      (update (cdr l) (- i 1) val))))

;relabel and create a new cluster-list by changing labels of intersecting points
; input : list of intersecting points and cluster list
; output : list of relabeled cluster points

(define (relabel inter-list cluster-list)
  (cond
    ((null? cluster-list) '())
    ((intersecting? (car cluster-list) inter-list) (cons (update (car cluster-list) 3 (cluster-ID (car inter-list))) (relabel inter-list (cdr cluster-list)) ))
    (else (cons (car cluster-list) (relabel inter-list (cdr cluster-list))))
    ))


;Returns true if point is intersecting with given list
;(intersection '((1 2 3) (2 3 4) (3 4 5)) '((3 6 7) (2 7 8) (1 2 3)))
; input : point and list of points
; output : true or false
(define (intersecting? point list)
  (cond
    ((null? list) #f)
    ((= (car point) (car (car list))) #t)
    (else (intersecting? point (cdr list)))
  ))


;takes a clusterlist and global list of partition points and returns list of intersecting points between the two lists
; input : cluster list and global list of points
; output : list of intersecting points
(define (intersection clist list)
  (if (null? clist) '()
  (if (intersecting? (car clist) list) (cons (car clist) (intersection (cdr clist) list))
  (intersection (cdr clist) list))))

;transform the global list removing partition number
; input : golbal list of points
; output : global list of points(removing partition number)
(define (remove-partition-no glist)
  (if (null? glist) '() (cons (cdr(car glist)) (remove-partition-no (cdr glist)))))


; general loop that goes over every single point and creates the cluster list
; input : lists of points
; output : list of points
(define (mergeClusters glist)
   (remove-same-ID (invert-list (remove-duplicates (loop-2 (remove-partition-no glist) '())))))

(define (loop-2 glist clist)
  (cond
    ((null? glist) (relabel (intersection clist glist) clist))
    ((null? clist) (append-list (extract-cluster glist (cluster-ID (car glist))) (loop-2 (update-list glist (cluster-ID (car glist))) (extract-cluster glist (cluster-ID (car glist))) )))
    (else (append-list (extract-cluster glist (cluster-ID (car glist))) (loop-2 (update-list glist (cluster-ID (car glist))) (append-list (extract-cluster glist (cluster-ID (car glist)))  (relabel (intersection clist glist) clist) )))
    )))

;extract cluster-list here that takes imported list and clusterID and outputs list of points belonging to a specififc cluster
; input : lists of points and cluster ID
; output : list of points
(define (extract-cluster list CID)
  (extract-cluster-2 list CID))

(define (extract-cluster-2 list CID)
  (cond
    ((null? list) '())
    ((= (cluster-ID (car list)) CID) (cons (car list) (extract-cluster-2 (cdr list) CID) ))
    (else (extract-cluster-2 (cdr list) CID) )))

;this function is used in tandem with extract-cluster
; input : list of pointers and cluster ID
; output : list of points
(define (update-list list CID)
  (update-list-2 list CID))

(define (update-list-2 list CID)
  (cond
    ((null? list) '())
    ((not(= (cluster-ID (car list)) CID)) (cons (car list) (update-list-2 (cdr list) CID) ))
    (else (update-list-2 (cdr list) CID) )))

;Call to the function
(mergeClusters (import))
(saveList "mergeClusters" (mergeClusters (import)))






