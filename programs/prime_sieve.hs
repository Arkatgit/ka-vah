data List a = EmptyList | Cons a (List a)
  deriving (Show, Eq)

rangeList :: Int -> Int -> List Int
rangeList start finish =
  if finish <= (start - 1)
  then EmptyList
  else Cons start (rangeList (start + 1) finish)

modList :: Int -> Int -> Int
modList x y =
  if x <= (y - 1)
  then x
  else modList (x - y) y

divisible :: Int -> Int -> Bool
divisible x y =
  modList x y == 0

filterMultiples :: Int -> List Int -> List Int
filterMultiples p xs =
  case xs of
    EmptyList -> EmptyList
    Cons x xt ->
      if divisible x p
      then filterMultiples p xt
      else Cons x (filterMultiples p xt)

sieve :: List Int -> List Int
sieve xs =
  case xs of
    EmptyList -> EmptyList
    Cons p rest ->
      Cons p (sieve (filterMultiples p rest))

primesUpTo :: Int -> List Int
primesUpTo n =
  if n <= 1
  then EmptyList
  else sieve (rangeList 2 n)

main :: IO ()
main =
  print (primesUpTo 300)