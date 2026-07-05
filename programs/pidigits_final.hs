data State = MkState Int Int Int
    deriving Show

composeState :: Int -> State -> State
composeState j s =
    case s of
        MkState n a d ->
            MkState
                (n * j)
                ((a + n * 2) * (j * 2 + 1))
                (d * (j * 2 + 1))

nextDigit :: State -> Int
nextDigit s =
    case s of
        MkState n a d ->
            (n * 3 + a) `div` d

safeDigit :: State -> Int -> Bool
safeDigit s q =
    case s of
        MkState n a d ->
            q == ((n * 4 + a) `div` d)

emitDigitState :: State -> Int -> State
emitDigitState s q =
    case s of
        MkState n a d ->
            MkState
                (n * 10)
                ((a - q * d) * 10)
                d

produceDigit :: Int -> State -> State
produceDigit j s =
    case composeState j s of
        MkState n a d ->
            let s2 = MkState n a d
                q  = nextDigit s2
            in
                if safeDigit s2 q
                then emitDigitState s2 q
                else produceDigit (j + 1) s2

checksum :: Int -> Int -> State -> Int -> Int
checksum count j s acc =
    if count == 0
    then acc
    else
        case produceDigit j s of
            MkState n a d ->
                let s2 = MkState n a d
                in checksum
                    (count - 1)
                    (j + 1)
                    s2
                    (acc + nextDigit s2)

main :: IO ()
main =
    print (checksum 13 1 (MkState 1 0 1) 0)