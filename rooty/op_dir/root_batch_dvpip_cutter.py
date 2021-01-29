import subprocess
import os
import time
import shutil
from shutil import copyfile

#data_dir = "/mnt/d/CLAS12Data/converted_skim8_filters/"
data_dir = "../root_skims/"
output_dir = "DVEP_roots/"
data_list = os.listdir(data_dir)

print(data_list)


root_macro = "scriptPi0_new.C"

default_root_outname = "output_root_file.root"
default_root_inname = "input_root_file.root"

infile = "testerfile.txt"
outfile = data_dir+"fixed.txt"


#new_list = data_list[1:3]
#print(new_list)

for count,file in enumerate(data_list):
    print("Trying to process file {}".format(count))
    copyfile(data_dir+file,default_root_inname)
    process = subprocess.Popen(['root','-b','-q', root_macro],
                     stdout=subprocess.PIPE, 
                     stderr=subprocess.PIPE)
    stdout, stderr = process.communicate()
    #print("STDOUT is {}".format(stdout))
    print("STDERR is {}".format(stderr))
    shutil.move(default_root_outname,output_dir+file.replace(".root","_DVEP.root"))
    time.sleep(0.5)
