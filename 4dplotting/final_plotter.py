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