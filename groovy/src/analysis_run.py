#!/usr/bin/python

import sys
import subprocess
import os
import json


me = os.path.dirname(os.path.abspath(__file__))


command1 = ["./run_main_multi.sh","100","1","1","testing arg mechanism la la  la la ala"]

f_out = open("blah.txt", "w")

subprocess.call(command1,stdout=f_out)# 100 1 1 'testing arg mechanism la la  la la ala'  "])

#subprocess.call(["echo","$THEANA"])

with open('temp_filename.txt') as f:
    for line in f:
        output_filename = line


print(output_filename)


subprocess.call(["rm","temp_filename.txt"])
