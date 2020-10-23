import sys
#sys.argv.append('-b')
import os, subprocess
from pdf2image import convert_from_path
from PIL import Image
import math



#pdf_dir = "./plots/new_data_2020/"
#os.chdir(pdf_dir)

def txtcombiner(txt_dir):

    with open("myfile.txt", "w") as file1: 
    # Writing data to a file 
    

        for txt_file in os.listdir(txt_dir):

            with open(txt_file,'r') as f_in:
                line = f_in.readline()
                while line:
                    file1.write(line)
                    line = f_in.readline()



pdf_dir = sys.argv[1]
txtcombiner(pdf_dir)