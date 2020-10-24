import sys
#sys.argv.append('-b')
import os, subprocess
from pdf2image import convert_from_path
from PIL import Image
import math



#pdf_dir = "./plots/new_data_2020/"
#os.chdir(pdf_dir)

def txtcombiner(txt_dir):

    #with open("myfile.txt", "w") as file1: 
    ## Writing data to a file 
    
    lists = []
    i = 0

    for txt_file in os.listdir(txt_dir):

        print(txt_file)
        

        lists.append([])

        #print(lists)

        with open(txt_file,'r') as f_in:
            line = f_in.readline()
            while line:
                event_number0 = line.split(',')
                #print(event_number0)
                event_number = int(event_number0[1])
                lists[i].append(event_number)
                #file1.write(line)
                line = f_in.readline()

            print(i)

        i +=1
    print(lists)
    return(lists)





pdf_dir = sys.argv[1]
lists = txtcombiner(pdf_dir)

a = lists[0]
b = lists[1]

i = 0
for element in b:
    #print(element)

    if element not in a:
       print(element)
       i += 1


print(i)





















