import sys
#sys.argv.append('-b')
import os, subprocess
from pdf2image import convert_from_path
from PIL import Image
import math
import pandas as pd


#pdf_dir = "./plots/new_data_2020/"
#os.chdir(pdf_dir)

def txtcombiner():

    #with open("myfile.txt", "w") as file1: 
    ## Writing data to a file 
    
    lists_andrey = []
    lists_bobby = []
    
    i = 0

    text1 = "andrei2.txt"
    text2 = "bobby.txt"


    with open(text2,'r') as f_in:
            line = f_in.readline()
            while line:
                event_number0 = line.split(',')
                #print(event_number0)
                event_number = int(event_number0[1])
                lists_bobby.append(event_number)
                #file1.write(line)
                line = f_in.readline()

    with open(text1,'r') as f_in:
            line = f_in.readline()
            #print(line)
            while line:
                event_number0 = line.split(',')
                #print(event_number0)
                event_number = int(event_number0[1][:-2])
                #print(event_number)
                #event_number = int(line)
                lists_andrey.append(event_number)
                #file1.write(line)
                line = f_in.readline()

    #print(lists_andrey)
    print(len(lists_andrey))
    print(pd.Series(lists_andrey).nunique())


    #print(lists_bobby)
    print(len(lists_bobby))
    print(pd.Series(lists_bobby).nunique())
    
    a = lists_andrey
    b = lists_bobby

    i = 0
    for element in a:
        #print(element)
        if element not in b:
            print(element)
            i += 1


    print(i)


    #for txt_file in os.listdir(txt_dir):

        #rint(txt_file)
"""  

        lists.append([])

        #print(lists)

        

            print(i)

        i +=1
    #print(lists)
    return(lists)

"""



txtcombiner()


"""
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

"""





















