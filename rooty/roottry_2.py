#!/usr/bin/python


import subprocess
import os
import ROOT
from ROOT import gStyle
from ROOT import gROOT
from ROOT import TStyle
from pathlib import Path
import json
import array, numpy


filename = "converted_filtered_skim8_005032.root"

#ff = ROOT.TFile(sys.argv[1])
ff = ROOT.TFile(filename)

print(ff.getEntries())

