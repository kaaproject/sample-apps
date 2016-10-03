# This is required for ESP8266 platform
# due to it's specific requirements regarding linked executable.
# The blank.c file is a placeholder for CMake's add_executable()
# All the code (Kaa SDK, ESP8266 SDK and demo) is compiled as static libraries
# and linked into that executable.
add_subdirectory(targets/${KAA_PLATFORM})
if("${KAA_PLATFORM}" STREQUAL "esp8266")
    add_library(demo_client_s STATIC %APP_SOURCES)
    file(WRITE ${CMAKE_BINARY_DIR}/blank.c "")
    add_executable(demo_client ${CMAKE_BINARY_DIR}/blank.c)
    target_link_libraries(demo_client demo_client_s)
    target_link_libraries(demo_client_s %APP_LIBRARIES)
else()
    add_executable(demo_client %APP_SOURCES)
    target_link_libraries(demo_client %APP_LIBRARIES)
endif()
