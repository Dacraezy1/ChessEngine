cmake_minimum_required(VERSION 3.22.1)
project("chessengine")

add_library(
    chessengine
    SHARED
    chess-engine.cpp
    native-lib.cpp
)

find_library(log-lib log)

target_link_libraries(
    chessengine
    ${log-lib}
)
