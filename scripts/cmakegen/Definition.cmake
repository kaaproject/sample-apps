if (NOT DEFINED %variable)
    set(%variable "%value")
endif (NOT DEFINED %variable)
add_definitions(-D%variable="${%variable}")

