# Technical Challenge
This is an application used to process user_access_log file into the database and we can filter and display ips by duration and request limit. 

## Content
View Project Structure description and Instruction.

```
.
├── /src/
├──── /main/
├────── /java/
├──────── /com/dot/
├────────── /entity                                 -> contains classes representing the database tables
├────────── /exception                              -> contains custom excpetion class
├────────── /repo                                   -> contains classes used to interact with database and tables
├────────── /util                                   -> contains all constants
├────────── FileReader                              -> Main Class
├────── /resources
├──────── /META-INF/persistence.xml                 -> contains database configuration
├──────── /log4j2.xml                               -> contains logging configuration
├──── /test                                         -> contains all test classes
├── /target                                         -> contains auto generated file
├── .gitignore                                      -> contains all files and folder that shouldn't be pushed to git
├── pom.xml                                         -> file used to manage dependencies  
└── README.md                                       -> application structure description and startup/setup guide
    
Instructions 

    1. To setup the application,  
        a. Ensure that jdk11 is installed
        b. Ensure that maven is installed 
        c. You will have to clone the project.
        d. Go to the project root directory on the terminal 
        e. Run the maven command on the terminal "mvn clean compile assembly:single" to generate the packaged jar with its dependencies which will be in the target directory
    
    2. Run the application by running the sample command on the terminal 'java -cp target/file-processor-jar-with-dependencies.jar com.dot.FileReader --accessFile=/Users/stanlee/Downloads/user_access.txt --start=2022-01-01.13:00:00 --duration=hourly -limit=100' in the root directory. note that values can be changes based on what you want

```