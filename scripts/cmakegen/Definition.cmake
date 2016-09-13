if (DEFINED %variable)
    add_definitions(-D%variable=${%variable})
else (DEFINED %variable)
    add_definitions(-D%variable="%value")
endif (DEFINED %variable)

