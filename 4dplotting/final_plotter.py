import pandas as pd
import numpy as np 
import matplotlib.pyplot as plt 
import random 
import sys
import os, subprocess
from pdf2image import convert_from_path
import math
from icecream import ic
import shutil
from PIL import Image, ImageDraw, ImageFont
import phi_Fitter
   

datadir = "pickled_data/"
datafile = "phi_fit_vals.pkl"
data = pd.read_pickle(datadir+datafile)
ic(data.shape)

print(data)

#-----------------------------------------------


#save_folder = "phi_pics/"
save_folder = "t_dependence_plots/"
if os.path.isdir(save_folder):
    print('removing previous database file')
    ## Try to remove tree; if failed show an error using try...except on screen
    try:
        shutil.rmtree(save_folder)
    except OSError as e:
        print ("Error: %s - %s." % (e.filename, e.strerror))
    #shutil.rmtree(save_folder)
    #shutil.rmtree(save_folder)
else:
    print(save_folder+" is not present, not deleteing")

subprocess.call(['mkdir','-p',save_folder])
print(save_folder+" is now present")


xb_ranges = [0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.7,0.85,1]
q2_ranges = [1.5,2.0,2.5,3.0,3.5,4.0,4.5,5.0,5.5,6.0,7.0,8.0,9.0,12.0]
t_ranges = [0.09,0.15,0.2,0.3,0.4,0.6,1,1.5,2,3,4.5,6]


plot_dir = save_folder

#xb_ranges = [0.15,0.2,0.25,0.3]
#q2_ranges = [1.5,2.0]

for xBmax in xb_ranges:
    for Q2max in q2_ranges:
        data_t = data[(data['xBmax']==xBmax) & (data['Q2max']==Q2max)]

        x = data_t["tmax"]
        y = data_t["A"]
        z = data_t["B"]
        w = data_t["C"]

        plt.plot(x,y,'r',marker='o', ms=10,label="A")
        plt.plot(x,z,'g',marker='o', ms=10,label="B")
        plt.plot(x,w,marker='o', ms=10,label="C")

        Q2maxstr = str(Q2max)
        if len(Q2maxstr) < 4:
            print("Q2maxstr is {} adding a 0 to yield: ".format(Q2maxstr))
            Q2maxstr = "0"+Q2maxstr
            print(Q2maxstr)
        #if len(Q2max) < 2:
        #    q2max = "0"+q2max

        t_title = 't-dependence-xb-{}-q2-{}'.format(xBmax,Q2maxstr)

        plt.title(t_title)
        plt.legend(loc='best')
        plt.xlabel(r't (GeV^2)')
        plt.ylabel(r'Unnormalized Scale')

        
        plot_title = plot_dir + t_title+".png"
        plt.savefig(plot_title)
        plt.close()
        print("plot saved to {}".format(plot_title))