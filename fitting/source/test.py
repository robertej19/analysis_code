#!/usr/bin/python

import sys
sys.argv.append('-b')
import ROOT

ff = ROOT.TFile(sys.argv[1])


for kk in ff.GetListOfKeys():
  obj = kk.ReadObj()
  print(obj.__class__)
  print(obj.GetTitle())
  print(obj.GetNbinsX())
  print(obj.GetNbinsY())
  #print("value of x bin is: ")
  #print(obj.GetXaxis().FindBin(100))#obj.FindLastBinAbove()))



#h1 = ff.Get("5038_H_proton_DeltaBeta_momentum_S2")#.ProjectionY("cutg",0,40,"[cutg]")#5038_Hist_deltaB_psec1_layer1")#.ProjectionY("cutg",0,40,"[cutg]")

h1 = ff.Get("5039_Hist_deltaB_psec1_layer1")#.ProjectionY("cutg",0,40,"[cutg]")



c1 = ROOT.TCanvas('c1','c1',1100,800)
c1.SetLogz()
h1.Draw("colz")
c1.Print("protons.pdf")
