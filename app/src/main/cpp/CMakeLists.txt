cmake_minimum_required(VERSION 3.18.1)
project(humanchess)

add_library(
    humanchess
    SHARED
    chess-engine.cpp
    native-lib.cpp
)

find_library(log-lib log)

target_link_libraries(humanchess ${log-lib})
