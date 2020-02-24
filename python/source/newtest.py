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
type2 = "5153_Hist_deltaB_psec1_layer1"
type3 = "5153_Hist_beta_p2sec1_layer1"
type4 = "5153_Hist_beta_p_ctof"

typeX = int(sys.argv[2])
print("type to print is {0}".format(typeX))

if typeX==1:
  type = type1
elif typeX==2:
  type = type2
elif typeX==3:
  type = type3
elif typeX==4:
  type = type4
else:
  print("type not found, ISSUE!!!!")

h1 = ff.Get(type)
print(h1)
c1 = ROOT.TCanvas('c1','c1',100,100)
c1.SetLogz()
h1.Draw("colz")
gStyle.SetOptStat(0)
h1.SetTitle("Proton vs. Electron Theta Angle")
h1.SetYTitle("Electron Angle")
h1.SetXTitle("Proton Angle")

c1.Draw()
c1.Print("plots/full_{}_{}.pdf".format(zz,type))
