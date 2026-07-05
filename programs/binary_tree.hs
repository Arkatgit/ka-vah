data Tree
    = EmptyTree
    | Node Int Tree Tree
    deriving (Show)

leftItem :: Int -> Int
leftItem item = (item * 2) - 1

rightItem :: Int -> Int
rightItem item = item * 2

nextDepth :: Int -> Int
nextDepth depth = depth - 1

makeLeft :: (Int -> Int -> Tree) -> Int -> Int -> Tree
makeLeft build item depth =
    build (leftItem item) (nextDepth depth)

makeRight :: (Int -> Int -> Tree) -> Int -> Int -> Tree
makeRight build item depth =
    build (rightItem item) (nextDepth depth)

makeTree :: Int -> Int -> Tree
makeTree item depth =
    if depth <= 0
    then Node item EmptyTree EmptyTree
    else
        Node
            item
            (makeLeft makeTree item depth)
            (makeRight makeTree item depth)

checkTree :: Tree -> Int
checkTree tree =
    case tree of
        EmptyTree -> 0
        Node item left right ->
            item + ((checkTree left) - (checkTree right))

main :: IO ()
main =
    print (checkTree (makeTree 21 10))