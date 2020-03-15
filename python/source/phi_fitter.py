#!/usr/bin/python

import sys
import os
sys.argv.append('-b')
import ROOT
from ROOT import gStyle
from ROOT import gROOT
from ROOT import TStyle

import numpy as np
from ROOT import TCanvas, TGraph
from ROOT import TF1


def plotdistributer(type,zz,zzz):
	makeplot(type,"LogOFF",zz,zzz)
	if type[4]:

		makeplot(type,"LogON",zz,zzz)

def makeplot(type,logtitle,zz,zzz):
	hist_title = type[0]
	h1 = ff.Get(hist_title)
	c1 = ROOT.TCanvas('c1','c1',120,100)

	h1.Draw("colz")

	#Use a custom function (altough the build in pol2 would also work)
	func = TF1('func', '[0] + [1]*cos(x*3.14159/180) + [2]*cos(2*x*3.14159/180)', 0, 360)
	fit = h1.Fit('func', 'QR')
    par = [fit.Get().Parameter(i) for i in range( 3 )]

    print("parameter list is: {}".format(par))

	c1.Draw()
	#g.Draw('AP')

	h1.Draw("colz")

	#gStyle.SetOptStat(0)
	h1.SetTitle(type[1])
	h1.SetXTitle(type[2])
	h1.SetYTitle(type[3])
	h1.SetLineWidth(5)

	c1.Draw()
	c1.Print("plots/{}/original_python_pdfs/{}_{}.pdf".format(zzz,logtitle,type[0]))


ff = ROOT.TFile(sys.argv[1])
qq = sys.argv[1]
ww = qq.split("/")
zz = ww[-1]
#print(ff.GetListOfKeys())
#print(zz)
xxx = zz.split(".")
zzz = xxx[0]
#print(zzz)

os.mkdir("plots/"+zzz)
os.mkdir("plots/"+zzz+"/original_python_pdfs")

for kk in ff.GetListOfKeys():
  obj = kk.ReadObj()
  title = obj.GetName()
  if "Ultra_Phi" in title:
	if obj.GetEntries()>10 and obj.GetMaximum()>10:
	  histTitle = title
	  type9 = (title,histTitle,"Phi","Counts",0,0,0,0,0,0)
	  print(obj.GetEntries())
	  plotdistributer(type9,zz,zzz)
