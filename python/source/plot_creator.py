#!/usr/bin/python

import sys
import os
sys.argv.append('-b')
import ROOT
from ROOT import gStyle
from ROOT import gROOT
from ROOT import TStyle

print(sys.argv[1])
ff = ROOT.TFile(sys.argv[1])
print(ff)
qq = sys.argv[1]
ww = qq.split("/")
zz = ww[-1]
print(ff.GetListOfKeys())

typeX = int(sys.argv[2])
LogOn = int(sys.argv[3])

def plotdistributer(type,zz):
	makeplot(type,"LogOFF",zz)
	if type[4]:
		c1.SetLogz()
		makeplot(type,"LogON",zz,newdir)

def makeplot(type,logtitle,zz):
	hist_title = type[0]
	h1 = ff.Get(hist_title)
	c1 = ROOT.TCanvas('c1','c1',120,100)
	if type[6] > 0:
		h1.GetXaxis().SetRange(type[5],type[6])
	h1.Draw("colz")
	gStyle.SetOptStat(0)
	h1.SetTitle(type[1])
	h1.SetXTitle(type[2])
	h1.SetYTitle(type[3])
	if logtitle == "LogON":
		c1.SetLogz()
	c1.Draw()
	c1.Print("plots/{}/{}_{}.pdf".format(zz,logtitle,type[0]))


"""for kk in ff.GetListOfKeys():
  obj = kk.ReadObj()
  print(obj.__class__)
  print(obj.GetTitle())
  print(obj.GetNbinsX())
  print(obj.GetNbinsY())
  #print("value of x bin is: ")
  #print(obj.GetXaxis().FindBin(100))#obj.FindLastBinAbove()))
#h1 = ff.Get("5038_H_proton_DeltaBeta_momentum_S2")#.ProjectionY("cutg",0,40,"[cutg]")#5038_Hist_deltaB_psec1_layer1")#.ProjectionY("cutg",0,40,"[cutg]")

#h1 = ff.Get("5039_Hist_deltaB_psec1_layer1")#.ProjectionY("cutg",0,40,"[cutg]")
"""
#type1 = "output_file_histos_Hist_xB_Q2"

	logtitle = "LogOFF"
h1.GetXaxis().SetRange(0,90)
"""FORMAT: Hist name, title, xaxis, yaxis,logON/LogOFF,xmin,xmax"""
type1 = ("output_file_histos_Hist_heleproTheta",
	"Proton vs. Electron Theta Angle","Proton Angle","Electron Angle",
	1,0,0)
type2 = ("output_file_histos_Hist_heleproThetaDVMP",
	"Proton vs. Electron Theta Angle, DVPP Candidates","Proton Angle","Electron Angle",
	1,0,0)
type3 = ("output_file_histos_Hist_heleTheta",
	"Electron Angle (Theta), with Proton Coincidence","Electron Angle","Counts",
	0,0,0)
type4 = ("output_file_histos_Hist_hproTheta","Proton Angle (Theta)","Proton Angle","Counts",
	0,0,0)
type5 = ("output_file_histos_Hist_LeptHadAngle",
	"Angle Between Lepton and Hadron Planes","Angle","Counts",
	0,0,90)

plots = [type1,type2,type3,type4,type5]

os.mkdir(zz)
for type in plots:
	plotdistributer(type,zz)
