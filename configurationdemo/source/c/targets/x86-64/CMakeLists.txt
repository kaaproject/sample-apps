# Target support library that provides abstractions for x86-64 target.
add_library(target_support INTERFACE)
target_include_directories(target_support INTERFACE ${CMAKE_CURRENT_SOURCE_DIR})
