data Tree
    = Leaf Integer
    | Node Tree Tree
    deriving Show

minDepth :: Int
minDepth = 4

hashLeaf :: Integer -> Integer
hashLeaf x =
    (x * 131) + 7

hashNode :: Integer -> Integer -> Integer
hashNode leftHash rightHash =
    (leftHash * 31) + (rightHash * 17) + 1

makeTree :: Int -> Integer -> Tree
makeTree depth seed =
    if depth == 0
    then Leaf seed
    else
        Node
            (makeTree (depth - 1) (seed * 2))
            (makeTree (depth - 1) ((seed * 2) + 1))

computeHash :: Tree -> Integer
computeHash tree =
    case tree of
        Leaf value ->
            hashLeaf value

        Node left right ->
            hashNode
                (computeHash left)
                (computeHash right)

sumTrees :: Int -> Int -> Integer -> Integer
sumTrees depth count acc =
    if count == 0
    then acc
    else
        sumTrees
            depth
            (count - 1)
            (acc + computeHash (makeTree depth (fromIntegral count)))

pow2 :: Int -> Int
pow2 n =
    if n == 0
    then 1
    else 2 * pow2 (n - 1)

depthLoop :: Int -> Int -> Integer -> Integer
depthLoop depth maxDepth acc =
    if depth <= maxDepth
    then
        depthLoop
            (depth + 2)
            maxDepth
            (acc + sumTrees depth (pow2 (maxDepth - depth + minDepth)) 0)
    else acc

main :: IO ()
main =
    print
        ( depthLoop minDepth 9 0
        + computeHash (makeTree 7 1)
        + computeHash (makeTree 8 1)
        )

--data Tree
--    = Leaf Int
--    | Node Tree Tree
--    deriving Show
--
--minDepth :: Int
--minDepth = 4
--
--hashLeaf :: Int -> Int
--hashLeaf x =
--    (x * 131) + 7
--
--hashNode :: Int -> Int -> Int
--hashNode leftHash rightHash =
--    (leftHash * 31) + (rightHash * 17) + 1
--
--makeTree :: Int -> Int -> Tree
--makeTree depth seed =
--    if depth == 0
--    then Leaf seed
--    else
--        Node
--            (makeTree (depth - 1) (seed * 2))
--            (makeTree (depth - 1) ((seed * 2) + 1))
--
--computeHash :: Tree -> Int
--computeHash tree =
--    case tree of
--        Leaf value ->
--            hashLeaf value
--
--        Node left right ->
--            hashNode
--                (computeHash left)
--                (computeHash right)
--
--sumTrees :: Int -> Int -> Int -> Int
--sumTrees depth count acc =
--    if count == 0
--    then acc
--    else
--        sumTrees
--            depth
--            (count - 1)
--            (acc + computeHash (makeTree depth count))
--
--pow2 :: Int -> Int
--pow2 n =
--    if n == 0
--    then 1
--    else 2 * pow2 (n - 1)
--
--depthLoop :: Int -> Int -> Int -> Int
--depthLoop depth maxDepth acc =
--    if depth <= maxDepth
--    then
--        depthLoop
--            (depth + 2)
--            maxDepth
--            (acc + sumTrees depth (pow2 (maxDepth - depth + minDepth)) 0)
--    else acc
--
--main :: IO ()
--main =
--    print
--        ( depthLoop minDepth 9 0
--        + computeHash (makeTree 7 1)
--        + computeHash (makeTree 8 1)
--        )