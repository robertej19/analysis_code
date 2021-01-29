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




#data_dir = "plottingfiles/"
#data_dir = "../rooty/plottingfiles/"
#data_dir = "groovy-test/"
data_dir = "../rooty/op_dir/final_txts/"

data_lists = os.listdir(data_dir)

data = pd.DataFrame()

for datacount, ijk in enumerate(data_lists):
    ic(datacount)
    print(ijk)
    frame = pd.read_csv(data_dir+ijk, sep=",", header=None)
    data = data.append(frame)

#data = pd.read_csv('afterfixes.txt', sep=",", header=None)
#data.columns = ["run_num", "event_num", "num_in_fd", "num_in_cd","helicity","xB","Q2","t","Phi"]



#data.columns = ["run_num", "event_num", "num_in_fd", "num_in_cd","helicity","xB","Q2","t","Phi","W","ThetaXPi","Diff_pX","Diff_pY","MM_EPX2","ME_EPGG","Pi_Mass"]
data.columns = ["Q2","xB","t","Phi"]


ic(data.shape)

outdir = "pickled_data/"
outfile = "skims-168.pkl"

data.to_pickle(outdir+outfile)
print("Saved file at {}".format(outdir+outfile))