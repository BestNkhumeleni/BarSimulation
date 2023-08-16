#Variables
SRC_DIR := src
OUT_DIR := bin
#Rules
default:
	javac -d bin src/clubSimulation/*.java
clean:
	rm $(OUT_DIR)/*.class
	rm $(SRC_DIR)/*.class
	rm -rf $(OUT_DIR)