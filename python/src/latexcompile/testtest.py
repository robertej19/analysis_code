import os
import subprocess
import environment_tex_gen
from os import listdir
from os.path import isfile, join
import sys

data = ""
with open("examplefile2.txt",'r') as f_in:
    line = f_in.readline()
    while line:
        data += line + r"""\\"""
        line = f_in.readline()




data = data.replace("%","\%")
data = data.replace("_","\_")


print(data)
