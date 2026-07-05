data Tree
    = EmptyTree
    | Node Int Tree Tree
    deriving Show

minN :: Int
minN = 4

makePrime :: Int -> Int -> Tree
makePrime n d =
    if d == 0
    then Node n EmptyTree EmptyTree
    else Node
        n
        (makePrime (n - 1) (d - 1))
        (makePrime (n + 1) (d - 1))

make :: Int -> Tree
make d =
    makePrime d d

check :: Tree -> Int
check tree =
    case tree of
        EmptyTree ->
            0

        Node item left right ->
            item + check left + check right

sumT :: Int -> Int -> Int -> Int
sumT d i acc =
    if i == 0
    then acc
    else sumT d (i - 1) (acc + check (make d))

pow2 :: Int -> Int
pow2 n =
    if n == 0
    then 1
    else 2 * pow2 (n - 1)

depthLoop :: Int -> Int -> Int -> Int
depthLoop d m acc =
    if d <= m
    then
        depthLoop
            (d + 2)
            m
            (acc + sumT d (pow2 (m - d + minN)) 0)
    else acc

main :: IO ()
main =
    print
        ( depthLoop minN 10 0
        + check (make 10)
        + check (make 10)
        )