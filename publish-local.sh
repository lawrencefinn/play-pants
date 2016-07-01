#!/bin/bash

./pants publish.jar --no-dryrun --local=~/.m2/repository src/scala/playpants/tool:
#./pants publish.jar --no-dryrun --local=~/.ivy2/pants src/scala/playpants/tool:
