#!/usr/bin/python

import sys
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
type1 = "output_file_histos_Hist_heleproTheta"
type2 = "output_file_histos_Hist_heleproThetaDVMP"
type3 = "output_file_histos_Hist_hproTheta"
type4 = "output_file_histos_Hist_heleTheta"

typeX = int(sys.argv[2])
LogOn = int(sys.argv[3])
print("type to print is {0}".format(typeX))

title1 = "Proton vs. Electron Theta Angle"
title2 = "Proton vs. Electron Theta Angle, DVPP Candidates"
title1 = "Electron Angle (Theta), with Proton Coincidence"
title2 = "Proton Angle (Theta)"
logtitle = "LogOFF"

if typeX==1:
  type = type1
  titleX = title1
elif typeX==2:
  type = type2
  titleX = title2
elif typeX==3:
  type = type3
  titleX = title3
elif typeX==4:
  type = type4
  titleX = title4
else:
  print("type not found, ISSUE!!!!")

h1 = ff.Get(type)
print(h1)
c1 = ROOT.TCanvas('c1','c1',100,100)
if LogOn:
	logtitle = "LogON"
	c1.SetLogz()
h1.Draw("colz")
gStyle.SetOptStat(0)
h1.SetTitle(titleX)
h1.SetYTitle("Electron Angle")
h1.SetXTitle("Proton Angle")

c1.Draw()
c1.Print("plots/full_{}_{}_{}.pdf".format(logtitle,zz,type))
