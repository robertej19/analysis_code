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

"""FORMAT: Hist name, title, xaxis, yaxis"""

type1 = "output_file_histos_Hist_heleproTheta"
type2 = "output_file_histos_Hist_heleproThetaDVMP"
type3 = "output_file_histos_Hist_heleTheta"
type4 = "output_file_histos_Hist_hproTheta"
type5 = "output_file_histos_Hist_LeptHadAngle"
type6 = "output_file_histos_Hist_hproThetaCD"
type7 = "output_file_histos_Hist_hproThetaFD"

typeX = int(sys.argv[2])
LogOn = int(sys.argv[3])
print("type to print is {0}".format(typeX))

title1 = "Proton vs. Electron Theta Angle"
title2 = "Proton vs. Electron Theta Angle, DVPP Candidates"
title3 = "Electron Angle (Theta), with Proton Coincidence"
title4 = "Proton Angle (Theta)"
title6 = "Proton Angle (Theta) in CD"
title7 = "Proton Angle (Theta) in FD"
logtitle = "LogOFF"


hist2d_y = "Electron Angle"
hist2d_x = "Proton Angle"
hist1d_y = "Counts"
hist1d_e = "Electron Angle"
hist1d_p = "Proton Angle"

if typeX==1:
  type = type1
  titleX = title1
  yaxisX = hist2d_y
  xaxisX = hist2d_x
elif typeX==2:
  type = type2
  titleX = title2
  yaxisX = hist2d_y
  xaxisX = hist2d_x
elif typeX==3:
  type = type3
  titleX = title3
  yaxisX = hist1d_y
  xaxisX = hist1d_e
elif typeX==4:
  type = type4
  titleX = title4
  yaxisX = hist1d_y
  xaxisX = hist1d_p
elif typeX==5:
  type = type5
  titleX = "Angle Between Lepton and Hadron Planes"
  yaxisX = hist1d_y
  xaxisX = "Angle"
  h1.GetXaxis().SetRange(0,90)
elif typeX==6:
  type = type6
  titleX = title6
  yaxisX = hist1d_y
  xaxisX = hist1d_p
#  h1.GetYaxis().SetRange(0,200000)
elif typeX==7:
  type = type7
  titleX = title7
  yaxisX = hist1d_y
  xaxisX = hist1d_p
#  h1.GetYaxis().SetRange(0,200000)
else:
  print("type not found, ISSUE!!!!")

h1 = ff.Get(type)
print(h1)
c1 = ROOT.TCanvas('c1','c1',120,100)
if LogOn:
	logtitle = "LogON"
	c1.SetLogz()
#h1.Draw("colz")
#h1.GetXaxis().SetRange(0,90)
h1.Draw("colz")
h1.SetAxisRange(0,220000,"Y")
gStyle.SetOptStat(0)
h1.SetTitle(titleX)
h1.SetYTitle(yaxisX)
h1.SetXTitle(xaxisX)

c1.Draw()
c1.Print("plots/full_{}_{}_{}.pdf".format(logtitle,zz,type))
