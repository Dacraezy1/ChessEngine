#include <cstring>
#include <algorithm>
#include <cstdlib>
#include <ctime>

// Mailbox 10x12 Board Representation
static int mailbox[120] = {
    -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
     -1, 0, 1, 2, 3, 4, 5, 6, 7,-1,
     -1, 8, 9,10,11,12,13,14,15,-1,
     -1,16,17,18,19,20,21,22,23,-1,
     -1,24,25,26,27,28,29,30,31,-1,
     -1,32,33,34,35,36,37,38,39,-1,
     -1,40,41,42,43,44,45,46,47,-1,
     -1,48,49,50,51,52,53,54,55,-1,
     -1,56,57,58,59,60,61,62,63,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,-1,-1
};

enum Piece { EMPTY, PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING };
enum Color { WHITE, BLACK };

struct Position {
    int board[64];
    bool whiteToMove;
    int ply;
};

Position pos;
int currentElo = 1500;

const int pst[7][64] = {
    {}, // EMPTY
    { // PAWN
         0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    },
    { // KNIGHT
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    },
    { // BISHOP
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    },
    { // ROOK
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         0,  0,  0,  5,  5,  0,  0,  0
    },
    { // QUEEN
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    },
    { // KING
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
         20, 30, 10,  0,  0, 10, 30, 20
    }
};

void init_position() {
    memset(pos.board, 0, sizeof(pos.board));
    // Set standard starting position
    pos.board[0] = ROOK | (BLACK << 3);
    pos.board[1] = KNIGHT | (BLACK << 3);
    pos.board[2] = BISHOP | (BLACK << 3);
    pos.board[3] = QUEEN | (BLACK << 3);
    pos.board[4] = KING | (BLACK << 3);
    pos.board[5] = BISHOP | (BLACK << 3);
    pos.board[6] = KNIGHT | (BLACK << 3);
    pos.board[7] = ROOK | (BLACK << 3);

    for(int i=8; i<16; ++i)
        pos.board[i] = PAWN | (BLACK << 3);

    for(int i=48; i<56; ++i)
        pos.board[i] = PAWN | (WHITE << 3);

    pos.board[56] = ROOK | (WHITE << 3);
    pos.board[57] = KNIGHT | (WHITE << 3);
    pos.board[58] = BISHOP | (WHITE << 3);
    pos.board[59] = QUEEN | (WHITE << 3);
    pos.board[60] = KING | (WHITE << 3);
    pos.board[61] = BISHOP | (WHITE << 3);
    pos.board[62] = KNIGHT | (WHITE << 3);
    pos.board[63] = ROOK | (WHITE << 3);

    pos.whiteToMove = true;
    pos.ply = 0;
}

int evaluate(Position& p) {
    int score = 0;
    const int pieceValues[] = {0, 100, 320, 330, 500, 900, 20000};
    
    for (int sq = 0; sq < 64; ++sq) {
        int pc = p.board[sq] & 7;
        int col = (p.board[sq] >> 3) & 1;
        if (pc != EMPTY) {
            int val = pieceValues[pc] + pst[pc][sq];
            score += (col == WHITE ? val : -val);
        }
    }
    return p.whiteToMove ? score : -score;
}

int alphaBeta(Position& p, int depth, int alpha, int beta) {
    if (depth <= 0) return evaluate(p);

    // Dummy move generator stub
    for (int from = 0; from < 64; ++from) {
        if ((p.board[from] >> 3) != p.whiteToMove) continue;
        for (int to = 0; to < 64; ++to) {
            int captured = p.board[to];
            p.board[to] = p.board[from];
            p.board[from] = EMPTY;
            p.whiteToMove = !p.whiteToMove;
            int score = -alphaBeta(p, depth - 1, -beta, -alpha);
            p.board[from] = p.board[to];
            p.board[to] = captured;
            p.whiteToMove = !p.whiteToMove;
            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }
    }
    return alpha;
}

extern "C"
void Java_com_example_chessengine_MainActivity_setElo(JNIEnv*, jobject, jint elo) {
    currentElo = elo;
}

extern "C"
jstring Java_com_example_chessengine_MainActivity_getBestMove(JNIEnv* env, jobject) {
    init_position();
    
    // Simulate ELO-based thinking depth
    int depth = 1 + (currentElo - 800) / 400;
    if (depth < 1) depth = 1;
    if (depth > 5) depth = 5;
    
    srand(time(nullptr));
    int bestScore = -999999;
    int bestFrom = -1, bestTo = -1;

    for (int from = 0; from < 64; ++from) {
        if ((pos.board[from] >> 3) != pos.whiteToMove) continue;
        for (int to = 0; to < 64; ++to) {
            int captured = pos.board[to];
            pos.board[to] = pos.board[from];
            pos.board[from] = EMPTY;
            pos.whiteToMove = !pos.whiteToMove;
            int score = -alphaBeta(pos, depth, -999999, -bestScore);
            pos.board[from] = pos.board[to];
            pos.board[to] = captured;
            pos.whiteToMove = !pos.whiteToMove;
            if (score > bestScore) {
                bestScore = score;
                bestFrom = from;
                bestTo = to;
            }
        }
    }

    // If no move found, pick a random legal move
    if (bestFrom == -1) {
        for (int from = 0; from < 64; ++from) {
            if ((pos.board[from] >> 3) != pos.whiteToMove) continue;
            for (int to = 0; to < 64; ++to) {
                if (pos.board[to] == EMPTY || ((pos.board[to] >> 3) != pos.whiteToMove)) {
                    bestFrom = from;
                    bestTo = to;
                    break;
                }
            }
            if (bestFrom != -1) break;
        }
    }

    char moveStr[6];
    snprintf(moveStr, sizeof(moveStr), "%c%d%c%d",
             'a' + (bestFrom % 8), 8 - (bestFrom / 8),
             'a' + (bestTo % 8), 8 - (bestTo / 8));

    return env->NewStringUTF(moveStr);
}
