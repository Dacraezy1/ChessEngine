#include <jni.h>
#include <string>

extern "C" JNIEXPORT void JNICALL
Java_com_example_chessengine_MainActivity_setElo(JNIEnv*, jobject, jint elo);

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_chessengine_MainActivity_getBestMove(JNIEnv* env, jobject);
