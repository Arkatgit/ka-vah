data List a
    = EmptyList
    | Cons a (List a)
    deriving Show

append :: List Int -> List Int -> List Int
append xs ys =
    case xs of
        EmptyList ->
            ys

        Cons x xt ->
            Cons x (append xt ys)

filterLE :: Int -> List Int -> List Int
filterLE pivot xs =
    case xs of
        EmptyList ->
            EmptyList

        Cons x xt ->
            if x <= pivot
            then Cons x (filterLE pivot xt)
            else filterLE pivot xt

filterGT :: Int -> List Int -> List Int
filterGT pivot xs =
    case xs of
        EmptyList ->
            EmptyList

        Cons x xt ->
            if x <= pivot
            then filterGT pivot xt
            else Cons x (filterGT pivot xt)

quickSort :: List Int -> List Int
quickSort xs =
    case xs of
        EmptyList ->
            EmptyList

        Cons pivot rest ->
            append
                (quickSort (filterLE pivot rest))
                (Cons pivot (quickSort (filterGT pivot rest)))

sumList :: List Int -> Int
sumList xs =
    case xs of
        EmptyList ->
            0

        Cons x xt ->
            x + sumList xt

makeList :: Int -> List Int
makeList n =
    if n == 0
    then EmptyList
    else
        Cons
            ((n * 37) - ((n `div` 27) * 999))
            (makeList (n - 1))

main :: IO ()
main =
    print (sumList (quickSort (makeList 150)))